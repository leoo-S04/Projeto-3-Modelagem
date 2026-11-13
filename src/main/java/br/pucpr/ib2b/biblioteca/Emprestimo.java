package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;

import java.time.LocalDate;

/**
 * Empréstimo de um livro reservado (Astah: Emprestimo; regra 7.2).
 * A operação renovar() do Astah, com a guarda numRenovacoes < 2, entra na Etapa 1 (State).
 */
public class Emprestimo extends Transacao {

    private LocalDate dataVencimento;
    private int numRenovacoes = 0;

    public Emprestimo(int idTransacao, LocalDate data, Livro livro, Estudante estudante, Atendente atendente) {
        super(idTransacao, data, livro, estudante, atendente);
    }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public int getNumRenovacoes() { return numRenovacoes; }

    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public void incrementarRenovacoes() { numRenovacoes++; }
}
