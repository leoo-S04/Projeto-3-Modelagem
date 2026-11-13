package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Devolução de um livro emprestado (Astah: Devolucao; regra 9.2). Guarda o empréstimo que encerra. */
public class Devolucao extends Transacao {

    private LocalDate dataDevolucao;
    private final Emprestimo emprestimo;

    public Devolucao(int idTransacao, LocalDate data, Livro livro, Estudante estudante, Atendente atendente,
                     Emprestimo emprestimo) {
        super(idTransacao, data, livro, estudante, atendente);
        this.emprestimo = emprestimo;
        this.dataDevolucao = data;
    }

    /** Dias corridos entre o vencimento e a devolução; 0 se devolvido no prazo. */
    public int calcularDiasAtraso() {
        long dias = ChronoUnit.DAYS.between(emprestimo.getDataVencimento(), dataDevolucao);
        return (int) Math.max(0, dias);
    }

    /** Atividade de entrada do estado DEVOLVIDO na máquina de estados (entry / registrarDataDevolucao). */
    public void registrarDataDevolucao(LocalDate data) { this.dataDevolucao = data; }

    public boolean estaEmAtraso() { return calcularDiasAtraso() > 0; }

    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public Emprestimo getEmprestimo() { return emprestimo; }
}
