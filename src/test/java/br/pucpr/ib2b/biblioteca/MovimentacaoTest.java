package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Movimentacao: ponto único de acesso às transações, com reservas, empréstimos e devoluções separados. */
class MovimentacaoTest {

    private final Cenario c = new Cenario();
    private final Movimentacao movimentacao = c.movimentacao();

    @Test
    void buscaAReservaPeloLivroEPeloEstudante() {
        Reserva r = c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");

        assertEquals(r.getIdTransacao(), movimentacao.buscarReserva(1, "M-100").getIdTransacao());
        assertSame(movimentacao.buscarReserva(1, "M-100"), movimentacao.buscarReservaAtiva(1));
        assertNull(movimentacao.buscarReserva(1, "M-999"));   // outro estudante
        assertNull(movimentacao.buscarReserva(2, "M-100"));   // outro livro
    }

    @Test
    void emprestimoRemoveAReservaECadaBuscaDevolveSoOSeuTipo() {
        c.emprestarLivro1();

        assertNull(movimentacao.buscarReserva(1, "M-100"));
        assertNotNull(movimentacao.buscarEmprestimo(1, "M-100"));
        assertNull(movimentacao.buscarDevolucao(1, "M-100"));
    }

    @Test
    void idsDeTransacaoNaoSeRepetem() {
        int a = movimentacao.gerarIdTransacao();
        int b = movimentacao.gerarIdTransacao();
        assertNotEquals(a, b);
    }
}
