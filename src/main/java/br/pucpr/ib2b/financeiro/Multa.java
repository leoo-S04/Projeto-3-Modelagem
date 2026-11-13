package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Multa emitida (Req. 2). Grava o VALOR calculado e a POLÍTICA aplicada no momento da emissão, e nunca é
 * recalculada: trocar a política vigente depois não altera multas já emitidas.
 */
public record Multa(int idMulta, String cpf, int idDevolucao, int diasAtraso, BigDecimal valor,
                    String politicaAplicada, LocalDate emitidaEm) { }
