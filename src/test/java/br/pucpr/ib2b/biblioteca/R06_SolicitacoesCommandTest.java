package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 6 — Command: cenários de verificação do enunciado. Empréstimo em 13/11 → vencimento 13/12/2026. */
class R06_SolicitacoesCommandTest {

    private final Cenario c = new Cenario();

    @Test
    void R06_mesmaRenovacaoExecutadaDuasVezesComOMesmoIdAcrescentaApenasTrintaDias() {
        c.emprestarLivro1();
        UUID s42 = UUID.randomUUID();

        ResultadoSolicitacao primeira = c.tratadorDeEmprestimo.renovarEmprestimo(s42, 1, "M-100");
        ResultadoSolicitacao repetida = c.tratadorDeEmprestimo.renovarEmprestimo(s42, 1, "M-100"); // reenvio

        assertEquals(StatusSolicitacao.EXECUTADA, primeira.status());
        assertEquals(primeira, repetida);                                    // resultado gravado, sem reexecutar
        assertEquals(LocalDate.of(2027, 1, 12), c.tratadorDeAcervo.consultarLivro(1).getVencimento()); // só +30
        assertEquals(1, c.tratadorDeAcervo.consultarLivro(1).getNumRenovacoes());
    }

    @Test
    void R06_solicitacoesDiferentesContinuamLimitadasADuasRenovacoes() {
        c.emprestarLivro1();

        var r1 = c.tratadorDeEmprestimo.renovarEmprestimo(UUID.randomUUID(), 1, "M-100");
        var r2 = c.tratadorDeEmprestimo.renovarEmprestimo(UUID.randomUUID(), 1, "M-100");
        var r3 = c.tratadorDeEmprestimo.renovarEmprestimo(UUID.randomUUID(), 1, "M-100");

        assertEquals(StatusSolicitacao.EXECUTADA, r1.status());
        assertEquals(StatusSolicitacao.EXECUTADA, r2.status());
        assertEquals(StatusSolicitacao.RECUSADA, r3.status());               // regra no Livro (State)
        assertTrue(r3.mensagem().contains("limite de 2 renovações"));
        assertEquals(LocalDate.of(2027, 2, 11), c.tratadorDeAcervo.consultarLivro(1).getVencimento());
    }

    @Test
    void submissoesSimultaneasDoMesmoIdExecutamUmaUnicaVez() throws Exception {
        c.emprestarLivro1();
        UUID id = UUID.randomUUID();
        ExecutorService threads = Executors.newFixedThreadPool(8);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<ResultadoSolicitacao>> futuros = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            futuros.add(threads.submit(() -> {
                largada.await();                                             // todas partem ao mesmo tempo
                return c.tratadorDeEmprestimo.renovarEmprestimo(id, 1, "M-100");
            }));
        }
        largada.countDown();
        int executadas = 0;
        for (Future<ResultadoSolicitacao> f : futuros) {
            if (f.get().status() == StatusSolicitacao.EXECUTADA) executadas++;
        }
        threads.shutdown();

        assertTrue(executadas >= 1);   // a vencedora (e repetições que já encontraram o resultado gravado)
        assertEquals(1, c.tratadorDeAcervo.consultarLivro(1).getNumRenovacoes());     // mas só UMA renovação
        assertEquals(LocalDate.of(2027, 1, 12), c.tratadorDeAcervo.consultarLivro(1).getVencimento());
    }

    @Test
    void reservaTambemEUmaSolicitacaoIdempotente() {
        UUID id = UUID.randomUUID();
        assertEquals(StatusSolicitacao.EXECUTADA, c.tratadorDeEmprestimo.reservarLivro(id, 1, "M-100", "F-01").status());
        // duplo clique: mesmo id → mesmo resultado, nenhuma segunda tentativa de reservar (que seria recusada)
        assertEquals(StatusSolicitacao.EXECUTADA, c.tratadorDeEmprestimo.reservarLivro(id, 1, "M-100", "F-01").status());
    }
}
