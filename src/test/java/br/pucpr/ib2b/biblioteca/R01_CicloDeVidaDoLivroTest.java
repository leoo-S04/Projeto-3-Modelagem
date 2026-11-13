package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 1 — State: cenários de verificação do enunciado, executados pelos tratadores. */
class R01_CicloDeVidaDoLivroTest {

    private final Cenario c = new Cenario();

    @Test
    void R01_terceiraRenovacaoRecusadaSemAlterarVencimento() {
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        c.tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01");       // vencimento 13/12/2026
        assertTrue(c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100")); // 12/01/2027
        assertTrue(c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100")); // 11/02/2027
        Livro livro = c.tratadorDeAcervo.consultarLivro(1);
        assertEquals(LocalDate.of(2027, 2, 11), livro.getVencimento());

        assertThrows(RenovacaoNegadaException.class,
                () -> c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100"));

        assertEquals(LocalDate.of(2027, 2, 11), livro.getVencimento());  // vencimento inalterado
        assertEquals(2, livro.getNumRenovacoes());
        assertEquals(Situacao.RENOVADO, livro.getEstado());
    }

    @Test
    void R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado() {
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        c.tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01");
        c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01");
        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());

        assertThrows(OperacaoInvalidaException.class,
                () -> c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01"));

        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
        assertNull(c.tratadorDeEmprestimo.consultarReserva(1, "M-100"));  // nenhuma reserva gravada
    }
}
