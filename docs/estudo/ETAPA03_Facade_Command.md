# Etapa 3 — Circulação: **Facade** (Req. 4) e **Command** (Req. 6)

---

## Parte A — Facade (Req. 4)

### A.1 O problema
Para efetivar um empréstimo é preciso: achar o estudante (Relacionamento), achar a reserva ativa
(Movimentacao), achar o livro (Acervo), mudar o estado do livro, remover a reserva, registrar o empréstimo e
confirmar. Se a interface (ou cada tratador) fizesse essa orquestração, ela conheceria as coleções e a ordem
interna — e uma falha no meio deixaria tudo pela metade.

### A.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Facade | «interface» `iCirculacao` / `ServicoDeCirculacao` | Casos de uso de circulação completos e transacionais; devolve comprovantes (DTOs). |
| Classes do subsistema | `Relacionamento`, `Movimentacao`, `Acervo`, `Livro` (+ estados) | Continuam com seus dados e regras; não conhecem a fachada. |
| Clientes | `TratadorDeEmprestimo` (f1–f4), `TratadorDeDevolucao` (f5, inspeção, reparo) | Mantêm os serviços e delegam à fachada. A web (Etapa 6) só conhece os tratadores. |
| DTOs | `ComprovanteReserva`, `ComprovanteEmprestimo`, `ComprovanteRenovacao`, `ComprovanteDevolucao`, `SituacaoLivroDTO` | Só dados (records); nenhuma coleção nem objeto interno. |
| Falha | `ReservaInvalidaException` | Reserva ausente, de outro estudante, ou estudante inexistente. |

### A.3 O que foi implementado
`ServicoDeCirculacao` tem uma operação por caso de uso (`reservar`, `consultarReserva`, `efetivarEmprestimo`,
`renovar`, `devolver`, `registrarInspecao`, `enviarParaReparo`, `retornarDoReparo`, `incinerar`,
`consultarSituacao`). Cada operação de escrita roda em `transacao.executar(...)` (Unidade de Trabalho da
Etapa 2). Os tratadores perderam as referências a `Acervo`, `Movimentacao` e `Relacionamento`.

### A.4 Cenário de verificação passo a passo — `R04_efetivarEmprestimo...`
```
TratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01")
 └ ServicoDeCirculacao.efetivarEmprestimo(1, "M-100", "F-01")   transacao.executar {
    1 relacionamento.buscarEstudante("M-100")      → Ana
    2 movimentacao.buscarReservaAtiva(1)           → Reserva nº 1 (Ana)
    3 exigirReservaDo(Ana, reserva)                → ok (mesma matrícula)
    4 acervo.buscarPorId(1)                        → livro (RESERVADO)
    5 livro.emprestarLivro(Ana, 13/11)             → Reservado.emprestarLivro → EMPRESTADO, vencimento 13/12
    6 movimentacao.removerReserva(reserva)
    7 movimentacao.registrarEmprestimo(novo Emprestimo nº 2)
    8 acervo.atualizar(livro)
    9 return ComprovanteEmprestimo{idEmprestimo=2, idLivro=1, M-100, 13/11, vencimento 13/12}
   } → confirmar()
```
O teste também verifica, por reflexão, que nenhum método de `iCirculacao`, `iEmprestimo` e `iDevolucao`
devolve `Collection`, `Map`, `Acervo`, `Movimentacao` ou `Relacionamento`.

### A.5 Na arguição, saiba explicar
**Quais classes a fachada coordena e em que ordem?** Na efetivação: Relacionamento (estudante) → Movimentacao
(reserva ativa) → validação da reserva → Acervo (livro) → Livro (`emprestarLivro`, State) → Movimentacao (remove
reserva, registra empréstimo) → Acervo (atualiza) → confirmação da unidade de trabalho.

**O que é desfeito se um passo falhar no meio?** Tudo: a operação inteira está dentro de
`transacao.executar`. Se o passo 7 lançar exceção, `desfazer()` devolve a cópia de trabalho ao último estado
confirmado — o livro volta a RESERVADO e a reserva removida no passo 6 reaparece. O teste
`falhaNoMeioDaOperacaoDesfazTudo` simula essa falha.

**Por que a interface não acessa Movimentacao diretamente?** Porque ela teria de conhecer as coleções e a ordem
dos passos, repetindo a regra em cada tela; qualquer mudança interna quebraria a interface. Com a fachada, a web
fala com os tratadores, os tratadores com `iCirculacao`, e o resto fica escondido.

---

## Parte B — Command (Req. 6)

### B.1 O problema
Uma renovação pode ser enviada duas vezes (duplo clique, resposta perdida na rede, F5). Se cada envio
executasse de novo, o estudante ganharia +60 dias. Precisamos que cada solicitação seja um **objeto** com
identificador, autor e instante, e que um executor central garanta "cada id executa uma vez".

### B.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Command | «abstract» `SolicitacaoDeCirculacao` | `id` (UUID), `autor`, `criadaEm` (somente leitura) e `executar()`. |
| ConcreteCommand | `SolicitacaoDeReserva`, `SolicitacaoDeRenovacao` | Guardam os parâmetros e chamam o receptor. |
| Invoker | `ExecutorDeSolicitacoes` (+ `RegistroDeSolicitacoes`) | Dispara a execução, garante idempotência, grava e reaproveita o resultado. |
| Receiver | `iCirculacao` (`ServicoDeCirculacao`) | Executa o caso de uso; as regras estão no Livro (State). |
| Client | `TratadorDeEmprestimo` (f1, f4) | Cria a solicitação concreta (com o id vindo da tela) e a entrega ao executor. |
| Resultado | `ResultadoSolicitacao` (`EXECUTADA`, `RECUSADA`, `EM_PROCESSAMENTO` + mensagem) | Gravado na seção `solicitacoes` do JSON. |

### B.3 O que foi implementado
```java
public ResultadoSolicitacao submeter(SolicitacaoDeCirculacao s) {
    if (!registro.reservarExecucao(s.getId()))                 // putIfAbsent: chave única, atômica
        return registro.resultadoDe(s.getId()).orElse(emProcessamento(s));
    try { resultado = s.executar(); }                          // receptor + State
    catch (ExcecaoDeDominio recusa) { resultado = recusada(s, recusa.getMessage()); }
    catch (RuntimeException falha) { registro.liberar(s.getId()); throw falha; }
    registro.concluir(resultado);                              // gravado (JSON) para as repetições
    return resultado;
}
```

### B.4 Cenários de verificação passo a passo
**R06_mesmaRenovacaoExecutadaDuasVezesComOMesmoIdAcrescentaApenasTrintaDias** (vencimento 13/12/2026)
```
renovarEmprestimo(S42, 1, "M-100")
  TratadorDeEmprestimo cria SolicitacaoDeRenovacao(S42, autor M-100, agora, idLivro 1)
  ExecutorDeSolicitacoes.submeter: reservarExecucao(S42) → true
  s42.executar() → circulacao.renovar(1, "M-100") → Emprestado.renovar: 0 = 0 ✔ → 12/01/2027
  registro.concluir(EXECUTADA "renovado até 2027-01-12")
renovarEmprestimo(S42, 1, "M-100")   (reenvio)
  reservarExecucao(S42) → false (já existe) → resultadoDe(S42) → o MESMO resultado; renovar NÃO é chamado
vencimento final = 12/01/2027 (só +30), numRenovacoes = 1
```
**R06_solicitacoesDiferentesContinuamLimitadasADuasRenovacoes**
```
S1 → EXECUTADA (12/01); S2 → EXECUTADA (11/02);
S3 → executa, Renovado.renovar: 2 < 2 ✘ → RenovacaoNegadaException → executor grava RECUSADA
     "Renovação recusada para o livro 1: limite de 2 renovações"
```
**Concorrência** (`submissoesSimultaneasDoMesmoIdExecutamUmaUnicaVez`): 8 threads submetem o mesmo id ao mesmo
tempo; `putIfAbsent` deixa só uma vencer; o livro termina com exatamente 1 renovação.

### B.5 Na arguição, saiba explicar
**Quem cria, quem invoca e quem executa a solicitação?** Cria: `TratadorDeEmprestimo` (Client), com o id gerado
pela tela. Invoca: `ExecutorDeSolicitacoes` (Invoker). Executa: o receptor `iCirculacao`/`ServicoDeCirculacao`,
e quem decide a regra é o `Livro` (State). A solicitação só guarda os parâmetros e chama o receptor.

**Como a repetição é detectada mesmo com duas submissões simultâneas?** `RegistroDeSolicitacoes.reservarExecucao`
usa `ConcurrentHashMap.putIfAbsent(id, ...)`, uma inserção atômica com chave única: das duas threads, só uma
recebe "inserido"; a outra recebe `false` e devolve o resultado gravado (ou "em processamento"). Os ids nunca são
removidos depois de concluídos, e os resultados ficam no JSON — então um reenvio depois de reiniciar a aplicação
também não executa de novo.

**Por que não há operação de desfazer?** Porque a solicitação representa um fato físico (o livro foi
entregue/renovado no balcão); "desfazer" por software não devolve o livro. Correções são operações
administrativas próprias.

---

## Exercícios de alteração ao vivo

**Facade — nova operação "cancelar reserva".** Acrescentar `cancelarReserva(idLivro, idEstudante)` em
`iCirculacao`/`ServicoDeCirculacao` (busca a reserva, `livro.cancelarReserva()` — ver Etapa 1 —, remove a
reserva, confirma) e um método no tratador. A web não muda de dependência; `Movimentacao` e `Acervo` não mudam.

**Command — nova solicitação "SolicitacaoDeDevolucao".** Criar a subclasse com `executar()` chamando
`circulacao.devolver(...)`. O executor e o registro não mudam — ganham idempotência de graça.

## Autoavaliação
1. Quais passos da efetivação ficam dentro da transação? 2. Por que os tratadores não conhecem a Movimentacao?
3. Quem é o Invoker? 4. O que impede duas renovações com o mesmo id? 5. O que acontece com a 3ª renovação
submetida por um id novo?

**Respostas:** 1. Todos (do passo 1 ao 9). 2. Porque delegam à fachada `iCirculacao`. 3. `ExecutorDeSolicitacoes`.
4. `putIfAbsent` no registro + resultado gravado. 5. Executa, o estado recusa, e o resultado gravado é RECUSADA.
