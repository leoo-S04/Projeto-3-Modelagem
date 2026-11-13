package br.pucpr.ib2b.biblioteca;

/**
 * Papel GoF: Facade (contrato) — Req. 4. Casos de uso de circulação completos e transacionais.
 * Devolve comprovantes (DTOs), nunca coleções nem os objetos internos (Acervo, Movimentacao, Relacionamento).
 * Livro ou estudante inexistente → {@code null}; recusa de regra → exceção de domínio com a mensagem.
 */
public interface iCirculacao {
    ComprovanteReserva reservar(int idLivro, String idEstudante, String idAtendente);
    ComprovanteReserva consultarReserva(int idLivro, String idEstudante);
    ComprovanteEmprestimo efetivarEmprestimo(int idLivro, String idEstudante, String idAtendente);
    ComprovanteRenovacao renovar(int idLivro, String idEstudante);
    ComprovanteDevolucao devolver(int idLivro, String idEstudante, String idAtendente);
    ComprovanteDevolucao consultarDevolucao(int idLivro, String idEstudante);
    SituacaoLivroDTO registrarInspecao(int idLivro, boolean emBomEstado);
    SituacaoLivroDTO enviarParaReparo(int idLivro);
    SituacaoLivroDTO retornarDoReparo(int idLivro);
    SituacaoLivroDTO incinerar(int idLivro);
    SituacaoLivroDTO consultarSituacao(int idLivro);
}
