package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.persistencia.RepositorioRelacionamento;

/** Guarda os atendentes cadastrados (Astah: GrupoDeColaboradores), delegando ao repositório da família em uso. */
public class GrupoDeColaboradores {

    private final RepositorioRelacionamento repositorio;

    public GrupoDeColaboradores(RepositorioRelacionamento repositorio) { this.repositorio = repositorio; }

    public void adicionar(Atendente a) { repositorio.salvarAtendente(a); }

    public boolean remover(String matricula) { return repositorio.removerAtendente(matricula); }

    public java.util.List<Atendente> listar() { return repositorio.listarAtendentes(); }

    /** Devolve o atendente com essa matrícula funcional, ou {@code null}. */
    public Atendente buscarPorMatricula(String matricula) { return repositorio.buscarAtendente(matricula); }

    /** Id do próximo papel ("todo papel tem um id"). */
    public int gerarIdPapel() { return repositorio.gerarIdPapel(); }
}
