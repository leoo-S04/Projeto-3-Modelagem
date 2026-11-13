package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.persistencia.RepositorioRelacionamento;

/** Guarda as pessoas cadastradas (Astah: GrupoDePessoas), delegando ao repositório da família em uso. */
public class GrupoDePessoas {

    private final RepositorioRelacionamento repositorio;

    public GrupoDePessoas(RepositorioRelacionamento repositorio) { this.repositorio = repositorio; }

    public void adicionar(Pessoa p) { repositorio.salvarPessoa(p); }

    public boolean remover(String cpf) { return repositorio.removerPessoa(cpf); }

    /** Devolve a pessoa com esse CPF, ou {@code null} se não estiver cadastrada. */
    public Pessoa buscarPorCpf(String cpf) { return repositorio.buscarPessoa(cpf); }
}
