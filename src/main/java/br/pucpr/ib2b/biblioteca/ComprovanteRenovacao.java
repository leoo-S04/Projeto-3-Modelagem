package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;

/** Comprovante (DTO) da renovação: novo vencimento e quantas renovações já foram feitas. */
public record ComprovanteRenovacao(int idLivro, String titulo, LocalDate novoVencimento, int numRenovacoes) { }
