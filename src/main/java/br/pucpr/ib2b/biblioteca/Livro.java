package br.pucpr.ib2b.biblioteca;

/**
 * Livro do acervo (Astah: Livro). Nasce LIVRE (transição inicial: inserirLivro).
 * As mudanças de estado (reservar, emprestar, renovar, devolver...) entram na Etapa 1, com o padrão State.
 */
public class Livro {

    private final int idLivro;
    private String titulo;
    private Situacao estado = Situacao.LIVRE;

    public Livro(int idLivro, String titulo) {
        this.idLivro = idLivro;
        this.titulo = titulo;
    }

    public int getIdLivro() { return idLivro; }
    public String getTitulo() { return titulo; }
    public Situacao getEstado() { return estado; }

    /** Regra 1.2 (atualizar livro): muda o título, nunca o id. */
    public void setTitulo(String titulo) { this.titulo = titulo; }
}
