package br.pucpr.ib2b;

import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.Acervo;
import br.pucpr.ib2b.biblioteca.Movimentacao;
import br.pucpr.ib2b.biblioteca.Relacionamento;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.persistencia.ConfiguradorDePersistencia;
import br.pucpr.ib2b.persistencia.Perfil;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Montagem do sistema para os testes: perfil TESTE (família em memória), relógio ajustável começando em
 * 13/11/2026 e dados básicos: pessoas 111 (Ana) e 222 (Bruno), estudante M-100 (Ana), atendente F-01 (Bruno),
 * livro 1 "Dom Casmurro".
 */
public class Cenario {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 13);

    public final RelogioAjustavel relogio = RelogioAjustavel.fixoEm(HOJE, FUSO);
    public final Sistema sistema = Sistema.montar(ConfiguradorDePersistencia.fabricaPara(Perfil.TESTE, null), relogio);

    public final TratadorDePessoa pessoas = sistema.tratadorDePessoa;
    public final TratadorDeEstudante estudantes = sistema.tratadorDeEstudante;
    public final TratadorDeAtendente atendentes = sistema.tratadorDeAtendente;
    public final TratadorDeAcervo tratadorDeAcervo = sistema.tratadorDeAcervo;
    public final TratadorDeEmprestimo tratadorDeEmprestimo = sistema.tratadorDeEmprestimo;
    public final TratadorDeDevolucao tratadorDeDevolucao = sistema.tratadorDeDevolucao;

    public Cenario() {
        pessoas.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        pessoas.inserirPessoa("222", "Bruno", "Alves", "bruno@pucpr.br");
        estudantes.inserirEstudante("111", "M-100");
        atendentes.inserirAtendente("222", "F-01");
        tratadorDeAcervo.inserirLivro(1, "Dom Casmurro");
    }

    public Acervo acervo() { return sistema.acervo(); }
    public Movimentacao movimentacao() { return sistema.movimentacao(); }
    public Relacionamento relacionamento() { return sistema.relacionamento(); }

    /** Reserva e empresta o livro 1 para M-100 hoje (vencimento = hoje + 30). */
    public void emprestarLivro1() {
        tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01");
    }
}
