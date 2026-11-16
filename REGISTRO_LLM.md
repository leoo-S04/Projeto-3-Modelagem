# Registro de uso de LLM

Resumo dos principais pedidos feitos ao modelo e do que a equipe aceitou, corrigiu ou descartou
(exigência do Trabalho 03). A coluna "Decisão da equipe" foi preenchida pela equipe.

A equipe usou o modelo em todas as partes do trabalho (código, testes, diagramas e documentação) e aceitou as
propostas como vieram, com uma exceção: a interface web, refeita a partir das ideias da equipe (linhas 16, 19 e 20).

- **Ferramenta:** Claude Code (Anthropic), trabalhando direto no repositório.
- **Modelo:** Claude Opus 5.5 (`claude-opus-5-5`).

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe |
|---|---|---|---|
| 1 | Ler o enunciado, as soluções do professor, os Trabalhos 01/02 e o Astah; planejar a Etapa 0 | Plano com 4 divergências PDF × Astah para a equipe decidir | Escolheu a opção A nas 4 perguntas |
| 2 | Não alterar o Astah durante a implementação; anotar as mudanças | Lista de mudanças do Astah | Aceitou |
| 3 | Criar o projeto Maven | `pom.xml` (Java 17, JUnit 5), Maven Wrapper, `.gitignore`, README | Aceitou |
| 4 | Pacotes por componente | Pacotes `rh`, `academico`, `biblioteca`, `financeiro`, `web` | Aceitou |
| 5 | Reorganizar a Etapa 0 em 3 passos | Plano reorganizado | Aceitou |
| 6 | Domínio e cadastros | Classes de RH, Acadêmico, Livro/Acervo e tratadores (regras 1 a 4), com testes | Aceitou |
| 7 | Circulação | `Reserva`, `Emprestimo`, `Devolucao`, `Movimentacao`, `Relacionamento`, tratadores; apontou divergência na `Movimentacao` | Decidiu separar em 3 associações |
| 8 | Fechar a Etapa 0 | 27 testes aprovados | Aceitou |
| 9 | Planejar a Etapa 1 | 3 perguntas sobre vencimento, nomes dos eventos e recusas | Aceitou as recomendações (prazo de 30 dias) |
| 10 | State (Req. 1) | `EstadoLivro` e os 7 estados; `Livro` delega os eventos | Aceitou |
| 11 | Seguir as etapas sem pedir aprovação a cada passo | Tratadores usando o State; teste R01 | Aceitou |
| 12 | Template Method (Req. 7) e Abstract Factory (Req. 8) | `TratadorDeCadastro`; fábricas memória/JSON; testes R07 e R08 | Aceitou |
| 13 | Facade (Req. 4) e Command (Req. 6) | `ServicoDeCirculacao`; solicitações de reserva e renovação; testes R04 e R06 | Aceitou |
| 14 | Observer (Req. 5), Strategy (Req. 2) e Adapter (Req. 3) | Ouvintes da devolução; 3 políticas de multa; adaptadores dos bancos A e B; testes R02, R03, R05 | Aceitou |
| 15 | Composite (Req. 10) e Decorator (Req. 9) | Organização do acervo em grupos; consulta com cache e medição; testes R09 e R10 | Aceitou |
| 16 | Interface web e demonstração | Aplicação Spring Boot com as telas; roteiro de demonstração | Descartou: a equipe achou as telas técnicas demais; substituídas pelas telas por ator (linhas 19 e 20) |
| 17 | Diagramas do Astah e PDF | Diagramas de classes e de sequência dos 10 padrões e máquina de estados, gerados do código; `docs/uml/Trabalho03_Diagramas.pdf` | Aceitou |
| 18 | Enxugar o repositório | Removidos guias de estudo, registro de decisões e documentos de acompanhamento; README e rastreabilidade resumidos (o conteúdo antigo continua no histórico do Git) | Aceitou |
| 19 | Refazer a interface web com login e uma área para cada ator (estudante, atendente, administrador) | Login (senha = CPF; administrador fixo), controle de acesso por perfil, telas do estudante, do balcão do atendente e da administração; telas antigas e API JSON removidas; teste do fluxo web reescrito | Corrigiu: ideia da equipe — login e uma tela para cada ator do diagrama de casos de uso |
| 20 | Telas com boas-vindas e acompanhamento: barra do prazo, histórico, monitoramento colorido, consulta do empréstimo e ficha do estudante | Nova consulta de empréstimos na fachada (`consultarEmprestimos`, com `EmprestimoDTO` e `HistoricoDeEmprestimos`) e teste; telas do estudante, do atendente (balcão, monitoramento, consulta) e do administrador (estudantes, empréstimos) | Corrigiu: a equipe definiu o conteúdo das telas (barra do prazo, histórico, aviso de multa, monitoramento colorido, ficha do estudante) |
