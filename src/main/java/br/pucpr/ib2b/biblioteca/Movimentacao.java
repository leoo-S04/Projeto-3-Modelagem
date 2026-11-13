package br.pucpr.ib2b.biblioteca;

import java.util.ArrayList;
import java.util.List;

/**
 * Ponto interno único de acesso às transações (Astah: Movimentacao): reservas, empréstimos e devoluções.
 * Os tratadores de circulação acessam as transações por delegação a esta classe.
 */
public class Movimentacao {

    private final List<Transacao> transacoes = new ArrayList<>();
    private int ultimoIdTransacao = 0;

    /** Gera o id da próxima transação (reserva, empréstimo ou devolução), sem repetição. */
    public int gerarIdTransacao() { return ++ultimoIdTransacao; }

    public Transacao adicionar(Transacao t) {
        transacoes.add(t);
        return t;
    }

    public boolean remover(Transacao t) { return transacoes.remove(t); }

    /** Reserva do livro para esse estudante (idEstudante = matrícula), ou {@code null}. */
    public Reserva buscarReserva(int idLivro, String idEstudante) {
        return buscar(Reserva.class, idLivro, idEstudante);
    }

    public Emprestimo buscarEmprestimo(int idLivro, String idEstudante) {
        return buscar(Emprestimo.class, idLivro, idEstudante);
    }

    public Devolucao buscarDevolucao(int idLivro, String idEstudante) {
        return buscar(Devolucao.class, idLivro, idEstudante);
    }

    /** Procura a transação do tipo pedido (Reserva, Emprestimo ou Devolucao) que envolve o livro e o estudante. */
    private <T extends Transacao> T buscar(Class<T> tipo, int idLivro, String idEstudante) {
        for (Transacao t : transacoes) {
            if (tipo.isInstance(t) && t.envolve(idLivro, idEstudante)) return tipo.cast(t);
        }
        return null;
    }
}
