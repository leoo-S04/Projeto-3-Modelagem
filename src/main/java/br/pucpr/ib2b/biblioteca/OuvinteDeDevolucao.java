package br.pucpr.ib2b.biblioteca;

/** Papel GoF: Observer (Req. 5). Uma reação independente à devolução. */
public interface OuvinteDeDevolucao {
    void aoDevolver(EventoDevolucao evento);

    /** Nome exibido nos registros de falha. */
    default String nome() { return getClass().getSimpleName(); }
}
