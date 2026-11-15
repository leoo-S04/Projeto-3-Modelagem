package br.pucpr.ib2b.web;

import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.financeiro.AdaptadorBancoA;
import br.pucpr.ib2b.financeiro.AdaptadorBancoB;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoA;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoB;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Fronteira com o Banco «external».
 * - Pontos de entrada das notificações: o banco A chama um webhook JSON, o banco B envia XML; cada uma vai para o
 *   seu adaptador (Adapter, Req. 3), que traduz e entrega ao Financeiro.
 * - Simulação do pagamento (para a demonstração): o estudante "paga" o boleto, o banco simulado gera a notificação
 *   e a entrega ao mesmo ponto de entrada.
 */
@Controller
public class BancosController extends ControladorBase {

    private final ApiBancoA apiBancoA;
    private final ApiBancoB apiBancoB;
    private final AdaptadorBancoA adaptadorBancoA;
    private final AdaptadorBancoB adaptadorBancoB;
    private final RelogioAjustavel relogio;

    public BancosController(ApiBancoA apiBancoA, ApiBancoB apiBancoB, AdaptadorBancoA adaptadorBancoA,
                            AdaptadorBancoB adaptadorBancoB, RelogioAjustavel relogio) {
        this.apiBancoA = apiBancoA;
        this.apiBancoB = apiBancoB;
        this.adaptadorBancoA = adaptadorBancoA;
        this.adaptadorBancoB = adaptadorBancoB;
        this.relogio = relogio;
    }

    // ---- Pontos de entrada chamados pelos bancos ----

    @PostMapping(value = "/api/bancos/a/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String webhookBancoA(@RequestBody String json) { return adaptadorBancoA.receberWebhook(json); }

    @PostMapping(value = "/api/bancos/b/notificacao", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    @ResponseBody
    public String notificacaoBancoB(@RequestBody String xml) { return adaptadorBancoB.receberNotificacao(xml); }

    // ---- Pagamento simulado (área do estudante) ----

    @PostMapping("/simulador/banco-a/pagar/{nossoNumero}")
    public String pagarNoBancoA(@PathVariable String nossoNumero, RedirectAttributes ra) {
        return executar(ra, "/estudante", () -> {
            webhookBancoA(apiBancoA.liquidar(nossoNumero, relogio.hoje()));
            return "Pagamento confirmado pelo banco A.";
        });
    }

    @PostMapping("/simulador/banco-b/pagar/{txid}")
    public String pagarNoBancoB(@PathVariable String txid, RedirectAttributes ra) {
        return executar(ra, "/estudante", () -> {
            notificacaoBancoB(apiBancoB.pagar(txid, relogio.hoje()));
            return "Pagamento confirmado pelo banco B.";
        });
    }
}
