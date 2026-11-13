package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Notificação de pagamento já traduzida pelo adaptador para o formato institucional. */
public record ConfirmacaoPagamento(String banco, String idExterno, String idPagamento, BigDecimal valorPago,
                                   LocalDate dataPagamento) { }
