package br.pucpr.ib2b.biblioteca;

import java.time.Clock;

/**
 * Serviços f1 a f4 do Estudante (Astah: TratadorDeEmprestimo). Acessa Acervo, Movimentacao e Relacionamento
 * por delegação. O Clock é injetado para que testes e demonstração controlem a data de "hoje".
 *
 * <p>Etapa 0 (decisão da equipe, Pergunta 4 = A): só f2 funciona. f1, f3 e f4 dependem das regras de estado
 * do Livro e são implementadas na Etapa 1 (State), sem condicionais sobre a situação do livro.</p>
 */
public class TratadorDeEmprestimo implements iEmprestimo {

    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final Clock relogio;

    public TratadorDeEmprestimo(Acervo acervo, Movimentacao movimentacao, Relacionamento relacionamento,
                                Clock relogio) {
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
    }

    /** f1 — regra 5. */
    @Override
    public Reserva reservarLivro(int idLivro, String idEstudante, String idAtendente) {
        throw new UnsupportedOperationException("f1 reservarLivro: implementado na Etapa 1 (State)");
    }

    /** f2 — regra 6: devolve a reserva registrada na Movimentacao, ou {@code null}. */
    @Override
    public Reserva consultarReserva(int idLivro, String idEstudante) {
        return movimentacao.buscarReserva(idLivro, idEstudante);
    }

    /** f3 — regra 7. */
    @Override
    public Emprestimo emprestarLivro(int idLivro, String idEstudante, String idAtendente) {
        throw new UnsupportedOperationException("f3 emprestarLivro: implementado na Etapa 1 (State)");
    }

    /** f4 — regra 8. */
    @Override
    public boolean renovarEmprestimo(int idLivro, String idEstudante) {
        throw new UnsupportedOperationException("f4 renovarEmprestimo: implementado na Etapa 1 (State)");
    }
}
