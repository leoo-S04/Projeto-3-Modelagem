package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.comum.ResultadoCadastro;

/** Interface "manter Estudante" fornecida pelo componente Academico (Astah: iEstudante). */
public interface iEstudante {
    ResultadoCadastro<Estudante> inserirEstudante(String cpf, String matricula);
    Estudante consultarEstudante(String matricula);
    boolean atualizarEstudante(String matricula, String cpf);
    boolean excluirEstudante(String matricula);
}
