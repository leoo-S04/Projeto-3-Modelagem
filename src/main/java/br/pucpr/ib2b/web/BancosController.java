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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fronteira com o componente «external» Banco (diagrama de implantação: servidores dos bancos ligados por HTTPS).
 * <ul>
 *   <li>Pontos de entrada das notificações reais: o banco A chama o webhook JSON, o banco B envia XML; cada um vai
 *       para o seu adaptador (Adapter, Req. 3), que traduz e entrega ao Financeiro (iQuitacao).</li>
 *   <li>Simulador para a demonstração: "pagar" um boleto faz o banco simulado gerar a notificação e entregá-la ao
 *       mesmo ponto de entrada. "Reenviar" repete a última notificação — e a quitação não acontece duas vezes.</li>
 * </ul>
 */
@Controller
public class BancosController extends ControladorBase {

    private final ApiBancoA apiBancoA;
    private final ApiBancoB apiBancoB;
    private final AdaptadorBancoA adaptadorBancoA;
    private final AdaptadorBancoB adaptadorBancoB;
    private final RelogioAjustavel relogio;
    /** Última notificação enviada por id externo (para o botão "reenviar"). */
    private final Map<String, String> ultimasNotificacoes = new ConcurrentHashMap<>();

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

    // ---- Simulador dos bancos (demonstração) ----
    @PostMapping("/simulador/banco-a/pagar/{nossoNumero}")
    public String pagarNoBancoA(@PathVariable String nossoNumero, RedirectAttributes ra) {
        return executar(ra, "/financeiro", () -> {
            String webhook = apiBancoA.liquidar(nossoNumero, relogio.hoje());
            ultimasNotificacoes.put(nossoNumero, webhook);
            return "Banco A pagou o título " + nossoNumero + " e enviou o webhook (" + webhookBancoA(webhook) + ").";
        });
    }

    @PostMapping("/simulador/banco-b/pagar/{txid}")
    public String pagarNoBancoB(@PathVariable String txid, RedirectAttributes ra) {
        return executar(ra, "/financeiro", () -> {
            String xml = apiBancoB.pagar(txid, relogio.hoje());
            ultimasNotificacoes.put(txid, xml);
            return "Banco B concluiu a cobrança " + txid + " e enviou a notificação XML (" + notificacaoBancoB(xml) + ").";
        });
    }

    @PostMapping("/simulador/banco-a/reenviar/{nossoNumero}")
    public String reenviarBancoA(@PathVariable String nossoNumero, RedirectAttributes ra) {
        return executar(ra, "/financeiro", () -> {
            String webhook = ultimasNotificacoes.get(nossoNumero);
            if (webhook == null) return erro("Nenhuma notificação enviada ainda para " + nossoNumero + ".");
            webhookBancoA(webhook);
            return "Webhook do banco A reenviado para " + nossoNumero + ": o boleto continua com UMA quitação.";
        });
    }

    @PostMapping("/simulador/banco-b/reenviar/{txid}")
    public String reenviarBancoB(@PathVariable String txid, RedirectAttributes ra) {
        return executar(ra, "/financeiro", () -> {
            String xml = ultimasNotificacoes.get(txid);
            if (xml == null) return erro("Nenhuma notificação enviada ainda para " + txid + ".");
            notificacaoBancoB(xml);
            return "Notificação do banco B reenviada para " + txid + ": o boleto continua com UMA quitação.";
        });
    }
}
