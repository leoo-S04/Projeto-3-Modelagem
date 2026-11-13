# Rastreabilidade

Exigência do Trabalho 03: para cada requisito, o padrão, as classes que desempenham cada papel e o teste que
comprova o cenário de verificação. Preenchida ao final de cada etapa.

Legenda: ✅ implementado e testado · ⬜ pendente (etapa prevista)

## Requisitos do Trabalho 03 (padrões GoF)

| Req | Padrão | Papel GoF → Classe | Teste do cenário | Situação |
|---|---|---|---|---|
| 1 | State | Context → `Livro`; State → `EstadoLivro`; ConcreteState → `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado`; Clientes → `TratadorDeEmprestimo`, `TratadorDeDevolucao`, `TratadorDeAcervo` | `R01_CicloDeVidaDoLivroTest`: `R01_terceiraRenovacaoRecusadaSemAlterarVencimento`, `R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado` (+ `EstadoLivroTest`) | ✅ |
| 2 | Strategy | _a definir na Etapa 4_ | `R02_...` | ⬜ Etapa 4 |
| 3 | Adapter | _a definir na Etapa 4_ | `R03_...` | ⬜ Etapa 4 |
| 4 | Facade | _a definir na Etapa 3_ | `R04_...` | ⬜ Etapa 3 |
| 5 | Observer | _a definir na Etapa 4_ | `R05_...` | ⬜ Etapa 4 |
| 6 | Command | _a definir na Etapa 3_ | `R06_...` | ⬜ Etapa 3 |
| 7 | Template Method | _a definir na Etapa 2_ | `R07_...` | ⬜ Etapa 2 |
| 8 | Abstract Factory | _a definir na Etapa 2_ | `R08_...` | ⬜ Etapa 2 |
| 9 | Decorator | _a definir na Etapa 5_ | `R09_...` | ⬜ Etapa 5 |
| 10 | Composite | _a definir na Etapa 5_ | `R10_...` | ⬜ Etapa 5 |

## Base preservada dos Trabalhos 01 e 02 (Etapa 0)

| Regra de negócio | Componente / pacote | Classes | Teste | Situação |
|---|---|---|---|---|
| 1.1–1.4 Manter Livro | Biblioteca / `biblioteca` | `iLivro`, `TratadorDeAcervo`, `Acervo`, `Livro`, `Situacao` | `TratadorDeAcervoTest` | ✅ |
| 2.1–2.4 Manter Pessoa | RH / `rh` | `iPessoa`, `TratadorDePessoa`, `GrupoDePessoas`, `Pessoa` | `TratadorDePessoaTest` | ✅ |
| 3.1–3.4 Manter Estudante | Academico / `academico` | `iEstudante`, `TratadorDeEstudante`, `GrupoDeAcademicos`, `Estudante`, `Papel` | `TratadorDeEstudanteTest` | ✅ |
| 4.1–4.4 Manter Atendente | RH / `rh` | `iAtendente`, `TratadorDeAtendente`, `GrupoDeColaboradores`, `Atendente`, `Papel` | `TratadorDeAtendenteTest` | ✅ |
| 5 Reservar livro (f1) | Biblioteca | `iEmprestimo`, `TratadorDeEmprestimo`, `Livro` + estados, `Reserva` | `R01_...`, `EstadoLivroTest` | ✅ |
| 6 Consultar reserva (f2) | Biblioteca | `TratadorDeEmprestimo`, `Movimentacao`, `Reserva` | `TratadorDeEmprestimoTest`, `MovimentacaoTest` | ✅ |
| 7 Emprestar livro (f3) | Biblioteca | `TratadorDeEmprestimo`, `Reservado`, `Emprestimo` | `R01_...`, `EstadoLivroTest` | ✅ |
| 8 Renovar empréstimo (f4) | Biblioteca | `TratadorDeEmprestimo`, `Emprestado`, `Renovado` | `R01_terceiraRenovacaoRecusadaSemAlterarVencimento` | ✅ |
| 9 Registrar devolução (f5) e inspeção | Biblioteca | `iDevolucao`, `TratadorDeDevolucao`, `Devolvido`, `Reparando`, `Devolucao` | `TratadorDeDevolucaoTest`, `DevolucaoTest` | ✅ |
| 10 Emitir boleto de multa (f6) | Financeiro | — | — | ⬜ Etapa 4 |
| Pontos internos de acesso (T02) | Biblioteca | `Acervo`, `Relacionamento`, `Movimentacao` | `TratadorDeEmprestimoTest`, `MovimentacaoTest` | ✅ |
