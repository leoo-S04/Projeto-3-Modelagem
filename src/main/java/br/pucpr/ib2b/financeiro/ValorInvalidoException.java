package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.comum.ExcecaoDeDominio;

/** Valor que não pode ser convertido para o formato do banco (ex.: mais de duas casas decimais). */
public class ValorInvalidoException extends ExcecaoDeDominio {
    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
