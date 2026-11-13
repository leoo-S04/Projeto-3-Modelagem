package br.pucpr.ib2b.financeiro.bancos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Papel GoF: Adaptee («external») — simulação da API do banco B, incompatível com a do banco A: valor DECIMAL
 * em reais, documento FORMATADO (000.000.000-00), cobrança identificada por "txid", situações em texto
 * ("ATIVA", "CONCLUIDA", "REMOVIDA") e notificação de pagamento em XML.
 */
public class ApiBancoB {

    private final AtomicInteger sequencia = new AtomicInteger();
    private final Map<String, String> cobrancas = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> valores = new ConcurrentHashMap<>();
    private volatile BigDecimal ultimoValor;
    private volatile String ultimoDocumento;

    public CobrancaB abrirCobranca(String documento, BigDecimal valorDecimal, String referencia) {
        String txid = String.format("TXB%06d", sequencia.incrementAndGet());
        cobrancas.put(txid, "ATIVA");
        valores.put(txid, valorDecimal);
        ultimoValor = valorDecimal;
        ultimoDocumento = documento;
        return new CobrancaB(txid, "00020126BR.GOV.BCB.PIX" + txid + valorDecimal);
    }

    public String statusCobranca(String txid) { return cobrancas.getOrDefault(txid, "REMOVIDA"); }

    /** Simula o pagamento: conclui a cobrança e devolve a notificação (XML) que o banco enviaria. */
    public String pagar(String txid, LocalDate data) {
        cobrancas.put(txid, "CONCLUIDA");
        return "<notificacao><txid>" + txid + "</txid><situacao>CONCLUIDA</situacao><valor>"
                + valores.getOrDefault(txid, BigDecimal.ZERO) + "</valor><e2eid>E2E-" + txid + "</e2eid><pagoEm>"
                + data + "</pagoEm></notificacao>";
    }

    /** Para a verificação do Req. 3. */
    public BigDecimal ultimoValor() { return ultimoValor; }
    public String ultimoDocumento() { return ultimoDocumento; }
}
