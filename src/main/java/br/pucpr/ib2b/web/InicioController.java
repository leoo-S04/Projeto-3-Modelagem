package br.pucpr.ib2b.web;

import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Página inicial: situação dos livros e indicadores. Só chama tratadores. */
@Controller
public class InicioController {

    private final TratadorDeAcervo acervo;
    private final TratadorDeDevolucao devolucao;
    private final TratadorDeMulta multas;

    public InicioController(TratadorDeAcervo acervo, TratadorDeDevolucao devolucao, TratadorDeMulta multas) {
        this.acervo = acervo;
        this.devolucao = devolucao;
        this.multas = multas;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("livros", acervo.listarLivros());
        model.addAttribute("indicadores", devolucao.indicadores());
        model.addAttribute("inspecoes", devolucao.inspecoesPendentes());
        model.addAttribute("cobrancas", multas.relatorioDeCobrancas());
        return "inicio";
    }
}
