package br.pucpr.ib2b.biblioteca;

/**
 * Serviços do Atendente (Astah: iDevolucao): f5 e as operações de inspeção e reparo.
 * f6 emitirBoletoDeMulta é acrescentada na Etapa 4, junto com o componente Financeiro.
 */
public interface iDevolucao {
    ComprovanteDevolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente);   // f5
    boolean inspecionar(int idLivro, boolean emBomEstado);
    boolean enviarParaReparo(int idLivro);
    boolean retornarDoReparo(int idLivro);
    boolean incinerar(int idLivro);
}
