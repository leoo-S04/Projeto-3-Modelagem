package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.comum.ExcecaoDeDominio;

/** Ciclo na árvore, componente que já tem pai, ou livro que já pertence a outro grupo. A árvore não muda. */
public class HierarquiaInvalidaException extends ExcecaoDeDominio {
    public HierarquiaInvalidaException(String motivo) {
        super("Organização do acervo: " + motivo);
    }
}
