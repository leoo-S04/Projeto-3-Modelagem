package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.comum.ResultadoCadastro;

/** Interface "manter Pessoa" fornecida pelo componente RH (Astah: iPessoa). */
public interface iPessoa {
    ResultadoCadastro<Pessoa> inserirPessoa(String cpf, String nome, String sobrenome, String email);
    Pessoa consultarPessoa(String cpf);
    boolean atualizarPessoa(String cpf, String nome, String sobrenome, String email);
    boolean excluirPessoa(String cpf);
}
