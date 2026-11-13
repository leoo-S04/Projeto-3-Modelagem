package br.pucpr.ib2b.biblioteca;

import java.time.Instant;
import java.util.UUID;

/** Papel GoF: ConcreteCommand — renovação de empréstimo (f4). Guarda os parâmetros e chama o receptor. */
public class SolicitacaoDeRenovacao extends SolicitacaoDeCirculacao {

    private final int idLivro;
    private final String idEstudante;

    public SolicitacaoDeRenovacao(UUID id, String autor, Instant criadaEm, iCirculacao circulacao,
                                  int idLivro, String idEstudante) {
        super(id, autor, criadaEm, circulacao);
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
    }

    @Override
    public ResultadoSolicitacao executar() {
        ComprovanteRenovacao c = circulacao.renovar(idLivro, idEstudante);   // guarda numRenovacoes < 2 no Livro
        if (c == null) return ResultadoSolicitacao.recusada(this, "Não há empréstimo deste livro para o estudante");
        return ResultadoSolicitacao.executada(this,
                "Livro " + c.idLivro() + " renovado até " + c.novoVencimento() + " (" + c.numRenovacoes() + "ª renovação)");
    }

    @Override public String tipo() { return "RENOVACAO"; }
}
