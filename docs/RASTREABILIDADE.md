# Rastreabilidade

Exigência do Trabalho 03: para cada requisito, o padrão, as classes que desempenham cada papel e o teste que
comprova o cenário de verificação. Preenchida ao final de cada etapa.

Legenda: ✅ implementado e testado · ⬜ pendente (etapa prevista)

## Requisitos do Trabalho 03 (padrões GoF)

| Req | Padrão | Papel GoF → Classe | Teste do cenário | Situação |
|---|---|---|---|---|
| 1 | State | Context → `Livro`; State → `EstadoLivro`; ConcreteState → `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado`; Clientes → `TratadorDeEmprestimo`, `TratadorDeDevolucao`, `TratadorDeAcervo` | `R01_CicloDeVidaDoLivroTest`: `R01_terceiraRenovacaoRecusadaSemAlterarVencimento`, `R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado` (+ `EstadoLivroTest`) | ✅ |
| 2 | Strategy | Strategy → `PoliticaDeMulta`; ConcreteStrategy → `MultaDiaria`, `MultaComTeto`, `MultaComTolerancia`; Context → `ServicoDeMulta`, `ServicoDeSimulacao`; seleção → `SeletorDePolitica`, `ConfiguracaoAdministrativa`; resultado → `Multa` | `R02_PoliticasDeMultaTest`: `R02_cincoDiasDeAtrasoValemCincoReaisNaOriginalETresNaTolerancia`, `R02_semAtrasoOValorEZero` (+ troca de política não recalcula) | ✅ |
| 3 | Adapter | Target → `iCobranca` (saída), `iQuitacao` (entrada); Adapter → `AdaptadorBancoA`, `AdaptadorBancoB`; Adaptee → `ApiBancoA`, `ApiBancoB`; Client → `ServicoDeMulta`; idempotência → `ServicoDeQuitacao` | `R03_BancosAdapterTest`: `R03_mesmaSolicitacaoDeSeteReaisViraSetecentosCentavosNoBancoAESeteNoBancoB`, `R03_notificacaoRepetidaNaoGeraSegundaQuitacao` | ✅ |
| 4 | Facade | Facade → `iCirculacao` / `ServicoDeCirculacao`; subsistema → `Relacionamento`, `Movimentacao`, `Acervo`, `Livro`; clientes → `TratadorDeEmprestimo`, `TratadorDeDevolucao`; DTOs → `ComprovanteReserva`, `ComprovanteEmprestimo`, `ComprovanteRenovacao`, `ComprovanteDevolucao`, `SituacaoLivroDTO` | `R04_FachadaDeCirculacaoTest`: `R04_efetivarEmprestimoValidaReservaRemoveReservaCriaEmprestimoEAtualizaLivroSemExporColecoes` (+ falha no meio desfaz tudo) | ✅ |
| 5 | Observer | Subject → `PublicadorDeDevolucao`; Observer → `OuvinteDeDevolucao`; ConcreteObserver → `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail`; evento → `EventoDevolucao`; disparo → `TratadorDeDevolucao` | `R05_ReacoesADevolucaoTest`: `R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores`, `R05_devolucaoNoPrazoNaoGeraMulta`, `R05_livroPermaneceDevolvidoAteAInspecao` (+ falha do e-mail) | ✅ |
| 6 | Command | Command → `SolicitacaoDeCirculacao`; ConcreteCommand → `SolicitacaoDeReserva`, `SolicitacaoDeRenovacao`; Invoker → `ExecutorDeSolicitacoes` (+ `RegistroDeSolicitacoes`); Receiver → `iCirculacao`; Client → `TratadorDeEmprestimo` | `R06_SolicitacoesCommandTest`: `R06_mesmaRenovacaoExecutadaDuasVezesComOMesmoIdAcrescentaApenasTrintaDias`, `R06_solicitacoesDiferentesContinuamLimitadasADuasRenovacoes` (+ submissões simultâneas) | ✅ |
| 7 | Template Method | AbstractClass → `TratadorDeCadastro<T, D>` (método-modelo `inserir`, final; gancho `verificarPreRequisitos`); ConcreteClass → `TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante`, `TratadorDeAtendente`; resultado → `ResultadoCadastro` | `R07_CadastroTemplateMethodTest`: `R07_estudanteDuplicadoRetornaOExistente`, `R07_estudanteComCpfNaoCadastradoFalhaAntesDeQualquerGravacao` | ✅ |
| 8 | Abstract Factory | AbstractFactory → `FabricaDePersistencia`; ConcreteFactory → `FabricaEmMemoria`, `FabricaJson`; AbstractProduct → `UnidadeDeTrabalho`, `RepositorioLivro`, `RepositorioMovimentacao`, `RepositorioRelacionamento`; ConcreteProduct → `...EmMemoria`, `...Json`; escolha → `ConfiguradorDePersistencia`; cliente → `Sistema` (Acervo, Movimentacao, grupos) | `R08_FamiliasDePersistenciaTest`: `R08_configuracaoDeTesteFornecesSoObjetosEmMemoria`, `R08_configuracaoDeProducaoFornecesRepositoriosJsonQueCompartilhamAMesmaUnidade`, `R08_desfazerAposFalhaDeixaOArquivoInalterado`, `R08_naoEPermitidoMisturarFamilias` | ✅ |
| 9 | Decorator | Component → `ConsultaAcervo`; ConcreteComponent → `ConsultaAcervoBasica`; Decorator → `DecoradorDeConsulta`; ConcreteDecorator → `DecoradorDeMedicao`, `DecoradorDeCache`; montagem → `MontadorDeConsulta`; cliente → `TratadorDeAcervo.buscarMetadados` | `R09_ConsultaComDecoradoresTest`: `R09_segundaConsultaDeMetadadosUsaCacheEAindaGeraMedicao`, `R09_cacheAntigoNuncaAutorizaReservarLivroQueDeixouDeEstarLivre` (+ falha do cache) | ✅ |
| 10 | Composite | Component → `ComponenteDoAcervo`; Leaf → `ItemDoAcervo`; Composite → `GrupoDoAcervo` (+ `TipoDeGrupo`); Client → `OrganizadorDoAcervo` (via `TratadorDeAcervo`) | `R10_OrganizacaoDoAcervoTest`: `R10_unidadeComDuasSecoesDeTresEDoisLivrosInformaCinco`, `R10_moverLivroEntreSecoesNaoAlteraIdEstadoNemEmprestimo` (+ ciclos e duplicidade) | ✅ |

## Base preservada dos Trabalhos 01 e 02 (Etapa 0)

| Regra de negócio | Componente / pacote | Classes | Teste | Situação |
|---|---|---|---|---|
| 1.1–1.4 Manter Livro | Biblioteca / `biblioteca` | `iLivro`, `TratadorDeAcervo`, `Acervo`, `Livro`, `Situacao` | `TratadorDeAcervoTest` | ✅ |
| 2.1–2.4 Manter Pessoa | RH / `rh` | `iPessoa`, `TratadorDePessoa`, `GrupoDePessoas`, `Pessoa` | `TratadorDePessoaTest` | ✅ |
| 3.1–3.4 Manter Estudante | Academico / `academico` | `iEstudante`, `TratadorDeEstudante`, `GrupoDeAcademicos`, `Estudante`, `Papel` | `TratadorDeEstudanteTest` | ✅ |
| 4.1–4.4 Manter Atendente | RH / `rh` | `iAtendente`, `TratadorDeAtendente`, `GrupoDeColaboradores`, `Atendente`, `Papel` | `TratadorDeAtendenteTest` | ✅ |
| 5 Reservar livro (f1) | Biblioteca | `iEmprestimo`, `TratadorDeEmprestimo`, `Livro` + estados, `Reserva` | `R01_...`, `EstadoLivroTest` | ✅ |
| 6 Consultar reserva (f2) | Biblioteca | `TratadorDeEmprestimo`, `Movimentacao`, `Reserva` | `TratadorDeEmprestimoTest`, `MovimentacaoTest` | ✅ |
| 7 Emprestar livro (f3) | Biblioteca | `TratadorDeEmprestimo`, `Reservado`, `Emprestimo` | `R01_...`, `EstadoLivroTest` | ✅ |
| 8 Renovar empréstimo (f4) | Biblioteca | `TratadorDeEmprestimo`, `Emprestado`, `Renovado` | `R01_terceiraRenovacaoRecusadaSemAlterarVencimento` | ✅ |
| 9 Registrar devolução (f5) e inspeção | Biblioteca | `iDevolucao`, `TratadorDeDevolucao`, `Devolvido`, `Reparando`, `Devolucao` | `TratadorDeDevolucaoTest`, `DevolucaoTest` | ✅ |
| 10 Emitir boleto de multa (f6) | Biblioteca + Financeiro | `TratadorDeDevolucao.emitirBoletoDeMulta`, `iMulta`, `TratadorDeMulta`, `BoletoDeMulta` | `R05_ReacoesADevolucaoTest.f6EmiteSegundaViaDoMesmoBoleto` | ✅ |
| Pontos internos de acesso (T02) | Biblioteca | `Acervo`, `Relacionamento`, `Movimentacao` | `TratadorDeEmprestimoTest`, `MovimentacaoTest` | ✅ |

## Interface web e critérios de pronto (Etapa 6)

| Item | Classes | Teste | Situação |
|---|---|---|---|
| Fluxo reserva → empréstimo → renovação → devolução atrasada → multa → pagamento, pela web | `web.*Controller`, templates Thymeleaf, `ConfiguracaoDoSistema` | `FluxoCompletoWebTest.fluxoReservaEmprestimoRenovacaoDevolucaoAtrasadaMultaEPagamento` | ✅ |
| API JSON com recusa de regra (HTTP 422) | `ApiController` | `FluxoCompletoWebTest.apiJsonRecusaComMensagemDoDominio` | ✅ |
| A interface não referencia Movimentacao, Acervo, Relacionamento | pacote `web` | `ArquiteturaTest.interfaceWebNaoReferenciaMovimentacaoAcervoRelacionamento` | ✅ |
| Nenhum tratador testa a situação do Livro | `Tratador*` | `ArquiteturaTest.nenhumTratadorTestaASituacaoDoLivro` | ✅ |
| Serviço de multas sem `if` por banco ou política | `ServicoDeMulta` | `ArquiteturaTest.servicoDeMultasNaoTemCondicionalPorBancoNemPorPolitica` | ✅ |
| Um `biblioteca.jar`, `biblioteca.json`, bancos simulados | `pom.xml` (spring-boot-maven-plugin), `UnidadeDeTrabalhoJson`, `financeiro.bancos` | `R08_...`, `R03_...`; execução real do jar | ✅ |

## Modelos UML (Astah) × requisitos

Cada requisito tem, no `.asta`, um pacote `Req NN - <Padrão>` com um diagrama de classes (DC) e um de sequência (SD)
do cenário de verificação. Exportados em `docs/uml/Trabalho03_Diagramas.pdf`. As classes dos diagramas são lidas do
código compilado (pacote `Trabalho 03 - Implementacao`), então nomes, atributos e operações coincidem com o código.

| Req | Diagrama de classes | Diagrama de sequência (cenário) | Teste |
|---|---|---|---|
| 1 | DC Req 01 - State (ciclo de vida do Livro) + Maquina de Estados - Livro | SD R01 - Terceira renovacao recusada | `R01_CicloDeVidaDoLivroTest` |
| 2 | DC Req 02 - Strategy (politicas de multa) | SD R02 - Politicas de multa para 5 dias e sem atraso | `R02_PoliticasDeMultaTest` |
| 3 | DC Req 03 - Adapter (bancos A e B) | SD R03 - R$ 7,00 nos bancos A e B | `R03_BancosAdapterTest` |
| 4 | DC Req 04 - Facade (circulacao) | SD R04 - Efetivar emprestimo pela fachada | `R04_FachadaDeCirculacaoTest` |
| 5 | DC Req 05 - Observer (reacoes a devolucao) | SD R05 - Devolucao atrasada notifica os ouvintes | `R05_ReacoesADevolucaoTest` |
| 6 | DC Req 06 - Command (solicitacoes de circulacao) | SD R06 - Mesma renovacao enviada duas vezes | `R06_SolicitacoesCommandTest` |
| 7 | DC Req 07 - Template Method (cadastros) | SD R07 - Cadastro de estudante (metodo-modelo) | `R07_CadastroTemplateMethodTest` |
| 8 | DC Req 08 - Abstract Factory (familias de persistencia) | SD R08 - Familias de teste e de producao | `R08_FamiliasDePersistenciaTest` |
| 9 | DC Req 09 - Decorator (consulta ao acervo) | SD R09 - Segunda consulta vem do cache e e medida | `R09_ConsultaComDecoradoresTest` |
| 10 | DC Req 10 - Composite (organizacao do acervo) | SD R10 - Contar livros e mover entre secoes | `R10_OrganizacaoDoAcervoTest` |
