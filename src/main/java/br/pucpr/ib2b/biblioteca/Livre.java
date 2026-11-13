package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;

/** Papel GoF: ConcreteState — LIVRE: cadastrado e disponível para reserva. */
public final class Livre extends EstadoLivro {

    static final Livre INSTANCIA = new Livre();   // sem atributos: uma instância compartilhada
    private Livre() { }

    /** T2: reservarLivro [estudante cadastrado] / reservante := e → RESERVADO. */
    @Override
    void reservarLivro(Livro l, Estudante e) {
        if (e == null) throw new OperacaoInvalidaException("Reserva recusada: estudante não cadastrado");
        l.definirReservante(e);
        l.mudarPara(Reservado.INSTANCIA);
    }

    /** T9: enviarParaReparo [dano no acervo] → REPARANDO. */
    @Override
    void enviarParaReparo(Livro l) {
        l.mudarPara(Reparando.INSTANCIA);
    }

    /** T11: excluirLivro → estado final (o tratador remove o livro do Acervo). */
    @Override
    void excluirLivro(Livro l) { }

    @Override
    public Situacao situacao() { return Situacao.LIVRE; }
}
