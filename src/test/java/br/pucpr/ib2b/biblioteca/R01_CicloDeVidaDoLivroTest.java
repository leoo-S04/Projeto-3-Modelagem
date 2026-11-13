package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Req. 1 — State: cenários de verificação do enunciado, executados pelos tratadores.
 * f1 e f4 passam pelo executor de solicitações (Command): a recusa do estado volta como resultado RECUSADA,
 * com a mensagem da exceção de domínio.
 */
class R01_CicloDeVidaDoLivroTest {

    private final Cenario c = new Cenario();

    @Test
    void R01_terceiraRenovacaoRecusadaSemAlterarVencimento() {
        c.emprestarLivro1();                                                         // vencimento 13/12/2026
        assertTrue(c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100").foiExecutada()); // 12/01/2027
        assertTrue(c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100").foiExecutada()); // 11/02/2027
        assertEquals(LocalDate.of(2027, 2, 11), c.tratadorDeAcervo.consultarLivro(1).getVencimento());

        ResultadoSolicitacao terceira = c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100");

        assertEquals(StatusSolicitacao.RECUSADA, terceira.status());
        assertTrue(terceira.mensagem().contains("limite de 2 renovações"));        // RenovacaoNegadaException
        Livro depois = c.tratadorDeAcervo.consultarLivro(1);
        assertEquals(LocalDate.of(2027, 2, 11), depois.getVencimento());          // vencimento inalterado
        assertEquals(2, depois.getNumRenovacoes());
        assertEquals(Situacao.RENOVADO, depois.getEstado());
    }

    @Test
    void R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado() {
        c.emprestarLivro1();
        c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01");
        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());

        ResultadoSolicitacao reserva = c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");

        assertEquals(StatusSolicitacao.RECUSADA, reserva.status());
        assertTrue(reserva.mensagem().contains("DEVOLVIDO"));                     // OperacaoInvalidaException
        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
        assertNull(c.tratadorDeEmprestimo.consultarReserva(1, "M-100"));          // nenhuma reserva gravada
    }
}
