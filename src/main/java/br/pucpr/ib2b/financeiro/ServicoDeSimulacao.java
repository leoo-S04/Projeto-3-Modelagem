package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Papel GoF: outro Context do Strategy (Req. 2). Simula o valor de cada política para um número de dias,
 * reutilizando o MESMO cálculo da produção. Não cria multa nem emite boleto.
 */
public class ServicoDeSimulacao {

    private final SeletorDePolitica seletor;

    public ServicoDeSimulacao(SeletorDePolitica seletor) { this.seletor = seletor; }

    /** Ex.: simular(5) → {ORIGINAL=5.00, TETO=5.00, TOLERANCIA=3.00}. */
    public Map<String, BigDecimal> simular(int dias) {
        Map<String, BigDecimal> valores = new LinkedHashMap<>();
        for (PoliticaDeMulta p : seletor.todas()) valores.put(p.id(), p.calcular(dias));
        return valores;
    }
}
