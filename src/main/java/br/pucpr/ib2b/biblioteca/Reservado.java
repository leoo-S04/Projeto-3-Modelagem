package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;

import java.time.LocalDate;

/** Papel GoF: ConcreteState — RESERVADO: há uma reserva registrada para um estudante. */
public final class Reservado extends EstadoLivro {

    static final Reservado INSTANCIA = new Reservado();
    private Reservado() { }

    /** T3: emprestarLivro [reserva do mesmo estudante] / vencimento := data + prazo → EMPRESTADO. */
    @Override
    void emprestarLivro(Livro l, Estudante e, LocalDate data) {
        if (e == null || !e.getMatricula().equals(l.getReservante().getMatricula())) {
            throw new OperacaoInvalidaException("Empréstimo recusado: o livro está reservado para outro estudante");
        }
        l.definirVencimento(data.plusDays(Livro.PRAZO_EMPRESTIMO_DIAS));
        l.mudarPara(Emprestado.INSTANCIA);
    }

    @Override
    public Situacao situacao() { return Situacao.RESERVADO; }
}
