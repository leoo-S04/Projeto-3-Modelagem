package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Movimentacao: ponto único de acesso às transações. */
class MovimentacaoTest {

    private final Movimentacao movimentacao = new Movimentacao();
    private final Livro livro = new Livro(1, "Dom Casmurro");
    private final Estudante ana = new Estudante(1, "M-100", new Pessoa("111", "Ana", "Souza", "ana@pucpr.br"));
    private final Atendente bruno = new Atendente(1, "F-01", new Pessoa("222", "Bruno", "Alves", "bruno@pucpr.br"));
    private final LocalDate hoje = LocalDate.of(2026, 11, 13);

    @Test
    void buscaAReservaPeloLivroEPeloEstudante() {
        Reserva r = new Reserva(1, hoje, livro, ana, bruno);
        movimentacao.adicionar(r);

        assertSame(r, movimentacao.buscarReserva(1, "M-100"));
        assertNull(movimentacao.buscarReserva(1, "M-999"));   // outro estudante
        assertNull(movimentacao.buscarReserva(2, "M-100"));   // outro livro
    }

    @Test
    void reservaRemovidaDeixaDeSerEncontrada() {
        Reserva r = new Reserva(1, hoje, livro, ana, bruno);
        movimentacao.adicionar(r);

        assertTrue(movimentacao.remover(r));
        assertNull(movimentacao.buscarReserva(1, "M-100"));
    }

    @Test
    void cadaBuscaDevolveSoOSeuTipoDeTransacao() {
        Emprestimo e = new Emprestimo(2, hoje, livro, ana, bruno);
        movimentacao.adicionar(e);

        assertSame(e, movimentacao.buscarEmprestimo(1, "M-100"));
        assertNull(movimentacao.buscarReserva(1, "M-100"));
        assertNull(movimentacao.buscarDevolucao(1, "M-100"));
    }
}
