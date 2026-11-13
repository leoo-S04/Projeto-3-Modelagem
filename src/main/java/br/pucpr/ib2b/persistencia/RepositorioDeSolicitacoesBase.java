package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.ResultadoSolicitacao;

import java.util.UUID;

/** Implementação comum: resultados de solicitações na cópia de trabalho (seção "solicitacoes"). */
abstract class RepositorioDeSolicitacoesBase implements RepositorioDeSolicitacoes {

    private final UnidadeDeTrabalhoBase uow;

    protected RepositorioDeSolicitacoesBase(UnidadeDeTrabalhoBase uow) { this.uow = uow; }

    @Override public ResultadoSolicitacao buscar(UUID id) { return uow.dados().solicitacoes.get(id); }
    @Override public void salvar(ResultadoSolicitacao r) { uow.dados().solicitacoes.put(r.idSolicitacao(), r); }
    @Override public UnidadeDeTrabalho unidade() { return uow; }
}
