package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.GrupoDoAcervo;

import java.util.List;

/** Papel GoF: AbstractProduct — árvore de organização do acervo (Composite), seção "acervo" do JSON. */
public interface RepositorioDeOrganizacao {
    /** As unidades (raízes); os grupos e itens são alterados no próprio objeto (cópia de trabalho). */
    List<GrupoDoAcervo> unidades();
    void adicionarUnidade(GrupoDoAcervo unidade);
    UnidadeDeTrabalho unidade();
}
