package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;

import java.time.LocalDate;

/**
 * Transação da biblioteca (Astah: Transacao). Generalização de Reserva, Emprestimo e Devolucao:
 * "toda transação ocorre em uma data e envolve um livro, um estudante e um atendente".
 */
public abstract class Transacao {

    private final int idTransacao;
    private final LocalDate data;
    private final Livro livro;
    private final Estudante estudante;
    private final Atendente atendente;

    protected Transacao(int idTransacao, LocalDate data, Livro livro, Estudante estudante, Atendente atendente) {
        this.idTransacao = idTransacao;
        this.data = data;
        this.livro = livro;
        this.estudante = estudante;
        this.atendente = atendente;
    }

    public int getIdTransacao() { return idTransacao; }
    public LocalDate getData() { return data; }
    public Livro getLivro() { return livro; }
    public Estudante getEstudante() { return estudante; }
    public Atendente getAtendente() { return atendente; }

    /** Usado pela Movimentacao para achar a transação de um livro e de um estudante (idEstudante = matrícula). */
    boolean envolve(int idLivro, String idEstudante) {
        return livro.getIdLivro() == idLivro && estudante.getMatricula().equals(idEstudante);
    }
}
