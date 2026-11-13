package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Papel GoF: ConcreteStrategy — política original: R$ 1,00 por dia de atraso (d × 1,00). */
public class MultaDiaria implements PoliticaDeMulta {

    private static final BigDecimal VALOR_DIA = new BigDecimal("1.00");

    @Override public String id() { return "ORIGINAL"; }
    @Override public String descricao() { return "R$ 1,00 por dia de atraso"; }

    @Override
    public BigDecimal calcular(int dias) {
        return VALOR_DIA.multiply(BigDecimal.valueOf(Math.max(0, dias))).setScale(2, RoundingMode.HALF_EVEN);
    }
}
