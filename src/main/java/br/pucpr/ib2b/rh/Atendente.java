package br.pucpr.ib2b.rh;

/** Papel de atendente da biblioteca (Astah: Atendente), identificado pela matrícula funcional. */
public class Atendente extends Papel {

    private final String matriculaFuncional;

    public Atendente(int id, String matriculaFuncional, Pessoa pessoa) {
        super(id, pessoa);
        this.matriculaFuncional = matriculaFuncional;
    }

    public String getMatriculaFuncional() { return matriculaFuncional; }
}
