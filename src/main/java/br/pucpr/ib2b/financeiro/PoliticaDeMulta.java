package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;

/**
 * Papel GoF: Strategy (Req. 2). Contrato das políticas de cálculo de multa: quem usa (ServicoDeMulta,
 * ServicoDeSimulacao) calcula sem saber qual política está em uso.
 */
public interface PoliticaDeMulta {
    /** Identificador gravado na configuração e na multa (ex.: "ORIGINAL"). */
    String id();
    String descricao();
    /** Valor da multa para {@code dias} dias corridos de atraso (d >= 0), em reais, escala 2. */
    BigDecimal calcular(int dias);
}
