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

## Etapa 4 — Observer, Strategy e Adapter (decisões do modelo, modo autônomo)

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D36 | Strategy com os nomes das Soluções (`PoliticaDeMulta`, `MultaDiaria`, `MultaComTeto`, `MultaComTolerancia`, `SeletorDePolitica`, `ConfiguracaoAdministrativa`, `ServicoDeMulta`, `ServicoDeSimulacao`); nova entidade `Multa` (record) com valor e política aplicada | Calcular no `BoletoDeMulta` (Astah: `valorDiario`, `calcularValor()`) | O padrão exige o cálculo fora do boleto; a `Multa` imutável garante a não retroatividade. |
| D37 | `BoletoDeMulta` (Astah) mantido como o boleto registrado no banco (≈ `Cobranca` das Soluções): valor em `BigDecimal`, banco, id externo, situação EMITIDO/QUITADO; saem `valorDiario`, `calcularValor()` e `enviarPorEmail()` | Criar `Cobranca` e manter o boleto do Astah | Mantém o nome do Astah. O e-mail com o boleto é enviado pelo banco (`registrarBoleto(..., email)`, já previsto no Astah). |
| D38 | Target do Adapter = `iCobranca` do Astah (≈ `GatewayDeCobranca`), com `valor: BigDecimal`; `consultarPagamento` recebe o id externo; novo `codigoDoBanco()` | Nomes das Soluções | Mantém o nome do Astah; adaptadores sem estado (não precisam guardar idBoleto ↔ idExterno). |
| D39 | Entrada das notificações = `iQuitacao.registrarQuitacao(ConfirmacaoPagamento): boolean` (≈ `ReceptorDePagamento`) | `registrarQuitacao(idBoleto, data)` do Astah | O banco só conhece o próprio id externo; o adaptador traduz para a confirmação institucional. |
| D40 | Bancos A e B simulados em `financeiro.bancos` (`ApiBancoA`, `ApiBancoB`) com APIs incompatíveis (centavos/decimal, CPF puro/formatado, nosso número/txid, JSON/XML) | Chamar serviços reais | Permitido pelo Req. 3 e pela nota do diagrama de implantação. |
| D41 | Banco da instalação escolhido na montagem, a partir de `ConfiguracaoAdministrativa.bancoEmUso`, por consulta a um mapa (sem `if`); troca vale após reiniciar | Trocar a cada cobrança | Soluções: "decidido na montagem da aplicação, por configuração". |
| D42 | Quitação idempotente em `ServicoDeQuitacao` + `BoletoDeMulta.quitarSeAberto` dentro da transação exclusiva (`executar` é synchronized); cobrança desconhecida vai para conciliação (lista em memória) | Restrição única em banco de dados | Sem servidor de banco; a exclusividade vem da unidade de trabalho. |
| D43 | `iMulta.gerarMulta(cpf, idDevolucao, diasAtraso)`, idempotente por `idDevolucao` (chave das Soluções); `consultarMulta(idBoleto)` mantido | `gerarMulta(cpf, diasAtraso)` do Astah | Reenvio do ouvinte ou f6 não geram segunda multa. |
| D44 | `TratadorDeMulta` (Astah) realiza `iMulta` e `iQuitacao` e delega; os adaptadores entregam as confirmações ao `ServicoDeQuitacao` (que também realiza `iQuitacao`) | Adaptadores chamarem o tratador | Evita ciclo na montagem (serviço de multa → adaptador → tratador → serviço de multa). |
| D45 | Observer com os nomes das Soluções; a publicação ocorre no `TratadorDeDevolucao` depois do commit da fachada; ouvintes registrados no `Sistema` | Publicar dentro da transação | Soluções: "publicação após a confirmação"; falha de ouvinte não desfaz a devolução. |
| D46 | Agenda de inspeção, painel de indicadores, fila de reenvio e e-mail ficam em memória | Persistir no JSON | Simplificação: a situação do livro (DEVOLVIDO) e as multas, que importam, são gravadas. Registrado como limitação. |
| D47 | f6 `emitirBoletoDeMulta(idLivro, idEstudante)` devolve o boleto da devolução atrasada (segunda via); a geração normal é feita pelo `OuvinteMulta` | f6 gerando a multa a cada chamada | Regra 10.2 fala em "segunda via"; a regra 9.3 (extensão em caso de atraso) virou reação à devolução. |
| D48 | Novo produto da fábrica `RepositorioFinanceiro` (seções `multas`, `cobrancas`, `configuracao` do JSON) | Guardar só em memória | Multas e configuração precisam sobreviver ao reinício. |
| D49 | `ServicoDeMulta` consulta o e-mail do estudante pela interface `iPessoa` | Receber o e-mail pelo evento | Diagrama de componentes: Financeiro requer `iPessoa` (consulta de CPF). |

## Etapa 5 — Composite e Decorator (decisões do modelo, modo autônomo)

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D50 | Composite com os nomes das Soluções (`ComponenteDoAcervo`, `ItemDoAcervo`, `GrupoDoAcervo`, `TipoDeGrupo`, `OrganizadorDoAcervo`); tipo do grupo como atributo | Uma classe por nível | Profundidade variável sem classes novas (Soluções, Problema 10). |
| D51 | `GrupoDoAcervo.adicionar` recusa ciclo, componente com pai e id já presente na árvore; o `OrganizadorDoAcervo` também confere entre unidades | Só o organizador validar | O enunciado pede que o **grupo** recuse; o organizador cobre árvores diferentes. |
| D52 | Grupos identificados por nome único; persistidos na seção `acervo` (um registro por grupo com o nome do pai, em profundidade) pelo novo produto `RepositorioDeOrganizacao` | Tabela com pai_id | Formato simples no JSON; pai sempre gravado antes dos filhos. |
| D53 | Organização e consulta expostas à web pelo `TratadorDeAcervo` (não há tratador novo no Astah) | Controlador web chamando o organizador/consulta | Mantém "a web depende só dos tratadores". |
| D54 | Decorator com os nomes das Soluções (`ConsultaAcervo`, `ConsultaAcervoBasica`, `DecoradorDeConsulta`, `DecoradorDeMedicao`, `DecoradorDeCache`, `MontadorDeConsulta`, `MetadadosLivro`, `Criterio`); medição por fora do cache por padrão (`ConfigInstalacao.completa()`, TTL 5 min) | Cache por fora | O cenário exige medir também as consultas atendidas pelo cache. |
| D55 | Armazenamento do cache atrás da interface `ArmazenamentoDeCache` (padrão: memória) | Mapa fixo dentro do decorador | Permite simular falha do cache (teste) sem alterar o decorador. |
| D56 | Invalidação do cache só por TTL (sem invalidar ao renomear livro) | Invalidar no `atualizarLivro` | Simplicidade; metadados desatualizados não afetam reservas (Soluções). |

## Etapa 6 — Interface web e demonstração (decisões do modelo, modo autônomo)

| # | Decisão | Alternativas | Motivo |
|---|---|---|---|
| D57 | Spring Boot 3.5.6 + Thymeleaf só no pacote `web`; BOM importado (sem `spring-boot-starter-parent`); `spring-boot-maven-plugin` gera `biblioteca.jar` executável; compilador com `-parameters` | Usar o parent do Spring Boot | Mantém o domínio em Java puro e o `pom` do projeto; um único artefato, como no diagrama de implantação. |
| D58 | `ConfiguracaoDoSistema` escolhe a família chamando `ConfiguradorDePersistencia.fabricaPara(perfil, arquivo)` com a propriedade `biblioteca.perfil` e publica como beans só os tratadores (e ferramentas da demonstração) | `@Profile` do Spring; injetar o `Sistema` nos controladores | O padrão Abstract Factory não pode ser substituído pelo Spring (CLAUDE.md); a web deve depender só dos tratadores. |
| D59 | `BancosController` na web: pontos de entrada HTTP dos bancos (webhook JSON do A, notificação XML do B) e simulador para a demonstração | Simular o pagamento dentro do Financeiro | É a fronteira com o componente «external» Banco; toda conversão continua nos adaptadores. |
| D60 | Tratadores ganharam consultas em DTO para as telas (`listarPessoas`, `listarEstudantes`, `listarAtendentes`, `listarLivros`, `inspecoesPendentes`, `indicadores`, `relatorioDeCobrancas`) | Telas acessando grupos/repositórios | "A interface não referencia coleções do domínio": devolvem cópias em records somente leitura. |
| D61 | HTTP na porta 8080 por padrão; HTTPS pelo perfil opcional `https` (porta 8443, certificado de teste gerado com `keytool`, fora do Git) | HTTPS obrigatório com certificado no repositório | O diagrama pede HTTPS; para a demonstração local, um certificado autoassinado gera alerta no navegador. Ambas as formas foram testadas. |
| D62 | Primeira execução: se `dados/biblioteca.json` não existe, é copiado de `dados/exemplo.json` (gerado pela própria família JSON); para recomeçar, apagar o arquivo | Arquivo vazio + botão "carregar exemplo" | Simples e seguro (não sobrescreve dados existentes). |
| D63 | Formulários de reserva e renovação levam um `idSolicitacao` (UUID) gerado ao abrir a página | Gerar o id no servidor a cada envio | Duplo clique / reenvio do formulário usa o mesmo id e não executa duas vezes (Command). |
| D64 | `ArquiteturaTest` verifica no código-fonte os critérios de pronto (web sem pontos internos; tratadores sem teste de situação; serviço de multas sem condicional por banco/política) e `FluxoCompletoWebTest` percorre o fluxo inteiro pela web (MockMvc, perfil TESTE) | Verificação manual | Os critérios ficam garantidos a cada `mvnw test`. |
