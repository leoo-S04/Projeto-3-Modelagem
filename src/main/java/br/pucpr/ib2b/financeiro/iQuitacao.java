package br.pucpr.ib2b.financeiro;

/**
 * Interface fornecida pelo Financeiro e requerida pelo Banco (Astah: iQuitacao) — porta de ENTRADA das
 * notificações de pagamento, já traduzidas pelo adaptador do banco.
 */
public interface iQuitacao {
    /** true = quitou agora; false = cobrança desconhecida ou já quitada (notificação repetida). */
    boolean registrarQuitacao(ConfirmacaoPagamento confirmacao);
}
