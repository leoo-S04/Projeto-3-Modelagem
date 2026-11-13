package br.pucpr.ib2b.biblioteca;

import java.time.Instant;
import java.util.UUID;

/** Papel GoF: ConcreteCommand — reserva de livro (f1). Guarda os parâmetros e chama o receptor. */
public class SolicitacaoDeReserva extends SolicitacaoDeCirculacao {

    private final int idLivro;
    private final String idEstudante;
    private final String idAtendente;

    public SolicitacaoDeReserva(UUID id, String autor, Instant criadaEm, iCirculacao circulacao,
                                int idLivro, String idEstudante, String idAtendente) {
        super(id, autor, criadaEm, circulacao);
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
        this.idAtendente = idAtendente;
    }

    @Override
    public ResultadoSolicitacao executar() {
        ComprovanteReserva c = circulacao.reservar(idLivro, idEstudante, idAtendente);
        if (c == null) return ResultadoSolicitacao.recusada(this, "Livro ou estudante não encontrado");
        return ResultadoSolicitacao.executada(this,
                "Reserva nº " + c.idReserva() + " do livro " + c.idLivro() + " (" + c.titulo() + ") em " + c.data());
    }

    @Override public String tipo() { return "RESERVA"; }
}
