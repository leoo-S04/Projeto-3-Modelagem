package br.pucpr.ib2b.biblioteca;

/**
 * Serviços do Atendente (Astah: TratadorDeDevolucao): f5, inspeção e reparo. Delega à fachada iCirculacao
 * (Facade, Req. 4); não acessa Acervo, Movimentacao nem Relacionamento e não testa a situação do livro.
 * A f6 (boleto de multa) e a publicação do evento de devolução entram na Etapa 4.
 */
public class TratadorDeDevolucao implements iDevolucao {

    private final iCirculacao circulacao;

    public TratadorDeDevolucao(iCirculacao circulacao) {
        this.circulacao = circulacao;
    }

    /** f5 — regra 9: o livro fica DEVOLVIDO até a inspeção. */
    @Override
    public ComprovanteDevolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente) {
        return circulacao.devolver(idLivro, idEstudante, idAtendente);
    }

    /** DEVOLVIDO → LIVRE (bom estado) ou REPARANDO (danificado). */
    @Override
    public boolean inspecionar(int idLivro, boolean emBomEstado) {
        return circulacao.registrarInspecao(idLivro, emBomEstado) != null;
    }

    /** LIVRE → REPARANDO (dano identificado no acervo). */
    @Override
    public boolean enviarParaReparo(int idLivro) { return circulacao.enviarParaReparo(idLivro) != null; }

    /** REPARANDO → LIVRE. */
    @Override
    public boolean retornarDoReparo(int idLivro) { return circulacao.retornarDoReparo(idLivro) != null; }

    /** REPARANDO → INCINERADO (laudo irrecuperável). */
    @Override
    public boolean incinerar(int idLivro) { return circulacao.incinerar(idLivro) != null; }
}
