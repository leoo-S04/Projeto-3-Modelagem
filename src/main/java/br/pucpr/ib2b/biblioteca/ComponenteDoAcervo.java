package br.pucpr.ib2b.biblioteca;

import java.util.List;

/**
 * Papel GoF: Component (Composite, Req. 10). Grupos (unidade, seção, subseção) e itens (exemplares)
 * respondem às MESMAS operações de contagem e listagem; quem usa não precisa saber se é grupo ou item.
 */
public abstract class ComponenteDoAcervo {

    private GrupoDoAcervo pai;   // 0..1

    /** Quantos livros existem neste componente (item = 1; grupo = soma dos filhos). */
    public abstract int contarLivros();

    /** Ids dos livros deste componente (item = o seu; grupo = os de todos os filhos). */
    public abstract List<Integer> listarIds();

    public abstract String getNome();

    public GrupoDoAcervo getPai() { return pai; }

    /** Só o grupo define o pai, ao adicionar/remover (mantém as invariantes da árvore). */
    void definirPai(GrupoDoAcervo novoPai) { this.pai = novoPai; }

    /** Sobe até a raiz da árvore (a unidade). */
    ComponenteDoAcervo raiz() {
        ComponenteDoAcervo c = this;
        while (c.getPai() != null) c = c.getPai();
        return c;
    }
}
