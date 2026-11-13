package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;

/**
 * Papel GoF: Target do Adapter (Req. 3) — contrato institucional de cobrança de que o Financeiro depende
 * (Astah: iCobranca, fornecida pelo componente externo Banco). Valores sempre em reais (BigDecimal, escala 2).
 * Cada banco real tem uma API diferente; um adaptador por banco converte para este contrato.
 */
public interface iCobranca {

    /** Registra o boleto no banco e devolve o identificador externo (nosso número, txid...). */
    String registrarBoleto(int idBoleto, String cpf, BigDecimal valor, String email);

    /** true se o banco informa a cobrança como paga. */
    boolean consultarPagamento(String idExterno);

    /** Código do banco atendido por este adaptador ("A", "B"). */
    String codigoDoBanco();
}
