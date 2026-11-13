package br.pucpr.ib2b.rh;

/** Regras 2.1 a 2.4 (manter Pessoa). Delega o armazenamento a GrupoDePessoas. */
public class TratadorDePessoa implements iPessoa {

    private final GrupoDePessoas grupoDePessoas;

    public TratadorDePessoa(GrupoDePessoas grupoDePessoas) {
        this.grupoDePessoas = grupoDePessoas;
    }

    /** 2.1: se já cadastrada, devolve a existente; senão cria e insere. */
    @Override
    public Pessoa inserirPessoa(String cpf, String nome, String sobrenome, String email) {
        Pessoa existente = grupoDePessoas.buscarPorCpf(cpf);
        if (existente != null) return existente;
        Pessoa nova = new Pessoa(cpf, nome, sobrenome, email);
        grupoDePessoas.adicionar(nova);
        return nova;
    }

    /** 2.3: devolve a pessoa, ou {@code null} se não cadastrada. */
    @Override
    public Pessoa consultarPessoa(String cpf) {
        return grupoDePessoas.buscarPorCpf(cpf);
    }

    /** 2.2: só atualiza se cadastrada. */
    @Override
    public boolean atualizarPessoa(String cpf, String nome, String sobrenome, String email) {
        Pessoa p = grupoDePessoas.buscarPorCpf(cpf);
        if (p == null) return false;
        p.atualizarDados(nome, sobrenome, email);
        return true;
    }

    /** 2.4: só exclui se cadastrada (integridade referencial fora do escopo, como no enunciado). */
    @Override
    public boolean excluirPessoa(String cpf) {
        return grupoDePessoas.remover(cpf);
    }
}
