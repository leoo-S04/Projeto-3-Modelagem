package br.pucpr.ib2b.web;

import br.pucpr.ib2b.biblioteca.ResultadoSolicitacao;
import br.pucpr.ib2b.comum.ExcecaoDeDominio;
import br.pucpr.ib2b.comum.ResultadoCadastro;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * Apoio das telas: executa a chamada ao tratador e transforma o resultado (ou a recusa de regra) em mensagem.
 * Não há regra de negócio aqui — a mensagem de recusa vem pronta do domínio.
 */
abstract class ControladorBase {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Executa a ação; sucesso → mensagem verde; recusa de regra → mensagem vermelha. Volta para "destino". */
    protected String executar(RedirectAttributes ra, String destino, Supplier<String> acao) {
        try {
            String mensagem = acao.get();
            if (mensagem != null && mensagem.startsWith("ERRO:")) ra.addFlashAttribute("erro", mensagem.substring(5).trim());
            else ra.addFlashAttribute("sucesso", mensagem);
        } catch (ExcecaoDeDominio recusa) {
            ra.addFlashAttribute("erro", recusa.getMessage());
        } catch (IllegalArgumentException | NullPointerException dadosInvalidos) {
            ra.addFlashAttribute("erro", "Dados inválidos: " + dadosInvalidos.getMessage());
        }
        return "redirect:" + destino;
    }

    /** Texto para o resultado de um cadastro (NOVO / EXISTENTE / RECUSADO). */
    protected static String descrever(ResultadoCadastro<?> r, String oQue) {
        return switch (r.getStatus()) {
            case NOVO -> oQue + " cadastrado(a).";
            case EXISTENTE -> oQue + " já estava cadastrado(a): mantido o cadastro existente.";
            case RECUSADO -> erro("Cadastro recusado: " + r.getMotivo());
        };
    }

    /** Texto para o resultado de uma solicitação (reserva ou renovação). Executada → "sucesso" (pode ser nulo). */
    protected static String mensagem(ResultadoSolicitacao r, String sucesso) {
        return switch (r.status()) {
            case EXECUTADA -> sucesso;
            case RECUSADA -> erro(r.mensagem());
            case EM_PROCESSAMENTO -> erro("Pedido ainda em processamento. Atualize a página em instantes.");
        };
    }

    protected static String dataBr(LocalDate d) { return d == null ? "—" : d.format(DATA); }

    protected static String erro(String mensagem) { return "ERRO: " + mensagem; }
}
