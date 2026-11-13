package br.pucpr.ib2b.biblioteca;

import java.util.UUID;

/**
 * Serviços do Estudante, f1 a f4 (Astah: iEmprestimo). idEstudante = matrícula; idAtendente = matrícula funcional.
 * f1 e f4 são solicitações (Command): a tela pode informar o id da solicitação para que um reenvio
 * (duplo clique, resposta perdida) não execute duas vezes.
 */
public interface iEmprestimo {
    ResultadoSolicitacao reservarLivro(int idLivro, String idEstudante, String idAtendente);                     // f1
    ResultadoSolicitacao reservarLivro(UUID idSolicitacao, int idLivro, String idEstudante, String idAtendente); // f1
    ComprovanteReserva consultarReserva(int idLivro, String idEstudante);                                         // f2
    ComprovanteEmprestimo emprestarLivro(int idLivro, String idEstudante, String idAtendente);                   // f3
    ResultadoSolicitacao renovarEmprestimo(int idLivro, String idEstudante);                                      // f4
    ResultadoSolicitacao renovarEmprestimo(UUID idSolicitacao, int idLivro, String idEstudante);                  // f4
    SituacaoLivroDTO consultarSituacao(int idLivro);
}
