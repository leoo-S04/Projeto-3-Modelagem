package br.pucpr.ib2b.biblioteca;

/** Papel GoF: ConcreteObserver — agenda a inspeção do livro devolvido. */
public class OuvinteInspecao implements OuvinteDeDevolucao {

    private final AgendaDeInspecao agenda;

    public OuvinteInspecao(AgendaDeInspecao agenda) { this.agenda = agenda; }

    @Override
    public void aoDevolver(EventoDevolucao ev) {
        agenda.agendar(new AgendaDeInspecao.ItemDeInspecao(ev.idLivro(), ev.titulo(), ev.devolvidoEm(), ev.diasAtraso()));
    }
}
