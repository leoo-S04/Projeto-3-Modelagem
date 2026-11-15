# Progresso do projeto

Legenda: ✅ feito · ⏳ em andamento · ⬜ pendente

Os passos de cada etapa são detalhados quando o plano da etapa é aprovado.

## Etapa 0 — Fundação ✅

Decisões da equipe no plano: Pergunta 1 = A (`Acervo` + `Relacionamento`), Pergunta 2 = A (sem `CRUD` nas entidades),
Pergunta 3 = A (enum `Situacao`), Pergunta 4 = A (f1, f3, f4, f5 pendentes até as etapas dos padrões).
Reorganizada em 3 passos (CLAUDE.md: etapa estrutural com no máximo 3 passos).

- ✅ Passo 1 — Projeto Maven e pacotes: `pom.xml`, Maven Wrapper, `.gitignore`, `README.md`, `PROGRESSO.md`,
  `ATUALIZACOES_ASTAH.md`, `package-info.java` de `rh`, `academico`, `biblioteca`, `financeiro`, `web`
- ✅ Passo 2 — Domínio e cadastros: RH (`Pessoa`, `Papel`, `Atendente`, `GrupoDePessoas`, `GrupoDeColaboradores`,
  `iPessoa`, `TratadorDePessoa`, `iAtendente`, `TratadorDeAtendente`), Acadêmico (`Estudante`, `GrupoDeAcademicos`,
  `iEstudante`, `TratadorDeEstudante`), Biblioteca (`Livro`, `Situacao`, `Acervo`, `iLivro`, `TratadorDeAcervo`) e testes
- ✅ Passo 3 — Circulação e documentação: `Transacao`, `Reserva`, `Emprestimo`, `Devolucao`, `Movimentacao`,
  `Relacionamento`, `iEmprestimo`, `TratadorDeEmprestimo`, `iDevolucao`, `TratadorDeDevolucao`, testes,
  `REGISTRO_LLM.md`, `RASTREABILIDADE.md`, conferência Astah × código, README
- ✅ Fechamento da Etapa 0 (27 testes aprovados; guia `docs/estudo/ETAPA00_Fundacao.md`)
- Decidido para a Etapa 2: separar a `Movimentacao` em 3 associações (reservas, empréstimos, devoluções).

## Etapa 1 — Ciclo de vida do Livro: State (Req 1) ✅

Decisões da equipe no plano: Pergunta 1 = A (reservante, vencimento e numRenovacoes no `Livro`),
Pergunta 2 = A (nomes dos eventos do Astah), Pergunta 3 = A (recusa do estado sobe como exceção com a mensagem;
livro/estudante inexistente → `null`/`false`). Prazo do primeiro empréstimo: 30 dias.

- ✅ Passo 1 — Padrão State no Livro: `EstadoLivro`, 7 estados concretos, `OperacaoInvalidaException`,
  `RenovacaoNegadaException`; `Livro`, `Emprestimo`, `Devolucao` alterados; `EstadoLivroTest`
- ✅ Passo 2 — Integração: `TratadorDeEmprestimo` (f1, f3, f4), `TratadorDeDevolucao` (f5, inspeção, reparo),
  `TratadorDeAcervo` (excluir), `Movimentacao.gerarIdTransacao`; `R01_CicloDeVidaDoLivroTest`, `TratadorDeDevolucaoTest`
- ✅ Fechamento: 51 testes aprovados; guia `docs/estudo/ETAPA01_State.md`; `docs/DECISOES.md` criado

**A partir daqui: MODO AUTÔNOMO (CLAUDE.md).** Etapas executadas em sequência; decisões em `docs/DECISOES.md`.

## Etapa 2 — Cadastros e persistência: Template Method (Req 7) e Abstract Factory (Req 8) ✅

- ✅ Template Method: `comum.TratadorDeCadastro` (método-modelo `inserir` final), `ResultadoCadastro`; 4 tratadores de cadastro como ConcreteClass
- ✅ Abstract Factory: pacote `persistencia` — fábricas em memória e JSON, unidade de trabalho compartilhada, gravação atômica
- ✅ `Movimentacao` em 3 associações; `Sistema` (montagem); `RelogioAjustavel`
- ✅ Testes `R07_CadastroTemplateMethodTest`, `R08_FamiliasDePersistenciaTest` (63 testes aprovados)
- ✅ Guia `docs/estudo/ETAPA02_TemplateMethod_AbstractFactory.md`; decisões D15–D27

## Etapa 3 — Circulação: Facade (Req 4) e Command (Req 6) ✅

- ✅ Facade: `iCirculacao` / `ServicoDeCirculacao` (transacional, devolve comprovantes); tratadores delegam
- ✅ Command: `SolicitacaoDeCirculacao`, `SolicitacaoDeReserva`, `SolicitacaoDeRenovacao`, `ExecutorDeSolicitacoes`,
  `RegistroDeSolicitacoes` (idempotente, gravado no JSON — novo produto `RepositorioDeSolicitacoes`)
- ✅ Testes `R04_FachadaDeCirculacaoTest`, `R06_SolicitacoesCommandTest` (70 testes aprovados)
- ✅ Guia `docs/estudo/ETAPA03_Facade_Command.md`; decisões D28–D35

## Etapa 4 — Devolução e financeiro: Observer (Req 5), Strategy (Req 2) e Adapter (Req 3) ✅

- ✅ Observer: `PublicadorDeDevolucao`, `EventoDevolucao`, ouvintes de inspeção, multa, indicadores e e-mail (publicação após o commit)
- ✅ Strategy: `PoliticaDeMulta` + 3 políticas, `SeletorDePolitica`, `ServicoDeMulta`, `ServicoDeSimulacao`, `Multa`
- ✅ Adapter: `iCobranca`, `AdaptadorBancoA/B`, APIs simuladas `ApiBancoA/B`, `ServicoDeQuitacao` (idempotente)
- ✅ f6 (`emitirBoletoDeMulta`), `TratadorDeMulta`, `RepositorioFinanceiro` (multas, cobranças, configuração no JSON)
- ✅ Testes `R02_PoliticasDeMultaTest`, `R03_BancosAdapterTest`, `R05_ReacoesADevolucaoTest` (88 testes aprovados)
- ✅ Guia `docs/estudo/ETAPA04_Observer_Strategy_Adapter.md`; decisões D36–D49

## Etapa 5 — Acervo e consulta: Composite (Req 10) e Decorator (Req 9) ✅

- ✅ Composite: `ComponenteDoAcervo`, `ItemDoAcervo`, `GrupoDoAcervo`, `OrganizadorDoAcervo` (árvore gravada na seção `acervo`)
- ✅ Decorator: `ConsultaAcervo`, `ConsultaAcervoBasica`, `DecoradorDeMedicao`, `DecoradorDeCache`, `MontadorDeConsulta`
- ✅ Integração no `TratadorDeAcervo`; novo produto `RepositorioDeOrganizacao`
- ✅ Testes `R09_ConsultaComDecoradoresTest`, `R10_OrganizacaoDoAcervoTest` (100 testes aprovados)
- ✅ Guia `docs/estudo/ETAPA05_Composite_Decorator.md`; decisões D50–D56

## Etapa 6 — Interface web e demonstração ✅

- ✅ Aplicação web (Spring Boot + Thymeleaf, pacote `web`): cadastros, circulação (f1–f6, inspeção, reparo), financeiro
  (política, simulação, bancos simulados), acervo (consulta e organização), administração (relógio, e-mail)
- ✅ API JSON e pontos de entrada dos bancos (webhook JSON / notificação XML); perfil `https` opcional
- ✅ `dados/exemplo.json` copiado na primeira execução; `biblioteca.jar` executável verificado (HTTP e HTTPS)
- ✅ Testes `FluxoCompletoWebTest` e `ArquiteturaTest` (105 testes aprovados)
- ✅ README final, `docs/estudo/DEMO.md`, `docs/estudo/ARGUICAO.md`, guia `ETAPA06_Web_Demonstracao.md`
- ✅ `docs/uml/ATUALIZACOES_ASTAH.md` com a lista final por diagrama; decisões D57–D64

## Entrega UML — diagramas no Astah ✅

- ✅ `.asta` atualizado pela API do Astah (`ferramentas/astah/gerar.cmd`): pacote `Trabalho 03 - Implementacao`
  (121 classes lidas do código), 10 diagramas de classes (um por padrão), 10 de sequência (um por cenário R01–R10)
- ✅ Máquina de Estados - Livro atualizada (efeitos das transições; devolução publica o evento — Observer)
- ✅ Exportação: `docs/uml/Trabalho03_Diagramas.pdf` e PNGs em `docs/uml/Trabalho 03/`; guia `docs/estudo/DIAGRAMAS_UML.md`; decisões D65–D69

**Projeto concluído.** Próximos passos da equipe: abrir o `.asta` no Astah e conferir os diagramas, preencher as
colunas "Decisão da equipe" do `REGISTRO_LLM.md`, estudar `docs/estudo/` e ensaiar `DEMO.md`. Opcional: aplicar no
"Classes de Projeto", Componentes e SDs do Trabalho 02 os itens restantes de `docs/uml/ATUALIZACOES_ASTAH.md`.
