package br.pucpr.ib2b.biblioteca;

/** Interface "manter Livro" fornecida pelo componente Biblioteca (Astah: iLivro). */
public interface iLivro {
    Livro inserirLivro(int idLivro, String titulo);
    Livro consultarLivro(int idLivro);
    boolean atualizarLivro(int idLivro, String titulo);
    boolean excluirLivro(int idLivro);
}
