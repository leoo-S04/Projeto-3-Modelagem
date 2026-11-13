package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.Livro;

import java.util.List;

/** Papel GoF: AbstractProduct — acesso aos livros (usado pelo Acervo). */
public interface RepositorioLivro {
    Livro buscar(int idLivro);
    void salvar(Livro livro);
    boolean remover(int idLivro);
    List<Livro> listar();
    /** A unidade de trabalho da família que criou este repositório. */
    UnidadeDeTrabalho unidade();
}
