package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.comum.ResultadoCadastro;
import br.pucpr.ib2b.comum.TratadorDeCadastro;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;

/**
 * Papel GoF: ConcreteClass do Template Method (Req. 7). Regras 4.1 a 4.4 (manter Atendente).
 * Sobrescreve o gancho verificarPreRequisitos: só cadastra atendente de pessoa já cadastrada (via iPessoa).
 */
public class TratadorDeAtendente extends TratadorDeCadastro<Atendente, DadosAtendente> implements iAtendente {

    private final GrupoDeColaboradores grupoDeColaboradores;
    private final iPessoa pessoas;

    public TratadorDeAtendente(GrupoDeColaboradores grupoDeColaboradores, iPessoa pessoas, UnidadeDeTrabalho transacao) {
        super(transacao);
        this.grupoDeColaboradores = grupoDeColaboradores;
        this.pessoas = pessoas;
    }

    /** 4.1: preserva a operação do Astah; delega ao método-modelo. */
    @Override
    public ResultadoCadastro<Atendente> inserirAtendente(String cpf, String matriculaFuncional) {
        return inserir(new DadosAtendente(cpf, matriculaFuncional));
    }

    // ---- Passos variáveis do Template Method ----
    @Override protected Atendente buscarExistente(DadosAtendente d) {
        return grupoDeColaboradores.buscarPorMatricula(d.matriculaFuncional());
    }

    @Override protected void validar(DadosAtendente d) {
        exigir(preenchido(d.matriculaFuncional()), "matrícula funcional é obrigatória");
        exigir(preenchido(d.cpf()), "CPF é obrigatório");
    }

    /** Gancho: a pessoa precisa estar cadastrada no RH. */
    @Override protected void verificarPreRequisitos(DadosAtendente d) {
        exigir(pessoas.consultarPessoa(d.cpf()) != null, "pessoa não cadastrada (CPF " + d.cpf() + ")");
    }

    @Override protected Atendente criar(DadosAtendente d) {
        return new Atendente(grupoDeColaboradores.gerarIdPapel(), d.matriculaFuncional(), pessoas.consultarPessoa(d.cpf()));
    }

    @Override protected void armazenar(Atendente a) { grupoDeColaboradores.adicionar(a); }

    // ---- Demais operações de iAtendente ----
    /** 4.3: devolve o atendente, ou {@code null} se não cadastrado. */
    @Override
    public Atendente consultarAtendente(String matriculaFuncional) {
        return grupoDeColaboradores.buscarPorMatricula(matriculaFuncional);
    }

    /** 4.2: só atualiza se o atendente e a nova pessoa estiverem cadastrados. */
    @Override
    public boolean atualizarAtendente(String matriculaFuncional, String cpf) {
        return transacao.executar(() -> {
            Atendente a = grupoDeColaboradores.buscarPorMatricula(matriculaFuncional);
            Pessoa pessoa = pessoas.consultarPessoa(cpf);
            if (a == null || pessoa == null) return false;
            a.setPessoa(pessoa);
            return true;
        });
    }

    /** 4.4: só exclui se cadastrado. */
    @Override
    public boolean excluirAtendente(String matriculaFuncional) {
        return transacao.executar(() -> grupoDeColaboradores.remover(matriculaFuncional));
    }
}
