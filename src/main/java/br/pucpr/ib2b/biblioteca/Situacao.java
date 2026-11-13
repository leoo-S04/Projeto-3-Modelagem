package br.pucpr.ib2b.biblioteca;

/**
 * Os 7 estados do ciclo de vida do Livro (máquina de estados do Astah; Trabalho 02, seção 11).
 * É o valor gravado na persistência e exibido na interface; o comportamento de cada estado fica nas
 * classes do padrão State (EstadoLivro e subclasses), e EstadoLivro.para(Situacao) faz a reconstituição.
 * No Astah esta enumeração se chama EstadoLivro; o nome foi liberado para a classe do padrão State.
 */
public enum Situacao {
    LIVRE, RESERVADO, EMPRESTADO, RENOVADO, DEVOLVIDO, REPARANDO, INCINERADO
}
