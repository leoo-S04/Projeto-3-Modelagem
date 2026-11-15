# Sistema de Biblioteca com Padrões GoF — Trabalho 03

PUCPR · Modelagem de Sistemas Computacionais · Prof. Edson Emilio Scalabrin · 2º semestre de 2026

Cadastro, reserva, empréstimo, renovação e devolução de livros (Trabalhos 01 e 02), com os **dez padrões GoF** do
Trabalho 03. Cada padrão tem um teste com o cenário do enunciado (`R01_` … `R10_`).

## Como rodar

Pré-requisito: JDK 17 ou superior. O Maven Wrapper baixa o Maven na primeira execução (precisa de internet).

| O quê | Windows | Linux / macOS |
|---|---|---|
| **Compilar e rodar todos os testes** | `mvnw.cmd test` | `./mvnw test` |
| Rodar a aplicação web | `mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| Gerar e rodar o jar | `mvnw.cmd package` e `java -jar target\biblioteca.jar` | `./mvnw package` e `java -jar target/biblioteca.jar` |

Abra **http://localhost:8080**. Na primeira execução, `dados/biblioteca.json` é criado a partir de
`dados/exemplo.json`; para recomeçar com os dados de exemplo, pare a aplicação e apague `dados/biblioteca.json`.
Os testes usam só objetos em memória (não mexem no arquivo).

## Persistência

Por orientação do professor em sala, a persistência é um **arquivo JSON local** (`dados/biblioteca.json`) em vez de
um banco relacional. As duas famílias pedidas no Req. 8 existem: **memória** (testes) e **JSON** (produção), criadas
pela mesma fábrica abstrata. Cada operação grava o arquivo inteiro de uma vez; se algo falhar no meio, nada é gravado.

## Organização do código (pacotes = componentes do diagrama)

| Pacote `br.pucpr.ib2b.` | Componente |
|---|---|
| `rh` | RH: pessoas e atendentes |
| `academico` | Acadêmico: estudantes |
| `biblioteca` | Biblioteca: livros, circulação, acervo |
| `financeiro` (`bancos`) | Financeiro: multas e boletos; bancos A e B simulados |
| `comum`, `persistencia` | Apoio: rotina de cadastro, exceções, arquivo JSON |
| `web` | Interface web: só chama os tratadores |

## Os dez padrões

| Req | Padrão | Problema | Classe principal | Teste |
|---|---|---|---|---|
| 1 | State | Cada situação do livro aceita eventos diferentes | `Livro` → `EstadoLivro` (`Livre`, `Reservado`, …) | `R01_CicloDeVidaDoLivroTest` |
| 2 | Strategy | Várias regras de cálculo de multa | `PoliticaDeMulta` (`MultaDiaria`, `MultaComTolerancia`, `MultaComTeto`) | `R02_PoliticasDeMultaTest` |
| 3 | Adapter | Bancos com APIs diferentes | `iCobranca` → `AdaptadorBancoA`, `AdaptadorBancoB` | `R03_BancosAdapterTest` |
| 4 | Facade | Empréstimo mexe em várias classes | `ServicoDeCirculacao` (`iCirculacao`) | `R04_FachadaDeCirculacaoTest` |
| 5 | Observer | Devolução dispara várias reações | `PublicadorDeDevolucao` → ouvintes | `R05_ReacoesADevolucaoTest` |
| 6 | Command | Mesmo pedido enviado duas vezes | `SolicitacaoDeRenovacao`, `ExecutorDeSolicitacoes` | `R06_SolicitacoesCommandTest` |
| 7 | Template Method | Cadastros seguem os mesmos passos | `TratadorDeCadastro.inserir` | `R07_CadastroTemplateMethodTest` |
| 8 | Abstract Factory | Memória nos testes, JSON na produção | `FabricaDePersistencia` → `FabricaEmMemoria`, `FabricaJson` | `R08_FamiliasDePersistenciaTest` |
| 9 | Decorator | Medição e cache opcionais na consulta | `ConsultaAcervo` → `DecoradorDeMedicao`, `DecoradorDeCache` | `R09_ConsultaComDecoradoresTest` |
| 10 | Composite | Acervo em unidades e seções | `ComponenteDoAcervo` → `GrupoDoAcervo`, `ItemDoAcervo` | `R10_OrganizacaoDoAcervoTest` |

Papéis de cada classe: [`docs/RASTREABILIDADE.md`](docs/RASTREABILIDADE.md).
Diagramas UML (classes de cada padrão, máquina de estados do Livro, sequência dos cenários): no `.asta` e em
[`docs/uml/Trabalho03_Diagramas.pdf`](docs/uml/Trabalho03_Diagramas.pdf).

## Demonstração (5 min)

Dados de exemplo: estudantes **M-100** (Ana) e **M-300** (Caio), atendente **F-01**, livros 1 a 6.

1. Rodar `mvnw.cmd test`: os 10 cenários passam.
2. **Cadastros:** estudante com CPF inexistente é recusado; estudante repetido devolve o existente (Template Method).
3. **Circulação:** reservar o livro 1 para M-100, emprestar, renovar 2 vezes; a 3ª renovação é recusada (State, Facade, Command).
4. **Administração:** avançar o relógio 95 dias (após 2 renovações o livro vence em 90). **Circulação:** devolver o
   livro 1 → 5 dias de atraso, multa, inspeção e e-mail (Observer).
5. **Financeiro:** boleto de R$ 5,00 no banco A (700 centavos); pagar; reenviar a notificação não paga duas vezes (Strategy, Adapter).
6. **Acervo:** buscar "dom" duas vezes (cache e medição — Decorator); mover um livro de seção, a contagem continua certa (Composite).

## Uso de IA

Ferramenta **Claude Code** com o modelo **Claude Opus 5.5** (Anthropic), usado em todas as partes: código, testes,
interface web, diagramas UML (gerados no Astah pela API, a partir do código — `ferramentas/astah/`) e documentação.
Os pedidos e o que a equipe aceitou, corrigiu ou descartou estão em [`REGISTRO_LLM.md`](REGISTRO_LLM.md).
