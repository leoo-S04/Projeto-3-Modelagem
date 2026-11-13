package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.persistencia.RepositorioRelacionamento;

/** Guarda os estudantes cadastrados (Astah: GrupoDeAcademicos), delegando ao repositório da família em uso. */
public class GrupoDeAcademicos {

    private final RepositorioRelacionamento repositorio;

    public GrupoDeAcademicos(RepositorioRelacionamento repositorio) { this.repositorio = repositorio; }

    public void adicionar(Estudante e) { repositorio.salvarEstudante(e); }

    public boolean remover(String matricula) { return repositorio.removerEstudante(matricula); }

    public java.util.List<Estudante> listar() { return repositorio.listarEstudantes(); }

    /** Devolve o estudante com essa matrícula, ou {@code null}. */
    public Estudante buscarPorMatricula(String matricula) { return repositorio.buscarEstudante(matricula); }

    /** Id do próximo papel ("todo papel tem um id"). */
    public int gerarIdPapel() { return repositorio.gerarIdPapel(); }
}
