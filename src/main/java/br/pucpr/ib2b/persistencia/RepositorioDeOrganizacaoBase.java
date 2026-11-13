package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.GrupoDoAcervo;

import java.util.List;

/** Implementação comum: unidades (raízes da árvore do Composite) na cópia de trabalho. */
abstract class RepositorioDeOrganizacaoBase implements RepositorioDeOrganizacao {

    private final UnidadeDeTrabalhoBase uow;

    protected RepositorioDeOrganizacaoBase(UnidadeDeTrabalhoBase uow) { this.uow = uow; }

    @Override public List<GrupoDoAcervo> unidades() { return List.copyOf(uow.dados().organizacao); }
    @Override public void adicionarUnidade(GrupoDoAcervo unidade) { uow.dados().organizacao.add(unidade); }
    @Override public UnidadeDeTrabalho unidade() { return uow; }
}
