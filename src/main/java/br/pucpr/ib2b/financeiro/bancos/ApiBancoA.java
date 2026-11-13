package br.pucpr.ib2b.financeiro.bancos;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Papel GoF: Adaptee («external») — simulação da API do banco A, usada "como é" (Req. 3: os bancos podem ser
 * simulados). Particularidades: valor em CENTAVOS (long), CPF sem formatação, título identificado por
 * "nosso número", situações EM_ABERTO/LIQUIDADO/BAIXADO e notificação de pagamento em JSON (webhook).
 */
public class ApiBancoA {

    private final AtomicInteger sequencia = new AtomicInteger();
    private final Map<String, StatusA> titulos = new ConcurrentHashMap<>();
    private final Map<String, Long> valores = new ConcurrentHashMap<>();
    private volatile long ultimoValorCentavos = -1;

    public TituloA gerarTitulo(String cpf, long centavos, String seuNumero) {
        String nossoNumero = String.format("A-%04d", sequencia.incrementAndGet());
        titulos.put(nossoNumero, StatusA.EM_ABERTO);
        valores.put(nossoNumero, centavos);
        ultimoValorCentavos = centavos;
        return new TituloA(nossoNumero, "34191.79001 " + nossoNumero + " " + centavos);
    }

    public StatusA consultarTitulo(String nossoNumero) { return titulos.getOrDefault(nossoNumero, StatusA.BAIXADO); }

    /** Simula o pagamento no banco: liquida o título e devolve o webhook (JSON) que o banco enviaria. */
    public String liquidar(String nossoNumero, LocalDate data) {
        titulos.put(nossoNumero, StatusA.LIQUIDADO);
        return "{\"evento\":\"TITULO_LIQUIDADO\",\"nossoNumero\":\"" + nossoNumero + "\",\"status\":\"LIQUIDADO\","
                + "\"valorPagoCentavos\":" + valores.getOrDefault(nossoNumero, 0L) + ","
                + "\"idPagamento\":\"PGA-" + nossoNumero + "\",\"dataPagamento\":\"" + data + "\"}";
    }

    /** Para a verificação do Req. 3: o último valor recebido, em centavos. */
    public long ultimoValorCentavos() { return ultimoValorCentavos; }
}
