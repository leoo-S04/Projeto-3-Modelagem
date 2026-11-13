package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha do relatório de cobranças para as telas: boleto + política aplicada na multa (somente leitura). */
public record CobrancaDTO(int idBoleto, String cpf, int diasAtraso, BigDecimal valor, String politicaAplicada,
                          String banco, String idExterno, String situacao, LocalDate dataEmissao,
                          LocalDate dataPagamento) { }
