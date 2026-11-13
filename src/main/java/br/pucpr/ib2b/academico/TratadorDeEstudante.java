package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.comum.ResultadoCadastro;
import br.pucpr.ib2b.comum.TratadorDeCadastro;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;
import br.pucpr.ib2b.rh.Pessoa;
import br.pucpr.ib2b.rh.iPessoa;

/**
 * Papel GoF: ConcreteClass do Template Method (Req. 7). Regras 3.1 a 3.4 (manter Estudante).
 * Sobrescreve o gancho verificarPreRequisitos: a pessoa precisa estar cadastrada (consultada pela interface
 * iPessoa, que o componente Academico requer do RH).
 */
public class TratadorDeEstudante extends TratadorDeCadastro<Estudante, DadosEstudante> implements iEstudante {

    private final GrupoDeAcademicos grupoDeAcademicos;
    private final iPessoa pessoas;

    public TratadorDeEstudante(GrupoDeAcademicos grupoDeAcademicos, iPessoa pessoas, UnidadeDeTrabalho transacao) {
        super(transacao);
        this.grupoDeAcademicos = grupoDeAcademicos;
        this.pessoas = pessoas;
    }

    /** 3.1: preserva a operação do Astah; delega ao método-modelo. */
    @Override
    public ResultadoCadastro<Estudante> inserirEstudante(String cpf, String matricula) {
        return inserir(new DadosEstudante(cpf, matricula));
    }

    // ---- Passos variáveis do Template Method ----
    @Override protected Estudante buscarExistente(DadosEstudante d) {
        return grupoDeAcademicos.buscarPorMatricula(d.matricula());
    }

    @Override protected void validar(DadosEstudante d) {
        exigir(preenchido(d.matricula()), "matrícula é obrigatória");
        exigir(preenchido(d.cpf()), "CPF é obrigatório");
    }

    /** Gancho: só cadastra estudante de pessoa já cadastrada (restrição do Trabalho 01/02). */
    @Override protected void verificarPreRequisitos(DadosEstudante d) {
        exigir(pessoas.consultarPessoa(d.cpf()) != null, "pessoa não cadastrada (CPF " + d.cpf() + ")");
    }

    @Override protected Estudante criar(DadosEstudante d) {
        return new Estudante(grupoDeAcademicos.gerarIdPapel(), d.matricula(), pessoas.consultarPessoa(d.cpf()));
    }

    @Override protected void armazenar(Estudante e) { grupoDeAcademicos.adicionar(e); }

    // ---- Demais operações de iEstudante ----
    /** Estudantes cadastrados (DTOs), para as telas. */
    public java.util.List<EstudanteDTO> listarEstudantes() { return grupoDeAcademicos.listar().stream().map(EstudanteDTO::de).toList(); }

    /** 3.3: devolve o estudante, ou {@code null} se não cadastrado. */
    @Override
    public Estudante consultarEstudante(String matricula) { return grupoDeAcademicos.buscarPorMatricula(matricula); }

    /** 3.2: só atualiza se o estudante e a nova pessoa estiverem cadastrados. */
    @Override
    public boolean atualizarEstudante(String matricula, String cpf) {
        return transacao.executar(() -> {
            Estudante e = grupoDeAcademicos.buscarPorMatricula(matricula);
            Pessoa pessoa = pessoas.consultarPessoa(cpf);
            if (e == null || pessoa == null) return false;
            e.setPessoa(pessoa);
            return true;
        });
    }

    /** 3.4: só exclui se cadastrado. */
    @Override
    public boolean excluirEstudante(String matricula) {
        return transacao.executar(() -> grupoDeAcademicos.remover(matricula));
    }
}
