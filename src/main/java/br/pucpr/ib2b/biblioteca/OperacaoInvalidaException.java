package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.comum.ExcecaoDeDominio;

/**
 * Recusa de domínio: o evento não é aceito no estado atual do Livro (ou sua guarda é falsa).
 * É lançada antes de qualquer alteração, por isso o livro fica como estava.
 */
public class OperacaoInvalidaException extends ExcecaoDeDominio {

    public OperacaoInvalidaException(Situacao situacao, String evento) {
        super("Operação " + evento + " não permitida: livro está " + situacao);
    }

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
