package br.pucpr.ib2b.biblioteca;

import java.time.Clock;

/**
 * Serviços do Atendente (Astah: TratadorDeDevolucao). Acessa Acervo, Movimentacao e Relacionamento por
 * delegação; o Clock injetado define a data da devolução (testes e demonstração simulam atraso).
 *
 * <p>Etapa 0 (decisão da equipe, Pergunta 4 = A): todas as operações dependem das regras de estado do Livro
 * e são implementadas na Etapa 1 (State). A f6 entra na Etapa 4.</p>
 */
public class TratadorDeDevolucao implements iDevolucao {

    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final Clock relogio;

    public TratadorDeDevolucao(Acervo acervo, Movimentacao movimentacao, Relacionamento relacionamento,
                               Clock relogio) {
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
    }

    /** f5 — regra 9. */
    @Override
    public Devolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente) {
        throw new UnsupportedOperationException("f5 registrarDevolucao: implementado na Etapa 1 (State)");
    }

    @Override
    public boolean inspecionar(int idLivro, boolean emBomEstado) {
        throw new UnsupportedOperationException("inspecionar: implementado na Etapa 1 (State)");
    }

    @Override
    public boolean enviarParaReparo(int idLivro) {
        throw new UnsupportedOperationException("enviarParaReparo: implementado na Etapa 1 (State)");
    }

    @Override
    public boolean retornarDoReparo(int idLivro) {
        throw new UnsupportedOperationException("retornarDoReparo: implementado na Etapa 1 (State)");
    }

    @Override
    public boolean incinerar(int idLivro) {
        throw new UnsupportedOperationException("incinerar: implementado na Etapa 1 (State)");
    }
}
