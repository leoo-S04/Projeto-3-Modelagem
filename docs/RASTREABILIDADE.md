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
