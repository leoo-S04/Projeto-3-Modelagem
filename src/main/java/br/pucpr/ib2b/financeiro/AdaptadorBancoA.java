package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.financeiro.bancos.ApiBancoA;
import br.pucpr.ib2b.financeiro.bancos.StatusA;
import br.pucpr.ib2b.financeiro.bancos.TituloA;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Papel GoF: Adapter (de objeto) — Req. 3. Converte o contrato institucional iCobranca para a API do banco A
 * (saída) e traduz o webhook JSON do banco A em ConfirmacaoPagamento (entrada). Todo o conhecimento do banco A
 * fica aqui: reais → centavos, situação StatusA → pago/não pago, formato do webhook.
 */
public class AdaptadorBancoA implements iCobranca {

    private final ApiBancoA api;               // Adaptee
    private final iQuitacao receptor;          // porta de entrada do Financeiro
    private final ObjectMapper json = new ObjectMapper();

    public AdaptadorBancoA(ApiBancoA api, iQuitacao receptor) {
        this.api = api;
        this.receptor = receptor;
    }

    @Override
    public String registrarBoleto(int idBoleto, String cpf, BigDecimal valor, String email) {
        TituloA titulo = api.gerarTitulo(cpf, paraCentavos(valor), String.valueOf(idBoleto)); // idBoleto = "seu número"
        return titulo.nossoNumero();
    }

    @Override
    public boolean consultarPagamento(String nossoNumero) {
        return traduzir(api.consultarTitulo(nossoNumero));
    }

    @Override public String codigoDoBanco() { return "A"; }

    /** Entrada: webhook JSON do banco A → ConfirmacaoPagamento → iQuitacao. Responde "OK" mesmo se repetido. */
    public String receberWebhook(String corpoJson) {
        try {
            JsonNode n = json.readTree(corpoJson);
            if (!traduzir(StatusA.valueOf(n.get("status").asText()))) return "IGNORADO";
            ConfirmacaoPagamento c = new ConfirmacaoPagamento("A", n.get("nossoNumero").asText(),
                    n.get("idPagamento").asText(),
                    BigDecimal.valueOf(n.get("valorPagoCentavos").asLong(), 2),       // centavos → reais
                    LocalDate.parse(n.get("dataPagamento").asText()));
            receptor.registrarQuitacao(c);
            return "OK";
        } catch (IOException e) {
            throw new UncheckedIOException("Webhook do banco A inválido", e);
        }
    }

    /** R$ 7,00 → 700. Mais de duas casas decimais → recusa antes de chamar o banco. */
    private long paraCentavos(BigDecimal valor) {
        try {
            return valor.movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new ValorInvalidoException("Valor com mais de duas casas decimais: " + valor);
        }
    }

    private boolean traduzir(StatusA status) { return status == StatusA.LIQUIDADO; }
}
