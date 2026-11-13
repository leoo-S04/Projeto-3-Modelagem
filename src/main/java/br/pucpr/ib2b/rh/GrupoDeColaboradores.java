package br.pucpr.ib2b.rh;

import java.util.ArrayList;
import java.util.List;

/** Guarda os atendentes cadastrados (Astah: GrupoDeColaboradores). */
public class GrupoDeColaboradores {

    private final List<Atendente> atendentes = new ArrayList<>();

    public void adicionar(Atendente a) { atendentes.add(a); }

    public boolean remover(String matricula) {
        return atendentes.removeIf(a -> a.getMatriculaFuncional().equals(matricula));
    }

    /** Devolve o atendente com essa matrícula funcional, ou {@code null}. */
    public Atendente buscarPorMatricula(String matricula) {
        for (Atendente a : atendentes) {
            if (a.getMatriculaFuncional().equals(matricula)) return a;
        }
        return null;
    }
}
