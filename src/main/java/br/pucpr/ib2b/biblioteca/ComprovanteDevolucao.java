package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;

/**
 * Comprovante (DTO) da devolução: traz os dados de que as reações à devolução precisam (vencimento, data da
 * devolução e estudante), porque o empréstimo ativo deixa de existir depois da devolução.
 */
public record ComprovanteDevolucao(int idDevolucao, int idLivro, String titulo, String matriculaEstudante,
                                   String cpfEstudante, String emailEstudante, LocalDate vencimento,
                                   LocalDate devolvidoEm, int diasAtraso) {

    static ComprovanteDevolucao de(Devolucao d) {
        return new ComprovanteDevolucao(d.getIdTransacao(), d.getLivro().getIdLivro(), d.getLivro().getTitulo(),
                d.getEstudante().getMatricula(), d.getEstudante().getPessoa().getCpf(),
                d.getEstudante().getPessoa().getEmail(), d.getVencimento(), d.getDataDevolucao(),
                d.calcularDiasAtraso());
    }
}
