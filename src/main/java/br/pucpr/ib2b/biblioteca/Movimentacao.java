package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.persistencia.RepositorioMovimentacao;

import java.util.List;

/**
 * Ponto interno único de acesso às transações (Astah: Movimentacao), mantido por três associações distintas:
 * reservas, empréstimos e devoluções (Trabalho 02). Os dados ficam no RepositorioMovimentacao
 * da família de persistência em uso.
 */
public class Movimentacao {

    private final RepositorioMovimentacao repositorio;

    public Movimentacao(RepositorioMovimentacao repositorio) { this.repositorio = repositorio; }

    /** Gera o id da próxima transação (reserva, empréstimo ou devolução), sem repetição. */
    public int gerarIdTransacao() { return repositorio.gerarIdTransacao(); }

    // ---- Reservas ----
    public void registrarReserva(Reserva r) { repositorio.salvarReserva(r); }
    public boolean removerReserva(Reserva r) { return repositorio.removerReserva(r); }

    /** Reserva do livro para esse estudante (idEstudante = matrícula), ou {@code null}. */
    public Reserva buscarReserva(int idLivro, String idEstudante) {
        return ultima(repositorio.reservas(), idLivro, idEstudante);
    }

    /** Reserva ativa do livro, de qualquer estudante, ou {@code null}. */
    public Reserva buscarReservaAtiva(int idLivro) { return ultima(repositorio.reservas(), idLivro, null); }

    // ---- Empréstimos ----
    public void registrarEmprestimo(Emprestimo e) { repositorio.salvarEmprestimo(e); }
    public boolean removerEmprestimo(Emprestimo e) { return repositorio.removerEmprestimo(e); }

    public Emprestimo buscarEmprestimo(int idLivro, String idEstudante) {
        return ultima(repositorio.emprestimos(), idLivro, idEstudante);
    }

    public Emprestimo buscarEmprestimoAtivo(int idLivro) { return ultima(repositorio.emprestimos(), idLivro, null); }

    /** Empréstimos em andamento (cópia somente leitura). */
    public List<Emprestimo> listarEmprestimos() { return List.copyOf(repositorio.emprestimos()); }

    // ---- Devoluções ----
    public void registrarDevolucao(Devolucao d) { repositorio.salvarDevolucao(d); }

    /** Devolução mais recente do livro por esse estudante, ou {@code null}. */
    public Devolucao buscarDevolucao(int idLivro, String idEstudante) {
        return ultima(repositorio.devolucoes(), idLivro, idEstudante);
    }

    /** Devoluções registradas (cópia somente leitura). */
    public List<Devolucao> listarDevolucoes() { return List.copyOf(repositorio.devolucoes()); }

    /** A transação mais recente da lista que envolve o livro (e o estudante, se informado). */
    private static <T extends Transacao> T ultima(List<T> transacoes, int idLivro, String idEstudante) {
        T encontrada = null;
        for (T t : transacoes) {
            boolean doLivro = t.getLivro().getIdLivro() == idLivro;
            boolean doEstudante = idEstudante == null || t.envolve(idLivro, idEstudante);
            if (doLivro && doEstudante) encontrada = t;
        }
        return encontrada;
    }
}
