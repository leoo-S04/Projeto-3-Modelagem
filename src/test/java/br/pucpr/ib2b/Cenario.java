package br.pucpr.ib2b;

import br.pucpr.ib2b.academico.GrupoDeAcademicos;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.Acervo;
import br.pucpr.ib2b.biblioteca.Movimentacao;
import br.pucpr.ib2b.biblioteca.Relacionamento;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.rh.GrupoDeColaboradores;
import br.pucpr.ib2b.rh.GrupoDePessoas;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Montagem do sistema para os testes, com relógio fixo e dados básicos:
 * pessoas 111 (Ana) e 222 (Bruno), estudante M-100 (Ana), atendente F-01 (Bruno), livro 1 "Dom Casmurro".
 */
public class Cenario {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 13);

    public final Clock relogio;
    public final TratadorDePessoa pessoas;
    public final TratadorDeEstudante estudantes;
    public final TratadorDeAtendente atendentes;
    public final Acervo acervo = new Acervo();
    public final Movimentacao movimentacao = new Movimentacao();
    public final Relacionamento relacionamento;
    public final TratadorDeAcervo tratadorDeAcervo;
    public final TratadorDeEmprestimo tratadorDeEmprestimo;
    public final TratadorDeDevolucao tratadorDeDevolucao;

    public Cenario() { this(HOJE); }

    public Cenario(LocalDate hoje) {
        relogio = Clock.fixed(hoje.atStartOfDay(FUSO).toInstant(), FUSO);
        pessoas = new TratadorDePessoa(new GrupoDePessoas());
        estudantes = new TratadorDeEstudante(new GrupoDeAcademicos(), pessoas);
        atendentes = new TratadorDeAtendente(new GrupoDeColaboradores(), pessoas);
        relacionamento = new Relacionamento(estudantes, atendentes);
        tratadorDeAcervo = new TratadorDeAcervo(acervo);
        tratadorDeEmprestimo = new TratadorDeEmprestimo(acervo, movimentacao, relacionamento, relogio);
        tratadorDeDevolucao = new TratadorDeDevolucao(acervo, movimentacao, relacionamento, relogio);

        pessoas.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        pessoas.inserirPessoa("222", "Bruno", "Alves", "bruno@pucpr.br");
        estudantes.inserirEstudante("111", "M-100");
        atendentes.inserirAtendente("222", "F-01");
        tratadorDeAcervo.inserirLivro(1, "Dom Casmurro");
    }
}
