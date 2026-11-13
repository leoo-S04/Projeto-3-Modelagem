package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.financeiro.iMulta;

/**
 * Papel GoF: ConcreteObserver — devolução atrasada → solicita a multa ao Financeiro (interface iMulta, que a
 * Biblioteca requer). No prazo, não faz nada. O idDevolucao é a chave de idempotência: se o Financeiro estiver
 * indisponível e a notificação for reenviada, não nasce uma segunda multa.
 */
public class OuvinteMulta implements OuvinteDeDevolucao {

    private final iMulta financeiro;

    public OuvinteMulta(iMulta financeiro) { this.financeiro = financeiro; }

    @Override
    public void aoDevolver(EventoDevolucao ev) {
        if (ev.diasAtraso() > 0) {
            financeiro.gerarMulta(ev.cpfEstudante(), ev.idDevolucao(), ev.diasAtraso());
        }
    }
}
