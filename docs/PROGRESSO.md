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

## Etapa 2 — Cadastros e persistência: Template Method (Req 7) e Abstract Factory (Req 8) ⬜

## Etapa 3 — Circulação: Facade (Req 4) e Command (Req 6) ⬜

## Etapa 4 — Devolução e financeiro: Observer (Req 5), Strategy (Req 2) e Adapter (Req 3) ⬜

## Etapa 5 — Acervo e consulta: Composite (Req 10) e Decorator (Req 9) ⬜

## Etapa 6 — Interface web e demonstração ⬜
