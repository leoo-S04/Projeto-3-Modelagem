package br.pucpr.ib2b.biblioteca;

/** Critério de busca normalizado (minúsculas, sem espaços nas pontas) — também é a chave do cache. */
public record Criterio(String termo) {
    public Criterio {
        termo = termo == null ? "" : termo.trim().toLowerCase();
    }
}
