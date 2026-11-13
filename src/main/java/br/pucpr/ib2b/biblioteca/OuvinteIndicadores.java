package br.pucpr.ib2b.biblioteca;

/** Papel GoF: ConcreteObserver — atualiza os indicadores de devolução. */
public class OuvinteIndicadores implements OuvinteDeDevolucao {

    private final PainelDeIndicadores painel;

    public OuvinteIndicadores(PainelDeIndicadores painel) { this.painel = painel; }

    @Override
    public void aoDevolver(EventoDevolucao ev) { painel.registrar(ev.diasAtraso()); }
}
