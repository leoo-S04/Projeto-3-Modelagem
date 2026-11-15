package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.EstudanteDTO;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.ComprovanteDevolucao;
import br.pucpr.ib2b.biblioteca.ComprovanteEmprestimo;
import br.pucpr.ib2b.biblioteca.Situacao;
import br.pucpr.ib2b.biblioteca.SituacaoLivroDTO;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Balcão do atendente (casos de uso: emprestar livro reservado, registrar devolução, emitir boleto de multa) e o
 * cuidado com os livros devolvidos (inspeção e reparo). Só chama tratadores.
 */
@Controller
@RequestMapping("/atendente")
public class AtendenteController extends ControladorBase {

    private final TratadorDeAcervo acervo;
    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeDevolucao devolucoes;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeMulta multas;

    public AtendenteController(TratadorDeAcervo acervo, TratadorDeEmprestimo emprestimos, TratadorDeDevolucao devolucoes,
                               TratadorDeEstudante estudantes, TratadorDeMulta multas) {
        this.acervo = acervo;
        this.emprestimos = emprestimos;
        this.devolucoes = devolucoes;
        this.estudantes = estudantes;
        this.multas = multas;
    }

    @GetMapping
    public String pagina(Model model) {
        List<SituacaoLivroDTO> livros = acervo.listarLivros();
        model.addAttribute("reservados", com(livros, Set.of(Situacao.RESERVADO)));
        model.addAttribute("emprestados", com(livros, Set.of(Situacao.EMPRESTADO, Situacao.RENOVADO)));
        model.addAttribute("emReparo", com(livros, Set.of(Situacao.REPARANDO)));
        model.addAttribute("livres", com(livros, Set.of(Situacao.LIVRE)));
        model.addAttribute("inspecoes", devolucoes.inspecoesPendentes());
        model.addAttribute("multas", multas.relatorioDeCobrancas());
        model.addAttribute("nomes", estudantes.listarEstudantes().stream()
                .collect(Collectors.toMap(EstudanteDTO::matricula, EstudanteDTO::nome, (a, b) -> a)));
        model.addAttribute("nomesPorCpf", estudantes.listarEstudantes().stream()
                .collect(Collectors.toMap(EstudanteDTO::cpf, EstudanteDTO::nome, (a, b) -> a)));
        return "atendente";
    }

    @PostMapping("/emprestar")
    public String emprestar(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam int idLivro,
                            @RequestParam String idEstudante, RedirectAttributes ra) {
        return executar(ra, "/atendente", () -> {
            ComprovanteEmprestimo c = emprestimos.emprestarLivro(idLivro, idEstudante, eu.identificador());
            return "Empréstimo registrado: \"" + c.titulo() + "\" com " + c.matriculaEstudante()
                   + ". Devolver até " + dataBr(c.vencimento()) + ".";
        });
    }

    @PostMapping("/devolver")
    public String devolver(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam int idLivro,
                           @RequestParam String idEstudante, RedirectAttributes ra) {
        return executar(ra, "/atendente", () -> {
            ComprovanteDevolucao d = devolucoes.registrarDevolucao(idLivro, idEstudante, eu.identificador());
            if (d == null) return erro("Não há empréstimo deste livro para " + idEstudante + ".");
            if (d.diasAtraso() == 0) return "Devolução registrada no prazo. O livro aguarda inspeção.";
            BoletoDeMulta b = devolucoes.emitirBoletoDeMulta(idLivro, idEstudante);   // caso de uso "emitir boleto"
            return "Devolução registrada com " + d.diasAtraso() + " dia(s) de atraso. "
                   + (b == null ? "Sem multa pela política vigente." : "Boleto nº " + b.getIdBoleto() + " de R$ "
                      + b.getValorTotal() + " emitido no banco " + b.getBanco() + ".")
                   + " O livro aguarda inspeção.";
        });
    }

    @PostMapping("/inspecionar")
    public String inspecionar(@RequestParam int idLivro, @RequestParam boolean emBomEstado, RedirectAttributes ra) {
        return executar(ra, "/atendente", () -> devolucoes.inspecionar(idLivro, emBomEstado)
                ? (emBomEstado ? "Livro inspecionado: disponível de novo." : "Livro inspecionado: enviado para reparo.")
                : erro("Livro não encontrado."));
    }

    @PostMapping("/reparo")
    public String reparo(@RequestParam int idLivro, @RequestParam String acao, RedirectAttributes ra) {
        Map<String, String> textos = Map.of("enviar", "enviado para reparo", "retornar", "voltou do reparo e está disponível",
                "incinerar", "descartado (laudo irrecuperável)");
        return executar(ra, "/atendente", () -> {
            boolean feito = switch (acao) {
                case "enviar" -> devolucoes.enviarParaReparo(idLivro);
                case "retornar" -> devolucoes.retornarDoReparo(idLivro);
                case "incinerar" -> devolucoes.incinerar(idLivro);
                default -> throw new IllegalArgumentException("ação desconhecida: " + acao);
            };
            return feito ? "Livro " + idLivro + " " + textos.get(acao) + "." : erro("Livro não encontrado.");
        });
    }

    private static List<SituacaoLivroDTO> com(List<SituacaoLivroDTO> livros, Set<Situacao> situacoes) {
        return livros.stream().filter(l -> situacoes.contains(l.situacao())).toList();
    }
}
