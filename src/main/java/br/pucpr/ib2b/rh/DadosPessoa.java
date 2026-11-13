package br.pucpr.ib2b.rh;

/** Dados do formulário de cadastro de pessoa (entrada do método-modelo inserir). */
public record DadosPessoa(String cpf, String nome, String sobrenome, String email) { }
