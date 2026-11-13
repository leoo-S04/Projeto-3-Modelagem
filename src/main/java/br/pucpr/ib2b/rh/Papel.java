package br.pucpr.ib2b.rh;

/**
 * Papel assumido por uma Pessoa (Astah: classe Papel). Generalização de Estudante e Atendente:
 * "todo papel tem um id" e está ligado a uma pessoa já cadastrada.
 */
public abstract class Papel {

    private final int id;
    private Pessoa pessoa;

    protected Papel(int id, Pessoa pessoa) {
        this.id = id;
        this.pessoa = pessoa;
    }

    public int getId() { return id; }
    public Pessoa getPessoa() { return pessoa; }

    /** Regras 3.2 e 4.2 (atualizar estudante/atendente): liga o papel a outra pessoa cadastrada. */
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }
}
