package br.pucpr.ib2b.persistencia;

import java.util.function.Supplier;

/**
 * Parte comum às duas famílias: guarda a cópia de trabalho e implementa {@link #executar} (confirma no
 * sucesso, desfaz na falha). O que muda entre famílias é COMO confirmar e desfazer (subclasses).
 */
abstract class UnidadeDeTrabalhoBase implements UnidadeDeTrabalho {

    /** volatile: desfazer() troca a cópia inteira, e outras threads devem ver a nova. */
    protected volatile BaseDeDados dados;
    private int profundidade = 0;

    BaseDeDados dados() { return dados; }

    /**
     * synchronized: uma operação de escrita por vez. Duas operações simultâneas não podem alterar a mesma
     * cópia de trabalho nem gravar o arquivo ao mesmo tempo.
     */
    @Override
    public synchronized <T> T executar(Supplier<T> operacao) {
        profundidade++;
        try {
            T resultado = operacao.get();
            if (profundidade == 1) confirmar();
            return resultado;
        } catch (RuntimeException falha) {
            if (profundidade == 1) desfazer();
            throw falha;
        } finally {
            profundidade--;
        }
    }

    /** Ids sequenciais guardados na própria cópia de trabalho (são gravados junto). */
    synchronized int proximoIdTransacao() { return ++dados.ultimoIdTransacao; }
    synchronized int proximoIdPapel() { return ++dados.ultimoIdPapel; }
}
