package br.pucpr.ib2b.web;

import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.comum.ServicoDeEmailSimulado;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Administração da demonstração: relógio ajustável (simular devolução atrasada sem esperar), caixa de saída do
 * e-mail simulado e falha simulada do e-mail (para mostrar que um ouvinte que falha não desfaz a devolução).
 */
@Controller
@RequestMapping("/admin")
public class AdministracaoController extends ControladorBase {

    private final RelogioAjustavel relogio;
    private final ServicoDeEmailSimulado email;
    private final TratadorDeDevolucao devolucoes;

    public AdministracaoController(RelogioAjustavel relogio, ServicoDeEmailSimulado email, TratadorDeDevolucao devolucoes) {
        this.relogio = relogio;
        this.email = email;
        this.devolucoes = devolucoes;
    }

    @GetMapping
    public String pagina(Model model) {
        List<ServicoDeEmailSimulado.Mensagem> mensagens = new ArrayList<>(email.enviadas());
        Collections.reverse(mensagens);
        model.addAttribute("emails", mensagens);
        model.addAttribute("emailFalhando", email.estaFalhando());
        model.addAttribute("falhasPendentes", devolucoes.falhasDeNotificacaoPendentes());
        return "admin";
    }

    @PostMapping("/relogio/avancar")
    public String avancar(@RequestParam int dias, RedirectAttributes ra) {
        return executar(ra, "/admin", () -> { relogio.avancarDias(dias); return "Relógio adiantado " + dias + " dia(s): hoje é " + relogio.hoje() + "."; });
    }

    @PostMapping("/relogio/definir")
    public String definir(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data, RedirectAttributes ra) {
        return executar(ra, "/admin", () -> { relogio.definirData(data); return "Hoje passou a ser " + relogio.hoje() + "."; });
    }

    @PostMapping("/relogio/normal")
    public String normal(RedirectAttributes ra) {
        return executar(ra, "/admin", () -> { relogio.voltarAoNormal(); return "Relógio de volta à data real: " + relogio.hoje() + "."; });
    }

    @PostMapping("/email/falha")
    public String falhaDoEmail(@RequestParam boolean ativar, RedirectAttributes ra) {
        return executar(ra, "/admin", () -> {
            email.simularFalha(ativar);
            return ativar ? "Servidor de e-mail simulado FORA DO AR." : "Servidor de e-mail simulado normalizado.";
        });
    }

    @PostMapping("/notificacoes/reenviar")
    public String reenviar(RedirectAttributes ra) {
        return executar(ra, "/admin", () -> {
            devolucoes.reenviarNotificacoesComFalha();
            return "Reenvio feito. Falhas pendentes: " + devolucoes.falhasDeNotificacaoPendentes() + ".";
        });
    }
}
