package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.rh.Papel;
import br.pucpr.ib2b.rh.Pessoa;

/** Papel de estudante (Astah: Estudante), identificado pela matrícula (idEstudante nas operações f1–f6). */
public class Estudante extends Papel {

    private final String matricula;

    public Estudante(int id, String matricula, Pessoa pessoa) {
        super(id, pessoa);
        this.matricula = matricula;
    }

    public String getMatricula() { return matricula; }
}
