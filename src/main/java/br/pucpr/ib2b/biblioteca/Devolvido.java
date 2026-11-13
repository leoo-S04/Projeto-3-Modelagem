package br.pucpr.ib2b.biblioteca;

/**
 * Papel GoF: ConcreteState — DEVOLVIDO: recebido pelo atendente, aguarda inspeção.
 * Não sobrescreve reservarLivro: a reserva é recusada pela implementação padrão até a inspeção.
 */
public final class Devolvido extends EstadoLivro {

    static final Devolvido INSTANCIA = new Devolvido();
    private Devolvido() { }

    /**
     * T7: inspecionar [livro em bom estado] / limparCiclo → LIVRE.
     * T8: inspecionar [livro danificado] / enviarParaReparo → REPARANDO.
     */
    @Override
    void inspecionar(Livro l, boolean emBomEstado) {
        if (emBomEstado) {
            l.limparCiclo();
            l.mudarPara(Livre.INSTANCIA);
        } else {
            l.mudarPara(Reparando.INSTANCIA);
        }
    }

    @Override
    public Situacao situacao() { return Situacao.DEVOLVIDO; }
}
