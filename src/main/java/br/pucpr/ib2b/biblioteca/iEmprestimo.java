package br.pucpr.ib2b.biblioteca;

/** Serviços do Estudante, f1 a f4 (Astah: iEmprestimo). idEstudante = matrícula; idAtendente = matrícula funcional. */
public interface iEmprestimo {
    Reserva reservarLivro(int idLivro, String idEstudante, String idAtendente);        // f1
    Reserva consultarReserva(int idLivro, String idEstudante);                         // f2
    Emprestimo emprestarLivro(int idLivro, String idEstudante, String idAtendente);    // f3
    boolean renovarEmprestimo(int idLivro, String idEstudante);                        // f4
}
