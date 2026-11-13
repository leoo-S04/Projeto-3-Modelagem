package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.Devolucao;
import br.pucpr.ib2b.biblioteca.Emprestimo;
import br.pucpr.ib2b.biblioteca.Reserva;

import java.util.List;

/** Papel GoF: AbstractProduct — reservas, empréstimos e devoluções em associações distintas (usado pela Movimentacao). */
public interface RepositorioMovimentacao {
    int gerarIdTransacao();

    void salvarReserva(Reserva r);
    boolean removerReserva(Reserva r);
    List<Reserva> reservas();

    void salvarEmprestimo(Emprestimo e);
    boolean removerEmprestimo(Emprestimo e);
    List<Emprestimo> emprestimos();

    void salvarDevolucao(Devolucao d);
    List<Devolucao> devolucoes();

    UnidadeDeTrabalho unidade();
}
