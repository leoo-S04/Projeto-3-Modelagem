package br.pucpr.ib2b.academico;

/** Interface "manter Estudante" fornecida pelo componente Academico (Astah: iEstudante). */
public interface iEstudante {
    Estudante inserirEstudante(String cpf, String matricula);
    Estudante consultarEstudante(String matricula);
    boolean atualizarEstudante(String matricula, String cpf);
    boolean excluirEstudante(String matricula);
}
