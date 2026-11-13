# Decisões de projeto

Registro de cada decisão tomada quando os PDFs e o Astah divergiram ou havia escolha em aberto
(o que foi decidido, alternativas e motivo). Até a Etapa 1 inclusive, as decisões foram tomadas pela equipe;
a partir da Etapa 2 (modo autônomo, `CLAUDE.md`), pelo modelo, seguindo a regra: Soluções do professor onde o
padrão exigir; nomes do Astah nos demais casos.

## Etapa 0 — Fundação

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D01 | `AcervoDeLivros` → `Acervo`; nova classe `Relacionamento` em `biblioteca`, usando `iEstudante`/`iAtendente` | Manter os nomes do Astah e explicar a equivalência | O T03 exige os pontos internos Acervo, Relacionamento e Movimentacao; usar as interfaces requeridas evita dependência entre componentes. (Equipe) |
| D02 | Sem interface `CRUD` nas entidades | Implementar `CRUD` com cada entidade recebendo o grupo | Conflita com repositórios (Req. 8) e Template Method (Req. 7). (Equipe) |
| D03 | Enum `EstadoLivro` → `Situacao` | Manter o enum e dar outro nome à classe do State | Libera `EstadoLivro` para a classe do State, como nas Soluções. (Equipe) |
| D04 | f1, f3, f4, f5 pendentes na Etapa 0 | Implementar versão com `if` sobre o estado | Evitar código com `if` que seria descartado. (Equipe) |
| D05 | Tratadores de Estudante e Atendente usam `iPessoa` | Acessar `GrupoDePessoas` diretamente (Astah) | Diagrama de componentes: Acadêmico requer `iPessoa`; igual às Soluções (Req. 7). |
| D06 | `Movimentacao` será separada em 3 associações (reservas, empréstimos, devoluções) na Etapa 2 | Manter a lista única do Astah | T02 e Soluções pedem associações distintas; o JSON já tem as 3 seções. (Equipe) |

## Etapa 1 — State

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D07 | `reservante`, `vencimento` e `numRenovacoes` ficam no `Livro`; `Emprestimo` perde esses dados | Manter no `Emprestimo` e passar o empréstimo aos eventos | As guardas da máquina do Livro usam esses dados; o estado recebe só o Livro (Soluções). (Equipe, P1 = A) |
| D08 | Eventos com os nomes do Astah (`reservarLivro`, `renovarEmprestimo`, `inspecionar`, `retornarDoReparo`, `incinerar`...) | Nomes das Soluções (`aprovarInspecao`, `registrarLaudoIrrecuperavel`...) | A máquina de estados do Astah não precisa mudar; `incinerar` é o "evento de laudo irrecuperável". (Equipe, P2 = A) |
| D09 | Recusa do estado → exceção com mensagem; livro/estudante inexistente → `null`/`false` | Engolir a exceção; objeto `Resultado` | Mantém as assinaturas do Astah sem perder o motivo da recusa. (Equipe, P3 = A) |
| D10 | Prazo do primeiro empréstimo: 30 dias (`Livro.PRAZO_EMPRESTIMO_DIAS`) | 7, 14 dias | Os enunciados não definem; 30 dias é coerente com a renovação. (Equipe) |
| D11 | `Emprestado` e `Renovado` têm cada um o seu `renovarEmprestimo` (guardas `= 0` e `< 2`) | `Renovado` herdar de `Emprestado` (Soluções) | Espelha as transições T4 e T5 da máquina do Astah, uma por método. |
| D12 | `Livro.setEstado` público → `mudarPara` de pacote; `getEstado()` devolve `Situacao` | Manter `setEstado` | Um setter público permitiria pular a máquina de estados. |
| D13 | `emprestarLivro(e, data)` recebe a data de hoje | Livro consultar o relógio | O domínio não depende do relógio; o tratador passa `LocalDate.now(clock)`. |
| D14 | Até a Etapa 3, a orquestração (Acervo + Livro + Movimentacao) fica nos tratadores | Criar a Facade já | A Facade é o Req. 4 (Etapa 3); o State foi integrado primeiro. |
