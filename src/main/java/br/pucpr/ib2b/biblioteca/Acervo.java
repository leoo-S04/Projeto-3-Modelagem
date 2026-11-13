package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.persistencia.RepositorioLivro;

import java.util.Collections;
import java.util.List;

/**
 * Ponto interno de acesso aos livros (Astah: AcervoDeLivros, renomeado para Acervo).
 * Os tratadores acessam os livros por delegação a esta classe, que guarda os livros no RepositorioLivro
 * da família de persistência em uso (memória ou JSON) — sem saber qual é.
 */
public class Acervo {

    private final RepositorioLivro repositorio;

    public Acervo(RepositorioLivro repositorio) { this.repositorio = repositorio; }

    public void adicionar(Livro livro) { repositorio.salvar(livro); }

    /** Registra as alterações de um livro já cadastrado (ex.: mudança de estado). */
    public void atualizar(Livro livro) { repositorio.salvar(livro); }

    public boolean remover(int idLivro) { return repositorio.remover(idLivro); }

    /** Devolve o livro com esse id, ou {@code null} se não estiver no acervo. */
    public Livro buscarPorId(int idLivro) { return repositorio.buscar(idLivro); }

    /** Cópia somente leitura: quem chama não consegue alterar a coleção interna. */
    public List<Livro> listar() { return Collections.unmodifiableList(repositorio.listar()); }
}
