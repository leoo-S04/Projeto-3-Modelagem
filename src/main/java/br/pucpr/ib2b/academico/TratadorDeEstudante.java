package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.rh.Pessoa;
import br.pucpr.ib2b.rh.iPessoa;

/**
 * Regras 3.1 a 3.4 (manter Estudante). Delega a GrupoDeAcademicos. A pessoa é consultada pela
 * interface iPessoa, que o componente Academico requer do RH (diagrama de componentes).
 */
public class TratadorDeEstudante implements iEstudante {

    private final GrupoDeAcademicos grupoDeAcademicos;
    private final iPessoa pessoas;
    private int proximoId = 1;   // gera o id do Papel

    public TratadorDeEstudante(GrupoDeAcademicos grupoDeAcademicos, iPessoa pessoas) {
        this.grupoDeAcademicos = grupoDeAcademicos;
        this.pessoas = pessoas;
    }

    /** 3.1: já cadastrado → devolve o existente; pessoa não cadastrada → null, nada é gravado. */
    @Override
    public Estudante inserirEstudante(String cpf, String matricula) {
        Estudante existente = grupoDeAcademicos.buscarPorMatricula(matricula);
        if (existente != null) return existente;
        Pessoa pessoa = pessoas.consultarPessoa(cpf);
        if (pessoa == null) return null;
        Estudante novo = new Estudante(proximoId++, matricula, pessoa);
        grupoDeAcademicos.adicionar(novo);
        return novo;
    }

    /** 3.3: devolve o estudante, ou {@code null} se não cadastrado. */
    @Override
    public Estudante consultarEstudante(String matricula) {
        return grupoDeAcademicos.buscarPorMatricula(matricula);
    }

    /** 3.2: só atualiza se o estudante e a nova pessoa estiverem cadastrados. */
    @Override
    public boolean atualizarEstudante(String matricula, String cpf) {
        Estudante e = grupoDeAcademicos.buscarPorMatricula(matricula);
        Pessoa pessoa = pessoas.consultarPessoa(cpf);
        if (e == null || pessoa == null) return false;
        e.setPessoa(pessoa);
        return true;
    }

    /** 3.4: só exclui se cadastrado. */
    @Override
    public boolean excluirEstudante(String matricula) {
        return grupoDeAcademicos.remover(matricula);
    }
}
