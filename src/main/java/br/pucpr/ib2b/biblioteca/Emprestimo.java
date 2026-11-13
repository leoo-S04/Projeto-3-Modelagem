package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;

import java.time.LocalDate;

/**
 * Empréstimo de um livro reservado (Astah: Emprestimo; regra 7.2).
 * O vencimento e o número de renovações ficam no Livro (decisão da equipe, Etapa 1, Pergunta 1 = A),
 * porque são dados usados pelas guardas da máquina de estados do Livro.
 */
public class Emprestimo extends Transacao {

    public Emprestimo(int idTransacao, LocalDate data, Livro livro, Estudante estudante, Atendente atendente) {
        super(idTransacao, data, livro, estudante, atendente);
    }
}
