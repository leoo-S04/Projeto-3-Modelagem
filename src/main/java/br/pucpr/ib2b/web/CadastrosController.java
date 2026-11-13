package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas de cadastro (Administrador): pessoa, estudante, atendente e livro — via tratadores (Template Method). */
@Controller
@RequestMapping("/cadastros")
public class CadastrosController extends ControladorBase {

    private final TratadorDePessoa pessoas;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeAtendente atendentes;
    private final TratadorDeAcervo acervo;

    public CadastrosController(TratadorDePessoa pessoas, TratadorDeEstudante estudantes,
                               TratadorDeAtendente atendentes, TratadorDeAcervo acervo) {
        this.pessoas = pessoas;
        this.estudantes = estudantes;
        this.atendentes = atendentes;
        this.acervo = acervo;
    }

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("pessoas", pessoas.listarPessoas());
        model.addAttribute("estudantes", estudantes.listarEstudantes());
        model.addAttribute("atendentes", atendentes.listarAtendentes());
        model.addAttribute("livros", acervo.listarLivros());
        return "cadastros";
    }

    @PostMapping("/pessoa")
    public String inserirPessoa(@RequestParam String cpf, @RequestParam String nome, @RequestParam String sobrenome,
                                @RequestParam String email, RedirectAttributes ra) {
        return executar(ra, "/cadastros",
                () -> descrever(pessoas.inserirPessoa(cpf.trim(), nome.trim(), sobrenome.trim(), email.trim()), "Pessoa " + cpf));
    }

    @PostMapping("/pessoa/excluir")
    public String excluirPessoa(@RequestParam String cpf, RedirectAttributes ra) {
        return executar(ra, "/cadastros",
                () -> pessoas.excluirPessoa(cpf) ? "Pessoa " + cpf + " excluída." : erro("Pessoa " + cpf + " não cadastrada."));
    }

    @PostMapping("/estudante")
    public String inserirEstudante(@RequestParam String cpf, @RequestParam String matricula, RedirectAttributes ra) {
        return executar(ra, "/cadastros",
                () -> descrever(estudantes.inserirEstudante(cpf.trim(), matricula.trim()), "Estudante " + matricula));
    }

    @PostMapping("/estudante/excluir")
    public String excluirEstudante(@RequestParam String matricula, RedirectAttributes ra) {
        return executar(ra, "/cadastros", () -> estudantes.excluirEstudante(matricula)
                ? "Estudante " + matricula + " excluído(a)." : erro("Estudante " + matricula + " não cadastrado(a)."));
    }

    @PostMapping("/atendente")
    public String inserirAtendente(@RequestParam String cpf, @RequestParam String matriculaFuncional, RedirectAttributes ra) {
        return executar(ra, "/cadastros",
                () -> descrever(atendentes.inserirAtendente(cpf.trim(), matriculaFuncional.trim()), "Atendente " + matriculaFuncional));
    }

    @PostMapping("/atendente/excluir")
    public String excluirAtendente(@RequestParam String matriculaFuncional, RedirectAttributes ra) {
        return executar(ra, "/cadastros", () -> atendentes.excluirAtendente(matriculaFuncional)
                ? "Atendente " + matriculaFuncional + " excluído(a)." : erro("Atendente " + matriculaFuncional + " não cadastrado(a)."));
    }

    @PostMapping("/livro")
    public String inserirLivro(@RequestParam int idLivro, @RequestParam String titulo, RedirectAttributes ra) {
        return executar(ra, "/cadastros", () -> descrever(acervo.inserirLivro(idLivro, titulo.trim()), "Livro " + idLivro));
    }

    @PostMapping("/livro/excluir")
    public String excluirLivro(@RequestParam int idLivro, RedirectAttributes ra) {
        return executar(ra, "/cadastros",
                () -> acervo.excluirLivro(idLivro) ? "Livro " + idLivro + " excluído." : erro("Livro " + idLivro + " não cadastrado."));
    }
}
