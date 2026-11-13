package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Cálculo de atraso da devolução (regra 9.3: atraso leva à multa). */
class DevolucaoTest {

    private final Livro livro = new Livro(1, "Dom Casmurro");
    private final Estudante ana = new Estudante(1, "M-100", new Pessoa("111", "Ana", "Souza", "ana@pucpr.br"));
    private final Atendente bruno = new Atendente(1, "F-01", new Pessoa("222", "Bruno", "Alves", "bruno@pucpr.br"));

    private Devolucao devolverEm(LocalDate vencimento, LocalDate devolvidoEm) {
        Emprestimo emp = new Emprestimo(1, LocalDate.of(2026, 10, 11), livro, ana, bruno);
        return new Devolucao(2, devolvidoEm, livro, ana, bruno, emp, vencimento);
    }

    @Test
    void devolucaoTresDiasDepoisDoVencimentoTemTresDiasDeAtraso() {
        Devolucao d = devolverEm(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13));

        assertEquals(3, d.calcularDiasAtraso());
        assertTrue(d.estaEmAtraso());
    }

    @Test
    void devolucaoNoDiaDoVencimentoOuAntesNaoTemAtraso() {
        assertEquals(0, devolverEm(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10)).calcularDiasAtraso());

        Devolucao antes = devolverEm(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 5));
        assertEquals(0, antes.calcularDiasAtraso());
        assertFalse(antes.estaEmAtraso());
    }

    @Test
    void registrarDataDevolucaoRecalculaOAtraso() {
        Devolucao d = devolverEm(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10));

        d.registrarDataDevolucao(LocalDate.of(2026, 11, 15));

        assertEquals(5, d.calcularDiasAtraso());
    }
}
