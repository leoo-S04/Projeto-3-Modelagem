package br.pucpr.ib2b.biblioteca;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado da execução de uma solicitação (Command). É gravado no registro e devolvido de novo, sem reexecutar,
 * quando a mesma solicitação (mesmo id) é submetida outra vez.
 */
public record ResultadoSolicitacao(UUID idSolicitacao, String tipo, String autor, Instant criadaEm,
                                   StatusSolicitacao status, String mensagem) {

    static ResultadoSolicitacao executada(SolicitacaoDeCirculacao s, String mensagem) {
        return new ResultadoSolicitacao(s.getId(), s.tipo(), s.getAutor(), s.getCriadaEm(), StatusSolicitacao.EXECUTADA, mensagem);
    }

    static ResultadoSolicitacao recusada(SolicitacaoDeCirculacao s, String motivo) {
        return new ResultadoSolicitacao(s.getId(), s.tipo(), s.getAutor(), s.getCriadaEm(), StatusSolicitacao.RECUSADA, motivo);
    }

    static ResultadoSolicitacao emProcessamento(SolicitacaoDeCirculacao s) {
        return new ResultadoSolicitacao(s.getId(), s.tipo(), s.getAutor(), s.getCriadaEm(),
                StatusSolicitacao.EM_PROCESSAMENTO, "Solicitação já está em processamento");
    }

    public boolean foiExecutada() { return status == StatusSolicitacao.EXECUTADA; }
}
