# Sistema de Biblioteca com Padrões GoF — Trabalho 03 (PUCPR · Modelagem de Sistemas Computacionais)

Você é o construtor deste projeto e o mentor da equipe. Somos estudantes e **cada integrante será arguido individualmente sobre qualquer parte do código** (código que ninguém explica é considerado não implementado). Por isso, além de construir o sistema, você documenta tudo de forma didática para a equipe estudar.

## Fontes

Em `docs/`:
- `Trabalho_Implementacao_GoF_Biblioteca.pdf` — enunciado do Trabalho 03 (especificação oficial).
- `Solucoes_GoF_Biblioteca_EES.pdf` — soluções do professor para os 10 padrões (referência de projeto).
- `TRABALHO_PARTE_1.pdf` e `TRABALHO_PARTE_2.pdf` — regras de negócio dos Trabalhos 01 e 02.
- Imagens `.png` — diagramas do nosso Astah.

Quando os PDFs e o nosso Astah divergirem, siga a regra de decisões da seção "Forma de trabalho".

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

## Forma de trabalho: MODO AUTÔNOMO

A equipe pediu velocidade. **Execute todas as etapas em sequência, sem parar para pedir aprovação.** O objetivo principal é o sistema **rodar na web** com os 10 padrões e os 10 testes aprovados. Toda a explicação fica documentada para a equipe estudar depois.

**Decisões:** quando houver divergência entre os PDFs e o Astah, ou qualquer escolha em aberto, **decida você mesmo** pela opção que recomendaria: siga as Soluções do professor onde o padrão exigir; nos demais casos, mantenha os nomes do Astah. Registre cada decisão em `docs/DECISOES.md` (o que foi decidido, alternativas e motivo). **Só pare e pergunte** se algo impedir de continuar (ex.: falta de acesso, erro que você não consegue resolver) ou se a ação for destrutiva.

**Ritmo de cada etapa:**
1. Implemente a etapa inteira (classes, integração e testes).
2. Rode todos os testes. Se algum falhar, corrija antes de seguir.
3. Escreva o guia de estudo `docs/estudo/ETAPAxx_<tema>.md`: papéis do padrão → classes, fluxo do cenário passo a passo com valores concretos, respostas às perguntas "Na arguição, saiba explicar" do enunciado e um exercício de alteração ao vivo.
4. Atualize `docs/PROGRESSO.md`, `docs/RASTREABILIDADE.md`, `REGISTRO_LLM.md` e `docs/uml/ATUALIZACOES_ASTAH.md` (o que muda no Astah, para a equipe fazer no fim).
5. Faça commit e push para o GitHub com uma mensagem clara.
6. Escreva no chat **um resumo de no máximo 5 linhas** do que foi feito e siga direto para a próxima etapa.

**Se a sessão ficar longa ou for interrompida:** ao recomeçar, leia `docs/PROGRESSO.md` e continue de onde parou.

**Entrega final (Etapa 6):**
- Aplicação web rodando localmente com um comando descrito no README, cobrindo os cadastros e o fluxo reserva → empréstimo → renovação → devolução atrasada → multa.
- `README.md` completo: como compilar, rodar e testar; arquitetura (pacotes = componentes); **uma seção por padrão** explicando em linguagem simples o problema, as classes e o fluxo; persistência JSON; ferramentas de IA usadas e em quais partes.
- `docs/estudo/DEMO.md`: roteiro da demonstração de 5 minutos.
- `docs/estudo/ARGUICAO.md`: as perguntas prováveis de cada padrão com respostas curtas, para a equipe estudar.
- `docs/uml/ATUALIZACOES_ASTAH.md`: lista final do que mudar no Astah, organizada por diagrama.

## Etapas

- **Etapa 0 — Fundação:** projeto Maven, estrutura de pacotes, classes de domínio do Astah, tratadores, README com o comando único, `PROGRESSO.md`, `REGISTRO_LLM.md`, `RASTREABILIDADE.md`.
- **Etapa 1 — Ciclo de vida do Livro:** State (Req 1).
- **Etapa 2 — Cadastros e persistência:** Template Method (Req 7) e Abstract Factory (Req 8), com as famílias em memória e JSON.
- **Etapa 3 — Circulação:** Facade (Req 4) e Command (Req 6).
- **Etapa 4 — Devolução e financeiro:** Observer (Req 5), Strategy (Req 2) e Adapter (Req 3).
- **Etapa 5 — Acervo e consulta:** Composite (Req 10) e Decorator (Req 9).
- **Etapa 6 — Interface web e demonstração:** telas e APIs para cadastros e para o fluxo reserva → empréstimo → renovação → devolução atrasada → multa; README final (inclusive ferramentas de IA usadas); roteiro da demonstração de 5 minutos.
