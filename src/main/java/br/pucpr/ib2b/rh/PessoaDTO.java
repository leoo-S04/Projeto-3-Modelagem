package br.pucpr.ib2b.rh;

/** Dados de uma pessoa para as telas (somente leitura). */
public record PessoaDTO(String cpf, String nome, String sobrenome, String email) {
    public static PessoaDTO de(Pessoa p) { return new PessoaDTO(p.getCpf(), p.getNome(), p.getSobrenome(), p.getEmail()); }
}
