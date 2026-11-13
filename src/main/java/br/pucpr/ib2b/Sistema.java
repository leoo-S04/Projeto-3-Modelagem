package br.pucpr.ib2b;

import br.pucpr.ib2b.academico.GrupoDeAcademicos;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.*;
import br.pucpr.ib2b.comum.ServicoDeEmailSimulado;
import br.pucpr.ib2b.financeiro.*;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoA;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoB;
import br.pucpr.ib2b.persistencia.FabricaDePersistencia;
import br.pucpr.ib2b.persistencia.RepositorioFinanceiro;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;
import br.pucpr.ib2b.rh.GrupoDeColaboradores;
import br.pucpr.ib2b.rh.GrupoDePessoas;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import br.pucpr.ib2b.biblioteca.MontadorDeConsulta.ConfigInstalacao;

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

    // Financeiro e bancos simulados (nós externos do diagrama de implantação, simulados dentro do jar)
    private final ApiBancoA apiBancoA = new ApiBancoA();
    private final ApiBancoB apiBancoB = new ApiBancoB();
    private final AdaptadorBancoA adaptadorBancoA;
    private final AdaptadorBancoB adaptadorBancoB;
    private final ServicoDeQuitacao servicoDeQuitacao;

    // Observer: publicador e apoio dos ouvintes
    private final PublicadorDeDevolucao publicador = new PublicadorDeDevolucao();
    private final AgendaDeInspecao agendaDeInspecao = new AgendaDeInspecao();
    private final PainelDeIndicadores painelDeIndicadores = new PainelDeIndicadores();
    private final ServicoDeEmailSimulado email = new ServicoDeEmailSimulado();

    // Decorator: destino das medições da consulta pública
    private final RegistroDeMetricas registroDeMetricas = new RegistroDeMetricas();

    public final TratadorDePessoa tratadorDePessoa;
    public final TratadorDeEstudante tratadorDeEstudante;
    public final TratadorDeAtendente tratadorDeAtendente;
    public final TratadorDeAcervo tratadorDeAcervo;
    public final TratadorDeEmprestimo tratadorDeEmprestimo;
    public final TratadorDeDevolucao tratadorDeDevolucao;
    public final TratadorDeMulta tratadorDeMulta;

    private Sistema(FabricaDePersistencia fabrica, Clock relogio, ConfigInstalacao instalacao) {
        // Abstract Factory (Req. 8): uma unidade de trabalho e repositórios da MESMA família
        unidadeDeTrabalho = fabrica.criarUnidadeDeTrabalho();
        var repositorioLivro = fabrica.criarRepositorioLivro(unidadeDeTrabalho);
        var repositorioMovimentacao = fabrica.criarRepositorioMovimentacao(unidadeDeTrabalho);
        var repositorioRelacionamento = fabrica.criarRepositorioRelacionamento(unidadeDeTrabalho);
        RepositorioFinanceiro repositorioFinanceiro = fabrica.criarRepositorioFinanceiro(unidadeDeTrabalho);

        // RH e Acadêmico (Template Method, Req. 7)
        tratadorDePessoa = new TratadorDePessoa(new GrupoDePessoas(repositorioRelacionamento), unidadeDeTrabalho);
        tratadorDeEstudante = new TratadorDeEstudante(new GrupoDeAcademicos(repositorioRelacionamento),
                tratadorDePessoa, unidadeDeTrabalho);
        tratadorDeAtendente = new TratadorDeAtendente(new GrupoDeColaboradores(repositorioRelacionamento),
                tratadorDePessoa, unidadeDeTrabalho);

        // Financeiro: Strategy (Req. 2) — políticas registradas no seletor
        SeletorDePolitica seletor = new SeletorDePolitica(repositorioFinanceiro,
                List.of(new MultaDiaria(), new MultaComTeto(), new MultaComTolerancia()));
        // Adapter (Req. 3): um adaptador por banco; o banco da instalação vem da configuração (sem if por banco)
        servicoDeQuitacao = new ServicoDeQuitacao(repositorioFinanceiro);
        adaptadorBancoA = new AdaptadorBancoA(apiBancoA, servicoDeQuitacao);
        adaptadorBancoB = new AdaptadorBancoB(apiBancoB, servicoDeQuitacao);
        Map<String, iCobranca> bancos = Map.of("A", adaptadorBancoA, "B", adaptadorBancoB);
        iCobranca bancoDaInstalacao = bancos.get(repositorioFinanceiro.configuracao().getBancoEmUso());
        ServicoDeMulta servicoDeMulta = new ServicoDeMulta(seletor, bancoDaInstalacao, repositorioFinanceiro,
                tratadorDePessoa, relogio);
        tratadorDeMulta = new TratadorDeMulta(servicoDeMulta, servicoDeQuitacao, new ServicoDeSimulacao(seletor),
                seletor, repositorioFinanceiro);

        // Biblioteca: pontos internos de acesso
        acervo = new Acervo(repositorioLivro);
        movimentacao = new Movimentacao(repositorioMovimentacao);
        relacionamento = new Relacionamento(tratadorDeEstudante, tratadorDeAtendente);
        // Composite (Req. 10): organização hierárquica; Decorator (Req. 9): cadeia montada conforme a instalação
        OrganizadorDoAcervo organizador = new OrganizadorDoAcervo(fabrica.criarRepositorioDeOrganizacao(unidadeDeTrabalho), acervo);
        ConsultaAcervo consulta = MontadorDeConsulta.montar(instalacao, new ConsultaAcervoBasica(acervo),
                ArmazenamentoDeCache.emMemoria(), registroDeMetricas, relogio);
        tratadorDeAcervo = new TratadorDeAcervo(acervo, unidadeDeTrabalho, organizador, consulta);

        // Facade (Req. 4): única entrada das operações de circulação
        circulacao = new ServicoDeCirculacao(acervo, movimentacao, relacionamento, relogio, unidadeDeTrabalho);
        // Command (Req. 6): invocador com registro idempotente
        executor = new ExecutorDeSolicitacoes(
                new RegistroDeSolicitacoes(fabrica.criarRepositorioDeSolicitacoes(unidadeDeTrabalho)));

        // Observer (Req. 5): ouvintes registrados na montagem — o tratador não os conhece
        publicador.registrar(new OuvinteInspecao(agendaDeInspecao));
        publicador.registrar(new OuvinteMulta(tratadorDeMulta));
        publicador.registrar(new OuvinteIndicadores(painelDeIndicadores));
        publicador.registrar(new OuvinteEmail(email));

        tratadorDeEmprestimo = new TratadorDeEmprestimo(circulacao, executor, relogio);
        tratadorDeDevolucao = new TratadorDeDevolucao(circulacao, publicador, tratadorDeMulta, agendaDeInspecao, relogio);
    }

    public static Sistema montar(FabricaDePersistencia fabrica, Clock relogio) {
        return new Sistema(fabrica, relogio, ConfigInstalacao.completa());
    }

    /** Montagem com a combinação de recursos da consulta escolhida pela instalação (medição, cache, TTL). */
    public static Sistema montar(FabricaDePersistencia fabrica, Clock relogio, ConfigInstalacao instalacao) {
        return new Sistema(fabrica, relogio, instalacao);
    }

    public UnidadeDeTrabalho unidadeDeTrabalho() { return unidadeDeTrabalho; }
    public Acervo acervo() { return acervo; }
    public Movimentacao movimentacao() { return movimentacao; }
    public Relacionamento relacionamento() { return relacionamento; }
    public iCirculacao circulacao() { return circulacao; }
    public ExecutorDeSolicitacoes executor() { return executor; }

    public PublicadorDeDevolucao publicador() { return publicador; }
    public AgendaDeInspecao agendaDeInspecao() { return agendaDeInspecao; }
    public PainelDeIndicadores painelDeIndicadores() { return painelDeIndicadores; }
    public ServicoDeEmailSimulado email() { return email; }

    public ApiBancoA apiBancoA() { return apiBancoA; }
    public ApiBancoB apiBancoB() { return apiBancoB; }
    public AdaptadorBancoA adaptadorBancoA() { return adaptadorBancoA; }
    public AdaptadorBancoB adaptadorBancoB() { return adaptadorBancoB; }
    public ServicoDeQuitacao servicoDeQuitacao() { return servicoDeQuitacao; }
    public RegistroDeMetricas registroDeMetricas() { return registroDeMetricas; }
}
