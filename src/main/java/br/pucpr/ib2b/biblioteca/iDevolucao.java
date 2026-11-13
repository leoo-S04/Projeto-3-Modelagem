package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.financeiro.BoletoDeMulta;

/** Serviços do Atendente (Astah: iDevolucao): f5, f6 e as operações de inspeção e reparo. */
public interface iDevolucao {
    ComprovanteDevolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente);   // f5
    BoletoDeMulta emitirBoletoDeMulta(int idLivro, String idEstudante);                             // f6
    boolean inspecionar(int idLivro, boolean emBomEstado);
    boolean enviarParaReparo(int idLivro);
    boolean retornarDoReparo(int idLivro);
    boolean incinerar(int idLivro);
}
