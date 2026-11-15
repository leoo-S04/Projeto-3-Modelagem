# Guia de estudo — Diagramas UML do Trabalho 03 (Astah)

O enunciado pede, no Astah (`.asta`) e exportados em PDF: **um diagrama de classes de cada padrão**, **a máquina de
estados de Livro** e **os diagramas de sequência dos cenários**. Tudo isso está no arquivo
`PROJETO 2- Leo T, Vicente A, Pedro B e Gustavos L.asta` e em [`docs/uml/Trabalho03_Diagramas.pdf`](../uml/Trabalho03_Diagramas.pdf)
(PNGs em [`docs/uml/Trabalho 03/`](../uml/Trabalho%2003/)).

## 1. Onde está cada coisa no `.asta`

| Pacote na árvore do Astah | Conteúdo |
|---|---|
| (raiz) | Diagramas do Trabalho 02, mantidos como histórico. O "Classes de Projeto" tem uma nota apontando para o modelo novo. |
| `Livro` → **Maquina de Estados - Livro** | Máquina de estados do T02 **atualizada** para o padrão State. |
| **Trabalho 03 - Implementacao** | As classes do código, com os mesmos subpacotes do Java: `biblioteca`, `financeiro` (`bancos`), `comum`, `persistencia`, `rh`, `academico`. |
| **Req 01 - State** … **Req 10 - Composite** | Em cada um: `DC Req NN - ...` (classes do padrão + nota com os papéis GoF) e `SD RNN - ...` (sequência do cenário de verificação). |

## 2. Como ler os diagramas de classes

- Cada diagrama tem uma **nota rosa** com os papéis do padrão (ex.: *Context: Livro; State: EstadoLivro; ConcreteState: Livre…*).
  Comece por ela.
- Os membros vêm do código: `+` público, `-` privado, `#` protegido, `~` pacote. Sublinhado = `static`; itálico = abstrato;
  `{leaf}` = `final` (ex.: `TratadorDeCadastro.inserir`).
- Um campo cujo tipo é outra classe do diagrama aparece como **associação** (seta com o nome do campo e a multiplicidade),
  não como atributo. Ex.: `Livro -estado → EstadoLivro [1]`; `GrupoDoAcervo -filhos ◆→ ComponenteDoAcervo [0..*]` (composição).
- Seta tracejada com triângulo = realização de interface; linha cheia com triângulo = herança;
  tracejada com `«create»` = a classe cria objetos da outra (ex.: fábricas → produtos no Abstract Factory);
  `«use»` = usa como parâmetro/retorno.

## 3. Como ler os diagramas de sequência

- A primeira linha de vida é o **teste JUnit** do cenário (`R01_CicloDeVidaDoLivroTest` etc.).
- A numeração mostra o aninhamento: `1.2.2.1` é uma chamada feita **dentro** da `1.2.2`.
- Seta tracejada aberta = retorno (o texto é o valor devolvido, com os valores do cenário: "R$ 3,00", "vence 12/01/2027").
- `«create»` = criação do objeto (ex.: a `SolicitacaoDeRenovacao` no Command).
- Fragmentos: `alt` (condição), `opt` (só se a guarda for verdadeira), `loop` (repetição). Ex.: em R05, `loop` sobre os
  ouvintes e `opt [ev.diasAtraso() > 0]` antes de gerar a multa.
- A nota rosa no fim explica o resultado esperado e a 2ª parte do cenário, quando existe.

## 4. Máquina de estados do Livro — o que mudou e por quê

| Antes (T02) | Agora (T03) | Motivo |
|---|---|---|
| `entry / setEstado(LIVRE)` | `entry / mudarPara(Livre.INSTANCIA)` | No State o estado é um **objeto**; `mudarPara` tem visibilidade de pacote (só as classes de estado chamam). |
| `/ adicionar(reserva)` | `/ definirReservante(e)` | É o que `Livre.reservarLivro` faz; a gravação da reserva é da fachada. |
| `/ adicionar(emprestimo)` | `/ definirVencimento(data + 30)` | Efeito executado por `Reservado.emprestarLivro`. |
| `/ renovar()` | `/ prorrogar(30)` | `prorrogar` soma 30 dias e incrementa `numRenovacoes` (guardas `= 0` e `< 2` mantidas). |
| `inspecionar [bom estado]`, `retornarDoReparo` | `/ limparCiclo()` | Ao voltar a LIVRE, reservante, vencimento e renovações são zerados. |
| nó de decisão `[atraso] / emitirBoletoDeMulta` | `registrarDevolucao [emprestimo registrado] / publicar(EventoDevolucao)` | A multa virou **reação ao evento** (Observer): o `OuvinteMulta` gera a multa quando `diasAtraso() > 0`. |

Nomes de eventos e guardas não mudaram (D08). `incinerar` é o "laudo irrecuperável".

## 5. Na arguição, saiba explicar

- **"O código corresponde ao modelo?"** Sim: as classes dos diagramas foram geradas do código compilado (reflexão).
  Mostre, por exemplo, `Renovado.renovarEmprestimo` no diagrama de classes, a transição `RENOVADO → RENOVADO [numRenovacoes < 2]`
  na máquina de estados e a mensagem `1.2.2.1.4.1` do SD R01 — os três são o mesmo método.
- **"Onde está o padrão no diagrama?"** Leia a nota de papéis e aponte as classes.
- **"Por que a máquina de estados não tem mais o nó de decisão da multa?"** Ver a última linha da tabela da seção 4.
- **"Por que os diagramas do T02 continuam no arquivo?"** Histórico; as diferenças estão em `docs/uml/ATUALIZACOES_ASTAH.md` (D66).

## 6. Como regenerar (se o código mudar)

Feche o Astah (o arquivo não pode estar aberto) e, na raiz do projeto:

```
ferramentas\astah\gerar.cmd
```

O script compila o projeto, roda `ferramentas/astah/GeradorDiagramasT03.java` (API Java do Astah), apaga o que foi
gerado antes e recria tudo, exporta os PNGs com a linha de comando do Astah e monta o PDF (`MontarPdf.java`).
Para mudar o conteúdo: classes de cada diagrama em `DIAGRAMAS` (lista de linhas), fluxos em `sequencias()`.

## 7. Exercício

Acrescente uma política `MultaProgressiva` (Strategy), registre no `Sistema` e inclua o nome dela na linha das
políticas do `DC Req 02` em `GeradorDiagramasT03.java`. Rode `gerar.cmd` e confira a nova classe realizando
`PoliticaDeMulta` no diagrama, sem nenhuma mudança no `ServicoDeMulta`.
