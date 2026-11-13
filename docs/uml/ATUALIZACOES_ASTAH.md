# Atualizações pendentes no Astah

**Combinado com a equipe:** o Astah **não** é alterado durante a implementação. Cada mudança que o código
exigir é registrada aqui, no momento em que surge. Na Etapa 6, esta lista é entregue completa e
organizada por diagrama, para a equipe aplicar tudo de uma vez.

Colunas: **Diagrama** · **Elemento** · **De → Para** · **Motivo** · **Origem** (etapa/passo em que surgiu ou em que o código passa a refletir a mudança).

## Registro (ordem cronológica)

| # | Diagrama | Elemento | De → Para | Motivo | Origem |
|---|---|---|---|---|---|
| 1 | Classes de Projeto | Classe `AcervoDeLivros` | `AcervoDeLivros` → `Acervo` (atributos e operações mantidos) | O Trabalho 03 manda preservar o ponto interno chamado **Acervo**; as soluções do professor usam esse nome. Decisão da equipe (Pergunta 1 = A). | Plano da Etapa 0; código na Etapa 0, Passo 2 |
| 2 | Componentes | Nota de classes do componente Biblioteca | "AcervoDeLivros" → "Acervo"; acrescentar "Relacionamento" | Coerência com os itens 1 e 3. | Plano da Etapa 0; código na Etapa 0, Passos 2 e 3 |
| 3 | Classes de Projeto | Nova classe `Relacionamento` (componente Biblioteca) | (inexistente) → classe `Relacionamento`, ponto único de acesso da Biblioteca a estudantes e atendentes, que usa as interfaces `iEstudante` e `iAtendente`; associações de delegação a partir de `TratadorDeEmprestimo` e `TratadorDeDevolucao` | O Trabalho 03 manda preservar o ponto interno **Relacionamento** (Trabalho 02: "único ponto de acesso interno … mantido por associações diferentes"). Usar `iEstudante`/`iAtendente`, que o componente Biblioteca já requer, evita dependência direta entre componentes. Os três `GrupoDe...` continuam como estão. Decisão da equipe (Pergunta 1 = A). Operações exatas: registradas no Passo 3. | Plano da Etapa 0; código na Etapa 0, Passo 3 |
| 4 | Classes de Projeto | Interface `CRUD` | Remover a interface e as realizações por `Pessoa`, `Estudante`, `Atendente` e `Livro`, junto com as operações `inserir()`, `consultar()`, `atualizar()` e `excluir()` dessas quatro classes | Com `CRUD`, cada entidade precisaria saber onde está gravada, o que conflita com os repositórios do Req. 8 (Abstract Factory) e com a rotina de cadastro do Req. 7 (Template Method). O cadastro fica nos tratadores e nos grupos. Decisão da equipe (Pergunta 2 = A). | Plano da Etapa 0; código na Etapa 0, Passo 2 |
| 5 | Classes de Projeto | Enumeração `EstadoLivro` | `EstadoLivro` → `Situacao` (mesmos 7 literais) | Libera o nome `EstadoLivro` para a classe abstrata do padrão State (Etapa 1), como na solução do professor. O tipo do atributo `Livro.estado` e as operações `getEstado`/`setEstado` serão ajustados e registrados na Etapa 1. Decisão da equipe (Pergunta 3 = A). | Plano da Etapa 0; código na Etapa 0, Passo 2 |
| 6 | Classes de Projeto | Classe `Pessoa` | Acrescentar `+getNome(): String`, `+getSobrenome(): String` e `+atualizarDados(nome: String, sobrenome: String, email: String): void` | Sem a operação `atualizar()` de `CRUD` (item 4), `TratadorDePessoa.atualizarPessoa` precisa de uma operação que mude os dados sem mudar o CPF (regra 2.2). | Etapa 0, Passo 2 |
| 7 | Classes de Projeto | Classe `Papel` | Acrescentar `+setPessoa(pessoa: Pessoa): void` | `atualizarEstudante(matricula, cpf)` e `atualizarAtendente(matriculaFuncional, cpf)` ligam o papel a outra pessoa cadastrada (regras 3.2 e 4.2). | Etapa 0, Passo 2 |
| 8 | Classes de Projeto | Classe `Atendente` | Acrescentar `+getMatriculaFuncional(): String` | Usada por `GrupoDeColaboradores.buscarPorMatricula` (o Astah já tem `getMatricula()` em `Estudante`). | Etapa 0, Passo 2 |
| 9 | Classes de Projeto | Classe `Livro` | Acrescentar `+getIdLivro(): int`, `+getTitulo(): String`, `+setTitulo(titulo: String): void`. **Atenção:** `+setEstado(estado: EstadoLivro)` ainda não foi implementada; o destino de `getEstado`/`setEstado` será definido e registrado na Etapa 1 (State). | `setTitulo` substitui o `atualizar()` de `CRUD` (regra 1.2); `getIdLivro` é usado pelo `Acervo`. | Etapa 0, Passo 2 |
| 10 | Classes de Projeto | Classe `GrupoDeAcademicos`, atributo | `-estudante : List<Estudante>` → `-estudantes : List<Estudante>` | Plural, como `pessoas` e `atendentes` nos outros grupos. | Etapa 0, Passo 2 |
| 11 | Classes de Projeto | Associação "delega" de `TratadorDeEstudante` para `GrupoDePessoas` | Associação com `GrupoDePessoas` → dependência («use») da interface `iPessoa`, atributo `-pessoas : iPessoa` | `TratadorDeEstudante` está no componente Academico, que **requer** `iPessoa` do RH (diagrama de componentes); acessar a classe `GrupoDePessoas` diretamente criaria dependência entre componentes sem interface. Igual à solução do professor (Req. 7). | Etapa 0, Passo 2 |
| 12 | Classes de Projeto | Associação "delega" de `TratadorDeAtendente` para `GrupoDePessoas` | Associação com `GrupoDePessoas` → dependência («use») da interface `iPessoa`, atributo `-pessoas : iPessoa` | Mesmo tratamento do item 11, como no plano aprovado e na solução do professor (Req. 7). | Etapa 0, Passo 2 |
| 13 | Classes de Projeto | Classes `TratadorDeEstudante` e `TratadorDeAtendente` | Acrescentar atributo `-proximoId : int` | Gera o `id` do `Papel` ("todo papel tem um id") na inclusão. Pode mudar na Etapa 2 (persistência). | Etapa 0, Passo 2 |
| 14 | Classes de Projeto | Classe `Transacao` | Acrescentar `+getIdTransacao(): int`, `+getAtendente(): Atendente` e `~envolve(idLivro: int, idEstudante: String): boolean` | Getters dos atributos já existentes; `envolve` é usado pelas buscas da `Movimentacao` (livro + matrícula do estudante). | Etapa 0, Passo 3 |
| 15 | Classes de Projeto | Classe `Relacionamento` (complementa o item 3) | Atributos `-estudantes : iEstudante`, `-atendentes : iAtendente`; operações `+buscarEstudante(matricula: String): Estudante` e `+buscarAtendente(matriculaFuncional: String): Atendente`; dependências («use») de `iEstudante` e `iAtendente` | Detalhe da classe criada pela decisão da Pergunta 1 = A. A busca é pela matrícula porque as operações f1–f6 do Astah identificam o estudante por `idEstudante` (matrícula). | Etapa 0, Passo 3 |
| 16 | Classes de Projeto | Associações de `TratadorDeEmprestimo` e `TratadorDeDevolucao` | Associações "delega" com `GrupoDeAcademicos` e `GrupoDeColaboradores` → uma associação "delega" com `Relacionamento` (as com `Acervo` e `Movimentacao` permanecem); acrescentar o atributo `-relogio : Clock` nos dois tratadores | Os grupos pertencem a outros componentes; a Biblioteca passa a acessá-los só pelo `Relacionamento` (interfaces `iEstudante`/`iAtendente`). O `Clock` define a data de "hoje", para testes e demonstração simularem atrasos. | Etapa 0, Passo 3 |
| 17 | Classes de Projeto | Classe `Devolucao` | Acrescentar `+getDataDevolucao(): LocalDate` e `+getEmprestimo(): Emprestimo` | Getters dos atributos já existentes, usados para calcular o atraso e, mais tarde, pelo evento de devolução (Req. 5). | Etapa 0, Passo 3 |

## Conferência: diagrama "Classes de Projeto" × código (fim da Etapa 0)

| Elemento no Astah | No código | Situação |
|---|---|---|
| `iPessoa`, `iAtendente`, `iEstudante`, `iLivro` | mesmos nomes e operações | igual |
| `iEmprestimo` | mesmo nome e operações | igual (f1, f3, f4 lançam "pendente: Etapa 1") |
| `iDevolucao` | sem `emitirBoletoDeMulta` | parcial: f6 entra na Etapa 4; as demais lançam "pendente: Etapa 1" |
| `iMulta`, `iQuitacao`, `iCobranca`, `TratadorDeMulta`, `BoletoDeMulta` | — | pendente: Etapa 4 (Strategy e Adapter) |
| `TratadorDePessoa`, `TratadorDeAtendente`, `TratadorDeEstudante`, `TratadorDeAcervo` | mesmos nomes e operações | igual (itens 11–13 acima) |
| `TratadorDeEmprestimo`, `TratadorDeDevolucao` | mesmos nomes e operações | igual (item 16 acima) |
| `GrupoDePessoas`, `GrupoDeColaboradores`, `GrupoDeAcademicos` | mesmos nomes e operações | igual (item 10 acima) |
| `Pessoa`, `Papel`, `Atendente`, `Estudante` | mesmos nomes | igual, sem `CRUD` (itens 4, 6–8) |
| `AcervoDeLivros` | `Acervo` | renomeada (item 1) |
| `Livro` | mesmo nome | sem `CRUD` e sem `setEstado` por enquanto (itens 4, 9) |
| `EstadoLivro` («enumeration») | `Situacao` | renomeada (item 5) |
| `Movimentacao` | mesmo nome, atributo e operações | igual |
| `Transacao`, `Reserva`, `Devolucao` | mesmos nomes | igual (itens 14, 17) |
| `Emprestimo` | mesmo nome | sem `renovar()` por enquanto: a regra de renovação entra na Etapa 1 (State) |
| `CRUD` | — | removida (item 4) |
| — | `Relacionamento` | nova (itens 3, 15) |

## Lista final por diagrama

_Entregue na Etapa 6._
