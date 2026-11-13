package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import br.pucpr.ib2b.financeiro.iMulta;

import java.time.Clock;
import java.time.Instant;

/**
 * Serviços do Atendente (Astah: TratadorDeDevolucao): f5, f6, inspeção e reparo.
 * Delega à fachada iCirculacao (Req. 4). Na f5, só PUBLICA o evento depois que a devolução foi confirmada
 * (Observer, Req. 5): o tratador não conhece os ouvintes — novas reações são registradas na montagem.
 */
public class TratadorDeDevolucao implements iDevolucao {

    private final iCirculacao circulacao;
    private final PublicadorDeDevolucao publicador;
    private final iMulta financeiro;
    private final AgendaDeInspecao agenda;
    private final PainelDeIndicadores painel;
    private final Clock relogio;

    public TratadorDeDevolucao(iCirculacao circulacao, PublicadorDeDevolucao publicador, iMulta financeiro,
                               AgendaDeInspecao agenda, PainelDeIndicadores painel, Clock relogio) {
        this.circulacao = circulacao;
        this.publicador = publicador;
        this.financeiro = financeiro;
        this.agenda = agenda;
        this.painel = painel;
        this.relogio = relogio;
    }

    // ---- Consultas para as telas (alimentadas pelos ouvintes da devolução) ----
    public java.util.List<AgendaDeInspecao.ItemDeInspecao> inspecoesPendentes() { return agenda.pendentes(); }

    public IndicadoresDTO indicadores() {
        return new IndicadoresDTO(painel.totalDeDevolucoes(), painel.devolucoesAtrasadas(), painel.totalDeDiasDeAtraso());
    }

    public int falhasDeNotificacaoPendentes() { return publicador.falhasPendentes().size(); }

    public void reenviarNotificacoesComFalha() { publicador.reenviarFalhas(); }

    /** f5 — regra 9: devolve (transação confirmada) e SÓ ENTÃO publica o evento. O livro fica DEVOLVIDO. */
    @Override
    public ComprovanteDevolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente) {
        ComprovanteDevolucao comprovante = circulacao.devolver(idLivro, idEstudante, idAtendente);   // commit
        if (comprovante != null) {
            publicador.publicar(EventoDevolucao.de(comprovante, Instant.now(relogio)));             // após o commit
        }
        return comprovante;
    }

    /**
     * f6 — regra 10: se houve devolução com atraso, devolve o boleto da multa (segunda via, se já emitido pelo
     * ouvinte de multa). Sem devolução ou sem atraso → {@code null}.
     */
    @Override
    public BoletoDeMulta emitirBoletoDeMulta(int idLivro, String idEstudante) {
        ComprovanteDevolucao d = circulacao.consultarDevolucao(idLivro, idEstudante);
        if (d == null || d.diasAtraso() == 0) return null;
        return financeiro.gerarMulta(d.cpfEstudante(), d.idDevolucao(), d.diasAtraso());   // idempotente
    }

    /** DEVOLVIDO → LIVRE (bom estado) ou REPARANDO (danificado); a inspeção agendada é concluída. */
    @Override
    public boolean inspecionar(int idLivro, boolean emBomEstado) {
        boolean feito = circulacao.registrarInspecao(idLivro, emBomEstado) != null;
        if (feito) agenda.concluir(idLivro);
        return feito;
    }

    /** LIVRE → REPARANDO (dano identificado no acervo). */
    @Override
    public boolean enviarParaReparo(int idLivro) { return circulacao.enviarParaReparo(idLivro) != null; }

    /** REPARANDO → LIVRE. */
    @Override
    public boolean retornarDoReparo(int idLivro) { return circulacao.retornarDoReparo(idLivro) != null; }

    /** REPARANDO → INCINERADO (laudo irrecuperável). */
    @Override
    public boolean incinerar(int idLivro) { return circulacao.incinerar(idLivro) != null; }
}
