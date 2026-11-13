package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.ResultadoSolicitacao;

import java.util.UUID;

/** Papel GoF: AbstractProduct — resultados das solicitações de circulação (Command, Req. 6). */
public interface RepositorioDeSolicitacoes {
    ResultadoSolicitacao buscar(UUID idSolicitacao);
    void salvar(ResultadoSolicitacao resultado);
    UnidadeDeTrabalho unidade();
}
