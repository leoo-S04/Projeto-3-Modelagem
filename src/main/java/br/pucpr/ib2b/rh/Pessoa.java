package br.pucpr.ib2b.rh;

/**
 * Pessoa usuária da biblioteca (Astah: classe Pessoa). Identificada pelo CPF.
 * Pode assumir os papéis de Estudante ou de Atendente (associação "assume").
 */
public class Pessoa {

    private final String cpf;   // identifica a pessoa; não muda depois do cadastro
    private String nome;
    private String sobrenome;
    private String email;

    public Pessoa(String cpf, String nome, String sobrenome, String email) {
        this.cpf = cpf;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
    }

    /** Regra 2.2 (atualizar pessoa): muda os dados, nunca o CPF. */
    public void atualizarDados(String nome, String sobrenome, String email) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getSobrenome() { return sobrenome; }
    public String getEmail() { return email; }
}
