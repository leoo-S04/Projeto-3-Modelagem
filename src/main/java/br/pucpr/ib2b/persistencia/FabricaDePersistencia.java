package br.pucpr.ib2b.persistencia;

/**
 * Papel GoF: AbstractFactory (Req. 8). Cria a unidade de trabalho e os repositórios de UMA família.
 * Os repositórios recebem a unidade de trabalho na criação, por isso escrevem na mesma transação.
 */
public interface FabricaDePersistencia {
    UnidadeDeTrabalho criarUnidadeDeTrabalho();
    RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho uow);
    RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho uow);
    RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho uow);
    RepositorioDeSolicitacoes criarRepositorioDeSolicitacoes(UnidadeDeTrabalho uow);
    RepositorioFinanceiro criarRepositorioFinanceiro(UnidadeDeTrabalho uow);
    RepositorioDeOrganizacao criarRepositorioDeOrganizacao(UnidadeDeTrabalho uow);
}
