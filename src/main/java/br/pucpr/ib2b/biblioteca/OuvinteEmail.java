package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.comum.ServicoDeEmail;

/** Papel GoF: ConcreteObserver — envia ao estudante o recibo da devolução (e o aviso de atraso, se houver). */
public class OuvinteEmail implements OuvinteDeDevolucao {

    private final ServicoDeEmail email;

    public OuvinteEmail(ServicoDeEmail email) { this.email = email; }

    @Override
    public void aoDevolver(EventoDevolucao ev) {
        String atraso = ev.diasAtraso() > 0
                ? " Houve " + ev.diasAtraso() + " dia(s) de atraso; o boleto da multa será enviado pelo banco."
                : " Devolução dentro do prazo.";
        email.enviar(ev.emailEstudante(), "Devolução registrada: " + ev.titulo(),
                "Recebemos o livro \"" + ev.titulo() + "\" em " + ev.devolvidoEm() + "." + atraso);
    }
}
