package br.pucpr.ib2b.biblioteca;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Serviços f1 a f4 do Estudante (Astah: TratadorDeEmprestimo). Mantém os serviços e DELEGA:
 * <ul>
 *   <li>à fachada iCirculacao (Facade, Req. 4) — nada de Acervo, Movimentacao ou Relacionamento aqui;</li>
 *   <li>f1 e f4 viram solicitações (Command, Req. 6): o tratador é o Client que cria a solicitação concreta e a
 *       entrega ao ExecutorDeSolicitacoes (Invoker).</li>
 * </ul>
 * Nenhum if sobre a situação do livro (State, Req. 1).
 */
public class TratadorDeEmprestimo implements iEmprestimo {

    private final iCirculacao circulacao;
    private final ExecutorDeSolicitacoes executor;
    private final Clock relogio;

    public TratadorDeEmprestimo(iCirculacao circulacao, ExecutorDeSolicitacoes executor, Clock relogio) {
        this.circulacao = circulacao;
        this.executor = executor;
        this.relogio = relogio;
    }

    /** f1 sem id informado: cada chamada é uma solicitação nova. */
    @Override
    public ResultadoSolicitacao reservarLivro(int idLivro, String idEstudante, String idAtendente) {
        return reservarLivro(UUID.randomUUID(), idLivro, idEstudante, idAtendente);
    }

    /** f1 — cria o ConcreteCommand e o submete ao Invoker. */
    @Override
    public ResultadoSolicitacao reservarLivro(UUID idSolicitacao, int idLivro, String idEstudante, String idAtendente) {
        return executor.submeter(new SolicitacaoDeReserva(idSolicitacao, idEstudante, Instant.now(relogio),
                circulacao, idLivro, idEstudante, idAtendente));
    }

    /** f2. */
    @Override
    public ComprovanteReserva consultarReserva(int idLivro, String idEstudante) {
        return circulacao.consultarReserva(idLivro, idEstudante);
    }

    /** f3 — delega à fachada, que valida a reserva e efetiva tudo numa transação. */
    @Override
    public ComprovanteEmprestimo emprestarLivro(int idLivro, String idEstudante, String idAtendente) {
        return circulacao.efetivarEmprestimo(idLivro, idEstudante, idAtendente);
    }

    /** f4 sem id informado: cada chamada é uma solicitação nova (continua limitada a 2 renovações pelo Livro). */
    @Override
    public ResultadoSolicitacao renovarEmprestimo(int idLivro, String idEstudante) {
        return renovarEmprestimo(UUID.randomUUID(), idLivro, idEstudante);
    }

    /** f4 — mesmo id submetido duas vezes acrescenta apenas 30 dias. */
    @Override
    public ResultadoSolicitacao renovarEmprestimo(UUID idSolicitacao, int idLivro, String idEstudante) {
        return executor.submeter(new SolicitacaoDeRenovacao(idSolicitacao, idEstudante, Instant.now(relogio),
                circulacao, idLivro, idEstudante));
    }

    @Override
    public SituacaoLivroDTO consultarSituacao(int idLivro) { return circulacao.consultarSituacao(idLivro); }
}
