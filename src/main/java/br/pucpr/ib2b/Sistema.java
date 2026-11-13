package br.pucpr.ib2b;

import br.pucpr.ib2b.academico.GrupoDeAcademicos;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.Acervo;
import br.pucpr.ib2b.biblioteca.ExecutorDeSolicitacoes;
import br.pucpr.ib2b.biblioteca.RegistroDeSolicitacoes;
import br.pucpr.ib2b.biblioteca.ServicoDeCirculacao;
import br.pucpr.ib2b.biblioteca.iCirculacao;
import br.pucpr.ib2b.biblioteca.Movimentacao;
import br.pucpr.ib2b.biblioteca.Relacionamento;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.persistencia.FabricaDePersistencia;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;
import br.pucpr.ib2b.rh.GrupoDeColaboradores;
import br.pucpr.ib2b.rh.GrupoDePessoas;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;

import java.time.Clock;

/**
 * Montagem do sistema (composition root): recebe a fábrica da família de persistência escolhida UMA vez
 * pelo ConfiguradorDePersistencia e liga todas as peças. Usada pela aplicação web e pelos testes.
 * A interface web só usa os tratadores; Acervo, Movimentacao e Relacionamento ficam expostos apenas
 * para os testes verificarem o estado interno.
 */
public final class Sistema {

    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final iCirculacao circulacao;
    private final ExecutorDeSolicitacoes executor;

    public final TratadorDePessoa tratadorDePessoa;
    public final TratadorDeEstudante tratadorDeEstudante;
    public final TratadorDeAtendente tratadorDeAtendente;
    public final TratadorDeAcervo tratadorDeAcervo;
    public final TratadorDeEmprestimo tratadorDeEmprestimo;
    public final TratadorDeDevolucao tratadorDeDevolucao;

    private Sistema(FabricaDePersistencia fabrica, Clock relogio) {
        // Abstract Factory: uma unidade de trabalho e repositórios da MESMA família
        unidadeDeTrabalho = fabrica.criarUnidadeDeTrabalho();
        var repositorioLivro = fabrica.criarRepositorioLivro(unidadeDeTrabalho);
        var repositorioMovimentacao = fabrica.criarRepositorioMovimentacao(unidadeDeTrabalho);
        var repositorioRelacionamento = fabrica.criarRepositorioRelacionamento(unidadeDeTrabalho);

        // RH e Acadêmico
        tratadorDePessoa = new TratadorDePessoa(new GrupoDePessoas(repositorioRelacionamento), unidadeDeTrabalho);
        tratadorDeEstudante = new TratadorDeEstudante(new GrupoDeAcademicos(repositorioRelacionamento),
                tratadorDePessoa, unidadeDeTrabalho);
        tratadorDeAtendente = new TratadorDeAtendente(new GrupoDeColaboradores(repositorioRelacionamento),
                tratadorDePessoa, unidadeDeTrabalho);

        // Biblioteca: pontos internos de acesso e tratadores
        acervo = new Acervo(repositorioLivro);
        movimentacao = new Movimentacao(repositorioMovimentacao);
        relacionamento = new Relacionamento(tratadorDeEstudante, tratadorDeAtendente);
        tratadorDeAcervo = new TratadorDeAcervo(acervo, unidadeDeTrabalho);

        // Facade (Req. 4): única entrada das operações de circulação
        circulacao = new ServicoDeCirculacao(acervo, movimentacao, relacionamento, relogio, unidadeDeTrabalho);
        // Command (Req. 6): invocador com registro idempotente
        executor = new ExecutorDeSolicitacoes(
                new RegistroDeSolicitacoes(fabrica.criarRepositorioDeSolicitacoes(unidadeDeTrabalho)));

        tratadorDeEmprestimo = new TratadorDeEmprestimo(circulacao, executor, relogio);
        tratadorDeDevolucao = new TratadorDeDevolucao(circulacao);
    }

    public static Sistema montar(FabricaDePersistencia fabrica, Clock relogio) {
        return new Sistema(fabrica, relogio);
    }

    public UnidadeDeTrabalho unidadeDeTrabalho() { return unidadeDeTrabalho; }
    public Acervo acervo() { return acervo; }
    public Movimentacao movimentacao() { return movimentacao; }
    public Relacionamento relacionamento() { return relacionamento; }
    public iCirculacao circulacao() { return circulacao; }
    public ExecutorDeSolicitacoes executor() { return executor; }
}
