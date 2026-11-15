package br.pucpr.ib2b.web;

import br.pucpr.ib2b.biblioteca.EmprestimoDTO;
import br.pucpr.ib2b.biblioteca.HistoricoDeEmprestimos;
import br.pucpr.ib2b.biblioteca.MetadadosLivro;
import br.pucpr.ib2b.biblioteca.Situacao;
import br.pucpr.ib2b.biblioteca.SituacaoLivroDTO;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.financeiro.CobrancaDTO;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Área do estudante (casos de uso: reservar livro, consultar reserva, renovar empréstimo): acompanhamento dos
 * empréstimos com o prazo, histórico, multas e catálogo. Só chama tratadores; a regra (pode reservar? pode renovar?)
 * é decidida no domínio e a recusa vira mensagem.
 */
@Controller
@RequestMapping("/estudante")
public class EstudanteController extends ControladorBase {

    private final TratadorDeAcervo acervo;
    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeMulta multas;
    private final RelogioAjustavel relogio;

    public EstudanteController(TratadorDeAcervo acervo, TratadorDeEmprestimo emprestimos, TratadorDeMulta multas,
                               RelogioAjustavel relogio) {
        this.acervo = acervo;
        this.emprestimos = emprestimos;
        this.multas = multas;
        this.relogio = relogio;
    }

    @GetMapping
    public String pagina(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam(required = false) String busca,
                         Model model) {
        List<SituacaoLivroDTO> todos = acervo.listarLivros();
        List<SituacaoLivroDTO> catalogo = todos;
        if (busca != null && !busca.isBlank()) {
            // consulta de metadados (com cache e medição — Decorator); a situação vem sempre do livro atual
            Set<Integer> encontrados = acervo.buscarMetadados(busca).stream()
                    .map(MetadadosLivro::idLivro).collect(Collectors.toSet());
            catalogo = todos.stream().filter(l -> encontrados.contains(l.idLivro())).toList();
            model.addAttribute("busca", busca);
        }
        HistoricoDeEmprestimos historico = emprestimos.consultarEmprestimos(eu.identificador());
        List<CobrancaDTO> minhasMultas = multas.relatorioDeCobrancas().stream().filter(c -> c.cpf().equals(eu.cpf())).toList();
        List<CobrancaDTO> emAberto = minhasMultas.stream().filter(c -> !"QUITADO".equals(c.situacao())).toList();

        model.addAttribute("catalogo", catalogo);
        model.addAttribute("comigo", historico.emAndamento());
        model.addAttribute("historico", historico.devolvidos());
        model.addAttribute("prazos", prazos(historico.emAndamento()));
        model.addAttribute("reservas", todos.stream()
                .filter(l -> l.situacao() == Situacao.RESERVADO && eu.identificador().equals(l.matriculaReservante())).toList());
        model.addAttribute("multas", minhasMultas);
        model.addAttribute("multasEmAberto", emAberto);
        model.addAttribute("totalEmAberto", emAberto.stream().map(CobrancaDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add));
        // cada botão leva um id de solicitação: um duplo clique reenvia o mesmo id e não executa duas vezes (Command)
        model.addAttribute("idReserva", todos.stream().collect(Collectors.toMap(SituacaoLivroDTO::idLivro, l -> UUID.randomUUID())));
        model.addAttribute("idRenovacao", todos.stream().collect(Collectors.toMap(SituacaoLivroDTO::idLivro, l -> UUID.randomUUID())));
        return "estudante";
    }

    @PostMapping("/reservar")
    public String reservar(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam UUID idSolicitacao,
                           @RequestParam int idLivro, RedirectAttributes ra) {
        return executar(ra, "/estudante", () -> mensagem(emprestimos.reservarLivro(idSolicitacao, idLivro, eu.identificador(), null),
                "Livro reservado. Retire no balcão com o atendente."));
    }

    @PostMapping("/renovar")
    public String renovar(@SessionAttribute(Usuario.CHAVE_NA_SESSAO) Usuario eu, @RequestParam UUID idSolicitacao,
                          @RequestParam int idLivro, RedirectAttributes ra) {
        return executar(ra, "/estudante", () -> {
            String r = mensagem(emprestimos.renovarEmprestimo(idSolicitacao, idLivro, eu.identificador()), null);
            if (r != null) return r;
            return "Empréstimo renovado. Novo vencimento: " + dataBr(emprestimos.consultarSituacao(idLivro).vencimento()) + ".";
        });
    }

    private Map<Integer, Prazo> prazos(List<EmprestimoDTO> lista) {
        return lista.stream().collect(Collectors.toMap(EmprestimoDTO::idEmprestimo,
                e -> Prazo.de(e.dataEmprestimo(), e.vencimento(), relogio.hoje()), (a, b) -> a));
    }
}
