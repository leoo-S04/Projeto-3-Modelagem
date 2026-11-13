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

## Etapa 2 — Template Method e Abstract Factory (decisões do modelo, modo autônomo)

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D15 | Método-modelo `inserir(D)` **final** em `comum.TratadorDeCadastro<T, D>`; as operações do Astah (`inserirLivro`, `inserirPessoa`...) foram mantidas e delegam a ele | Renomear para `incluir` (Soluções) | Mantém os nomes do Astah; o padrão só exige o método-modelo final. |
| D16 | `inserirX` passa a devolver `ResultadoCadastro<X>` (NOVO, EXISTENTE, RECUSADO + motivo) | Devolver a entidade ou `null` (Astah) | O padrão das Soluções devolve o resultado com o motivo; a tela precisa mostrar por que recusou. |
| D17 | A recusa (validação / pré-requisito) é convertida em RECUSADO dentro do método-modelo, e só `armazenar` roda dentro da transação | Converter no tratador (Soluções) | Garante, num único lugar, que nada é gravado quando um passo anterior falha. |
| D18 | Novos pacotes de infraestrutura `comum` e `persistencia` dentro de `biblioteca.jar` | Colocar tudo dentro dos pacotes de componentes | O Template Method e a persistência são usados por RH, Acadêmico e Biblioteca; evita dependência entre componentes. Diagrama de implantação não muda. |
| D19 | Uma unidade de trabalho por aplicação (criada na montagem); `executar` é `synchronized` e confirma/desfaz; chamadas aninhadas participam da externa | Uma unidade por operação (sequência das Soluções) | Com arquivo JSON, uma unidade por operação recarregaria o arquivo a cada operação e perderia alterações simultâneas. |
| D20 | As duas famílias usam a mesma estrutura de cópia de trabalho (`BaseDeDados`) e o mesmo mapeamento; diferem em `confirmar`/`desfazer` (fotografia em memória × arquivo). Repositórios concretos são subclasses finas de bases comuns | Código separado por família | Menos duplicação; a diferença relevante (onde e como gravar) fica nas unidades de trabalho. |
| D21 | Arquivo JSON: grava em `.tmp` e move com `ATOMIC_MOVE` (se o sistema não suportar, `REPLACE_EXISTING`); arquivo inexistente é criado vazio | Escrever direto no arquivo | Uma falha no meio nunca deixa o arquivo pela metade. |
| D22 | Ids de transação e de papel gerados por contadores gravados no próprio JSON (seção `contadores`) | Contador no tratador (Etapa 0) | Os ids continuam únicos depois de reiniciar a aplicação. |
| D23 | `Movimentacao` com 3 associações (via `RepositorioMovimentacao`): `registrarReserva/removerReserva/buscarReservaAtiva`, `registrarEmprestimo/removerEmprestimo/buscarEmprestimoAtivo`, `registrarDevolucao`; saem `adicionar`/`remover` genéricos | Manter a lista única | Decisão D06 da equipe; nomes das Soluções (Problema 4). |
| D24 | Repositórios definidos no pacote `persistencia`; `Acervo`, `Movimentacao` e grupos delegam a eles | Interfaces dentro de cada componente | `RepositorioRelacionamento` atende RH e Acadêmico; um só lugar para os produtos da fábrica. |
| D25 | BOM do Spring Boot importado só para gerenciar versões (Jackson, JUnit) | Versões fixas manualmente | Prepara a Etapa 6 sem colocar Spring no domínio. |
| D26 | `Sistema` (pacote raiz) é a montagem única; `RelogioAjustavel` (pacote `comum`) é o Clock injetado | Montagem em cada teste | Testes e web usam a mesma montagem; o relógio ajustável serve à demonstração. |
| D27 | Validação de CPF: só dígitos (sem verificar tamanho/dígitos verificadores) | Validação completa | Fora do escopo dos requisitos; mantém os dados de teste simples. |

## Etapa 3 — Facade e Command (decisões do modelo, modo autônomo)

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D28 | Fachada `iCirculacao` / `ServicoDeCirculacao` (nomes das Soluções) no pacote `biblioteca`; os tratadores mantêm os nomes do Astah e delegam | Pôr a orquestração na web | Req. 4: a interface depende só dos tratadores. |
| D29 | Tratadores devolvem DTOs: f2 `ComprovanteReserva`, f3 `ComprovanteEmprestimo`, f5 `ComprovanteDevolucao`; f1/f4 `ResultadoSolicitacao`; nova `consultarSituacao` → `SituacaoLivroDTO` | Devolver `Reserva`/`Emprestimo` (Astah) | Req. 4: comprovantes em vez de objetos internos ou coleções. |
| D30 | `efetivarEmprestimo` recusa com `ReservaInvalidaException` (estudante inexistente, sem reserva ativa, reserva de outro estudante) | Devolver `null` (D09) | Sequência e tratamento de falhas das Soluções (Problema 4). Nas demais operações, inexistente continua `null`/`false`. |
| D31 | Client do Command = `TratadorDeEmprestimo`; a tela informa o id (UUID) da solicitação | A tela cria a solicitação (Soluções) | Para criar a solicitação a tela precisaria conhecer `iCirculacao`, violando "a web depende só dos tratadores". O id vindo da tela preserva a idempotência em reenvios. |
| D32 | Recusa de domínio dentro de uma solicitação vira `ResultadoSolicitacao` RECUSADA com a mensagem | Deixar a exceção subir (D09) | Código do executor nas Soluções; a recusa é um resultado final, gravado e devolvido nas repetições. A mensagem não se perde. |
| D33 | Registro: `ConcurrentHashMap.putIfAbsent` + resultados gravados (seção `solicitacoes`), por meio do novo produto `RepositorioDeSolicitacoes` da fábrica; ids não são removidos após concluir; falha técnica libera o id | Só memória | Idempotência inclusive após reiniciar; sem corrida entre concluir e uma nova submissão. |
| D34 | Execução e registro do resultado em transações separadas | Mesma transação | A unidade de trabalho não tem "savepoint": numa transação única, uma recusa no meio exigiria confirmar alterações parciais. Risco residual (queda entre as duas) documentado no guia. |
| D35 | Sobrecargas sem id (`reservarLivro(idLivro, ...)`) geram um UUID novo | Exigir sempre o id | Mantém as assinaturas do Astah; cada chamada sem id é uma solicitação nova. |
