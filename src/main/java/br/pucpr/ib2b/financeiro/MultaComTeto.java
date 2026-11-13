package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Papel GoF: ConcreteStrategy — R$ 1,00 por dia, limitado a R$ 20,00: min(d × 1,00; 20,00). */
public class MultaComTeto implements PoliticaDeMulta {

    private static final BigDecimal VALOR_DIA = new BigDecimal("1.00");
    private static final BigDecimal TETO = new BigDecimal("20.00");

    @Override public String id() { return "TETO"; }
    @Override public String descricao() { return "R$ 1,00 por dia, com teto de R$ 20,00"; }

    @Override
    public BigDecimal calcular(int dias) {
        BigDecimal valor = VALOR_DIA.multiply(BigDecimal.valueOf(Math.max(0, dias)));
        return valor.min(TETO).setScale(2, RoundingMode.HALF_EVEN);
    }
}
