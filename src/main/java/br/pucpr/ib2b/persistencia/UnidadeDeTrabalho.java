package br.pucpr.ib2b.persistencia;

import java.util.function.Supplier;

/**
 * Papel GoF: AbstractProduct (Abstract Factory, Req. 8) — a transação.
 * Todos os repositórios de uma família leem e alteram a MESMA cópia de trabalho desta unidade;
 * {@link #confirmar()} grava tudo de uma vez e {@link #desfazer()} descarta as alterações.
 */
public interface UnidadeDeTrabalho {

    /** Torna definitivas as alterações feitas desde a última confirmação. */
    void confirmar();

    /** Descarta as alterações feitas desde a última confirmação. */
    void desfazer();

    /**
     * Executa a operação como uma transação: confirma se terminar bem, desfaz se lançar exceção.
     * Chamadas aninhadas participam da transação mais externa (só ela confirma ou desfaz).
     */
    <T> T executar(Supplier<T> operacao);

    default void executar(Runnable operacao) {
        executar(() -> { operacao.run(); return null; });
    }
}
