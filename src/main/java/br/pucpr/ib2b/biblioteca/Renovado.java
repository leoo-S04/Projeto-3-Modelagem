package br.pucpr.ib2b.biblioteca;

/** Papel GoF: ConcreteState — RENOVADO: o empréstimo foi renovado uma ou duas vezes. */
public final class Renovado extends EstadoLivro {

    static final Renovado INSTANCIA = new Renovado();
    private Renovado() { }

    /**
     * T5: renovarEmprestimo [numRenovacoes < 2] / vencimento += 30; numRenovacoes++ → RENOVADO (autotransição).
     * A guarda é avaliada antes de qualquer alteração: na 3ª tentativa nada muda.
     */
    @Override
    void renovarEmprestimo(Livro l) {
        if (l.getNumRenovacoes() >= 2) throw new RenovacaoNegadaException(l.getIdLivro());
        l.prorrogar(Livro.DIAS_POR_RENOVACAO);
        l.mudarPara(Renovado.INSTANCIA);
    }

    /** T6: registrarDevolucao → DEVOLVIDO (aguarda inspeção). */
    @Override
    void registrarDevolucao(Livro l) {
        l.mudarPara(Devolvido.INSTANCIA);
    }

    @Override
    public Situacao situacao() { return Situacao.RENOVADO; }
}
