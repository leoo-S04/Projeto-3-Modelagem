package br.pucpr.ib2b.academico;

import java.util.ArrayList;
import java.util.List;

/** Guarda os estudantes cadastrados (Astah: GrupoDeAcademicos). */
public class GrupoDeAcademicos {

    private final List<Estudante> estudantes = new ArrayList<>();

    public void adicionar(Estudante e) { estudantes.add(e); }

    public boolean remover(String matricula) {
        return estudantes.removeIf(e -> e.getMatricula().equals(matricula));
    }

    /** Devolve o estudante com essa matrícula, ou {@code null}. */
    public Estudante buscarPorMatricula(String matricula) {
        for (Estudante e : estudantes) {
            if (e.getMatricula().equals(matricula)) return e;
        }
        return null;
    }
}
