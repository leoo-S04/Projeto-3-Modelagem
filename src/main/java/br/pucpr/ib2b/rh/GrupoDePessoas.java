package br.pucpr.ib2b.rh;

import java.util.ArrayList;
import java.util.List;

/** Guarda as pessoas cadastradas (Astah: GrupoDePessoas). Os tratadores acessam por delegação. */
public class GrupoDePessoas {

    private final List<Pessoa> pessoas = new ArrayList<>();

    public void adicionar(Pessoa p) { pessoas.add(p); }

    public boolean remover(String cpf) {
        return pessoas.removeIf(p -> p.getCpf().equals(cpf));
    }

    /** Devolve a pessoa com esse CPF, ou {@code null} se não estiver cadastrada. */
    public Pessoa buscarPorCpf(String cpf) {
        for (Pessoa p : pessoas) {
            if (p.getCpf().equals(cpf)) return p;
        }
        return null;
    }
}
