package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Um empréstimo para as telas (somente leitura): em andamento ou já devolvido.
 *
 * @param devolvidoEm nulo enquanto o livro está com o estudante
 * @param diasAtraso  dias de atraso na devolução (0 se no prazo ou ainda em andamento)
 */
public record EmprestimoDTO(int idEmprestimo, int idLivro, String titulo, String matriculaEstudante, String nomeEstudante,
                            String atendente, LocalDate dataEmprestimo, LocalDate vencimento, int numRenovacoes,
                            LocalDate devolvidoEm, int diasAtraso) {

    public boolean emAndamento() { return devolvidoEm == null; }

    /** Empréstimo atual: vencimento e renovações vêm do Livro (são dele, no padrão State). */
    static EmprestimoDTO emAndamento(Emprestimo e) {
        Livro l = e.getLivro();
        return new EmprestimoDTO(e.getIdTransacao(), l.getIdLivro(), l.getTitulo(), e.getEstudante().getMatricula(),
                nome(e), atendente(e), e.getData(), l.getVencimento(), l.getNumRenovacoes(), null, 0);
    }

    /** Empréstimo encerrado: dados copiados na devolução (o Livro já começou outro ciclo). */
    static EmprestimoDTO devolvido(Devolucao d) {
        Emprestimo e = d.getEmprestimo();
        LocalDate inicio = e == null ? null : e.getData();
        int renovacoes = inicio == null ? 0 : (int) Math.max(0,
                (ChronoUnit.DAYS.between(inicio, d.getVencimento()) - Livro.PRAZO_EMPRESTIMO_DIAS) / Livro.DIAS_POR_RENOVACAO);
        return new EmprestimoDTO(e == null ? d.getIdTransacao() : e.getIdTransacao(), d.getLivro().getIdLivro(),
                d.getLivro().getTitulo(), d.getEstudante().getMatricula(), nome(d), atendente(e == null ? d : e),
                inicio, d.getVencimento(), renovacoes, d.getDataDevolucao(), d.calcularDiasAtraso());
    }

    private static String nome(Transacao t) {
        return t.getEstudante().getPessoa() == null ? t.getEstudante().getMatricula()
                : t.getEstudante().getPessoa().getNome() + " " + t.getEstudante().getPessoa().getSobrenome();
    }

    private static String atendente(Transacao t) {
        return t.getAtendente() == null ? null : t.getAtendente().getMatriculaFuncional();
    }
}
