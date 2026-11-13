package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;

/** Situação atual de um livro (DTO), para a tela consultar sem acessar o Livro nem o Acervo. */
public record SituacaoLivroDTO(int idLivro, String titulo, Situacao situacao, String matriculaReservante,
                               LocalDate vencimento, int numRenovacoes) {

    public static SituacaoLivroDTO de(Livro l) {
        return new SituacaoLivroDTO(l.getIdLivro(), l.getTitulo(), l.getEstado(),
                l.getReservante() == null ? null : l.getReservante().getMatricula(),
                l.getVencimento(), l.getNumRenovacoes());
    }
}
