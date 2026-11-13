package br.pucpr.ib2b.rh;

/**
 * Regras 4.1 a 4.4 (manter Atendente). Delega a GrupoDeColaboradores e consulta pessoas
 * pela interface iPessoa (restrição: só cadastra atendente de pessoa já cadastrada).
 */
public class TratadorDeAtendente implements iAtendente {

    private final GrupoDeColaboradores grupoDeColaboradores;
    private final iPessoa pessoas;
    private int proximoId = 1;   // gera o id do Papel

    public TratadorDeAtendente(GrupoDeColaboradores grupoDeColaboradores, iPessoa pessoas) {
        this.grupoDeColaboradores = grupoDeColaboradores;
        this.pessoas = pessoas;
    }

    /** 4.1: já cadastrado → devolve o existente; pessoa não cadastrada → null, nada é gravado. */
    @Override
    public Atendente inserirAtendente(String cpf, String matriculaFuncional) {
        Atendente existente = grupoDeColaboradores.buscarPorMatricula(matriculaFuncional);
        if (existente != null) return existente;
        Pessoa pessoa = pessoas.consultarPessoa(cpf);
        if (pessoa == null) return null;
        Atendente novo = new Atendente(proximoId++, matriculaFuncional, pessoa);
        grupoDeColaboradores.adicionar(novo);
        return novo;
    }

    /** 4.3: devolve o atendente, ou {@code null} se não cadastrado. */
    @Override
    public Atendente consultarAtendente(String matriculaFuncional) {
        return grupoDeColaboradores.buscarPorMatricula(matriculaFuncional);
    }

    /** 4.2: só atualiza se o atendente e a nova pessoa estiverem cadastrados. */
    @Override
    public boolean atualizarAtendente(String matriculaFuncional, String cpf) {
        Atendente a = grupoDeColaboradores.buscarPorMatricula(matriculaFuncional);
        Pessoa pessoa = pessoas.consultarPessoa(cpf);
        if (a == null || pessoa == null) return false;
        a.setPessoa(pessoa);
        return true;
    }

    /** 4.4: só exclui se cadastrado. */
    @Override
    public boolean excluirAtendente(String matriculaFuncional) {
        return grupoDeColaboradores.remover(matriculaFuncional);
    }
}
