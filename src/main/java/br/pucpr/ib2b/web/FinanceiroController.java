package br.pucpr.ib2b.web;

import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Financeiro: boletos emitidos, política de multa vigente (Strategy) e simulação. Só chama o TratadorDeMulta. */
@Controller
@RequestMapping("/financeiro")
public class FinanceiroController extends ControladorBase {

    private final TratadorDeMulta multas;

    public FinanceiroController(TratadorDeMulta multas) { this.multas = multas; }

    @GetMapping
    public String pagina(@RequestParam(required = false) Integer dias, Model model) {
        model.addAttribute("cobrancas", multas.relatorioDeCobrancas());
        model.addAttribute("politicas", multas.politicasDisponiveis());
        model.addAttribute("politicaVigente", multas.politicaVigente());
        model.addAttribute("bancoEmUso", multas.bancoEmUso());
        if (dias != null) {
            model.addAttribute("dias", dias);
            model.addAttribute("simulacao", multas.simular(dias));
        }
        return "financeiro";
    }

    @PostMapping("/politica")
    public String definirPolitica(@RequestParam String id, RedirectAttributes ra) {
        return executar(ra, "/financeiro", () -> {
            multas.definirPoliticaVigente(id);
            return "Política vigente: " + id + ". Multas já emitidas não são recalculadas.";
        });
    }
}
