package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.comum.ExcecaoDeDominio;

/** A configuração só aceita ids de políticas registradas no seletor. */
public class PoliticaInexistenteException extends ExcecaoDeDominio {
    public PoliticaInexistenteException(String id) {
        super("Política de multa inexistente: " + id);
    }
}
