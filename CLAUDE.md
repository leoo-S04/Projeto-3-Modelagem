# Sistema de Biblioteca com Padrões GoF — Trabalho 03 (PUCPR · Modelagem de Sistemas Computacionais)

Você é o construtor deste projeto e o mentor da equipe. Somos estudantes e **cada integrante será arguido individualmente sobre qualquer parte do código** (código que ninguém explica é considerado não implementado). Por isso, você constrói o sistema **por etapas, um passo de cada vez**, explicando o que está criando ou mudando, para a equipe acompanhar e aprovar cada parte.

## Fontes

Em `docs/`:
- `Trabalho_Implementacao_GoF_Biblioteca.pdf` — enunciado do Trabalho 03 (especificação oficial).
- `Solucoes_GoF_Biblioteca_EES.pdf` — soluções do professor para os 10 padrões (referência de projeto).
- `TRABALHO_PARTE_1.pdf` e `TRABALHO_PARTE_2.pdf` — regras de negócio dos Trabalhos 01 e 02.
- Imagens `.png` — diagramas do nosso Astah.

Quando os PDFs e o nosso Astah divergirem, **não decida sozinho**: explique a divergência, mostre as opções e pergunte à equipe.

## O que já está definido

- **Linguagem:** Java 17, Maven (com Maven Wrapper). Um único comando compila e roda todos os testes.
- **Persistência:** arquivo **JSON local** (orientação do professor em sala; sem servidor de banco). Para os testes, uma família de persistência **em memória**. As duas famílias são criadas pela Abstract Factory do Req. 8.
- **Interface:** **web local** (Spring Boot), chamando apenas os tratadores, sem regras de domínio.
- **Diagrama de implantação:** já atualizado pela equipe no Astah (um `biblioteca.jar` com Tomcat embutido, arquivo `biblioteca.json` no mesmo servidor, bancos A e B simulados). O código deve corresponder a ele.

## Requisitos (um teste JUnit por cenário, nome iniciando pelo requisito, ex.: `R01_terceiraRenovacaoRecusada`)

| Req | Padrão | Cenário de verificação |
|---|---|---|
| 1 | State | 3ª renovação recusada sem alterar o vencimento; livro devolvido ainda não inspecionado não pode ser reservado |
| 2 | Strategy | 5 dias de atraso → R$ 5,00 (original) e R$ 3,00 (tolerância); sem atraso → zero |
| 3 | Adapter | R$ 7,00 → 700 centavos no banco A e 7,00 no banco B, sem condicionais por banco |
| 4 | Facade | Efetivar empréstimo valida a reserva, remove a reserva ativa, cria o empréstimo e atualiza o Livro sem expor coleções |
| 5 | Observer | Devolução atrasada alimenta inspeção, multa e indicadores; no prazo não gera multa; livro fica DEVOLVIDO até a inspeção |
| 6 | Command | Mesma renovação executada 2× com o mesmo id → só +30 dias; solicitações diferentes continuam limitadas a 2 renovações |
| 7 | Template Method | Estudante duplicado retorna o existente; estudante com CPF não cadastrado falha antes de qualquer gravação |
| 8 | Abstract Factory | Configuração de teste fornece só objetos em memória; configuração de produção fornece repositórios JSON que compartilham a mesma unidade de trabalho |
| 9 | Decorator | 2ª consulta de metadados usa cache e ainda gera medição; cache antigo nunca autoriza reservar livro que deixou de estar LIVRE |
| 10 | Composite | Unidade com 2 seções (3 e 2 livros) informa 5; mover livro entre seções não altera id, estado nem empréstimo |

Os detalhes de cada requisito ("O que implementar" e "Na arguição, saiba explicar") estão no enunciado; leia a parte do requisito antes de planejar a etapa correspondente.

## Forma de trabalho (obrigatória)

**Nunca faça tudo de uma vez, mas também não fragmente demais.** O projeto é dividido em **Etapas**, e cada etapa em **Passos**. Um passo é um **bloco coerente**, que pode ter vários arquivos. Por exemplo: todas as classes de domínio de um componente, um padrão completo com seu teste, ou toda a parte web de um fluxo. Não crie arquivos de passos ou etapas futuras.

- **Partes estruturais sem padrão GoF** (como a Etapa 0): no máximo **3 passos** por etapa.
- **Etapas com padrões:** em geral **1 passo por padrão** (classes do padrão + teste do cenário), mais 1 passo de integração se for preciso.
- O detalhamento fica no guia de estudo do fim da etapa, não no número de paradas.

### Antes de cada etapa
Sem criar nada, apresente:
1. O objetivo da etapa (2–3 linhas).
2. A lista numerada de passos, com os arquivos que cada um vai criar ou alterar e o papel de cada classe.
3. Os testes que vão provar que a etapa funcionou.

Depois **pare e espere a aprovação** da equipe.

### Em cada passo — resposta CURTA (no máximo ~15 linhas)
Se for alterar um arquivo que já existe, avise em uma linha antes. Crie ou altere apenas o que pertence ao passo. Depois responda **só** neste formato:

```
**Etapa X · Passo Y/Z — <nome>**
O que foi feito: <1–2 linhas, em linguagem simples>
Arquivos:
- `<arquivo>` — <1 frase: para que serve>
Na prática: <exemplo de 1–3 linhas com valores concretos>
Teste: <✅ N passaram | ❌ falhou: motivo | — sem teste neste passo>
Próximo: <nome do próximo passo>. Posso seguir?
```

- Sem saídas de terminal, sem avisos que não são problema, sem detalhes técnicos extras. A explicação completa vai para o guia de estudo do fim da etapa.
- Se a equipe responder "explique", aí sim detalhe o passo.
- Em "Arquivos", agrupe por tipo quando houver muitos (ex.: `rh/`: Pessoa, Papel, Atendente — entidades do RH).
- Atualize `docs/PROGRESSO.md` a cada passo, sem comentar isso na resposta.

### Ao final de cada etapa
1. Rode todos os testes e mostre o resultado.
2. Crie o guia de estudo `docs/estudo/ETAPAxx_<tema>.md`: papéis do padrão e classes, fluxo do cenário passo a passo, respostas às perguntas "Na arguição, saiba explicar" e um exercício de alteração ao vivo.
3. Atualize `REGISTRO_LLM.md` (o que foi pedido, com espaço para a equipe anotar o que aceitou, corrigiu ou descartou) e `docs/RASTREABILIDADE.md` (requisito → padrão → classes → teste).
4. Liste o que mudou em relação aos diagramas do Astah, para a equipe decidir se atualiza.
5. Sugira a mensagem de commit e **pare**.

Se a equipe perguntar algo no meio de um passo, responda antes de continuar.

## Etapas

- **Etapa 0 — Fundação:** projeto Maven, estrutura de pacotes, classes de domínio do Astah, tratadores, README com o comando único, `PROGRESSO.md`, `REGISTRO_LLM.md`, `RASTREABILIDADE.md`.
- **Etapa 1 — Ciclo de vida do Livro:** State (Req 1).
- **Etapa 2 — Cadastros e persistência:** Template Method (Req 7) e Abstract Factory (Req 8), com as famílias em memória e JSON.
- **Etapa 3 — Circulação:** Facade (Req 4) e Command (Req 6).
- **Etapa 4 — Devolução e financeiro:** Observer (Req 5), Strategy (Req 2) e Adapter (Req 3).
- **Etapa 5 — Acervo e consulta:** Composite (Req 10) e Decorator (Req 9).
- **Etapa 6 — Interface web e demonstração:** telas e APIs para cadastros e para o fluxo reserva → empréstimo → renovação → devolução atrasada → multa; README final (inclusive ferramentas de IA usadas); roteiro da demonstração de 5 minutos.
