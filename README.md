# Sistema de Biblioteca com Padrões GoF — Trabalho 03

PUCPR · Modelagem de Sistemas Computacionais · Prof. Edson Emilio Scalabrin · 2º semestre de 2026

Implementação do sistema de cadastro, reserva, empréstimo, renovação e devolução de livros
(Trabalhos 01 e 02), aplicando os dez padrões GoF pedidos no Trabalho 03.

> README inicial (Etapa 0). Será completado ao longo das etapas e finalizado na Etapa 6.

## Pré-requisitos

- JDK 17 ou superior (o código é compilado para Java 17).
- Internet **na primeira execução**: o Maven Wrapper baixa o Maven 3.9.9 e as dependências uma única vez.
  Não é preciso instalar o Maven.

## Comando único: compilar e rodar todos os testes

Windows:

```
mvnw.cmd test
```

Linux / macOS:

```
./mvnw test
```

## Estrutura

Um único artefato, `target/biblioteca.jar` (diagrama de implantação). Os pacotes correspondem aos
componentes do diagrama de componentes:

| Pacote (`br.pucpr.ib2b.`) | Componente | Conteúdo |
|---|---|---|
| `rh` | RH | Pessoa, Atendente e seus cadastros (`iPessoa`, `iAtendente`) |
| `academico` | Acadêmico | Estudante e seu cadastro (`iEstudante`) |
| `biblioteca` | Biblioteca | Livros, Acervo, transações, Movimentacao, Relacionamento, f1–f6 (`iLivro`, `iEmprestimo`, `iDevolucao`) |
| `financeiro` | Financeiro | Multas e boletos; bancos A e B simulados (Etapa 4) |
| `web` | Interface Web | Telas, que só chamam os tratadores (Etapa 6) |

Documentação: [`docs/RASTREABILIDADE.md`](docs/RASTREABILIDADE.md) ·
[`REGISTRO_LLM.md`](REGISTRO_LLM.md) · [`docs/uml/ATUALIZACOES_ASTAH.md`](docs/uml/ATUALIZACOES_ASTAH.md)

## Estado atual

Veja o andamento em [`docs/PROGRESSO.md`](docs/PROGRESSO.md).
