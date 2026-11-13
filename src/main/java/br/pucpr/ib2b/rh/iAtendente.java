package br.pucpr.ib2b.rh;

/** Interface "manter Atendente" fornecida pelo componente RH (Astah: iAtendente). */
public interface iAtendente {
    Atendente inserirAtendente(String cpf, String matriculaFuncional);
    Atendente consultarAtendente(String matriculaFuncional);
    boolean atualizarAtendente(String matriculaFuncional, String cpf);
    boolean excluirAtendente(String matriculaFuncional);
}
