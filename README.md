# Sistema de Biblioteca com Padrões GoF — Trabalho 03

PUCPR · Modelagem de Sistemas Computacionais · Prof. Edson Emilio Scalabrin · 2º semestre de 2026

Versão executável do sistema de cadastro, reserva, empréstimo, renovação e devolução de livros dos Trabalhos 01 e
02, aplicando os **dez padrões GoF** pedidos no Trabalho 03. Cada padrão tem um teste automatizado com o cenário de
verificação do enunciado (`R01_` … `R10_`) e aparece na interface web.

---

## 1. Como compilar, testar e rodar

**Pré-requisitos:** JDK 17 ou superior (o código é compilado para Java 17). Não é preciso instalar o Maven: o
Maven Wrapper (`mvnw`) baixa o Maven 3.9.9 e as dependências **na primeira execução** (precisa de internet nessa vez).

| O quê | Windows | Linux / macOS |
|---|---|---|
| **Compilar e rodar todos os testes** (comando único) | `mvnw.cmd test` | `./mvnw test` |
| Rodar a aplicação web | `mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| Gerar o `biblioteca.jar` executável | `mvnw.cmd package` | `./mvnw package` |
| Rodar o jar gerado | `java -jar target\biblioteca.jar` | `java -jar target/biblioteca.jar` |

Depois de iniciar, abra **http://localhost:8080**.

- Na primeira execução, `dados/biblioteca.json` não existe e é criado a partir de `dados/exemplo.json` (pessoas,
  estudantes, atendente, 6 livros e a organização do acervo). Para **recomeçar do zero com os dados de exemplo**,
  pare a aplicação e apague `dados/biblioteca.json`.
- `mvnw test` usa só a família **em memória**: não lê nem grava o arquivo real.
- **HTTPS (como no diagrama de implantação):** gere um certificado de teste uma vez e use o perfil `https`
  (porta 8443; o navegador avisará que o certificado é autoassinado):
  ```
  keytool -genkeypair -alias biblioteca -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore dados/biblioteca.p12 -storepass biblioteca -validity 365 -dname "CN=localhost"
  java -jar target/biblioteca.jar --spring.profiles.active=https
  ```
  e abra **https://localhost:8443**.

Configurações em `src/main/resources/application.properties`: `biblioteca.perfil` (`PRODUCAO` = arquivo JSON;
`TESTE` = memória), `biblioteca.arquivo` e `biblioteca.exemplo`.

---

## 2. Arquitetura: pacotes = componentes

Um único artefato, `target/biblioteca.jar` (Spring Boot com **Tomcat embutido**), roda na JVM 17 e lê/grava
`dados/biblioteca.json` — exatamente o diagrama de implantação. Os pacotes correspondem aos componentes do
diagrama de componentes:

| Pacote `br.pucpr.ib2b.` | Componente | Conteúdo principal |
|---|---|---|
| `rh` | RH | Pessoa, Papel, Atendente, grupos e tratadores (`iPessoa`, `iAtendente`) |
| `academico` | Acadêmico | Estudante, grupo e tratador (`iEstudante`) |
| `biblioteca` | Biblioteca | Livro e seus estados, Acervo, Movimentacao, Relacionamento, transações, fachada de circulação, solicitações, ouvintes da devolução, organização e consulta do acervo; tratadores (`iLivro`, `iEmprestimo`, `iDevolucao`) |
| `financeiro` | Financeiro | Políticas de multa, Multa, BoletoDeMulta, adaptadores dos bancos, quitação; `TratadorDeMulta` (`iMulta`, `iQuitacao`) |
| `financeiro.bancos` | Banco «external» (simulado) | APIs simuladas e incompatíveis dos bancos A e B |
| `web` | Interface Web | Telas Thymeleaf e API JSON; **só chamam os tratadores** |
| `comum`, `persistencia` | (infraestrutura dentro do jar) | Template Method, exceções, relógio; Abstract Factory e as famílias memória/JSON |
| (raiz) `Sistema` | — | Montagem: liga todas as peças a partir da fábrica escolhida |

Regras que o código respeita (e que `ArquiteturaTest` verifica):
- a camada web não referencia `Acervo`, `Movimentacao` nem `Relacionamento` e não tem regra de negócio;
- nenhum tratador testa a situação do livro com `if`/`switch` (State);
- o serviço de multas não tem `if` por banco nem por política (Adapter, Strategy);
- o domínio e os padrões são Java puro: o Spring só existe no pacote `web`.

---

## 3. Os dez padrões, em linguagem simples

Cada seção: o **problema**, as **classes** (papel GoF → classe) e o **fluxo**. Guias completos, com o passo a passo
e as respostas da arguição, em `docs/estudo/ETAPA01..05_*.md`.

### Req. 1 — State: ciclo de vida do livro
**Problema:** o que se pode fazer com um livro depende do estado (LIVRE, RESERVADO, EMPRESTADO, RENOVADO, DEVOLVIDO,
REPARANDO, INCINERADO); com `if`s nos tratadores a regra se espalha.
**Classes:** Context `Livro`; State `EstadoLivro`; ConcreteState `Livre`, `Reservado`, `Emprestado`, `Renovado`,
`Devolvido`, `Reparando`, `Incinerado`.
**Fluxo:** `livro.renovarEmprestimo()` → `estado.renovarEmprestimo(livro)`. Em `Renovado`, a guarda
`numRenovacoes < 2` é a 1ª linha: na 3ª vez lança `RenovacaoNegadaException` antes de mudar o vencimento. Eventos que
o estado não aceita caem na recusa padrão de `EstadoLivro`. A situação é gravada como texto e reconstituída com
`EstadoLivro.para(Situacao)`. Teste: `R01_CicloDeVidaDoLivroTest`.

### Req. 2 — Strategy: políticas de multa
**Problema:** a fórmula da multa muda, mas o serviço de multas e o boleto não devem mudar.
**Classes:** Strategy `PoliticaDeMulta`; ConcreteStrategy `MultaDiaria` (R$ 1,00/dia), `MultaComTeto` (máx. R$ 20,00),
`MultaComTolerancia` (2 dias grátis); Context `ServicoDeMulta`, `ServicoDeSimulacao`; seleção `SeletorDePolitica` +
`ConfiguracaoAdministrativa`.
**Fluxo:** 5 dias de atraso → `seletor.vigente().calcular(5)` = R$ 5,00 (original) ou R$ 3,00 (tolerância). A `Multa`
grava valor e política na emissão: trocar a política depois não recalcula. Teste: `R02_PoliticasDeMultaTest`.

### Req. 3 — Adapter: bancos incompatíveis
**Problema:** o banco A quer centavos e "nosso número" e avisa por JSON; o B quer valor decimal, CPF formatado e
"txid" e avisa por XML. O Financeiro não pode ter `if (banco == ...)`.
**Classes:** Target `iCobranca` (saída) e `iQuitacao` (entrada); Adapter `AdaptadorBancoA`, `AdaptadorBancoB`;
Adaptee `ApiBancoA`, `ApiBancoB`; Client `ServicoDeMulta`; idempotência `ServicoDeQuitacao`.
**Fluxo:** R$ 7,00 → `AdaptadorBancoA` envia 700 centavos; `AdaptadorBancoB` envia 7,00 e "123.456.789-09". A
notificação de pagamento é traduzida pelo adaptador e quitada uma única vez (`quitarSeAberto`). Banco da instalação
escolhido na montagem pela configuração. Teste: `R03_BancosAdapterTest`.

### Req. 4 — Facade: entrada única da circulação
**Problema:** efetivar um empréstimo envolve Relacionamento, Movimentacao, Acervo e Livro, numa ordem, e não pode
ficar pela metade.
**Classes:** Facade `iCirculacao` / `ServicoDeCirculacao`; subsistema `Relacionamento`, `Movimentacao`, `Acervo`,
`Livro`; clientes `TratadorDeEmprestimo`, `TratadorDeDevolucao`; DTOs `Comprovante...`.
**Fluxo:** dentro de uma transação: valida a reserva → `livro.emprestarLivro` → remove a reserva → registra o
empréstimo → atualiza o livro → confirma; qualquer falha desfaz tudo. Devolve um `ComprovanteEmprestimo`, nunca
coleções. Teste: `R04_FachadaDeCirculacaoTest`.

### Req. 5 — Observer: reações à devolução
**Problema:** a devolução dispara inspeção, multa, indicadores e e-mail — e outras reações virão.
**Classes:** Subject `PublicadorDeDevolucao`; Observer `OuvinteDeDevolucao`; ConcreteObserver `OuvinteInspecao`,
`OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail`; evento `EventoDevolucao` (leva vencimento, data e estudante).
**Fluxo:** a fachada confirma a devolução e só então o tratador publica; cada ouvinte é isolado (falha do e-mail vai
para a fila de reenvio e não desfaz nada). O livro fica DEVOLVIDO até a inspeção. Teste: `R05_ReacoesADevolucaoTest`.

### Req. 6 — Command: solicitações como objetos
**Problema:** um duplo clique ou um reenvio não pode renovar duas vezes.
**Classes:** Command `SolicitacaoDeCirculacao`; ConcreteCommand `SolicitacaoDeReserva`, `SolicitacaoDeRenovacao`;
Invoker `ExecutorDeSolicitacoes` + `RegistroDeSolicitacoes`; Receiver `iCirculacao`; Client `TratadorDeEmprestimo`.
**Fluxo:** a tela manda um id (UUID); o executor reserva o id com `putIfAbsent` (atômico): o mesmo id executa uma vez
e as repetições recebem o resultado gravado. Teste: `R06_SolicitacoesCommandTest`.

### Req. 7 — Template Method: rotina comum dos cadastros
**Problema:** os 4 cadastros seguem os mesmos passos; esquecer a ordem causaria gravação parcial.
**Classes:** AbstractClass `TratadorDeCadastro<T, D>` (método-modelo `inserir`, **final**); ConcreteClass
`TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante`, `TratadorDeAtendente`; gancho `verificarPreRequisitos`.
**Fluxo:** buscar existente → validar → pré-requisitos → criar → armazenar → apresentar. Estudante com CPF não
cadastrado é recusado no gancho, antes de qualquer gravação. Teste: `R07_CadastroTemplateMethodTest`.

### Req. 8 — Abstract Factory: famílias de persistência
**Problema:** repositórios e transação têm de ser da mesma família (memória nos testes, arquivo JSON na produção).
**Classes:** AbstractFactory `FabricaDePersistencia`; ConcreteFactory `FabricaEmMemoria`, `FabricaJson`; produtos
`UnidadeDeTrabalho`, `RepositorioLivro`, `RepositorioMovimentacao`, `RepositorioRelacionamento` (e os de solicitações,
financeiro e organização); escolha única em `ConfiguradorDePersistencia`.
**Fluxo:** todos os repositórios recebem a mesma unidade de trabalho; `confirmar()` grava o arquivo inteiro de forma
atômica; `desfazer()` recarrega do disco. Fábrica recusa unidade de outra família. Teste: `R08_FamiliasDePersistenciaTest`.

### Req. 9 — Decorator: consulta com recursos opcionais
**Problema:** medição e cache são opcionais e combináveis por instalação, sem alterar a consulta original.
**Classes:** Component `ConsultaAcervo`; ConcreteComponent `ConsultaAcervoBasica`; Decorator `DecoradorDeConsulta`;
ConcreteDecorator `DecoradorDeMedicao`, `DecoradorDeCache`; montagem `MontadorDeConsulta`.
**Fluxo:** `Medicao(Cache(Basica))`: a 2ª consulta sai do cache e mesmo assim é medida. O cache guarda só id e título;
a reserva sempre lê o estado atual do Livro. Teste: `R09_ConsultaComDecoradoresTest`.

### Req. 10 — Composite: organização hierárquica do acervo
**Problema:** unidades, seções e subseções de profundidade variável; contar livros em qualquer nível do mesmo jeito.
**Classes:** Component `ComponenteDoAcervo`; Leaf `ItemDoAcervo` (referencia o livro pelo id); Composite
`GrupoDoAcervo`; Client `OrganizadorDoAcervo`.
**Fluxo:** unidade com seções de 3 e 2 livros → `contarLivros()` soma recursivamente = 5. O grupo recusa ciclos e
livros já colocados; mover troca só o pai do item. Teste: `R10_OrganizacaoDoAcervoTest`.

---

## 4. Persistência em arquivo JSON

**Por orientação do professor, dada em sala para toda a turma, a persistência relacional (MySQL/Oracle) foi
substituída por um arquivo JSON local.** Não há servidor de banco de dados nesta versão.

- Arquivo único `dados/biblioteca.json` (no `.gitignore`), com as seções `pessoas`, `estudantes`, `atendentes`,
  `livros`, `reservas`, `emprestimos`, `devolucoes`, `solicitacoes`, `multas`, `cobrancas`, `configuracao`, `acervo`
  e `contadores`. Datas ISO (`2026-11-27`), `BigDecimal` sem perder casas, arquivo indentado (Jackson).
- A situação do livro é gravada como texto (`"situacao" : "DEVOLVIDO"`) e reconstituída como objeto de estado.
- **Unidade de trabalho:** ao iniciar, o arquivo é carregado numa cópia de trabalho; todos os repositórios alteram
  essa cópia; `confirmar()` grava em `biblioteca.json.tmp` e substitui o original com `ATOMIC_MOVE`; `desfazer()`
  descarta a cópia e recarrega do disco. Uma falha no meio de uma operação nunca deixa o arquivo pela metade.
- Escritas exclusivas por `synchronized` (unidade de trabalho) e `ConcurrentHashMap.putIfAbsent` (Command).

---

## 5. Interface web

| Página | O que mostra / faz | Padrões em cena |
|---|---|---|
| `/` Início | Livros e situação, indicadores, inspeções pendentes, boletos | — |
| `/cadastros` | Pessoa, estudante, atendente e livro | Template Method |
| `/circulacao` | f1 reservar, f2 consultar, f3 emprestar, f4 renovar, f5 devolver, f6 boleto, inspeção, reparo | State, Facade, Command, Observer |
| `/financeiro` | Boletos, política vigente, simulação, pagamento no banco simulado e reenvio da notificação | Strategy, Adapter |
| `/acervo` | Consulta pública (com medições) e árvore da organização | Decorator, Composite |
| `/admin` | Relógio ajustável (simular atraso), caixa de saída e falha simulada do e-mail | Observer |
| `/api/...` | API JSON: `livros`, `pessoas`, `estudantes`, `atendentes`, `reservas`, `emprestimos`, `renovacoes`, `devolucoes`, `cobrancas`; webhooks `bancos/a/webhook` (JSON) e `bancos/b/notificacao` (XML) | — |

Roteiro da demonstração de 5 minutos: [`docs/estudo/DEMO.md`](docs/estudo/DEMO.md).

---

## 6. Testes

`mvnw.cmd test` roda **105 testes** (todos aprovados), incluindo os cenários de verificação:

| Req | Classe de teste | Req | Classe de teste |
|---|---|---|---|
| 1 | `R01_CicloDeVidaDoLivroTest` | 6 | `R06_SolicitacoesCommandTest` |
| 2 | `R02_PoliticasDeMultaTest` | 7 | `R07_CadastroTemplateMethodTest` |
| 3 | `R03_BancosAdapterTest` | 8 | `R08_FamiliasDePersistenciaTest` |
| 4 | `R04_FachadaDeCirculacaoTest` | 9 | `R09_ConsultaComDecoradoresTest` |
| 5 | `R05_ReacoesADevolucaoTest` | 10 | `R10_OrganizacaoDoAcervoTest` |

Além deles: testes de cada cadastro e tratador, das transições do State (`EstadoLivroTest`), da arquitetura
(`ArquiteturaTest`) e do fluxo web completo com MockMvc (`FluxoCompletoWebTest`).

---

## 7. Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/estudo/`](docs/estudo) | Guias de estudo por etapa, `DEMO.md` (roteiro de 5 min) e `ARGUICAO.md` (perguntas prováveis com respostas) |
| [`docs/RASTREABILIDADE.md`](docs/RASTREABILIDADE.md) | Requisito → padrão → classes por papel → teste |
| [`docs/DECISOES.md`](docs/DECISOES.md) | Cada decisão tomada (PDFs × Astah), alternativas e motivo |
| [`docs/uml/Trabalho03_Diagramas.pdf`](docs/uml/Trabalho03_Diagramas.pdf) | Diagramas UML exportados do Astah: 10 de classes (um por padrão), máquina de estados do Livro, 10 de sequência (um por cenário). PNGs em [`docs/uml/Trabalho 03/`](docs/uml/Trabalho%2003/) |
| [`docs/estudo/DIAGRAMAS_UML.md`](docs/estudo/DIAGRAMAS_UML.md) | Como os diagramas estão organizados no `.asta`, como ler cada um e como regenerar (`ferramentas/astah/gerar.cmd`) |
| [`docs/uml/ATUALIZACOES_ASTAH.md`](docs/uml/ATUALIZACOES_ASTAH.md) | Diferenças Trabalho 02 × Trabalho 03 por diagrama e o que já está no `.asta` |
| [`REGISTRO_LLM.md`](REGISTRO_LLM.md) | Pedidos feitos ao modelo e o que a equipe aceitou/corrigiu/descartou |
| [`docs/PROGRESSO.md`](docs/PROGRESSO.md) | Andamento por etapa |

---

## 8. Ferramentas e modelos de IA usados

- **Ferramenta:** Claude Code (CLI da Anthropic), trabalhando diretamente no repositório.
- **Modelo:** Claude Opus 5.5 (`claude-opus-5-5`).
- **Onde foi usado:** em todas as partes do sistema — leitura dos PDFs e diagramas, planejamento das etapas,
  código de domínio e dos 10 padrões, persistência JSON, interface web, testes automatizados e toda a documentação
  (guias de estudo, rastreabilidade, decisões, lista de mudanças do Astah, roteiro de demonstração).
  Também gerou os diagramas UML no `.asta` pela API do Astah (`ferramentas/astah/`): as classes são lidas do
  código compilado e os fluxos dos diagramas de sequência foram escritos a partir dos testes R01–R10.
- **Como:** Etapas 0 e 1 passo a passo, com aprovação da equipe a cada passo; Etapas 2 a 6 em modo autônomo, com
  as decisões registradas em `docs/DECISOES.md`. O detalhamento dos pedidos, e o espaço para a equipe anotar o que
  aceitou, corrigiu ou descartou, está em `REGISTRO_LLM.md`.
- **Responsabilidade:** o código é da equipe — cada integrante deve conseguir explicar qualquer parte
  (ver `docs/estudo/ARGUICAO.md`).
