# Etapa 0 — Fundação: guia de estudo

> A Etapa 0 **não aplica padrão GoF**. Ela monta a base dos Trabalhos 01/02 sobre a qual os 10 padrões serão
> acrescentados: componentes, entidades, tratadores e os pontos internos de acesso (Acervo, Relacionamento,
> Movimentacao). Na arguição, o professor pode apontar qualquer uma destas classes.

## 1. O que existe ao final da etapa

```
biblioteca.jar  (um único artefato, diagrama de implantação)
├── rh          → componente RH          fornece iPessoa, iAtendente
├── academico   → componente Acadêmico   fornece iEstudante        requer iPessoa
├── biblioteca  → componente Biblioteca  fornece iLivro, iEmprestimo, iDevolucao
│                                        requer iEstudante, iAtendente (iMulta na Etapa 4)
├── financeiro  → componente Financeiro  (Etapa 4)
└── web         → Interface Web          (Etapa 6; só chama tratadores)
```

Comando único: `mvnw.cmd test` (Windows) ou `./mvnw test`. O Maven Wrapper baixa o Maven na 1ª vez; o
`pom.xml` compila para Java 17 (`maven.compiler.release`) e gera `target/biblioteca.jar` (`finalName`).

## 2. Classes e responsabilidades

| Classe | Pacote | Responsabilidade | Quem a chama | Quem ela chama |
|---|---|---|---|---|
| `Pessoa` | rh | Dados da pessoa (CPF fixo; nome, sobrenome, e-mail atualizáveis) | grupos, tratadores | — |
| `Papel` (abstrata) | rh | id + pessoa; base de Estudante e Atendente | — | — |
| `Atendente` | rh | Papel com matrícula funcional | GrupoDeColaboradores, Relacionamento | — |
| `Estudante` | academico | Papel com matrícula (= idEstudante de f1–f6) | GrupoDeAcademicos, Relacionamento | — |
| `GrupoDePessoas`, `GrupoDeColaboradores`, `GrupoDeAcademicos` | rh / academico | Guardam as coleções; adicionar, remover, buscar | tratadores de cadastro | — |
| `iPessoa` / `TratadorDePessoa` | rh | Regras 2.1–2.4 | Interface Web (Etapa 6), TratadorDeEstudante, TratadorDeAtendente | GrupoDePessoas |
| `iAtendente` / `TratadorDeAtendente` | rh | Regras 4.1–4.4 | Web, Relacionamento | GrupoDeColaboradores, `iPessoa` |
| `iEstudante` / `TratadorDeEstudante` | academico | Regras 3.1–3.4 | Web, Relacionamento | GrupoDeAcademicos, `iPessoa` |
| `Livro`, `Situacao` | biblioteca | Livro com id, título e estado (nasce LIVRE); enum com os 7 estados | Acervo, tratadores | — |
| `Acervo` | biblioteca | Ponto interno de acesso aos livros; `listar()` devolve cópia somente leitura | TratadorDeAcervo, tratadores de circulação | — |
| `iLivro` / `TratadorDeAcervo` | biblioteca | Regras 1.1–1.4 | Web | Acervo |
| `Transacao` (abstrata) e `Reserva`, `Emprestimo`, `Devolucao` | biblioteca | Data, livro, estudante, atendente; `Devolucao.calcularDiasAtraso()` | tratadores de circulação, Movimentacao | Emprestimo (vencimento) |
| `Movimentacao` | biblioteca | Ponto interno de acesso às transações; busca por (idLivro, matrícula) e por tipo | tratadores de circulação | Transacao.envolve |
| `Relacionamento` | biblioteca | Ponto interno de acesso a estudantes e atendentes, **pelas interfaces** | tratadores de circulação | `iEstudante`, `iAtendente` |
| `iEmprestimo` / `TratadorDeEmprestimo` | biblioteca | f1–f4 (só f2 funciona na Etapa 0) | Web | Acervo, Movimentacao, Relacionamento, Clock |
| `iDevolucao` / `TratadorDeDevolucao` | biblioteca | f5, inspeção e reparo (Etapa 1); f6 na Etapa 4 | Web | Acervo, Movimentacao, Relacionamento, Clock |

## 3. Ideias que vocês precisam saber explicar

**Delegação.** O tratador não guarda dados: recebe o pedido e repassa a quem guarda (`TratadorDeAcervo` →
`Acervo`). Assim, a regra ("se já existe, devolve o existente") fica no tratador e o armazenamento fica no
grupo/ponto interno. É o mecanismo exigido pelo Trabalho 02.

**Interfaces fornecidas e requeridas.** Cada pacote é um componente. Um componente só usa outro por uma
interface: `TratadorDeEstudante` recebe um `iPessoa`, não o `GrupoDePessoas`. Se o RH trocar a forma de guardar
pessoas, o Acadêmico não muda. O mesmo vale para `Relacionamento`, que usa `iEstudante` e `iAtendente`.

**Pontos internos de acesso.** `Acervo` (livros), `Relacionamento` (pessoas nos papéis de estudante/atendente)
e `Movimentacao` (transações) são os únicos lugares por onde os tratadores de circulação chegam aos dados. A
web nunca os conhece.

**Por que não há `CRUD` nas entidades.** No Astah, `Livro.inserir()` exigiria que o livro soubesse onde é
gravado. Isso conflita com os repositórios do Req. 8 (Abstract Factory) e com a rotina de cadastro do Req. 7
(Template Method). Decisão da equipe: o cadastro fica nos tratadores e grupos.

**`Clock` injetado.** Os tratadores de circulação recebem um `java.time.Clock`. Em produção é o relógio do
sistema; nos testes, `Clock.fixed(13/11/2026)`. Assim um teste de "devolução com 3 dias de atraso" não precisa
esperar 3 dias, e a demonstração poderá ajustar a data pela tela de administração (Etapa 6).

**Por que f1, f3, f4 e f5 ainda lançam exceção.** Elas dependem das regras de estado do Livro. Escrevê-las agora
exigiria `if (estado == ...)` nos tratadores — exatamente o que o Req. 1 proíbe. Elas nascem na Etapa 1, com o State.

## 4. Fluxos passo a passo (sequência em texto)

**a) Estudante com CPF não cadastrado (regra 3.1)**
```
Teste → TratadorDeEstudante.inserirEstudante("999", "M-200")
  1  grupoDeAcademicos.buscarPorMatricula("M-200")  → null        (não existe ainda)
  2  pessoas.consultarPessoa("999")                 → null        (via iPessoa → TratadorDePessoa → GrupoDePessoas)
  3  pessoa == null → return null                                 (nenhum new Estudante, nenhum adicionar)
Teste → consultarEstudante("M-200") → null                        (prova: nada foi gravado)
```

**b) Estudante duplicado (regra 3.1)**
```
inserirEstudante("111", "M-100")  → cria Estudante(id=1) e adiciona
inserirEstudante("111", "M-100")  → buscarPorMatricula("M-100") encontra → devolve o MESMO objeto (id=1)
```

**c) f2 consultar reserva (regra 6)**
```
Teste → TratadorDeEmprestimo.consultarReserva(1, "M-100")
  1  movimentacao.buscarReserva(1, "M-100")
  1.1   para cada transacao: é Reserva? envolve(1, "M-100")? → encontra
  ← Reserva(data = 13/11/2026, livro 1, estudante M-100)
```

**d) Dias de atraso (base da multa — Reqs. 2 e 5)**
```
Emprestimo.dataVencimento = 10/11 ; Devolucao.dataDevolucao = 13/11
calcularDiasAtraso() = max(0, DAYS.between(10/11, 13/11)) = 3 ; estaEmAtraso() = true
Devolução em 05/11 → DAYS.between = -5 → max(0, -5) = 0 (nunca atraso negativo)
```

## 5. Perguntas prováveis na arguição (e respostas)

1. **Por que `TratadorDeEstudante` recebe `iPessoa` e não `GrupoDePessoas`?** Porque Estudante está no componente
   Acadêmico e Pessoa no RH; o diagrama de componentes diz que o Acadêmico *requer* `iPessoa`. Depender da
   interface mantém os componentes independentes.
2. **O que é o `Relacionamento` e por que ele existe se já há três grupos?** É o ponto único de acesso da
   Biblioteca a estudantes e atendentes, exigido pelo T02/T03. Ele não guarda dados: consulta `iEstudante` e
   `iAtendente`, que a Biblioteca requer.
3. **Por que `Acervo.listar()` devolve lista somente leitura?** Para quem chama não conseguir alterar a coleção
   interna por fora; só o `Acervo` adiciona e remove. Isso prepara o Req. 4 (fachada não expõe coleções).
4. **Como o teste controla a data de hoje?** Com `Clock.fixed(...)` passado ao construtor do tratador;
   `LocalDate.now(relogio)` devolve a data fixada.
5. **Onde fica a regra "só cadastra estudante se a pessoa existir"?** Em `TratadorDeEstudante.inserirEstudante`,
   antes de criar o objeto; por isso nada é gravado quando falha. (No Req. 7, isso vira o gancho
   `verificarPreRequisitos` do Template Method.)
6. **Por que o projeto compila para Java 17 se o JDK da máquina é mais novo?** O diagrama de implantação define
   JVM 17; `maven.compiler.release=17` garante bytecode e API compatíveis com ela.

## 6. Exercício de alteração ao vivo

**Pedido:** "Acrescentem ao cadastro de livros uma operação que liste todos os livros."

Passos:
1. Em `iLivro`, declarar `List<Livro> listarLivros();`.
2. Em `TratadorDeAcervo`, implementar delegando: `return acervo.listar();`.
3. Teste em `TratadorDeAcervoTest`: inserir 2 livros e verificar `listarLivros().size() == 2`.

Por que é simples: o caminho interface → tratador → ponto interno já existe; `Acervo` **não muda** (já tem
`listar()`, que devolve cópia somente leitura) e nenhuma outra classe é tocada.

## 7. O que muda nas próximas etapas

- **Etapa 1 (State):** `Livro` passa a delegar a objetos de estado (`EstadoLivro`); f1, f3, f4, f5, inspeção e
  reparo passam a funcionar. A decidir no plano: onde ficam `numRenovacoes`/vencimento e os nomes dos eventos.
- **Etapa 2:** `Movimentacao` separada em três associações (reservas, empréstimos, devoluções) — **decidido pela
  equipe**; Template Method nos cadastros; repositórios em memória e JSON.
- **Etapa 4:** f6, Financeiro e bancos simulados.
