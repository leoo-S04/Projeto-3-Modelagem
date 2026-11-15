# Rastreabilidade

Para cada requisito: o padrão, a classe de cada papel, o teste do cenário e os diagramas no Astah
(pacote `Req NN` no `.asta`; exportados em [`uml/Trabalho03_Diagramas.pdf`](uml/Trabalho03_Diagramas.pdf)).

| Req | Padrão | Papel → Classe | Teste | Diagramas |
|---|---|---|---|---|
| 1 | State | Context `Livro` · State `EstadoLivro` · ConcreteState `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado` | `R01_CicloDeVidaDoLivroTest` | DC Req 01, Máquina de Estados - Livro, SD R01 |
| 2 | Strategy | Strategy `PoliticaDeMulta` · ConcreteStrategy `MultaDiaria`, `MultaComTeto`, `MultaComTolerancia` · Context `ServicoDeMulta` | `R02_PoliticasDeMultaTest` | DC Req 02, SD R02 |
| 3 | Adapter | Target `iCobranca` · Adapter `AdaptadorBancoA`, `AdaptadorBancoB` · Adaptee `ApiBancoA`, `ApiBancoB` · Client `ServicoDeMulta` | `R03_BancosAdapterTest` | DC Req 03, SD R03 |
| 4 | Facade | Facade `iCirculacao` / `ServicoDeCirculacao` · Subsistema `Acervo`, `Movimentacao`, `Relacionamento`, `Livro` · Clientes `TratadorDeEmprestimo`, `TratadorDeDevolucao` | `R04_FachadaDeCirculacaoTest` | DC Req 04, SD R04 |
| 5 | Observer | Subject `PublicadorDeDevolucao` · Observer `OuvinteDeDevolucao` · ConcreteObserver `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail` | `R05_ReacoesADevolucaoTest` | DC Req 05, SD R05 |
| 6 | Command | Command `SolicitacaoDeCirculacao` · ConcreteCommand `SolicitacaoDeReserva`, `SolicitacaoDeRenovacao` · Invoker `ExecutorDeSolicitacoes` · Receiver `iCirculacao` | `R06_SolicitacoesCommandTest` | DC Req 06, SD R06 |
| 7 | Template Method | AbstractClass `TratadorDeCadastro` (método `inserir`) · ConcreteClass `TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante`, `TratadorDeAtendente` | `R07_CadastroTemplateMethodTest` | DC Req 07, SD R07 |
| 8 | Abstract Factory | AbstractFactory `FabricaDePersistencia` · ConcreteFactory `FabricaEmMemoria`, `FabricaJson` · Produtos `UnidadeDeTrabalho` e `Repositorio...` (memória / JSON) | `R08_FamiliasDePersistenciaTest` | DC Req 08, SD R08 |
| 9 | Decorator | Component `ConsultaAcervo` · ConcreteComponent `ConsultaAcervoBasica` · Decorator `DecoradorDeConsulta` · ConcreteDecorator `DecoradorDeMedicao`, `DecoradorDeCache` | `R09_ConsultaComDecoradoresTest` | DC Req 09, SD R09 |
| 10 | Composite | Component `ComponenteDoAcervo` · Leaf `ItemDoAcervo` · Composite `GrupoDoAcervo` · Client `OrganizadorDoAcervo` | `R10_OrganizacaoDoAcervoTest` | DC Req 10, SD R10 |
