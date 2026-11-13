package br.pucpr.ib2b.biblioteca;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Evento publicado depois que a devolução é CONFIRMADA (Observer, Req. 5). Objeto de valor imutável no modelo
 * "push": leva vencimento, data da devolução e estudante, porque o empréstimo ativo já foi removido e os
 * ouvintes não poderiam consultá-lo depois.
 */
public record EventoDevolucao(int idDevolucao, int idLivro, String titulo, String matriculaEstudante,
                              String cpfEstudante, String emailEstudante, LocalDate vencimento,
                              LocalDate devolvidoEm, Instant ocorridoEm) {

    public static EventoDevolucao de(ComprovanteDevolucao c, Instant ocorridoEm) {
        return new EventoDevolucao(c.idDevolucao(), c.idLivro(), c.titulo(), c.matriculaEstudante(),
                c.cpfEstudante(), c.emailEstudante(), c.vencimento(), c.devolvidoEm(), ocorridoEm);
    }

    /** Dias corridos de atraso (0 se no prazo), calculados só com os dados do evento. */
    public int diasAtraso() {
        if (vencimento == null || devolvidoEm == null) return 0;
        return (int) Math.max(0, ChronoUnit.DAYS.between(vencimento, devolvidoEm));
    }
}
