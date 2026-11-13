package br.pucpr.ib2b.financeiro;

/** Interface fornecida pelo Financeiro e requerida pela Biblioteca (Astah: iMulta). */
public interface iMulta {
    /**
     * Gera a multa de uma devolução atrasada e emite o boleto. Idempotente por devolução (idDevolucao é a chave):
     * chamar de novo devolve o mesmo boleto (segunda via). {@code null} se o valor calculado for zero.
     */
    BoletoDeMulta gerarMulta(String cpf, int idDevolucao, int diasAtraso);

    BoletoDeMulta consultarMulta(int idBoleto);
}
