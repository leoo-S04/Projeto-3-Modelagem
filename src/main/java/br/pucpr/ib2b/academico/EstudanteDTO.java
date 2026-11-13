package br.pucpr.ib2b.academico;

/** Dados de um estudante para as telas (somente leitura). */
public record EstudanteDTO(int id, String matricula, String cpf, String nome) {
    public static EstudanteDTO de(Estudante e) {
        return new EstudanteDTO(e.getId(), e.getMatricula(), e.getPessoa().getCpf(), e.getPessoa().getNome());
    }
}
