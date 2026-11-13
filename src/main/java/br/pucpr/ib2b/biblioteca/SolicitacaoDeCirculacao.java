package br.pucpr.ib2b.biblioteca;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Papel GoF: Command (Req. 6). Uma solicitação de circulação como objeto: identificador, autor, instante de
 * criação (somente leitura) e a operação {@link #executar()}. As regras NÃO ficam aqui: a execução chama o
 * receptor (iCirculacao), e o Livro (State) decide. Não há desfazer: a entrega física do livro não é revertida.
 */
public abstract class SolicitacaoDeCirculacao {

    private final UUID id;
    private final String autor;
    private final Instant criadaEm;
    protected final iCirculacao circulacao;          // Receiver

    protected SolicitacaoDeCirculacao(UUID id, String autor, Instant criadaEm, iCirculacao circulacao) {
        this.id = Objects.requireNonNull(id, "a solicitação precisa de um identificador");
        this.autor = autor;
        this.criadaEm = criadaEm;
        this.circulacao = circulacao;
    }

    /** Executa a solicitação no receptor. Recusas de regra saem como exceção de domínio. */
    public abstract ResultadoSolicitacao executar();

    /** Nome do tipo de solicitação (gravado no registro). */
    public abstract String tipo();

    public UUID getId() { return id; }
    public String getAutor() { return autor; }
    public Instant getCriadaEm() { return criadaEm; }
}
