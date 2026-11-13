package br.pucpr.ib2b.biblioteca;

/**
 * Os 7 estados do ciclo de vida do Livro (máquina de estados do Astah; Trabalho 02, seção 11).
 * No Astah esta enumeração se chama EstadoLivro; o nome foi liberado para a classe do padrão State (Etapa 1).
 */
public enum Situacao {
    LIVRE, RESERVADO, EMPRESTADO, RENOVADO, DEVOLVIDO, REPARANDO, INCINERADO
}
