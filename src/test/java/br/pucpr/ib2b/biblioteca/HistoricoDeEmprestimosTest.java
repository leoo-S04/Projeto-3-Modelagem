package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Consulta de empréstimos pela fachada (usada nas telas do estudante, do atendente e do administrador). */
class HistoricoDeEmprestimosTest {

    private final Cenario c = new Cenario();

    @Test
    void emprestimoEmAndamentoMostraDataVencimentoERenovacoes() {
        c.emprestarLivro1();                                                   // 13/11/2026, vence 13/12/2026
        c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100");                  // vence 12/01/2027

        HistoricoDeEmprestimos h = c.tratadorDeEmprestimo.consultarEmprestimos("M-100");
        assertEquals(1, h.emAndamento().size());
        EmprestimoDTO e = h.emAndamento().get(0);
        assertEquals("Dom Casmurro", e.titulo());
        assertEquals("Ana Souza", e.nomeEstudante());
        assertEquals("F-01", e.atendente());
        assertEquals(Cenario.HOJE, e.dataEmprestimo());
        assertEquals(LocalDate.of(2027, 1, 12), e.vencimento());
        assertEquals(1, e.numRenovacoes());
        assertSame(e, h.buscar(e.idEmprestimo()));
    }

    @Test
    void devolucaoAtrasadaFicaNoHistoricoComOsDiasDeAtraso() {
        c.emprestarLivro1();
        c.relogio.avancarDias(33);                                             // 3 dias depois do vencimento
        c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01");

        HistoricoDeEmprestimos h = c.tratadorDeEmprestimo.consultarEmprestimos("M-100");
        assertTrue(h.emAndamento().isEmpty());
        EmprestimoDTO d = h.devolvidos().get(0);
        assertEquals(Cenario.HOJE, d.dataEmprestimo());
        assertEquals(LocalDate.of(2026, 12, 16), d.devolvidoEm());
        assertEquals(3, d.diasAtraso());
        assertEquals(0, d.numRenovacoes());
    }

    @Test
    void semEstudanteTrazTodosEOutroEstudanteNaoVeOsEmprestimosDaAna() {
        c.emprestarLivro1();
        assertEquals(1, c.tratadorDeEmprestimo.consultarEmprestimos(null).todos().size());
        assertTrue(c.tratadorDeEmprestimo.consultarEmprestimos("M-300").todos().isEmpty());
    }
}
