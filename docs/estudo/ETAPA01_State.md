# Etapa 1 — Ciclo de vida do Livro: padrão **State** (Req. 1)

## 1. O problema
O comportamento do Livro depende do estado em que ele está: um livro LIVRE pode ser reservado; um DEVOLVIDO
ainda não inspecionado, não. Sem o padrão, essa regra vira uma cadeia de `if (estado == ...)` espalhada pelos
tratadores. Com o **State**, cada estado é uma classe que só implementa os eventos válidos para ele; o `Livro`
apenas repassa o evento ao estado atual.

## 2. Papéis do padrão → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Context | `Livro` | Guarda o estado atual (`estado : EstadoLivro`) e os dados do ciclo (`reservante`, `vencimento`, `numRenovacoes`). Cada evento público só faz `estado.evento(this, ...)`. |
| State | `EstadoLivro` (abstrata) | Declara os 9 eventos. A implementação padrão **recusa** (`OperacaoInvalidaException`) sem alterar o livro. Tem `para(Situacao)` para reconstituir o estado gravado. |
| ConcreteState | `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado` | Cada um sobrescreve **só** as transições que saem dele; guarda e ação ficam no corpo do método. Sem atributos → uma instância única (`INSTANCIA`). |
| Clientes | `TratadorDeEmprestimo`, `TratadorDeDevolucao`, `TratadorDeAcervo` | Buscam o livro (Acervo) e o estudante (Relacionamento), disparam o evento e registram a transação. **Nenhum `if` sobre a situação.** |
| Valor persistido | `Situacao` (enum) | O que é gravado e exibido (`"RENOVADO"`). |
| Recusas | `OperacaoInvalidaException`, `RenovacaoNegadaException` | Lançadas antes de qualquer alteração. |

## 3. O que foi implementado (passo a passo)

1. **`EstadoLivro`**: cada evento (`reservarLivro`, `emprestarLivro`, `renovarEmprestimo`, `registrarDevolucao`,
   `inspecionar`, `enviarParaReparo`, `retornarDoReparo`, `incinerar`, `excluirLivro`) tem corpo padrão
   `throw recusa("evento")`.
2. **7 estados concretos** — tabela de transições (igual à máquina de estados do Astah):

| Origem | Evento [guarda] / ação | Destino | Método |
|---|---|---|---|
| LIVRE | reservarLivro [estudante cadastrado] / reservante := e | RESERVADO | `Livre.reservarLivro` |
| LIVRE | enviarParaReparo [dano no acervo] | REPARANDO | `Livre.enviarParaReparo` |
| LIVRE | excluirLivro | final | `Livre.excluirLivro` |
| RESERVADO | emprestarLivro [mesmo estudante] / vencimento := data + 30 | EMPRESTADO | `Reservado.emprestarLivro` |
| EMPRESTADO | renovarEmprestimo [numRenovacoes = 0] / vencimento += 30; numRenovacoes++ | RENOVADO | `Emprestado.renovarEmprestimo` |
| RENOVADO | renovarEmprestimo [numRenovacoes < 2] / idem | RENOVADO | `Renovado.renovarEmprestimo` |
| EMPRESTADO, RENOVADO | registrarDevolucao | DEVOLVIDO | `Emprestado/Renovado.registrarDevolucao` |
| DEVOLVIDO | inspecionar [bom estado] / limparCiclo | LIVRE | `Devolvido.inspecionar` |
| DEVOLVIDO | inspecionar [danificado] | REPARANDO | `Devolvido.inspecionar` |
| REPARANDO | retornarDoReparo / limparCiclo | LIVRE | `Reparando.retornarDoReparo` |
| REPARANDO | incinerar (laudo irrecuperável) | INCINERADO | `Reparando.incinerar` |
| INCINERADO | excluirLivro | final | `Incinerado.excluirLivro` |

3. **`Livro`** virou o Context: `setEstado` público foi trocado por `mudarPara` (visível só no pacote), para que
   apenas os estados mudem o estado. Os dados do ciclo saíram do `Emprestimo` e foram para o `Livro`.
4. **Tratadores** (f1, f3, f4, f5, inspeção, reparo, incinerar, excluir): buscam os objetos, disparam o evento e,
   só se ele for aceito, registram a transação na `Movimentacao`. A ordem importa: se o estado recusar, nada é
   gravado.
5. **`Devolucao`** guarda uma cópia do vencimento (o Livro limpa o ciclo ao voltar a LIVRE).

## 4. Cenários de verificação passo a passo

**R01_terceiraRenovacaoRecusadaSemAlterarVencimento** (relógio fixo em 13/11/2026)
```
reservarLivro(1, "M-100", "F-01")   Livre.reservarLivro      → RESERVADO, reservante = Ana
emprestarLivro(1, "M-100", "F-01")  Reservado.emprestarLivro → EMPRESTADO, vencimento = 13/12/2026
renovarEmprestimo(1, "M-100")       Emprestado.renovar: 0 = 0 ✔ → prorrogar(30) → 12/01/2027, n = 1 → RENOVADO
renovarEmprestimo(1, "M-100")       Renovado.renovar:   1 < 2 ✔ → prorrogar(30) → 11/02/2027, n = 2
renovarEmprestimo(1, "M-100")       Renovado.renovar:   2 < 2 ✘ → throw RenovacaoNegadaException
                                    prorrogar NÃO é chamado → vencimento continua 11/02/2027, n = 2
```

**R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado**
```
reservar → emprestar → registrarDevolucao(1, "M-100", "F-01")  → DEVOLVIDO
reservarLivro(1, "M-100", "F-01")
  TratadorDeEmprestimo → Acervo.buscarPorId(1) → livro; Relacionamento.buscarEstudante("M-100") → Ana
  livro.reservarLivro(Ana) → estado.reservarLivro(livro, Ana)
  Devolvido NÃO sobrescreve reservarLivro → EstadoLivro.reservarLivro → throw OperacaoInvalidaException
  ("Operação reservarLivro não permitida: livro está DEVOLVIDO")
  a linha "new Reserva(...)" nunca é executada → consultarReserva(1, "M-100") = null
```

## 5. Na arguição, saiba explicar

**Onde está a guarda da renovação e por que ela é avaliada antes de alterar o vencimento?**
Em `Emprestado.renovarEmprestimo` (`numRenovacoes != 0` → recusa) e `Renovado.renovarEmprestimo`
(`numRenovacoes >= 2` → recusa). A guarda é a **primeira linha** do método: se for falsa, a exceção sai antes de
`prorrogar(30)`. Assim a 3ª tentativa não muda vencimento nem contador — não há nada a desfazer.

**O que acontece quando um evento chega a um estado que não o aceita?**
O estado concreto não sobrescreveu aquele método, então roda a implementação herdada de `EstadoLivro`, que lança
`OperacaoInvalidaException` sem tocar no livro. O tratador também não grava nada, porque a gravação vem depois
do evento. A exceção sobe com a mensagem (na Etapa 6, a tela a exibe).

**Como o estado é gravado e reconstituído na persistência?**
Grava-se o valor do enum (`livro.getEstado()` → `"RENOVADO"`), junto com `reservante`, `vencimento` e
`numRenovacoes`. Ao ler, `EstadoLivro.para(Situacao.RENOVADO)` devolve `Renovado.INSTANCIA`. Como os estados não
têm atributos, a instância única basta. (A gravação no arquivo JSON é feita na Etapa 2.)

**Outras perguntas prováveis**
- *Por que `mudarPara` não é público?* Para ninguém fora do pacote pular a máquina de estados.
- *Por que os estados são instâncias únicas?* Não têm atributos; os dados variáveis ficam no `Livro`.
- *O `if (emBomEstado)` em `Devolvido.inspecionar` não quebra a regra?* Não: é a **guarda** de duas transições
  que saem do mesmo estado, e fica dentro da classe do estado de origem. A regra proíbe `if` sobre a situação
  do livro **nos tratadores**.

## 6. Exercício de alteração ao vivo
**Pedido:** "permitir cancelar uma reserva (RESERVADO → LIVRE)."
1. `EstadoLivro`: `void cancelarReserva(Livro l) { throw recusa("cancelarReserva"); }`.
2. `Reservado`: sobrescrever — `l.limparCiclo(); l.mudarPara(Livre.INSTANCIA);`.
3. `Livro`: `public void cancelarReserva() { estado.cancelarReserva(this); }`.
4. Teste: reservar, cancelar → LIVRE; cancelar em LIVRE → `OperacaoInvalidaException`.

Nenhum dos outros 6 estados muda: eles herdam a recusa padrão. Nenhum tratador ganha `if`.

## 7. Autoavaliação
1. Qual classe decide se a reserva é aceita? 2. O que garante que a 3ª renovação não muda o vencimento?
3. Onde estão as guardas da máquina de estados? 4. Por que `Devolucao` copia o vencimento?
5. Como o estado volta do arquivo para o objeto?

**Respostas:** 1. O estado atual do livro (`Livre.reservarLivro` aceita; os demais recusam). 2. A guarda vem antes
de `prorrogar`. 3. No corpo do método do estado de origem. 4. Porque o Livro limpa o ciclo ao voltar a LIVRE e a
multa precisa do vencimento. 5. `EstadoLivro.para(Situacao)`.
