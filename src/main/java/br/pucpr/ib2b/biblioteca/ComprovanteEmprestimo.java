package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;

/** Comprovante (DTO) do empréstimo efetivado pela fachada. */
public record ComprovanteEmprestimo(int idEmprestimo, int idLivro, String titulo, String matriculaEstudante,
                                    LocalDate data, LocalDate vencimento) {

    static ComprovanteEmprestimo de(Emprestimo e, LocalDate vencimento) {
        return new ComprovanteEmprestimo(e.getIdTransacao(), e.getLivro().getIdLivro(), e.getLivro().getTitulo(),
                e.getEstudante().getMatricula(), e.getData(), vencimento);
    }
}
