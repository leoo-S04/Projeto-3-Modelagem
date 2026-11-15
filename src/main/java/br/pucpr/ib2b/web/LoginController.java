package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Entrada no sistema. Estudante: matrícula + CPF. Atendente: matrícula funcional + CPF.
 * Administrador: usuário e senha de application.properties.
 * O CPF como senha é uma simulação para a demonstração (o domínio não guarda senhas).
 */
@Controller
public class LoginController {

    private final TratadorDeEstudante estudantes;
    private final TratadorDeAtendente atendentes;
    private final String usuarioAdmin;
    private final String senhaAdmin;

    public LoginController(TratadorDeEstudante estudantes, TratadorDeAtendente atendentes,
                           @Value("${biblioteca.admin.usuario:admin}") String usuarioAdmin,
                           @Value("${biblioteca.admin.senha:admin}") String senhaAdmin) {
        this.estudantes = estudantes;
        this.atendentes = atendentes;
        this.usuarioAdmin = usuarioAdmin;
        this.senhaAdmin = senhaAdmin;
    }

    @GetMapping("/")
    public String inicio(HttpSession sessao) {
        Usuario u = (Usuario) sessao.getAttribute(Usuario.CHAVE_NA_SESSAO);
        return u == null ? "redirect:/login" : "redirect:" + u.perfil().inicio();
    }

    @GetMapping("/login")
    public String pagina() {
        return "login";
    }

    @PostMapping("/login")
    public String entrar(@RequestParam Usuario.Perfil perfil, @RequestParam String usuario, @RequestParam String senha,
                         HttpSession sessao, RedirectAttributes ra) {
        Usuario u = autenticar(perfil, usuario.trim(), senha.trim());
        if (u == null) {
            ra.addFlashAttribute("erro", "Usuário ou senha incorretos.");
            ra.addFlashAttribute("perfilEscolhido", perfil);
            return "redirect:/login";
        }
        sessao.setAttribute(Usuario.CHAVE_NA_SESSAO, u);
        return "redirect:" + perfil.inicio();
    }

    @PostMapping("/sair")
    public String sair(HttpSession sessao) {
        sessao.invalidate();
        return "redirect:/login";
    }

    private Usuario autenticar(Usuario.Perfil perfil, String usuario, String senha) {
        String cpf = senha.replaceAll("\\D", "");   // aceita o CPF com ou sem pontos e traço
        switch (perfil) {
            case ESTUDANTE -> {
                Estudante e = estudantes.consultarEstudante(usuario);
                if (e != null && confere(e.getPessoa(), cpf)) {
                    return new Usuario(perfil, e.getMatricula(), nomeCompleto(e.getPessoa()), e.getPessoa().getCpf(), e.getPessoa().getEmail());
                }
            }
            case ATENDENTE -> {
                Atendente a = atendentes.consultarAtendente(usuario);
                if (a != null && confere(a.getPessoa(), cpf)) {
                    return new Usuario(perfil, a.getMatriculaFuncional(), nomeCompleto(a.getPessoa()), a.getPessoa().getCpf(), a.getPessoa().getEmail());
                }
            }
            case ADMINISTRADOR -> {
                if (usuarioAdmin.equals(usuario) && senhaAdmin.equals(senha)) {
                    return new Usuario(perfil, usuario, "Administrador", null, null);
                }
            }
        }
        return null;
    }

    private static boolean confere(Pessoa p, String cpf) {
        return p != null && !cpf.isEmpty() && p.getCpf().equals(cpf);
    }

    private static String nomeCompleto(Pessoa p) {
        return p.getNome() + " " + p.getSobrenome();
    }
}
