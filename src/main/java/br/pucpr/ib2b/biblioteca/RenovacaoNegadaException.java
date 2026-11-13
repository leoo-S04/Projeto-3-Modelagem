package br.pucpr.ib2b.biblioteca;

/** Recusa da renovação pela guarda numRenovacoes < 2 (no máximo duas renovações). */
public class RenovacaoNegadaException extends OperacaoInvalidaException {

    public RenovacaoNegadaException(int idLivro) {
        super("Renovação recusada para o livro " + idLivro + ": limite de 2 renovações");
    }
}
