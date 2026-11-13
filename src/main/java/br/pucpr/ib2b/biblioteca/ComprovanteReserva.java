package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;

/** Comprovante (DTO) devolvido pela fachada de circulação: só dados, nenhuma coleção nem objeto do domínio. */
public record ComprovanteReserva(int idReserva, int idLivro, String titulo, String matriculaEstudante, LocalDate data) {

    static ComprovanteReserva de(Reserva r) {
        return new ComprovanteReserva(r.getIdTransacao(), r.getLivro().getIdLivro(), r.getLivro().getTitulo(),
                r.getEstudante().getMatricula(), r.getData());
    }
}
