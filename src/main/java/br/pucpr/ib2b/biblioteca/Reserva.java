package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;

import java.time.LocalDate;

/** Reserva de um livro LIVRE por um estudante (Astah: Reserva; regra 5.3). */
public class Reserva extends Transacao {

    public Reserva(int idTransacao, LocalDate data, Livro livro, Estudante estudante, Atendente atendente) {
        super(idTransacao, data, livro, estudante, atendente);
    }
}
