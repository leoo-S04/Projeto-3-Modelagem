package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.Livro;

import java.util.ArrayList;
import java.util.List;

/** Implementação comum dos repositórios de livro: lê e altera a cópia de trabalho da SUA unidade de trabalho. */
abstract class RepositorioLivroBase implements RepositorioLivro {

    private final UnidadeDeTrabalhoBase uow;

    protected RepositorioLivroBase(UnidadeDeTrabalhoBase uow) { this.uow = uow; }

    @Override public Livro buscar(int idLivro) { return uow.dados().livros.get(idLivro); }
    @Override public void salvar(Livro livro) { uow.dados().livros.put(livro.getIdLivro(), livro); }
    @Override public boolean remover(int idLivro) { return uow.dados().livros.remove(idLivro) != null; }
    @Override public List<Livro> listar() { return new ArrayList<>(uow.dados().livros.values()); }
    @Override public UnidadeDeTrabalho unidade() { return uow; }
}
