package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.comum.ResultadoCadastro;
import br.pucpr.ib2b.comum.TratadorDeCadastro;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;

/**
 * Papel GoF: ConcreteClass do Template Method (Req. 7). Regras 2.1 a 2.4 (manter Pessoa).
 * A inclusão segue o método-modelo herdado; aqui ficam só os passos próprios da pessoa.
 */
public class TratadorDePessoa extends TratadorDeCadastro<Pessoa, DadosPessoa> implements iPessoa {

    private final GrupoDePessoas grupoDePessoas;

    public TratadorDePessoa(GrupoDePessoas grupoDePessoas, UnidadeDeTrabalho transacao) {
        super(transacao);
        this.grupoDePessoas = grupoDePessoas;
    }

    /** 2.1: preserva a operação do Astah; delega ao método-modelo. */
    @Override
    public ResultadoCadastro<Pessoa> inserirPessoa(String cpf, String nome, String sobrenome, String email) {
        return inserir(new DadosPessoa(cpf, nome, sobrenome, email));
    }

    // ---- Passos variáveis do Template Method ----
    @Override protected Pessoa buscarExistente(DadosPessoa d) { return grupoDePessoas.buscarPorCpf(d.cpf()); }

    @Override protected void validar(DadosPessoa d) {
        exigir(preenchido(d.cpf()) && d.cpf().chars().allMatch(Character::isDigit), "CPF deve conter só dígitos");
        exigir(preenchido(d.nome()), "nome é obrigatório");
    }

    @Override protected Pessoa criar(DadosPessoa d) { return new Pessoa(d.cpf(), d.nome(), d.sobrenome(), d.email()); }

    @Override protected void armazenar(Pessoa p) { grupoDePessoas.adicionar(p); }

    // ---- Demais operações de iPessoa ----
    /** 2.3: devolve a pessoa, ou {@code null} se não cadastrada. */
    @Override
    public Pessoa consultarPessoa(String cpf) { return grupoDePessoas.buscarPorCpf(cpf); }

    /** 2.2: só atualiza se cadastrada. */
    @Override
    public boolean atualizarPessoa(String cpf, String nome, String sobrenome, String email) {
        return transacao.executar(() -> {
            Pessoa p = grupoDePessoas.buscarPorCpf(cpf);
            if (p == null) return false;
            p.atualizarDados(nome, sobrenome, email);
            return true;
        });
    }

    /** 2.4: só exclui se cadastrada (integridade referencial fora do escopo, como no enunciado). */
    @Override
    public boolean excluirPessoa(String cpf) { return transacao.executar(() -> grupoDePessoas.remover(cpf)); }
}
