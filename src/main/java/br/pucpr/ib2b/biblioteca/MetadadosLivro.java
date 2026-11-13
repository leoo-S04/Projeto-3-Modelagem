package br.pucpr.ib2b.biblioteca;

/**
 * Metadados de um livro (valor): SÓ id e título. De propósito não há situação/disponibilidade — por isso um
 * resultado antigo do cache nunca pode autorizar uma reserva; quem decide é o estado atual do Livro.
 */
public record MetadadosLivro(int idLivro, String titulo) { }
