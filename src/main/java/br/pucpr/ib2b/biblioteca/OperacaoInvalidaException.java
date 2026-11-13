package br.pucpr.ib2b.biblioteca;

/**
 * Recusa de domínio: o evento não é aceito no estado atual do Livro (ou sua guarda é falsa).
 * É lançada antes de qualquer alteração, por isso o livro fica como estava.
 */
public class OperacaoInvalidaException extends RuntimeException {

    public OperacaoInvalidaException(Situacao situacao, String evento) {
        super("Operação " + evento + " não permitida: livro está " + situacao);
    }

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
