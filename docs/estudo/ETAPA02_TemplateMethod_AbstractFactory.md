# Etapa 2 — Cadastros e persistência: **Template Method** (Req. 7) e **Abstract Factory** (Req. 8)

---

## Parte A — Template Method (Req. 7)

### A.1 O problema
Os quatro cadastros (Livro, Pessoa, Estudante, Atendente) fazem os mesmos passos, na mesma ordem: ver se já
existe, validar, conferir pré-requisitos, criar, gravar e mostrar o resultado. Só o **conteúdo** de alguns passos
muda. Sem o padrão, a ordem ficaria copiada em quatro lugares — e um esquecimento (gravar antes de validar)
causaria gravação parcial.

### A.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| AbstractClass | `comum.TratadorDeCadastro<T, D>` | Método-modelo `inserir(D)` **final**: fixa a ordem dos 6 passos. Implementa o passo comum `apresentar` e o gancho vazio `verificarPreRequisitos`. |
| Operações primitivas | `buscarExistente`, `validar`, `criar`, `armazenar` (abstratas) | Cada cadastro implementa. |
| Gancho (hook) | `verificarPreRequisitos` | Vazio por padrão; `TratadorDeEstudante` e `TratadorDeAtendente` exigem a pessoa cadastrada (via `iPessoa`). |
| ConcreteClass | `TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante`, `TratadorDeAtendente` | Herdam a rotina e continuam realizando `iLivro`, `iPessoa`, `iEstudante`, `iAtendente`. |
| Resultado | `ResultadoCadastro<T>` (`NOVO`, `EXISTENTE`, `RECUSADO` + motivo) | O que a tela mostra. |
| Entrada | `DadosLivro`, `DadosPessoa`, `DadosEstudante`, `DadosAtendente` | Dados do formulário. |

### A.3 O que foi implementado
```java
public final ResultadoCadastro<T> inserir(D dados) {
    T existente = buscarExistente(dados);                 // 1
    if (existente != null) return apresentar(existente, false);
    try {
        validar(dados);                                   // 2
        verificarPreRequisitos(dados);                    // 3 (gancho)
    } catch (CadastroRecusadoException e) {
        return ResultadoCadastro.recusado(e.getMessage());
    }
    T novo = criar(dados);                                // 4
    transacao.executar(() -> armazenar(novo));            // 5 (grava + confirma)
    return apresentar(novo, true);                        // 6
}
```
As operações do Astah foram preservadas: `inserirEstudante(cpf, matricula)` apenas monta
`new DadosEstudante(cpf, matricula)` e chama `inserir(...)`.

### A.4 Cenário de verificação passo a passo
**R07_estudanteDuplicadoRetornaOExistente** (Ana, CPF 111, já é a estudante M-100)
```
inserirEstudante("111", "M-100") → inserir(DadosEstudante{111, M-100})
 1 buscarExistente → GrupoDeAcademicos.buscarPorMatricula("M-100") → Estudante(id=1)
   ≠ null → apresentar(existente, false) → ResultadoCadastro(EXISTENTE, Estudante id=1)
   (passos 2 a 6 nem rodam)
```
**R07_estudanteComCpfNaoCadastradoFalhaAntesDeQualquerGravacao**
```
inserirEstudante("999", "M-999")
 1 buscarExistente("M-999") → null
 2 validar: matrícula e CPF preenchidos ✔
 3 verificarPreRequisitos: pessoas.consultarPessoa("999") → null → CadastroRecusadoException
   → ResultadoCadastro(RECUSADO, "pessoa não cadastrada (CPF 999)")
   criar e armazenar NÃO são chamados: a unidade de trabalho espiã registra 0 gravações
```

### A.5 Na arguição, saiba explicar
**Quais passos são fixos e quais variam?** Fixa é a **ordem** (no `inserir`, que é `final`) e o passo
`apresentar`. Variam `buscarExistente` (por CPF, matrícula ou id), `validar` (campos de cada formulário),
`criar` (qual objeto) e `armazenar` (em qual grupo/acervo).

**O que é um gancho e onde ele é usado?** É um passo com implementação padrão (aqui, vazia) que a subclasse
*pode* sobrescrever. `verificarPreRequisitos` só é sobrescrito por Estudante e Atendente, que exigem a pessoa
já cadastrada no RH. Livro e Pessoa usam o padrão (nada a verificar).

**Por que a falha do pré-requisito não permite gravação parcial?** Porque o método-modelo verifica antes de
`criar` e `armazenar`, e só `armazenar` roda dentro da transação. Se a pessoa não existe, a exceção acontece no
passo 3 e o método retorna RECUSADO — nada foi escrito. E como `inserir` é `final`, nenhuma subclasse consegue
mudar essa ordem.

---

## Parte B — Abstract Factory (Req. 8)

### B.1 O problema
Os repositórios de Livro, Movimentacao e Relacionamento e a transação precisam ser da **mesma família**:
nos testes, tudo em memória; na demonstração, tudo no arquivo `biblioteca.json`. Se misturássemos (livro no
arquivo, reserva na memória), um empréstimo poderia gravar o livro e "perder" a movimentação.

### B.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| AbstractFactory | `FabricaDePersistencia` | `criarUnidadeDeTrabalho()`, `criarRepositorioLivro(uow)`, `criarRepositorioMovimentacao(uow)`, `criarRepositorioRelacionamento(uow)` |
| ConcreteFactory | `FabricaEmMemoria`, `FabricaJson` | Cada uma só cria produtos da sua família e recusa a unidade de outra (`FamiliaIncompativelException`). |
| AbstractProduct | `UnidadeDeTrabalho`, `RepositorioLivro`, `RepositorioMovimentacao`, `RepositorioRelacionamento` | Contratos usados pelo domínio. |
| ConcreteProduct | `UnidadeDeTrabalhoEmMemoria`, `RepositorioLivroEmMemoria`... / `UnidadeDeTrabalhoJson`, `RepositorioLivroJson`... | Implementações ligadas à unidade de trabalho da própria família. |
| Escolha da família | `ConfiguradorDePersistencia.fabricaPara(Perfil, arquivo)` | Único ponto que conhece as famílias; perfil ausente → erro. |
| Cliente | `Sistema` (montagem) → `Acervo`, `Movimentacao`, grupos, tratadores | Usam só os contratos. |

Apoio: `BaseDeDados` (cópia de trabalho), `DocumentoDados` (formato do JSON), `MapeadorDeDados`
(objetos ⇄ documento), `ConversorJson` (Jackson).

### B.3 O que foi implementado
1. **Uma unidade de trabalho por aplicação**, criada na montagem. Ela guarda a **cópia de trabalho**
   (`BaseDeDados`): mapas de pessoas, estudantes, atendentes, livros e as listas de reservas, empréstimos e
   devoluções (as 3 associações da `Movimentacao`).
2. **Repositórios** de uma família recebem a mesma unidade e leem/alteram a mesma cópia.
3. `executar(operacao)` = transação: roda a operação; se terminou bem → `confirmar()`, se lançou exceção →
   `desfazer()`. É `synchronized` (uma escrita por vez). Chamadas aninhadas participam da mais externa.
4. **Família em memória:** `confirmar()` tira uma fotografia (`DocumentoDados`) da cópia; `desfazer()` volta à
   fotografia. Nada vai para o disco.
5. **Família JSON:** ao iniciar, lê `biblioteca.json` (ou cria vazio). `confirmar()` grava o arquivo inteiro em
   `biblioteca.json.tmp` e o move por cima do original com `ATOMIC_MOVE` — o arquivo nunca fica pela metade.
   `desfazer()` descarta a cópia e recarrega do disco.
6. A situação do livro é gravada como texto (`"situacao" : "RENOVADO"`) e reconstituída com
   `Livro.reconstituir(...)` → `EstadoLivro.para(Situacao)` (ligação com o Req. 1).
7. `Acervo`, `Movimentacao` e os grupos passaram a delegar aos repositórios; os tratadores envolvem cada
   operação em `transacao.executar(...)`.

Trecho do arquivo gerado:
```json
"livros" : [ { "id" : 1, "titulo" : "Dom Casmurro", "situacao" : "RESERVADO",
               "reservante" : "M-100", "vencimento" : null, "numRenovacoes" : 0 } ],
"reservas" : [ { "id" : 1, "data" : "2026-11-13", "idLivro" : 1, "matriculaEstudante" : "M-100", ... } ]
```

### B.4 Cenários de verificação passo a passo
**R08_configuracaoDeTesteFornecesSoObjetosEmMemoria**
```
ConfiguradorDePersistencia.fabricaPara(TESTE, ...) → FabricaEmMemoria
criarUnidadeDeTrabalho() → UnidadeDeTrabalhoEmMemoria; repositórios → *EmMemoria
executar(salvar livro) → confirmar() = fotografia em memória; a pasta temporária continua vazia
```
**R08_configuracaoDeProducaoFornecesRepositoriosJsonQueCompartilhamAMesmaUnidade**
```
fabricaPara(PRODUCAO, tmp/biblioteca.json) → FabricaJson
uow = UnidadeDeTrabalhoJson; livros.unidade() == movimentacao.unidade() == relacionamento.unidade() == uow
reservarLivro(1, "M-100"): na mesma transação, o livro muda para RESERVADO e a Reserva é registrada
→ um único confirmar() grava as duas coisas no arquivo
outro Sistema lendo o arquivo → livro RESERVADO e reserva de 13/11/2026
```
**R08_desfazerAposFalhaDeixaOArquivoInalterado**
```
arquivo com livro RENOVADO (2 renovações); antes = conteúdo do arquivo
executar: livro.setTitulo("ALTERADO") → throw IllegalStateException → desfazer() → recarrega do disco
3ª renovação → RenovacaoNegadaException → desfazer()
arquivo == antes; título continua "Dom Casmurro"; não sobra biblioteca.json.tmp
```

### B.5 Na arguição, saiba explicar
**Onde a família é escolhida?** Só em `ConfiguradorDePersistencia.fabricaPara(perfil, arquivo)`, uma vez, na
montagem (`Sistema.montar`). Testes usam `TESTE`; a aplicação web usará `PRODUCAO`. Sem perfil, a montagem
falha — não há família "por omissão". Não usamos `@Profile` do Spring: a escolha é explícita.

**Como os repositórios compartilham a mesma transação?** A fábrica entrega a mesma `UnidadeDeTrabalho` a
todos os repositórios; eles alteram a mesma cópia de trabalho. Um `confirmar()` grava tudo de uma vez (no JSON,
o arquivo inteiro de forma atômica); um `desfazer()` descarta tudo.

**O que impede misturar um repositório em memória com outro JSON?** Cada fábrica concreta só aceita a unidade de
trabalho da sua família (`FabricaJson.criarRepositorioLivro(uowDeMemoria)` → `FamiliaIncompativelException`), e
os construtores dos repositórios concretos exigem o tipo concreto da unidade (`RepositorioLivroJson(UnidadeDeTrabalhoJson)`).
Na prática, o sistema só recebe objetos de uma fábrica.

**Outras perguntas prováveis**
- *Por que gravar em `.tmp` e mover?* Se o programa cair no meio da escrita, o original continua intacto.
- *Por que `synchronized`?* Duas requisições web simultâneas não podem alterar a cópia nem gravar o arquivo ao
  mesmo tempo.
- *Qual a desvantagem do Abstract Factory?* Acrescentar um novo **tipo** de produto (ex.: repositório de multas)
  exige mudar a interface da fábrica e todas as fábricas concretas — é o que faremos nas próximas etapas.

---

## Exercícios de alteração ao vivo

**Template Method — novo cadastro (ex.: Editora).** Criar `DadosEditora` e `TratadorDeEditora extends
TratadorDeCadastro<Editora, DadosEditora>` implementando `buscarExistente`, `validar`, `criar`, `armazenar`.
A ordem dos passos vem pronta; nenhuma classe existente muda.

**Template Method — novo pré-requisito.** "Atendente não pode ter a mesma pessoa de um estudante": só o método
`TratadorDeAtendente.verificarPreRequisitos` muda.

**Abstract Factory — nova família (ex.: arquivo XML).** Criar `FabricaXml`, `UnidadeDeTrabalhoXml` e os três
repositórios `...Xml`, e um novo valor de `Perfil` no configurador. Acervo, Movimentacao, grupos e tratadores
não mudam: eles só conhecem as interfaces.

## Autoavaliação
1. Por que `inserir` é `final`? 2. Em que passo o estudante sem pessoa é recusado? 3. Onde se escolhe a família?
4. O que `desfazer()` faz na família JSON? 5. Por que a situação do livro é gravada como texto?

**Respostas:** 1. Para nenhuma subclasse alterar a ordem dos passos. 2. No gancho `verificarPreRequisitos`
(passo 3), antes de criar/armazenar. 3. `ConfiguradorDePersistencia.fabricaPara`. 4. Descarta a cópia de trabalho
e recarrega o arquivo. 5. Porque o objeto de estado não tem dados; `EstadoLivro.para(Situacao)` o recria.
