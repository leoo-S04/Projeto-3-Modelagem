package br.pucpr.ib2b.biblioteca;

import java.util.List;

/**
 * Papel GoF: Leaf (Composite, Req. 10). Representa um exemplar pelo id do Livro — referencia, não substitui:
 * id, estado e empréstimo continuam no Livro e na Movimentacao.
 */
public class ItemDoAcervo extends ComponenteDoAcervo {

    private final int idLivro;

    public ItemDoAcervo(int idLivro) { this.idLivro = idLivro; }

    @Override public int contarLivros() { return 1; }
    @Override public List<Integer> listarIds() { return List.of(idLivro); }
    @Override public String getNome() { return "Livro " + idLivro; }

    public int getIdLivro() { return idLivro; }
}
