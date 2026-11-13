# Roteiro da demonstração — 5 minutos

**Antes de começar (fora do tempo):**
1. Apagar `dados/biblioteca.json` (se existir) para partir dos dados de exemplo.
2. `mvnw.cmd test` → deixar no terminal o resultado: **105 testes, 0 falhas, BUILD SUCCESS**.
3. `mvnw.cmd spring-boot:run` → abrir http://localhost:8080 em uma aba; abrir `dados/biblioteca.json` em um editor.

Dados de exemplo: Ana (estudante **M-100**), Caio (estudante **M-300**), Bruno (atendente **F-01**), livros 1 a 6;
unidade "Biblioteca Central" com as seções Realismo (1, 2, 3) e Romantismo (5, 6).

| Tempo | Quem fala | O que fazer na tela | O que dizer (padrão) |
|---|---|---|---|
| 0:00–0:30 | Integrante 1 | Terminal com os testes | "Os 10 cenários do enunciado são testes JUnit `R01_` a `R10_`; todos passam com um único comando." |
| 0:30–1:00 | Integrante 1 | **Cadastros**: inserir estudante com CPF `00000000000` | **Template Method**: "a rotina é fixa; o gancho de pré-requisito recusa antes de gravar — veja a mensagem." Inserir de novo `M-100`: "duplicado devolve o existente." |
| 1:00–1:45 | Integrante 2 | **Circulação**: f1 reservar livro 1 para M-100 → f3 emprestar → f4 renovar duas vezes → tentar a 3ª | **State + Facade + Command**: "o estado decide; a 3ª renovação é recusada e o vencimento não muda. A fachada efetivou o empréstimo numa transação. Cada solicitação tem id: um duplo clique não renova duas vezes." |
| 1:45–2:00 | Integrante 2 | Tentar reservar o livro 1 para M-300 | "Recusado: o livro não está LIVRE." |
| 2:00–2:20 | Integrante 3 | **Administração**: adiantar o relógio **65** dias | "O relógio é injetado (`Clock`); simulamos o atraso sem esperar." |
| 2:20–3:10 | Integrante 3 | **Circulação**: f5 devolver o livro 1 → ver "5 dia(s) de atraso"; **Início**: inspeção pendente, indicadores; **Administração**: e-mail na caixa de saída | **Observer**: "a devolução foi confirmada e só então publicada; inspeção, multa, indicadores e e-mail reagiram. O livro fica DEVOLVIDO até a inspeção." |
| 3:10–3:50 | Integrante 4 | **Financeiro**: boleto de R$ 5,00, política ORIGINAL, banco A, id `A-0001` → "Pagar" → "Reenviar notificação" | **Strategy**: "a multa guardou valor e política." **Adapter**: "o banco A recebeu 700 centavos; a notificação JSON foi traduzida pelo adaptador; o reenvio não quita duas vezes." Simular 5 dias: ORIGINAL 5,00 / TOLERÂNCIA 3,00. |
| 3:50–4:30 | Integrante 4 | **Acervo**: buscar "dom" duas vezes (ver 2 medições); árvore: Central = 5; mover o livro 1 de Realismo para Romantismo | **Decorator**: "a 2ª busca veio do cache e mesmo assim foi medida; o cache só tem id e título." **Composite**: "a contagem é recursiva; mover não muda o id, o estado nem o empréstimo." |
| 4:30–5:00 | Qualquer um | Editor com `dados/biblioteca.json` | **Abstract Factory**: "em produção, a fábrica JSON; nos testes, a de memória — escolhidas uma vez pelo perfil. Tudo é gravado num único arquivo, de forma atômica; veja `"situacao" : "DEVOLVIDO"`." |

**Plano B:** se a aplicação não subir, rodar os testes e explicar os cenários por eles (`R01_`…`R10_`).

**Para a alteração ao vivo** (o professor escolhe): ver os "Exercícios de alteração ao vivo" de cada guia em
`docs/estudo/ETAPA0x_*.md` — por exemplo, nova política de multa (Strategy) ou novo ouvinte da devolução (Observer).
