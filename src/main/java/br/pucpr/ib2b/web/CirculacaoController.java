package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.ComprovanteDevolucao;
import br.pucpr.ib2b.biblioteca.ComprovanteEmprestimo;
import br.pucpr.ib2b.biblioteca.ComprovanteReserva;
import br.pucpr.ib2b.biblioteca.ResultadoSolicitacao;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Telas de circulação (Estudante e Atendente): f1 a f6, inspeção e reparo. Só chama os tratadores.
 * Reserva e renovação levam um id de solicitação gerado ao abrir a página: um duplo clique ou um reenvio do
 * mesmo formulário não executa duas vezes (Command, Req. 6).
 */
@Controller
@RequestMapping("/circulacao")
public class CirculacaoController extends ControladorBase {

    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeDevolucao devolucoes;
    private final TratadorDeAcervo acervo;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeAtendente atendentes;

    public CirculacaoController(TratadorDeEmprestimo emprestimos, TratadorDeDevolucao devolucoes,
                                TratadorDeAcervo acervo, TratadorDeEstudante estudantes, TratadorDeAtendente atendentes) {
        this.emprestimos = emprestimos;
        this.devolucoes = devolucoes;
        this.acervo = acervo;
        this.estudantes = estudantes;
        this.atendentes = atendentes;
    }

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("livros", acervo.listarLivros());
        model.addAttribute("estudantes", estudantes.listarEstudantes());
        model.addAttribute("atendentes", atendentes.listarAtendentes());
        model.addAttribute("idReserva", UUID.randomUUID());
        model.addAttribute("idRenovacao", UUID.randomUUID());
        model.addAttribute("inspecoes", devolucoes.inspecoesPendentes());
        model.addAttribute("indicadores", devolucoes.indicadores());
        return "circulacao";
    }

    /** f1 — Command. */
    @PostMapping("/reservar")
    public String reservar(@RequestParam UUID idSolicitacao, @RequestParam int idLivro, @RequestParam String idEstudante,
                           @RequestParam(required = false) String idAtendente, RedirectAttributes ra) {
        return executar(ra, "/circulacao",
                () -> texto(emprestimos.reservarLivro(idSolicitacao, idLivro, idEstudante, idAtendente)));
    }

    /** f2. */
    @PostMapping("/consultar-reserva")
    public String consultarReserva(@RequestParam int idLivro, @RequestParam String idEstudante, RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> {
            ComprovanteReserva r = emprestimos.consultarReserva(idLivro, idEstudante);
            return r == null ? erro("Não há reserva do livro " + idLivro + " para " + idEstudante + ".")
                    : "Reserva nº " + r.idReserva() + ": livro " + r.idLivro() + " (" + r.titulo() + ") para "
                      + r.matriculaEstudante() + " em " + r.data() + ".";
        });
    }

    /** f3 — Facade (efetivação atômica). */
    @PostMapping("/emprestar")
    public String emprestar(@RequestParam int idLivro, @RequestParam String idEstudante,
                            @RequestParam(required = false) String idAtendente, RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> {
            ComprovanteEmprestimo c = emprestimos.emprestarLivro(idLivro, idEstudante, idAtendente);
            return "Empréstimo nº " + c.idEmprestimo() + ": livro " + c.idLivro() + " (" + c.titulo()
                   + ") com " + c.matriculaEstudante() + ", vencimento " + c.vencimento() + ".";
        });
    }

    /** f4 — Command (mesmo id não renova duas vezes). */
    @PostMapping("/renovar")
    public String renovar(@RequestParam UUID idSolicitacao, @RequestParam int idLivro, @RequestParam String idEstudante,
                          RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> texto(emprestimos.renovarEmprestimo(idSolicitacao, idLivro, idEstudante)));
    }

    /** f5 — Facade + Observer (reações publicadas após a confirmação). */
    @PostMapping("/devolver")
    public String devolver(@RequestParam int idLivro, @RequestParam String idEstudante,
                           @RequestParam(required = false) String idAtendente, RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> {
            ComprovanteDevolucao d = devolucoes.registrarDevolucao(idLivro, idEstudante, idAtendente);
            if (d == null) return erro("Não há empréstimo do livro " + idLivro + " para " + idEstudante + ".");
            return "Devolução nº " + d.idDevolucao() + " registrada em " + d.devolvidoEm() + " (vencimento "
                   + d.vencimento() + "): " + (d.diasAtraso() > 0 ? d.diasAtraso() + " dia(s) de atraso — multa gerada."
                   : "no prazo.") + " O livro aguarda inspeção.";
        });
    }

    /** f6 — boleto da multa (segunda via). */
    @PostMapping("/boleto")
    public String boleto(@RequestParam int idLivro, @RequestParam String idEstudante, RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> {
            BoletoDeMulta b = devolucoes.emitirBoletoDeMulta(idLivro, idEstudante);
            return b == null ? erro("Sem multa: não há devolução atrasada do livro " + idLivro + " para " + idEstudante + ".")
                    : "Boleto nº " + b.getIdBoleto() + ": R$ " + b.getValorTotal() + " (" + b.getDiasAtraso()
                      + " dia(s)), banco " + b.getBanco() + ", id " + b.getIdExterno() + ", situação " + b.getSituacao() + ".";
        });
    }

    @PostMapping("/inspecionar")
    public String inspecionar(@RequestParam int idLivro, @RequestParam boolean emBomEstado, RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> devolucoes.inspecionar(idLivro, emBomEstado)
                ? "Livro " + idLivro + " inspecionado: " + (emBomEstado ? "volta a ficar LIVRE." : "enviado para REPARO.")
                : erro("Livro " + idLivro + " não cadastrado."));
    }

    @PostMapping("/reparo")
    public String reparo(@RequestParam int idLivro, @RequestParam String acao, RedirectAttributes ra) {
        return executar(ra, "/circulacao", () -> {
            boolean feito = switch (acao) {
                case "enviar" -> devolucoes.enviarParaReparo(idLivro);
                case "retornar" -> devolucoes.retornarDoReparo(idLivro);
                case "incinerar" -> devolucoes.incinerar(idLivro);
                default -> throw new IllegalArgumentException("ação desconhecida: " + acao);
            };
            return feito ? "Livro " + idLivro + ": " + acao + " registrado." : erro("Livro " + idLivro + " não cadastrado.");
        });
    }

    private static String texto(ResultadoSolicitacao r) {
        return switch (r.status()) {
            case EXECUTADA -> r.mensagem() + " (solicitação " + r.idSolicitacao().toString().substring(0, 8) + ")";
            case RECUSADA -> erro(r.mensagem());
            case EM_PROCESSAMENTO -> erro("Solicitação em processamento; tente consultar em instantes.");
        };
    }
}
