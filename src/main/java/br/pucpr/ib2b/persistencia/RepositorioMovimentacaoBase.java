package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.Devolucao;
import br.pucpr.ib2b.biblioteca.Emprestimo;
import br.pucpr.ib2b.biblioteca.Reserva;

import java.util.ArrayList;
import java.util.List;

/** Implementação comum: reservas, empréstimos e devoluções em três associações distintas da cópia de trabalho. */
abstract class RepositorioMovimentacaoBase implements RepositorioMovimentacao {

    private final UnidadeDeTrabalhoBase uow;

    protected RepositorioMovimentacaoBase(UnidadeDeTrabalhoBase uow) { this.uow = uow; }

    @Override public int gerarIdTransacao() { return uow.proximoIdTransacao(); }

    @Override public void salvarReserva(Reserva r) { uow.dados().reservas.add(r); }
    @Override public boolean removerReserva(Reserva r) {
        return uow.dados().reservas.removeIf(x -> x.getIdTransacao() == r.getIdTransacao());
    }
    @Override public List<Reserva> reservas() { return new ArrayList<>(uow.dados().reservas); }

    @Override public void salvarEmprestimo(Emprestimo e) { uow.dados().emprestimos.add(e); }
    @Override public boolean removerEmprestimo(Emprestimo e) {
        return uow.dados().emprestimos.removeIf(x -> x.getIdTransacao() == e.getIdTransacao());
    }
    @Override public List<Emprestimo> emprestimos() { return new ArrayList<>(uow.dados().emprestimos); }

    @Override public void salvarDevolucao(Devolucao d) { uow.dados().devolucoes.add(d); }
    @Override public List<Devolucao> devolucoes() { return new ArrayList<>(uow.dados().devolucoes); }

    @Override public UnidadeDeTrabalho unidade() { return uow; }
}
