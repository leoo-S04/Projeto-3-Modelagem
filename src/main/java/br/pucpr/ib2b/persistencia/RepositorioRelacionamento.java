package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;

/** Papel GoF: AbstractProduct — pessoas, estudantes e atendentes (usado pelos grupos do RH e do Acadêmico). */
public interface RepositorioRelacionamento {
    void salvarPessoa(Pessoa p);
    Pessoa buscarPessoa(String cpf);
    boolean removerPessoa(String cpf);

    void salvarEstudante(Estudante e);
    Estudante buscarEstudante(String matricula);
    boolean removerEstudante(String matricula);

    void salvarAtendente(Atendente a);
    Atendente buscarAtendente(String matriculaFuncional);
    boolean removerAtendente(String matriculaFuncional);

    /** Id do próximo papel (estudante ou atendente). */
    int gerarIdPapel();

    UnidadeDeTrabalho unidade();
}
