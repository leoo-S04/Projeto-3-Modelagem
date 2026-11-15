# Sistema de Biblioteca com Padrões GoF — Trabalho 03 (PUCPR · Modelagem de Sistemas Computacionais)

Somos estudantes e **cada integrante será arguido individualmente sobre qualquer parte do código** (código que ninguém explica é considerado não implementado). Por isso, prefira sempre a solução **mais simples** que atende ao enunciado.

## Fontes

Em `docs/`:
- `Trabalho_Implementacao_GoF_Biblioteca.pdf` — enunciado do Trabalho 03 (especificação oficial).
- `Solucoes_GoF_Biblioteca_EES.pdf` — soluções do professor para os 10 padrões (referência de projeto).
- `TRABALHO_PARTE_1.pdf` e `TRABALHO_PARTE_2.pdf` — regras de negócio dos Trabalhos 01 e 02.
- Imagens `.png` — diagramas do nosso Astah.

Quando os PDFs e o nosso Astah divergirem, siga as Soluções do professor onde o padrão exigir; nos demais casos, os nomes do Astah.

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
| 9 | Decorator | 2ª consulta de metadados usa cache e ainda gera medição; cache antigo nunca "auto"riza reservar livro que deixou de estar LIVRE |
| 10 | Composite | Unidade com 2 seções (3 e 2 livros) informa 5; mover livro entre seções não altera id, estado nem empréstimo |

Os detalhes de cada requisito ("O que implementar" e "Na arguição, saiba explicar") estão no enunciado; leia a parte do requisito antes de planejar a etapa correspondente.

## Forma de trabalho (atualizada em 15/11/2026)

As etapas 0 a 6 e os diagramas do Astah estão concluídos. A equipe pediu um repositório **enxuto**:

- **Não crie** guias de estudo, registros de decisões, arquivos de progresso nem outros documentos de acompanhamento.
- Documentos do repositório: `README.md`, `docs/RASTREABILIDADE.md`, `REGISTRO_LLM.md` (uma linha curta por pedido relevante; a coluna "Decisão da equipe" é só da equipe) e `docs/uml/`.
- Antes de mudanças grandes (apagar ou reescrever partes do código, trocar a interface), explique em poucas linhas e pergunte.
- Respostas no chat curtas e em linguagem simples.
- Se o código mudar, regenere os diagramas com `ferramentas/astah/gerar.cmd` (Astah fechado).
