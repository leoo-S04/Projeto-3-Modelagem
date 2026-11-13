package br.pucpr.ib2b.rh;

/** Dados de um atendente para as telas (somente leitura). */
public record AtendenteDTO(int id, String matriculaFuncional, String cpf, String nome) {
    public static AtendenteDTO de(Atendente a) {
        return new AtendenteDTO(a.getId(), a.getMatriculaFuncional(), a.getPessoa().getCpf(), a.getPessoa().getNome());
    }
}
