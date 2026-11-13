package br.pucpr.ib2b.biblioteca;

/** Papel GoF: ConcreteState — INCINERADO: sem reparo possível; só aceita a exclusão do cadastro. */
public final class Incinerado extends EstadoLivro {

    static final Incinerado INSTANCIA = new Incinerado();
    private Incinerado() { }

    /** T11: excluirLivro → estado final (o tratador remove o livro do Acervo). */
    @Override
    void excluirLivro(Livro l) { }

    @Override
    public Situacao situacao() { return Situacao.INCINERADO; }
}
