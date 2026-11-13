package br.pucpr.ib2b.web;

import br.pucpr.ib2b.biblioteca.NoDoAcervoDTO;
import br.pucpr.ib2b.biblioteca.RegistroDeMetricas;
import br.pucpr.ib2b.biblioteca.TipoDeGrupo;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/** Consulta pública (Decorator) e organização hierárquica do acervo (Composite), via TratadorDeAcervo. */
@Controller
@RequestMapping("/acervo")
public class AcervoController extends ControladorBase {

    private final TratadorDeAcervo acervo;
    private final RegistroDeMetricas metricas;

    public AcervoController(TratadorDeAcervo acervo, RegistroDeMetricas metricas) {
        this.acervo = acervo;
        this.metricas = metricas;
    }

    @GetMapping
    public String pagina(@RequestParam(required = false) String termo, Model model) {
        if (termo != null) {
            model.addAttribute("termo", termo);
            model.addAttribute("resultados", acervo.buscarMetadados(termo));
        }
        List<RegistroDeMetricas.Medicao> todas = metricas.medicoes();
        model.addAttribute("medicoes", todas.subList(Math.max(0, todas.size() - 8), todas.size()));
        List<NoDoAcervoDTO> estrutura = acervo.estruturaDoAcervo();
        model.addAttribute("estrutura", estrutura);
        model.addAttribute("grupos", nomes(estrutura));
        model.addAttribute("tipos", TipoDeGrupo.values());
        model.addAttribute("livros", acervo.listarLivros());
        return "acervo";
    }

    @PostMapping("/unidade")
    public String criarUnidade(@RequestParam String nome, RedirectAttributes ra) {
        return executar(ra, "/acervo", () -> { acervo.criarUnidade(nome.trim()); return "Unidade '" + nome + "' criada."; });
    }

    @PostMapping("/grupo")
    public String criarGrupo(@RequestParam String pai, @RequestParam String nome, @RequestParam TipoDeGrupo tipo,
                             RedirectAttributes ra) {
        return executar(ra, "/acervo", () -> {
            acervo.criarGrupo(pai, nome.trim(), tipo);
            return tipo + " '" + nome + "' criada em '" + pai + "'.";
        });
    }

    @PostMapping("/livro")
    public String adicionarLivro(@RequestParam String grupo, @RequestParam int idLivro, RedirectAttributes ra) {
        return executar(ra, "/acervo", () -> {
            acervo.adicionarLivroAoGrupo(grupo, idLivro);
            return "Livro " + idLivro + " colocado em '" + grupo + "' (" + acervo.contarLivros(grupo) + " livro(s) no grupo).";
        });
    }

    @PostMapping("/mover")
    public String mover(@RequestParam int idLivro, @RequestParam String destino, RedirectAttributes ra) {
        return executar(ra, "/acervo", () -> {
            acervo.moverLivro(idLivro, destino);
            return "Livro " + idLivro + " movido para '" + destino + "'. Id, estado e empréstimo do livro não mudam.";
        });
    }

    private static List<String> nomes(List<NoDoAcervoDTO> nos) {
        List<String> saida = new ArrayList<>();
        for (NoDoAcervoDTO n : nos) {
            saida.add(n.nome());
            saida.addAll(nomes(n.subgrupos()));
        }
        return saida;
    }
}
