# Etapa 5 — Acervo e consulta: **Composite** (Req. 10) e **Decorator** (Req. 9)

---

## Parte A — Composite (Req. 10)

### A.1 O problema
O acervo é organizado em unidades, seções e subseções, com profundidade variável. A tela precisa perguntar
"quantos livros há aqui?" a qualquer nível — e a um exemplar isolado — sem escrever um laço diferente para cada
nível.

### A.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Component | «abstract» `ComponenteDoAcervo` | `contarLivros()`, `listarIds()`, `getPai()` (0..1). |
| Leaf | `ItemDoAcervo` | Um exemplar, pelo **id** do Livro: conta 1, lista o próprio id. |
| Composite | `GrupoDoAcervo` (+ `TipoDeGrupo`: UNIDADE, SECAO, SUBSECAO) | Mantém filhos; soma e concatena recursivamente; protege as invariantes em `adicionar`. |
| Client | `OrganizadorDoAcervo` (e, por ele, `TratadorDeAcervo` → web) | Navega a partir das unidades; cria grupos; coloca e **move** livros. |
| Falha | `HierarquiaInvalidaException` | Ciclo, componente com pai, livro em dois grupos. |
| Persistência | `RepositorioDeOrganizacao` (novo produto da fábrica) | Seção `acervo` do JSON (um registro por grupo, com o nome do pai). |

### A.3 Fluxo do cenário — `R10_unidadeComDuasSecoesDeTresEDoisLivrosInformaCinco`
```
Central (UNIDADE)
 ├─ Literatura (SECAO): Item 1, Item 2, Item 3
 └─ Ciencias  (SECAO): Item 4, Item 5

contarLivros("Central") → GrupoDoAcervo(Central).contarLivros()
   para cada filho: Literatura.contarLivros() → 1 + 1 + 1 = 3
                    Ciencias.contarLivros()   → 1 + 1     = 2
   → 3 + 2 = 5
```
**`R10_moverLivroEntreSecoesNaoAlteraIdEstadoNemEmprestimo`** (livro 1 EMPRESTADO, vence 13/12/2026)
```
moverLivro(1, "Ciencias") → OrganizadorDoAcervo.mover   (dentro de uma transação)
  1 localizarItem(1)          → Item 1 (pai = Literatura)
  2 Literatura.remover(item)  → pai = null
  3 Ciencias.adicionar(item)  → verifica ciclo, pai e duplicidade → ok → pai = Ciencias
Literatura = 2, Ciencias = 3, Central = 5. O Livro 1 e a Movimentacao não recebem mensagem nenhuma:
id 1, EMPRESTADO, vencimento 13/12/2026 e empréstimo ativo continuam iguais.
```

### A.4 Na arguição, saiba explicar
**Como a contagem percorre a árvore?** Por recursão polimórfica: o grupo chama `contarLivros()` em cada filho e
soma; o item devolve 1. O cliente chama o mesmo método em qualquer nível, sem saber a profundidade.

**Quais regras impedem ciclos e livros em dois grupos?** Em `GrupoDoAcervo.adicionar(c)`: recusa `c == this` ou
`c` ancestral do grupo (ciclo); recusa `c` que já tem pai (para mudar de lugar, primeiro remover); recusa ids que
já aparecem na árvore (raiz). O `OrganizadorDoAcervo` também confere entre unidades diferentes. Em qualquer
recusa a árvore não muda.

**Por que mover um item não toca no Livro nem na Movimentacao?** Porque o item só **referencia** o livro pelo id:
a organização é uma visão do acervo, separada da circulação. Mover muda apenas o pai do item.

---

## Parte B — Decorator (Req. 9)

### B.1 O problema
A consulta pública de metadados pode precisar de recursos opcionais — medir o tempo, guardar em cache — em
combinações diferentes por instalação. Copiar o serviço para cada combinação ou encher a implementação original
de `if (usaCache)` seria frágil.

### B.2 Papéis → classes

| Papel GoF | Classe | Responsabilidade |
|---|---|---|
| Component | «interface» `ConsultaAcervo` | `buscarMetadados(Criterio): List<MetadadosLivro>`. |
| ConcreteComponent | `ConsultaAcervoBasica` | Implementação original, inalterada: lê o Acervo. |
| Decorator | «abstract» `DecoradorDeConsulta` | Guarda `#interno` e delega por padrão. |
| ConcreteDecorator | `DecoradorDeMedicao`, `DecoradorDeCache` | Medem o tempo; guardam metadados com validade (TTL). |
| Montagem | `MontadorDeConsulta` + `ConfigInstalacao` | Compõe a cadeia conforme a instalação. |
| Client | `TratadorDeAcervo.buscarMetadados` → web | Conhece só `ConsultaAcervo`. |
| Valor | `MetadadosLivro(idLivro, titulo)` | **Sem situação**: o cache não sabe se o livro está livre. |

### B.3 Fluxo do cenário — `R09_segundaConsultaDeMetadadosUsaCacheEAindaGeraMedicao`
```
cadeia = Medicao( Cache( Basica ) )
1ª consulta "Dom Casmurro":
   Medicao: início → Cache: entrada ausente → Basica.buscarMetadados → [{1, Dom Casmurro}] → guarda (TTL 5 min)
   Medicao: registra duração (medição nº 1)
2ª consulta "  dom casmurro " (Criterio normaliza → mesma chave):
   Medicao: início → Cache: entrada válida → devolve [{1, Dom Casmurro}] (Basica NÃO é chamada)
   Medicao: registra duração (medição nº 2)
Basica chamada 1 vez; 2 medições.
```
**`R09_cacheAntigoNuncaAutorizaReservarLivroQueDeixouDeEstarLivre`**
```
buscarMetadados("dom") → cache guarda [{1, Dom Casmurro}]
Ana reserva e leva o livro 1 → EMPRESTADO
buscarMetadados("dom") → cache ainda devolve [{1, Dom Casmurro}] — só id e título
Caio tenta reservar o livro 1 → TratadorDeEmprestimo → executor → iCirculacao.reservar
   → Acervo.buscarPorId(1) → Livro (estado ATUAL: EMPRESTADO) → reservarLivro → recusa
   → ResultadoSolicitacao RECUSADA "...livro está EMPRESTADO"
```

### B.4 Na arguição, saiba explicar
**Como a cadeia de decoradores é montada e por que a ordem importa?** `MontadorDeConsulta.montar(config, ...)`
embrulha a básica no cache (se `config.cache()`) e o resultado na medição (se `config.medicao()`). Com a medição
**por fora**, as consultas atendidas pelo cache também são medidas. Na ordem inversa, `Cache(Medicao(Basica))`,
as consultas do cache não passariam pela medição — o cenário do enunciado falharia.

**O que acontece se o cache falhar?** `DecoradorDeCache` captura a exceção do armazenamento, conta a falha e delega
ao interno: a consulta continua respondendo, só que sem cache (teste `falhaDoCacheNaoDerrubaAConsulta`).
Falha ao registrar a métrica também é capturada pelo `DecoradorDeMedicao`.

**Por que o cache não participa da decisão de reserva?** Porque guarda só metadados (id e título), que podem estar
velhos; a disponibilidade muda a cada operação. A reserva não usa a consulta: vai pela fachada ao Acervo, lê o
Livro atual e o estado (State) decide.

---

## Exercícios de alteração ao vivo

**Decorator — novo recurso "registro de auditoria".** `class DecoradorDeAuditoria extends DecoradorDeConsulta`
que anota quem/quando consultou e delega; acrescentar um campo em `ConfigInstalacao` e uma linha no
`MontadorDeConsulta`. `ConsultaAcervoBasica` e os outros decoradores não mudam.

**Composite — novo nível "estante".** Basta um novo valor em `TipoDeGrupo` (`ESTANTE`): a profundidade é
variável e a contagem recursiva já funciona; nenhuma classe nova.

## Autoavaliação
1. Quem soma a contagem? 2. O que impede um livro de estar em duas seções? 3. Mover altera o Livro?
4. Por que a medição fica por fora do cache? 5. O cache pode autorizar uma reserva?

**Respostas:** 1. `GrupoDoAcervo.contarLivros` (recursão nos filhos). 2. `GrupoDoAcervo.adicionar` (ids já na
árvore) e o `OrganizadorDoAcervo` (entre unidades). 3. Não, só o pai do item. 4. Para medir também os acertos do
cache. 5. Não: guarda só metadados, e a reserva lê o estado atual do Livro.
