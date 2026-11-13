# Registro de uso de LLM

Exigência do Trabalho 03 ("Uso de LLMs"): resumo dos principais pedidos feitos ao modelo e do que a equipe
**aceitou, corrigiu ou descartou**. As colunas "Decisão da equipe" e "Observações" são preenchidas pela equipe.

- **Ferramenta:** Claude Code (CLI), em sessão de trabalho no repositório.
- **Modelo:** Claude Opus 5.5 (`claude-opus-5-5`), da Anthropic.
- **Forma de trabalho:** definida em `CLAUDE.md` — etapas e passos curtos, cada passo aprovado pela equipe antes do próximo.

## Etapa 0 — Fundação

| # | Pedido ao modelo | O que o modelo produziu | Decisão da equipe (aceitou / corrigiu / descartou) | Observações |
|---|---|---|---|---|
| 1 | Ler `CLAUDE.md`, os PDFs (enunciado do T03, soluções do professor, T01, T02) e os diagramas do Astah; apresentar o plano da Etapa 0 | Plano com objetivo, passos e testes; 4 divergências PDF × Astah para decisão: nomes `Acervo`/`Relacionamento`, interface `CRUD`, nome do enum `EstadoLivro`, comportamento de f1/f3/f4/f5 na Etapa 0 | Equipe escolheu A nas 4 perguntas | |
| 2 | Não alterar o Astah durante a implementação; registrar cada mudança necessária e entregar a lista na Etapa 6 | `docs/uml/ATUALIZACOES_ASTAH.md`, preenchido a cada passo | | |
| 3 | Passo 1 — projeto Maven | `pom.xml` (Java 17, JUnit 5, `biblioteca.jar`), Maven Wrapper, `.gitignore`, `README.md`, `docs/PROGRESSO.md` | | |
| 4 | Passo 2 (antigo) — pacotes por componente | `package-info.java` de `rh`, `academico`, `biblioteca`, `financeiro`, `web` | | |
| 5 | Reorganizar a Etapa 0 em 3 passos e usar o formato curto de resposta | `PROGRESSO.md` reorganizado | | |
| 6 | Passo 2 — domínio e cadastros | Classes de RH, Acadêmico e Livro/Acervo com seus tratadores (regras 1 a 4) e 19 testes | | |
| 7 | Passo 3 — circulação e documentação | `Transacao`, `Reserva`, `Emprestimo`, `Devolucao`, `Movimentacao`, `Relacionamento`, tratadores de circulação (f2 funcionando; f1, f3, f4, f5 e inspeção pendentes para a Etapa 1), 8 testes, este registro e `docs/RASTREABILIDADE.md`. Apontou divergência: Movimentacao com lista única (Astah) × associações separadas (T02 e professor) | Equipe decidiu separar em 3 associações na Etapa 2 | |
| 8 | Fechamento da Etapa 0 | Execução completa dos testes (27 aprovados), guia `docs/estudo/ETAPA00_Fundacao.md`, atualização da rastreabilidade e deste registro, lista de mudanças no Astah, sugestão de commit | | |
