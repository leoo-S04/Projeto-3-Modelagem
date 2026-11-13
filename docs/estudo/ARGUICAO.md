# Arguição — perguntas prováveis e respostas curtas

Na arguição o professor aponta um trecho e pergunta **o que faz**, **qual papel o padrão desempenha** e **o que
aconteceria se fosse alterado**. As respostas abaixo são o mínimo; os detalhes estão nos guias `ETAPA0x_*.md`.

---

## Req. 1 — State (`Livro`, `EstadoLivro` e os 7 estados)
- **Onde está a guarda da renovação e por que é avaliada antes de alterar o vencimento?** Em
  `Emprestado.renovarEmprestimo` (= 0) e `Renovado.renovarEmprestimo` (< 2), na primeira linha. Se for falsa, a
  exceção sai antes de `prorrogar(30)`: vencimento e contador ficam intactos, nada a desfazer.
- **O que acontece quando um evento chega a um estado que não o aceita?** Roda a implementação herdada de
  `EstadoLivro`, que lança `OperacaoInvalidaException` sem tocar no livro; o tratador não chega a gravar a transação.
- **Como o estado é gravado e reconstituído?** Grava-se `getEstado().name()` (ex.: `"RENOVADO"`); na leitura,
  `Livro.reconstituir` chama `EstadoLivro.para(Situacao.RENOVADO)` → `Renovado.INSTANCIA`.
- **E se o `setEstado` fosse público?** Qualquer classe poderia pular a máquina de estados (ex.: pôr LIVRE um livro
  emprestado). Por isso é `mudarPara`, visível só no pacote.
- **E se a guarda fosse depois do `prorrogar`?** A 3ª renovação mudaria o vencimento e só depois falharia.

## Req. 2 — Strategy (`PoliticaDeMulta`, `MultaDiaria`, `MultaComTeto`, `MultaComTolerancia`)
- **Como uma nova política é incluída sem alterar o serviço de multas?** Nova classe que implementa
  `PoliticaDeMulta` + registro na lista do `SeletorDePolitica` (em `Sistema`). Nenhum `if` muda.
- **Por que trocar a política não recalcula multas emitidas?** A `Multa` (record imutável) grava valor e id da
  política na emissão; o boleto usa esse valor.
- **Como são calculados dias e valores?** Dias corridos: `ChronoUnit.DAYS.between(vencimento, devolução)`, mínimo 0.
  Dinheiro em `BigDecimal`, escala 2, `HALF_EVEN`; nunca `double`.
- **Onde a política vigente é escolhida?** `ConfiguracaoAdministrativa.politicaVigente` (gravada no JSON), lida por
  `SeletorDePolitica.vigente()`; id inexistente é recusado ao salvar.

## Req. 3 — Adapter (`iCobranca`, `AdaptadorBancoA/B`, `ApiBancoA/B`)
- **Onde ficam as conversões de cada banco?** Só nos adaptadores: A converte reais → centavos (`movePointRight(2)`),
  traduz `StatusA` e lê o webhook JSON; B formata o CPF, envia decimal, traduz `"CONCLUIDA"` e lê o XML.
- **Como o banco da instalação é escolhido?** Na montagem (`Sistema`), pela configuração `bancoEmUso`, consultando
  um mapa `{"A": adaptadorA, "B": adaptadorB}`. O `ServicoDeMulta` só conhece `iCobranca`.
- **Como a quitação é protegida contra notificações repetidas?** `ServicoDeQuitacao` localiza o boleto por
  (banco, idExterno) e chama `quitarSeAberto` dentro de uma transação exclusiva (`synchronized`); se já está
  QUITADO, devolve `false`. Cobrança desconhecida vai para conciliação.
- **E um terceiro banco?** `ApiBancoC` + `AdaptadorBancoC implements iCobranca` + entrada no mapa. Financeiro intacto.

## Req. 4 — Facade (`iCirculacao` / `ServicoDeCirculacao`)
- **Quais classes a fachada coordena e em que ordem?** Efetivar: Relacionamento (estudante) → Movimentacao
  (reserva ativa) → valida → Acervo (livro) → `livro.emprestarLivro` → Movimentacao (remove reserva, registra
  empréstimo) → Acervo (atualiza) → confirma.
- **O que é desfeito se um passo falhar no meio?** Tudo, porque a operação está dentro de
  `UnidadeDeTrabalho.executar`: `desfazer()` volta ao último estado confirmado (livro RESERVADO, reserva de volta).
- **Por que a interface não acessa Movimentacao diretamente?** Teria de conhecer coleções e a ordem dos passos;
  qualquer mudança interna a quebraria. A web fala com tratadores; os tratadores com a fachada.
- **O que a fachada devolve?** Comprovantes (records DTO); nunca coleções nem `Acervo`/`Movimentacao`.

## Req. 5 — Observer (`PublicadorDeDevolucao`, ouvintes, `EventoDevolucao`)
- **Como um novo ouvinte é incluído sem alterar o tratador?** Classe que implementa `OuvinteDeDevolucao` +
  `publicador.registrar(...)` em `Sistema`.
- **O que acontece quando o envio de e-mail falha?** O publicador captura a exceção daquele ouvinte, registra na fila
  de reenvio e continua com os outros; a devolução já tinha sido confirmada antes da publicação.
- **Por que o evento leva os dados em vez de o ouvinte consultá-los depois?** O empréstimo ativo é removido na
  devolução; o evento (imutável) registra vencimento, data e estudante daquele instante (modelo push).
- **Onde está a decisão de multar?** No `OuvinteMulta` (`diasAtraso() > 0`); idempotente por `idDevolucao`.

## Req. 6 — Command (`SolicitacaoDeCirculacao`, `ExecutorDeSolicitacoes`)
- **Quem cria, quem invoca e quem executa?** Cria: `TratadorDeEmprestimo` (com o id vindo da tela). Invoca:
  `ExecutorDeSolicitacoes`. Executa: o receptor `iCirculacao`; a regra está no Livro (State).
- **Como a repetição é detectada mesmo com submissões simultâneas?** `RegistroDeSolicitacoes.reservarExecucao` usa
  `ConcurrentHashMap.putIfAbsent`: só uma thread insere o id; as outras recebem o resultado gravado (também
  persistido no JSON, valendo após reiniciar).
- **Por que não há desfazer?** A entrega física do livro não é revertida por software; correções são operações
  administrativas.

## Req. 7 — Template Method (`TratadorDeCadastro`)
- **Quais passos são fixos e quais variam?** Fixa a ordem (`inserir`, final) e `apresentar`; variam
  `buscarExistente`, `validar`, `criar`, `armazenar`.
- **O que é um gancho e onde é usado?** Passo com implementação padrão vazia, que a subclasse pode sobrescrever:
  `verificarPreRequisitos`, usado por Estudante e Atendente para exigir a pessoa.
- **Por que a falha do pré-requisito não permite gravação parcial?** Ela acontece no passo 3, antes de `criar` e
  `armazenar`; só `armazenar` está na transação. E `inserir` é `final`: ninguém muda a ordem.

## Req. 8 — Abstract Factory (`FabricaDePersistencia`, `FabricaEmMemoria`, `FabricaJson`)
- **Onde a família é escolhida?** `ConfiguradorDePersistencia.fabricaPara(perfil, arquivo)`, uma vez, na montagem
  (perfil `TESTE` ou `PRODUCAO`, propriedade `biblioteca.perfil`). Sem perfil → erro. Não usamos `@Profile`.
- **Como os repositórios compartilham a mesma transação?** A fábrica entrega a mesma `UnidadeDeTrabalho` a todos;
  eles alteram a mesma cópia de trabalho; um `confirmar()` grava tudo (arquivo inteiro, atômico).
- **O que impede misturar famílias?** A fábrica concreta só aceita a sua unidade de trabalho
  (`FamiliaIncompativelException`) e os repositórios concretos exigem o tipo concreto da unidade.
- **Desvantagem?** Novo tipo de produto exige mudar a interface e todas as fábricas (aconteceu nas Etapas 3–5).

## Req. 9 — Decorator (`ConsultaAcervo`, `DecoradorDeMedicao`, `DecoradorDeCache`)
- **Como a cadeia é montada e por que a ordem importa?** `MontadorDeConsulta`: `Medicao(Cache(Basica))`. Com a
  medição por fora, as respostas do cache também são medidas; na ordem inversa, não seriam.
- **O que acontece se o cache falhar?** O decorador captura, conta a falha e delega ao interno: a consulta continua.
- **Por que o cache não participa da decisão de reserva?** Guarda só id e título; a reserva vai pela fachada ao
  Livro atual, e o estado decide.

## Req. 10 — Composite (`ComponenteDoAcervo`, `ItemDoAcervo`, `GrupoDoAcervo`)
- **Como a contagem percorre a árvore?** Recursão polimórfica: grupo soma `contarLivros()` dos filhos; item vale 1.
- **Quais regras impedem ciclos e livros em dois grupos?** `GrupoDoAcervo.adicionar`: recusa o próprio grupo ou um
  ancestral, componente que já tem pai e id já presente na árvore; o organizador confere entre unidades.
- **Por que mover um item não toca no Livro nem na Movimentacao?** O item só referencia o livro pelo id; mover
  troca apenas o pai do item.

---

## Perguntas gerais
- **Onde fica a regra de negócio?** No domínio (estados do Livro, tratadores, serviços). A web só chama tratadores
  e mostra mensagens (`ArquiteturaTest` verifica).
- **Por que `java.time.Clock` injetado?** Testes e demonstração controlam "hoje" (`RelogioAjustavel`): devolução
  atrasada sem esperar dias.
- **Por que JSON e não MySQL?** Orientação do professor em sala; a Abstract Factory permite trocar a família sem
  mudar o domínio.
- **O que é `Sistema`?** A montagem (composition root): cria a família escolhida e liga tratadores, fachada,
  executor, ouvintes, políticas e adaptadores.
- **Como o código corresponde ao diagrama de implantação?** Um `biblioteca.jar` (Tomcat embutido, JVM 17) que
  manifesta RH, Acadêmico, Biblioteca e Financeiro, lê/grava `biblioteca.json`; bancos A e B simulados no jar;
  navegador por HTTP(S).
