package br.pucpr.ib2b.biblioteca;

/** Papel GoF: ConcreteState — REPARANDO: o livro está no setor de reparos. */
public final class Reparando extends EstadoLivro {

    static final Reparando INSTANCIA = new Reparando();
    private Reparando() { }

    /** T10: retornarDoReparo / limparCiclo → LIVRE. */
    @Override
    void retornarDoReparo(Livro l) {
        l.limparCiclo();
        l.mudarPara(Livre.INSTANCIA);
    }

    /** incinerar [livro sem reparo possível] → INCINERADO (evento de laudo irrecuperável do Trabalho 03). */
    @Override
    void incinerar(Livro l) {
        l.mudarPara(Incinerado.INSTANCIA);
    }

    @Override
    public Situacao situacao() { return Situacao.REPARANDO; }
}
