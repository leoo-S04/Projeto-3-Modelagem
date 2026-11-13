package br.pucpr.ib2b.comum;

/**
 * Base das recusas de regra de negócio (estado inválido do livro, cadastro recusado, reserva inválida...).
 * Diferente de erro técnico: a mensagem é feita para ser mostrada ao usuário.
 */
public class ExcecaoDeDominio extends RuntimeException {
    public ExcecaoDeDominio(String mensagem) {
        super(mensagem);
    }
}
