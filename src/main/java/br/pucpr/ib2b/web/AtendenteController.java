package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.EstudanteDTO;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.ComprovanteDevolucao;
import br.pucpr.ib2b.biblioteca.ComprovanteEmprestimo;
import br.pucpr.ib2b.biblioteca.ComprovanteReserva;
import br.pucpr.ib2b.biblioteca.EmprestimoDTO;
import br.pucpr.ib2b.biblioteca.HistoricoDeEmprestimos;
import br.pucpr.ib2b.biblioteca.Situacao;
import br.pucpr.ib2b.biblioteca.SituacaoLivroDTO;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Balcão do atendente (casos de uso: registrar devolução, emitir boleto de multa; e, no balcão, reservar para o
 * estudante e efetivar o empréstimo reservado), o monitoramento dos empréstimos e a consulta de um empréstimo.
 * Só chama tratadores.
 */
@Controller
@RequestMapping("/atendente")
public class AtendenteController extends ControladorBase {

    private final TratadorDeAcervo acervo;
    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeDevolucao devolucoes;
    private final TratadorDeEstudante estudantes;
    private final RelogioAjustavel relogio;

    public AtendenteController(TratadorDeAcervo acervo, TratadorDeEmprestimo emprestimos, TratadorDeDevolucao devolucoes,
                               TratadorDeEstudante estudantes, RelogioAjustavel relogio) {
        this.acervo = acervo;
        this.emprestimos = emprestimos;
        this.devolucoes = devolucoes;
        this.estudantes = estudantes;
        this.relogio = relogio;
    }

    @GetMapping
    public String pagina(Model model) {
        List<SituacaoLivroDTO> livros = acervo.listarLivros();
        List<ComprovanteReserva> reservas = livros.stream()
                .filter(l -> l.situacao() == Situacao.RESERVADO)
                .map(l -> emprestimos.consultarReserva(l.idLivro(), l.matriculaReservante()))
                .filter(Objects::nonNull).toList();
        List<EmprestimoDTO> emAndamento = emprestimos.consultarEmprestimos(null).emAndamento();
        Map<Integer, Prazo> prazos = prazos(emAndamento);

        model.addAttribute("reservas", reservas);
        model.addAttribute("emAndamento", emAndamento);
        model.addAttribute("prazos", prazos);
        model.addAttribute("atrasados", prazos.values().stream().filter(p -> p.cor().equals("vermelho")).count());
        model.addAttribute("vencendo", prazos.values().stream().filter(p -> p.cor().equals("laranja")).count());
        model.addAttribute("livres", livros.stream().filter(l -> l.situacao() == Situacao.LIVRE).toList());
        model.addAttribute("estudantes", estudantes.listarEstudantes());
        model.addAttribute("nomes", nomes());
        model.addAttribute("inspecoes", devolucoes.inspecoesPendentes());
        model.addAttribute("emReparo", livros.stream().filter(l -> l.situacao() == Situacao.REPARANDO).toList());
        model.addAttribute("idSolicitacao", UUID.randomUUID());   // reenviar o formulário não reserva duas vezes (Command)
        return "atendente";
    }

    /** Consulta de um empréstimo (aberta ao clicar na linha do monitoramento). */
    @GetMapping("/emprestimo/{id}")
    public String consulta(@PathVariable int id, Model model, RedirectAttributes ra) {
        HistoricoDeEmprestimos todos = emprestimos.consultarEmprestimos(null);
        EmprestimoDTO e = todos.buscar(id);
        if (e == null) {
            ra.addFlashAttribute("erro", "Empréstimo nº " + id + " não encontrado.");
            return "redirect:/atendente";
        }
        model.addAttribute("e", e);
        model.addAttribute("prazo", Prazo.de(e.dataEmprestimo(), e.vencimento(), relogio.hoje()));
        model.addAttribute("doEstudante", emprestimos.consultarEmprestimos(e.matriculaEstudante()).todos());
        return "consulta-emprestimo";
    }

    @PostMapping("/reservar")
    public String reservar(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam UUID idSolicitacao,
                           @RequestParam int idLivro, @RequestParam String idEstudante, RedirectAttributes ra) {
        return executar(ra, "/atendente", () -> mensagem(
                emprestimos.reservarLivro(idSolicitacao, idLivro, idEstudante, eu.identificador()),
                "Reserva registrada para " + idEstudante + ". Agora é só aprovar o empréstimo."));
    }

    @PostMapping("/emprestar")
    public String emprestar(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam int idLivro,
                            @RequestParam String idEstudante, RedirectAttributes ra) {
        return executar(ra, "/atendente", () -> {
            ComprovanteEmprestimo c = emprestimos.emprestarLivro(idLivro, idEstudante, eu.identificador());
            return "Empréstimo nº " + c.idEmprestimo() + " aprovado: \"" + c.titulo() + "\" com " + c.matriculaEstudante()
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
            return "Devolução registrada com " + d.diasAtraso() + " dia(s) de atraso. " + descrever(b)
                   + " O livro aguarda inspeção.";
        });
    }

    /** Emitir boleto de multa (segunda via) de uma devolução atrasada. */
    @PostMapping("/boleto")
    public String boleto(@RequestParam int idEmprestimo, @RequestParam int idLivro, @RequestParam String idEstudante,
                         RedirectAttributes ra) {
        return executar(ra, "/atendente/emprestimo/" + idEmprestimo, () -> {
            BoletoDeMulta b = devolucoes.emitirBoletoDeMulta(idLivro, idEstudante);
            return b == null ? erro("Esta devolução não tem multa.")
                    : "Boleto nº " + b.getIdBoleto() + ": " + reais(b.getValorTotal()) + " (" + b.getDiasAtraso()
                      + " dia(s) de atraso), banco " + b.getBanco() + ", código " + b.getIdExterno() + ", "
                      + (BoletoDeMulta.QUITADO.equals(b.getSituacao()) ? "já pago." : "em aberto.");
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
        return executar(ra, "/atendente", () -> {
            boolean feito = switch (acao) {
                case "retornar" -> devolucoes.retornarDoReparo(idLivro);
                case "incinerar" -> devolucoes.incinerar(idLivro);
                default -> throw new IllegalArgumentException("ação desconhecida: " + acao);
            };
            return feito ? (acao.equals("retornar") ? "Livro reparado: disponível de novo." : "Livro descartado (laudo irrecuperável).")
                         : erro("Livro não encontrado.");
        });
    }

    private Map<Integer, Prazo> prazos(List<EmprestimoDTO> lista) {
        return lista.stream().collect(Collectors.toMap(EmprestimoDTO::idEmprestimo,
                e -> Prazo.de(e.dataEmprestimo(), e.vencimento(), relogio.hoje()), (a, b) -> a));
    }

    private Map<String, String> nomes() {
        return estudantes.listarEstudantes().stream()
                .collect(Collectors.toMap(EstudanteDTO::matricula, EstudanteDTO::nome, (a, b) -> a));
    }

    private static String descrever(BoletoDeMulta b) {
        return b == null ? "Sem multa pela política vigente."
                : "Multa de " + reais(b.getValorTotal()) + ": boleto nº " + b.getIdBoleto() + " emitido no banco " + b.getBanco() + ".";
    }
}
