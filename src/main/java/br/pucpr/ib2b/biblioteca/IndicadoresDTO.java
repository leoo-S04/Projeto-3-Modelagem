package br.pucpr.ib2b.biblioteca;

/** Indicadores de devolução para as telas (somente leitura). */
public record IndicadoresDTO(int totalDeDevolucoes, int devolucoesAtrasadas, int totalDeDiasDeAtraso) { }
