package br.pucpr.ib2b.biblioteca;

/** Papel GoF: ConcreteState — EMPRESTADO: o livro está com o estudante, sem renovação. */
public final class Emprestado extends EstadoLivro {

    static final Emprestado INSTANCIA = new Emprestado();
    private Emprestado() { }

    /** T4: renovarEmprestimo [numRenovacoes = 0] / vencimento += 30; numRenovacoes++ → RENOVADO. */
    @Override
    void renovarEmprestimo(Livro l) {
        if (l.getNumRenovacoes() != 0) throw new RenovacaoNegadaException(l.getIdLivro());   // guarda ANTES de alterar
        l.prorrogar(Livro.DIAS_POR_RENOVACAO);
        l.mudarPara(Renovado.INSTANCIA);
    }

    /** T6: registrarDevolucao → DEVOLVIDO (aguarda inspeção). */
    @Override
    void registrarDevolucao(Livro l) {
        l.mudarPara(Devolvido.INSTANCIA);
    }

    @Override
    public Situacao situacao() { return Situacao.EMPRESTADO; }
}
