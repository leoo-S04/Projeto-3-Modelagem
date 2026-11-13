# Etapa 4 — Devolução e financeiro: **Observer** (Req. 5), **Strategy** (Req. 2) e **Adapter** (Req. 3)

Visão do fluxo completo desta etapa:
```
f5 registrarDevolucao ─► ServicoDeCirculacao.devolver (commit) ─► PublicadorDeDevolucao.publicar(evento)
                                                                    ├─ OuvinteInspecao    → AgendaDeInspecao
                                                                    ├─ OuvinteMulta       → iMulta.gerarMulta ─► ServicoDeMulta
                                                                    │                          ├─ SeletorDePolitica.vigente() → PoliticaDeMulta.calcular  (Strategy)
                                                                    │                          └─ iCobranca.registrarBoleto → AdaptadorBancoA/B → ApiBancoA/B (Adapter)
                                                                    ├─ OuvinteIndicadores → PainelDeIndicadores
                                                                    └─ OuvinteEmail       → ServicoDeEmail
Banco paga ─► webhook/notificação ─► Adaptador.receber... ─► iQuitacao.registrarQuitacao (uma única vez)
```

---

## Parte A — Observer (Req. 5)

### A.1 O problema
Uma devolução dispara várias reações (agendar inspeção, gerar multa, atualizar indicadores, mandar e-mail) — e
novas reações aparecem com o tempo. Se o tratador chamasse cada uma, ele cresceria a cada pedido novo e uma
falha no e-mail poderia derrubar a devolução.

### A.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Subject | `PublicadorDeDevolucao` | `registrar`, `remover`, `publicar`; isola a falha de cada ouvinte (fila de reenvio). |
| Observer | «interface» `OuvinteDeDevolucao` | `aoDevolver(EventoDevolucao)`. |
| ConcreteObserver | `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail` | Reagem com os dados do evento. |
| Estado notificado | «value» `EventoDevolucao` | Leva vencimento, data da devolução e estudante (modelo push); `diasAtraso()`. |
| Quem dispara | `TratadorDeDevolucao` (f5) | Conclui a devolução pela fachada e **depois** publica. |
| Apoio | `AgendaDeInspecao`, `PainelDeIndicadores`, `ServicoDeEmail(Simulado)` | Destinos das reações. |

### A.3 Fluxo do cenário — `R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores`
```
Empréstimo em 13/11/2026 → vencimento 13/12. Relógio avança 33 dias → hoje 16/12.
TratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01")
 1 circulacao.devolver(...)  → livro DEVOLVIDO, Devolucao nº 3, confirmar()  ← a devolução já está gravada
 2 publicador.publicar(EventoDevolucao{venc 13/12, devolvido 16/12, cpf 111})  → diasAtraso() = 3
   2.1 OuvinteInspecao.aoDevolver    → agenda.agendar(livro 1)
   2.2 OuvinteMulta.aoDevolver       → 3 > 0 → iMulta.gerarMulta("111", 3, 3) → boleto de R$ 3,00 (ORIGINAL)
   2.3 OuvinteIndicadores.aoDevolver → painel: devoluções 1, atrasadas 1, dias 3
   2.4 OuvinteEmail.aoDevolver       → e-mail "Devolução registrada: Dom Casmurro"
Livro continua DEVOLVIDO até TratadorDeDevolucao.inspecionar(1, true/false).
```
No prazo (`R05_devolucaoNoPrazoNaoGeraMulta`): `diasAtraso() = 0` → `OuvinteMulta` não chama o Financeiro.

### A.4 Na arguição, saiba explicar
**Como um novo ouvinte é incluído sem alterar o tratador?** Cria-se a classe que implementa `OuvinteDeDevolucao`
e registra-se no publicador na montagem (`Sistema`: `publicador.registrar(new MeuOuvinte(...))`). O
`TratadorDeDevolucao` só conhece o publicador e o evento. (Teste `novoOuvinteEIncluidoSemAlterarOTratador`.)

**O que acontece quando o envio de e-mail falha?** O `PublicadorDeDevolucao.publicar` envolve cada ouvinte em
`try/catch`: a exceção vira um registro na fila de reenvio (`falhasPendentes`), os outros ouvintes continuam e a
devolução — já confirmada antes da publicação — não é desfeita. `reenviarFalhas()` tenta de novo depois.

**Por que o evento leva os dados em vez de o ouvinte consultá-los depois?** Porque, ao devolver, o empréstimo
ativo é removido da Movimentacao: o ouvinte não teria mais onde buscar o vencimento. Além disso, o evento
registra o fato como ele era naquele instante (imutável) e os ouvintes não dependem do subsistema de
circulação.

---

## Parte B — Strategy (Req. 2)

### B.1 O problema
O jeito de calcular a multa muda (R$ 1,00/dia, com teto, com tolerância...), mas quem usa o cálculo (geração da
multa, simulação) e a emissão do boleto não deveriam mudar.

### B.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Strategy | «interface» `PoliticaDeMulta` | `id()`, `calcular(dias): BigDecimal`. |
| ConcreteStrategy | `MultaDiaria` (ORIGINAL), `MultaComTeto` (TETO), `MultaComTolerancia` (TOLERANCIA) | d × 1,00; min(d × 1,00; 20,00); max(0; d − 2) × 1,00. |
| Context | `ServicoDeMulta`, `ServicoDeSimulacao` | Calculam sem saber qual política está em uso. |
| Seleção | `SeletorDePolitica` + `ConfiguracaoAdministrativa` | Traduzem o id configurado no objeto de política; recusam id inexistente. |
| Resultado | `Multa` (record) | Grava valor e política aplicada na emissão. |

### B.3 Fluxo do cenário — `R02_cincoDiasDeAtrasoValemCincoReaisNaOriginalETresNaTolerancia`
```
MultaDiaria.calcular(5)        = 1,00 × 5         = 5,00
MultaComTolerancia.calcular(5) = 1,00 × max(0, 5−2) = 3,00
MultaComTeto.calcular(5)       = min(5,00; 20,00)  = 5,00
calcular(0) = 0,00 em todas (R02_semAtrasoOValorEZero)
```
Na produção: `ServicoDeMulta.gerarMulta` → `seletor.vigente()` (lê "ORIGINAL" da configuração) →
`politica.calcular(d)` → se valor > 0, `new Multa(..., valor, politica.id())` → emite o boleto.

### B.4 Na arguição, saiba explicar
**Como uma nova política é incluída sem alterar o serviço de multas?** Nova classe que implementa
`PoliticaDeMulta` + um registro na lista do `SeletorDePolitica` (na montagem, `Sistema`). `ServicoDeMulta`,
`ServicoDeSimulacao` e o boleto não mudam — não há `if` por política em lugar nenhum.

**Por que uma troca de política não recalcula multas já emitidas?** Porque a `Multa` grava o **valor** e o **id da
política** no momento da emissão e é imutável (record). O boleto usa esse valor. O teste
`trocaDePoliticaNaoRecalculaMultasJaEmitidas` emite uma multa ORIGINAL (R$ 5,00), troca para TOLERANCIA e
verifica que ela continua R$ 5,00, enquanto a nova sai R$ 3,00.

**Como os dias de atraso e os valores monetários são calculados?** Dias corridos inteiros:
`ChronoUnit.DAYS.between(vencimento, devolução)`, com mínimo 0 (devolução antecipada não dá atraso negativo).
Dinheiro em `BigDecimal` com escala 2 e arredondamento explícito (`HALF_EVEN`), nunca `double`.

---

## Parte C — Adapter (Req. 3)

### C.1 O problema
Os bancos A e B fazem a mesma coisa com APIs incompatíveis: A quer o valor em **centavos** (`long`), CPF sem
formatação e identifica o título por "nosso número"; B quer valor **decimal**, CPF **formatado**, identifica por
"txid" e manda notificação em **XML** (A manda **JSON**). O Financeiro não pode ter `if (banco == A)`.

### C.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Target (saída) | «interface» `iCobranca` | `registrarBoleto(idBoleto, cpf, valor, email)`, `consultarPagamento(idExterno)`, `codigoDoBanco()`. |
| Target (entrada) | «interface» `iQuitacao` | `registrarQuitacao(ConfirmacaoPagamento)`. |
| Adapter | `AdaptadorBancoA`, `AdaptadorBancoB` | Convertem valores, documentos, ids e situações; traduzem as notificações. |
| Adaptee | «external» `ApiBancoA`, `ApiBancoB` (pacote `financeiro.bancos`, simulados) | APIs dos bancos, usadas como são. |
| Client | `ServicoDeMulta` (saída); o banco que envia a notificação (entrada) | Só usa o contrato institucional. |
| Idempotência | `ServicoDeQuitacao` + `BoletoDeMulta.quitarSeAberto` | Uma única quitação por boleto. |

### C.3 Fluxo do cenário — `R03_mesmaSolicitacaoDeSeteReaisViraSetecentosCentavosNoBancoAESeteNoBancoB`
```
Multa de R$ 7,00 → ServicoDeMulta.cobrar(multa) → cobranca.registrarBoleto(idBoleto, cpf, 7.00, email)
  instalação com A: AdaptadorBancoA.paraCentavos(7.00) = 7.00.movePointRight(2).longValueExact() = 700
                    ApiBancoA.gerarTitulo("12345678909", 700, "1") → TituloA{nossoNumero "A-0001"}
  instalação com B: AdaptadorBancoB.paraDocumento("12345678909") = "123.456.789-09"
                    ApiBancoB.abrirCobranca("123.456.789-09", 7.00, "MULTA-1") → CobrancaB{txid "TXB000001"}
ServicoDeMulta é o MESMO nos dois casos; ele só tem um campo do tipo iCobranca.
```
**Notificação repetida** (`R03_notificacaoRepetidaNaoGeraSegundaQuitacao`):
```
ApiBancoA.liquidar("A-0001") → webhook JSON {status LIQUIDADO, valorPagoCentavos 700, ...}
AdaptadorBancoA.receberWebhook(json) → ConfirmacaoPagamento{A, A-0001, 7.00} → registrarQuitacao
  ServicoDeQuitacao (transação exclusiva): buscarBoletoExterno(A, A-0001) → EMITIDO → quitarSeAberto → true
reenvio do mesmo webhook → ... → boleto já QUITADO → quitarSeAberto → false (responde "OK" ao banco)
```

### C.4 Na arguição, saiba explicar
**Onde ficam as conversões de cada banco?** Só nos adaptadores: `AdaptadorBancoA.paraCentavos` e
`traduzir(StatusA)` + leitura do JSON; `AdaptadorBancoB.paraDocumento`, `traduzir("CONCLUIDA")` + leitura do XML.

**Como o banco usado pela instalação é escolhido?** Na montagem (`Sistema`), pela configuração
administrativa (`bancoEmUso`, gravada no JSON): `Map.of("A", adaptadorA, "B", adaptadorB).get(bancoEmUso)`.
O `ServicoDeMulta` recebe só o `iCobranca` escolhido — ele não sabe qual é.

**Como a quitação é protegida contra notificações repetidas?** Em `ServicoDeQuitacao.registrarQuitacao`:
localiza o boleto por (banco, idExterno) e chama `quitarSeAberto` dentro de `UnidadeDeTrabalho.executar`, que é
`synchronized` — duas notificações simultâneas não passam juntas. Se o boleto já está QUITADO, devolve `false` e
nada muda. Notificação de cobrança desconhecida vai para conciliação, sem quitar nada.

---

## Exercícios de alteração ao vivo

**Strategy — nova política "progressiva" (R$ 1,00 nos 5 primeiros dias, R$ 2,00 depois).**
1. `class MultaProgressiva implements PoliticaDeMulta` com `id()` = "PROGRESSIVA" e o cálculo.
2. Em `Sistema`, acrescentar `new MultaProgressiva()` na lista do `SeletorDePolitica`.
3. Teste: `calcular(7)` = 5 × 1,00 + 2 × 2,00 = R$ 9,00.
`ServicoDeMulta`, `ServicoDeSimulacao`, `Multa` e `BoletoDeMulta` não mudam.

**Observer — novo ouvinte "OuvinteReservaEmEspera" (avisa o próximo interessado).** Classe que implementa
`OuvinteDeDevolucao` + `publicador.registrar(...)` no `Sistema`. O tratador não muda.

**Adapter — banco C.** `ApiBancoC` (simulada) + `AdaptadorBancoC implements iCobranca` + entrada `"C"` no mapa de
bancos do `Sistema`. O `ServicoDeMulta` não muda.

## Autoavaliação
1. Quando o evento é publicado? 2. Quem garante que a falha do e-mail não desfaz a devolução? 3. Onde fica o
`if` de "há atraso"? 4. O que impede `ServicoDeMulta` de saber o banco? 5. O que faz `quitarSeAberto` devolver
`false`?

**Respostas:** 1. Depois de `circulacao.devolver` confirmar a transação. 2. O publicador (try/catch por ouvinte)
e a ordem (publicar depois do commit). 3. No `OuvinteMulta` (`diasAtraso() > 0`). 4. Ele depende só da
interface `iCobranca`. 5. O boleto já estar QUITADO.
