package br.pucpr.ib2b.financeiro.bancos;

/** Cobrança aberta no banco B: identificada pelo "txid". */
public record CobrancaB(String txid, String copiaECola) { }
