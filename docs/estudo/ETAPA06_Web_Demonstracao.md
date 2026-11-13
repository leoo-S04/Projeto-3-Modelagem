# Etapa 6 — Interface web e demonstração

> A Etapa 6 não introduz padrão GoF novo: ela **expõe** os 10 padrões numa interface web local que chama apenas os
> tratadores. O ponto a explicar na arguição é a **separação de responsabilidades**.

## 1. O que foi implementado (passo a passo)

1. **`pom.xml`**: `spring-boot-starter-web` (Tomcat embutido) e `spring-boot-starter-thymeleaf`; plugin
   `spring-boot-maven-plugin` gera `target/biblioteca.jar` executável; opção `-parameters` do compilador.
2. **`web.AplicacaoBiblioteca`**: `main` do Spring Boot. O Spring só varre o pacote `web`.
3. **`web.ConfiguracaoDoSistema`**: monta o `Sistema` chamando **explicitamente**
   `ConfiguradorDePersistencia.fabricaPara(perfil, arquivo)` (Abstract Factory) e publica como beans só os
   **tratadores** e as ferramentas da demonstração (relógio, e-mail simulado, métricas, bancos simulados). Na primeira
   execução copia `dados/exemplo.json` para `dados/biblioteca.json`.
4. **Controladores** (um por tela): `Inicio`, `Cadastros`, `Circulacao`, `Financeiro`, `Acervo`, `Administracao`;
   `BancosController` (pontos de entrada dos bancos e simulador); `ApiController` (JSON).
5. **`ControladorBase`**: transforma o retorno do tratador ou a `ExcecaoDeDominio` em mensagem (sem regra própria).
6. **Templates Thymeleaf** (`src/main/resources/templates`) e `estilo.css`.
7. **Tratadores ganharam consultas em DTO** para as telas: `listarPessoas/Estudantes/Atendentes`, `listarLivros`,
   `inspecoesPendentes`, `indicadores`, `relatorioDeCobrancas`.
8. **Testes**: `FluxoCompletoWebTest` (MockMvc, perfil TESTE) e `ArquiteturaTest` (critérios de pronto no código).
9. **Perfil `https`** opcional (`application-https.properties`), como no diagrama de implantação.

## 2. Responsabilidades por camada

| Camada | Classes | Pode | Não pode |
|---|---|---|---|
| Web (`web`) | controladores, templates | receber formulários, chamar tratadores, mostrar mensagens | acessar `Acervo`, `Movimentacao`, `Relacionamento`; ter regras |
| Tratadores | `TratadorDe...` | preservar as operações do Astah e delegar | testar a situação do livro |
| Fachada / domínio | `ServicoDeCirculacao`, `Livro` + estados, serviços | regras e transações | conhecer HTTP/Spring |
| Persistência | `persistencia` | gravar a família escolhida | ser escolhida pelo Spring |

## 3. Fluxo de uma requisição, com valores

**Renovar o livro 1 (formulário da Circulação)**
```
Navegador  POST /circulacao/renovar  idSolicitacao=7f3c..., idLivro=1, idEstudante=M-100
CirculacaoController.renovar
  → TratadorDeEmprestimo.renovarEmprestimo(7f3c..., 1, "M-100")          (só tratador)
     → new SolicitacaoDeRenovacao(...) → ExecutorDeSolicitacoes.submeter  (Command)
        → ServicoDeCirculacao.renovar(1, "M-100")                         (Facade, transação)
           → Livro.renovarEmprestimo() → Emprestado.renovarEmprestimo     (State: 13/12 → 12/01)
           → UnidadeDeTrabalhoJson.confirmar() → biblioteca.json (atômico) (Abstract Factory)
     ← ResultadoSolicitacao EXECUTADA "Livro 1 renovado até 2027-01-12 (1ª renovação)"
  → mensagem verde (flash) → redirect /circulacao
Duplo clique: mesmo idSolicitacao → resultado gravado, sem segunda renovação.
```

**Pagamento no banco simulado**
```
POST /simulador/banco-a/pagar/A-0001 → ApiBancoA.liquidar → webhook JSON
  → BancosController.webhookBancoA(json) → AdaptadorBancoA.receberWebhook          (Adapter)
     → ConfirmacaoPagamento{A, A-0001, 7.00} → ServicoDeQuitacao.registrarQuitacao → quitarSeAberto → QUITADO
"Reenviar notificação" → mesmo JSON → quitarSeAberto → false (sem segunda quitação)
```

## 4. Na arguição, saiba explicar
- **Por que os controladores não usam o `Sistema` diretamente?** Para depender só dos tratadores (enunciado):
  `ConfiguracaoDoSistema` publica apenas eles; `ArquiteturaTest` falha se a web citar `Acervo`, `Movimentacao` ou
  `Relacionamento`.
- **O Spring substitui algum padrão?** Não. A família de persistência é escolhida pelo nosso configurador; a
  montagem é o `Sistema`; o Spring só faz HTTP e injeta os tratadores nos controladores.
- **Por que há um controlador de bancos na web?** Ele é a fronteira HTTP com o componente externo Banco (webhook
  JSON e notificação XML) e, na demonstração, o simulador do banco. Toda conversão fica nos adaptadores.
- **Como a tela evita renovar duas vezes?** O formulário leva um `idSolicitacao` gerado ao abrir a página; o
  executor (Command) não repete o mesmo id.

## 5. Exercício de alteração ao vivo
**Pedido:** "mostre na Circulação a situação de um livro digitando o id."
1. Em `CirculacaoController`, um `@PostMapping("/situacao")` que chama `emprestimos.consultarSituacao(idLivro)`
   e monta a mensagem com `situacao()`, `vencimento()` e `numRenovacoes()`.
2. Um formulário de uma linha em `circulacao.html`.
Nenhuma classe do domínio muda: a operação já existe no tratador (`consultarSituacao`).

## 6. Autoavaliação
1. Quem escolhe a família de persistência na aplicação web? 2. O que um controlador pode chamar? 3. Onde a
mensagem de recusa é criada? 4. Como `dados/biblioteca.json` nasce? 5. Como ativar HTTPS?

**Respostas:** 1. `ConfiguradorDePersistencia`, chamado em `ConfiguracaoDoSistema` com `biblioteca.perfil`.
2. Só tratadores (e as ferramentas da demonstração). 3. No domínio (mensagem da `ExcecaoDeDominio`). 4. Cópia de
`dados/exemplo.json` na primeira execução (ou vazio, se não houver exemplo). 5. Gerar `dados/biblioteca.p12` com
`keytool` e rodar com `--spring.profiles.active=https`.
