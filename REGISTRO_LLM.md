# Registro de uso de LLM

Exigência do Trabalho 03 ("Uso de LLMs"): resumo dos principais pedidos feitos ao modelo e do que a equipe
**aceitou, corrigiu ou descartou**. As colunas "Decisão da equipe" e "Observações" são preenchidas pela equipe.

- **Ferramenta:** Claude Code (CLI), em sessão de trabalho no repositório.
- **Modelo:** Claude Opus 5.5 (`claude-opus-5-5`), da Anthropic.
- **Forma de trabalho:** definida em `CLAUDE.md` — etapas e passos curtos, cada passo aprovado pela equipe antes do próximo.

## Etapa 0 — Fundação

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 1 | Ler `CLAUDE.md`, os PDFs (enunciado do T03, soluções do professor, T01, T02) e os diagramas do Astah; apresentar o plano da Etapa 0 | Plano com objetivo, passos e testes; 4 divergências PDF × Astah para decisão: nomes `Acervo`/`Relacionamento`, interface `CRUD`, nome do enum `EstadoLivro`, comportamento de f1/f3/f4/f5 na Etapa 0 | Equipe escolheu A nas 4 perguntas | |
| 2 | Não alterar o Astah durante a implementação; registrar cada mudança necessária e entregar a lista na Etapa 6 | `docs/uml/ATUALIZACOES_ASTAH.md`, preenchido a cada passo | Aceitou | |
| 3 | Passo 1 — projeto Maven | `pom.xml` (Java 17, JUnit 5, `biblioteca.jar`), Maven Wrapper, `.gitignore`, `README.md`, `docs/PROGRESSO.md` | Aceitou | |
| 4 | Passo 2 (antigo) — pacotes por componente | `package-info.java` de `rh`, `academico`, `biblioteca`, `financeiro`, `web` | Aceitou | |
| 5 | Reorganizar a Etapa 0 em 3 passos e usar o formato curto de resposta | `PROGRESSO.md` reorganizado | Aceitou | |
| 6 | Passo 2 — domínio e cadastros | Classes de RH, Acadêmico e Livro/Acervo com seus tratadores (regras 1 a 4) e 19 testes | Aceitou | |
| 7 | Passo 3 — circulação e documentação | `Transacao`, `Reserva`, `Emprestimo`, `Devolucao`, `Movimentacao`, `Relacionamento`, tratadores de circulação (f2 funcionando; f1, f3, f4, f5 e inspeção pendentes para a Etapa 1), 8 testes, este registro e `docs/RASTREABILIDADE.md`. Apontou divergência: Movimentacao com lista única (Astah) × associações separadas (T02 e professor) | Equipe decidiu separar em 3 associações na Etapa 2 | |
| 8 | Fechamento da Etapa 0 | Execução completa dos testes (27 aprovados), guia `docs/estudo/ETAPA00_Fundacao.md`, atualização da rastreabilidade e deste registro, lista de mudanças no Astah, sugestão de commit | Aceitou | |

## Etapa 1 — State (Req. 1)

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 9 | Apresentar o plano da Etapa 1 | Plano com 2 passos e 3 divergências: onde ficam vencimento/renovações, nomes dos eventos, tratamento da recusa | Equipe aceitou as recomendações (A, A, A; prazo de 30 dias) | |
| 10 | Passo 1 — State no Livro | `EstadoLivro`, 7 estados, 2 exceções; `Livro` como Context; `Emprestimo`/`Devolucao` ajustados; `EstadoLivroTest` | | |
| 11 | Mudar para o modo autônomo (CLAUDE.md) e executar as Etapas 1 a 6 com commit e push ao fim de cada uma | Passo 2 (tratadores usando o State), `R01_CicloDeVidaDoLivroTest`, `TratadorDeDevolucaoTest`, guia `ETAPA01_State.md`, `docs/DECISOES.md` | | |

## Etapa 2 — Template Method (Req. 7) e Abstract Factory (Req. 8) — modo autônomo

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 12 | Executar a Etapa 2 (CLAUDE.md, modo autônomo) | `TratadorDeCadastro` (método-modelo final) e os 4 tratadores como ConcreteClass; pacote `persistencia` com fábrica abstrata, famílias em memória e JSON (gravação atômica), unidade de trabalho compartilhada; `Movimentacao` em 3 associações; `Sistema` (montagem) e `RelogioAjustavel`; R07 e R08; guia `ETAPA02_TemplateMethod_AbstractFactory.md`; decisões D15–D27 | | |

## Etapa 3 — Facade (Req. 4) e Command (Req. 6) — modo autônomo

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 13 | Executar a Etapa 3 (CLAUDE.md, modo autônomo) | `iCirculacao`/`ServicoDeCirculacao` com comprovantes (DTOs) e transação; tratadores delegando; `SolicitacaoDeCirculacao`, `SolicitacaoDeReserva`, `SolicitacaoDeRenovacao`, `ExecutorDeSolicitacoes`, `RegistroDeSolicitacoes` (idempotente, gravado no JSON); R04 e R06 (inclui teste de concorrência); guia `ETAPA03_Facade_Command.md`; decisões D28–D35 | | |

## Etapa 4 — Observer (Req. 5), Strategy (Req. 2) e Adapter (Req. 3) — modo autônomo

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 14 | Executar a Etapa 4 (CLAUDE.md, modo autônomo) | Publicador, evento e 4 ouvintes de devolução; 3 políticas de multa com seletor e simulação; `Multa` e `BoletoDeMulta`; contrato `iCobranca`, adaptadores dos bancos A e B com APIs simuladas incompatíveis, quitação idempotente; f6; `TratadorDeMulta`; persistência de multas, boletos e configuração; R02, R03, R05; guia `ETAPA04_Observer_Strategy_Adapter.md`; decisões D36–D49 | | |

## Etapa 5 — Composite (Req. 10) e Decorator (Req. 9) — modo autônomo

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 15 | Executar a Etapa 5 (CLAUDE.md, modo autônomo) | `ComponenteDoAcervo`, `ItemDoAcervo`, `GrupoDoAcervo`, `OrganizadorDoAcervo` (com persistência da árvore no JSON); `ConsultaAcervo`, `ConsultaAcervoBasica`, decoradores de medição e cache, `MontadorDeConsulta`; integração no `TratadorDeAcervo`; R09 e R10; guia `ETAPA05_Composite_Decorator.md`; decisões D50–D56 | | |

## Etapa 6 — Interface web e demonstração — modo autônomo

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 16 | Executar a Etapa 6 (CLAUDE.md, modo autônomo) | Aplicação Spring Boot (pacote `web`): telas de cadastros, circulação, financeiro, acervo e administração; API JSON; pontos de entrada e simulador dos bancos; relógio ajustável; perfil HTTPS opcional; `dados/exemplo.json`; `FluxoCompletoWebTest` e `ArquiteturaTest`; README final; `DEMO.md`, `ARGUICAO.md`, guia `ETAPA06_Web_Demonstracao.md`; lista final do Astah por diagrama; decisões D57–D64. Verificado: `mvnw test` (105 testes) e execução real do `biblioteca.jar` (HTTP e HTTPS) | | |

## Entrega UML — diagramas no Astah (após a Etapa 6) — modo autônomo

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 17 | Fazer o que faltava do enunciado: atualizar o Astah (diagrama de classes de cada padrão, máquina de estados do Livro, diagramas de sequência dos 10 cenários) e exportar em PDF | Gerador pela API Java do Astah (`ferramentas/astah/GeradorDiagramasT03.java`, `MontarPdf.java`, `gerar.cmd`): pacote `Trabalho 03 - Implementacao` com 121 classes lidas do código compilado; 10 diagramas de classes e 10 de sequência (pacotes `Req 01` … `Req 10`); máquina de estados atualizada; `docs/uml/Trabalho03_Diagramas.pdf` e PNGs; guia `docs/estudo/DIAGRAMAS_UML.md`; decisões D65–D69. A coluna "Decisão da equipe" deste registro **não** foi preenchida pelo modelo (é declaração da equipe) | | |
