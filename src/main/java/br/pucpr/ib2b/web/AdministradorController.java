package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.NoDoAcervoDTO;
import br.pucpr.ib2b.biblioteca.RegistroDeMetricas;
import br.pucpr.ib2b.biblioteca.TipoDeGrupo;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.comum.ServicoDeEmailSimulado;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Área do administrador (casos de uso: manter pessoa, estudante, atendente e livro) + organização do acervo,
 * política de multa e ferramentas da demonstração (relógio, e-mails enviados). Só chama tratadores.
 */
@Controller
@RequestMapping("/admin")
public class AdministradorController extends ControladorBase {

    private final TratadorDePessoa pessoas;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeAtendente atendentes;
    private final TratadorDeAcervo acervo;
    private final TratadorDeMulta multas;
    private final RelogioAjustavel relogio;
    private final ServicoDeEmailSimulado email;
    private final RegistroDeMetricas metricas;

    public AdministradorController(TratadorDePessoa pessoas, TratadorDeEstudante estudantes, TratadorDeAtendente atendentes,
                                   TratadorDeAcervo acervo, TratadorDeMulta multas, RelogioAjustavel relogio,
                                   ServicoDeEmailSimulado email, RegistroDeMetricas metricas) {
        this.pessoas = pessoas;
        this.estudantes = estudantes;
        this.atendentes = atendentes;
        this.acervo = acervo;
        this.multas = multas;
        this.relogio = relogio;
        this.email = email;
        this.metricas = metricas;
    }

    @GetMapping
    public String pagina(@RequestParam(defaultValue = "pessoas") String aba, @RequestParam(required = false) Integer dias,
                         Model model) {
        model.addAttribute("aba", aba);
        model.addAttribute("pessoas", pessoas.listarPessoas());
        model.addAttribute("estudantes", estudantes.listarEstudantes());
        model.addAttribute("atendentes", atendentes.listarAtendentes());
        model.addAttribute("livros", acervo.listarLivros());
        List<NoDoAcervoDTO> estrutura = acervo.estruturaDoAcervo();
        model.addAttribute("estrutura", estrutura);
        model.addAttribute("grupos", nomes(estrutura));
        model.addAttribute("tipos", TipoDeGrupo.values());
        model.addAttribute("cobrancas", multas.relatorioDeCobrancas());
        model.addAttribute("politicas", multas.politicasDisponiveis());
        model.addAttribute("politicaVigente", multas.politicaVigente());
        if (dias != null) {
            model.addAttribute("dias", dias);
            model.addAttribute("simulacao", multas.simular(dias));
        }
        List<ServicoDeEmailSimulado.Mensagem> emails = new ArrayList<>(email.enviadas());
        Collections.reverse(emails);
        model.addAttribute("emails", emails);
        List<RegistroDeMetricas.Medicao> medicoes = new ArrayList<>(metricas.medicoes());
        Collections.reverse(medicoes);
        model.addAttribute("medicoes", medicoes.subList(0, Math.min(8, medicoes.size())));
        return "administrador";
    }

    // ---- Pessoas, estudantes e atendentes ----

    @PostMapping("/pessoa")
    public String inserirPessoa(@RequestParam String cpf, @RequestParam String nome, @RequestParam String sobrenome,
                                @RequestParam String email, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=pessoas", () -> descrever(
                pessoas.inserirPessoa(cpf.replaceAll("\\D", ""), nome.trim(), sobrenome.trim(), email.trim()), "Pessoa"));
    }

    @PostMapping("/pessoa/excluir")
    public String excluirPessoa(@RequestParam String cpf, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=pessoas",
                () -> pessoas.excluirPessoa(cpf) ? "Pessoa excluída." : erro("Pessoa não encontrada."));
    }

    @PostMapping("/estudante")
    public String inserirEstudante(@RequestParam String cpf, @RequestParam String matricula, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=pessoas",
                () -> descrever(estudantes.inserirEstudante(cpf.replaceAll("\\D", ""), matricula.trim()), "Estudante " + matricula.trim()));
    }

    @PostMapping("/estudante/excluir")
    public String excluirEstudante(@RequestParam String matricula, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=pessoas",
                () -> estudantes.excluirEstudante(matricula) ? "Estudante " + matricula + " excluído(a)." : erro("Estudante não encontrado(a)."));
    }

    @PostMapping("/atendente")
    public String inserirAtendente(@RequestParam String cpf, @RequestParam String matriculaFuncional, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=pessoas", () -> descrever(
                atendentes.inserirAtendente(cpf.replaceAll("\\D", ""), matriculaFuncional.trim()), "Atendente " + matriculaFuncional.trim()));
    }

    @PostMapping("/atendente/excluir")
    public String excluirAtendente(@RequestParam String matriculaFuncional, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=pessoas", () -> atendentes.excluirAtendente(matriculaFuncional)
                ? "Atendente " + matriculaFuncional + " excluído(a)." : erro("Atendente não encontrado(a)."));
    }

    // ---- Livros e organização do acervo ----

    @PostMapping("/livro")
    public String inserirLivro(@RequestParam int idLivro, @RequestParam String titulo, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=livros", () -> descrever(acervo.inserirLivro(idLivro, titulo.trim()), "Livro " + idLivro));
    }

    @PostMapping("/livro/excluir")
    public String excluirLivro(@RequestParam int idLivro, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=livros",
                () -> acervo.excluirLivro(idLivro) ? "Livro " + idLivro + " excluído." : erro("Livro não encontrado."));
    }

    @PostMapping("/acervo/unidade")
    public String criarUnidade(@RequestParam String nome, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=livros", () -> { acervo.criarUnidade(nome.trim()); return "Unidade \"" + nome.trim() + "\" criada."; });
    }

    @PostMapping("/acervo/grupo")
    public String criarGrupo(@RequestParam String pai, @RequestParam String nome, @RequestParam TipoDeGrupo tipo,
                             RedirectAttributes ra) {
        return executar(ra, "/admin?aba=livros", () -> {
            acervo.criarGrupo(pai, nome.trim(), tipo);
            return "\"" + nome.trim() + "\" criada dentro de \"" + pai + "\".";
        });
    }

    @PostMapping("/acervo/livro")
    public String colocarLivro(@RequestParam String grupo, @RequestParam int idLivro, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=livros", () -> {
            acervo.adicionarLivroAoGrupo(grupo, idLivro);
            return "Livro " + idLivro + " colocado em \"" + grupo + "\".";
        });
    }

    @PostMapping("/acervo/mover")
    public String mover(@RequestParam int idLivro, @RequestParam String destino, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=livros", () -> {
            acervo.moverLivro(idLivro, destino);
            return "Livro " + idLivro + " movido para \"" + destino + "\".";
        });
    }

    // ---- Multas ----

    @PostMapping("/politica")
    public String definirPolitica(@RequestParam String id, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=multas", () -> {
            multas.definirPoliticaVigente(id);
            return "Política de multa alterada. Multas já emitidas não mudam.";
        });
    }

    // ---- Demonstração: relógio ----

    @PostMapping("/relogio/avancar")
    public String avancar(@RequestParam int dias, RedirectAttributes ra) {
        return executar(ra, "/admin?aba=sistema",
                () -> { relogio.avancarDias(dias); return "Relógio adiantado " + dias + " dia(s): hoje é " + dataBr(relogio.hoje()) + "."; });
    }

    @PostMapping("/relogio/normal")
    public String normal(RedirectAttributes ra) {
        return executar(ra, "/admin?aba=sistema",
                () -> { relogio.voltarAoNormal(); return "Relógio de volta à data real: " + dataBr(relogio.hoje()) + "."; });
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
