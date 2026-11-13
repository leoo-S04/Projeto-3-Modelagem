package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.comum.ExcecaoDeDominio;

/** A efetivação do empréstimo exige uma reserva ativa do mesmo estudante (regra 7). */
public class ReservaInvalidaException extends ExcecaoDeDominio {
    public ReservaInvalidaException(String motivo) {
        super("Empréstimo recusado: " + motivo);
    }
}
