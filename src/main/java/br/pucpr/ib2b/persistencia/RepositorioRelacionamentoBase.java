package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;

/** Implementação comum: pessoas, estudantes e atendentes na cópia de trabalho. */
abstract class RepositorioRelacionamentoBase implements RepositorioRelacionamento {

    private final UnidadeDeTrabalhoBase uow;

    protected RepositorioRelacionamentoBase(UnidadeDeTrabalhoBase uow) { this.uow = uow; }

    @Override public void salvarPessoa(Pessoa p) { uow.dados().pessoas.put(p.getCpf(), p); }
    @Override public Pessoa buscarPessoa(String cpf) { return uow.dados().pessoas.get(cpf); }
    @Override public boolean removerPessoa(String cpf) { return uow.dados().pessoas.remove(cpf) != null; }

    @Override public void salvarEstudante(Estudante e) { uow.dados().estudantes.put(e.getMatricula(), e); }
    @Override public Estudante buscarEstudante(String matricula) { return uow.dados().estudantes.get(matricula); }
    @Override public boolean removerEstudante(String matricula) { return uow.dados().estudantes.remove(matricula) != null; }

    @Override public void salvarAtendente(Atendente a) { uow.dados().atendentes.put(a.getMatriculaFuncional(), a); }
    @Override public Atendente buscarAtendente(String matricula) { return uow.dados().atendentes.get(matricula); }
    @Override public boolean removerAtendente(String matricula) { return uow.dados().atendentes.remove(matricula) != null; }

    @Override public java.util.List<Pessoa> listarPessoas() { return new java.util.ArrayList<>(uow.dados().pessoas.values()); }
    @Override public java.util.List<Estudante> listarEstudantes() { return new java.util.ArrayList<>(uow.dados().estudantes.values()); }
    @Override public java.util.List<Atendente> listarAtendentes() { return new java.util.ArrayList<>(uow.dados().atendentes.values()); }

    @Override public int gerarIdPapel() { return uow.proximoIdPapel(); }

    @Override public UnidadeDeTrabalho unidade() { return uow; }
}
