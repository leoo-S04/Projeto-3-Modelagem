/**
 * Componente <b>Interface Web</b> (diagrama de componentes do Astah): Spring Boot (Tomcat embutido) + Thymeleaf.
 *
 * <ul>
 *   <li>Interfaces requeridas: {@code iLivro}, {@code iPessoa}, {@code iEstudante}, {@code iAtendente},
 *       {@code iEmprestimo}, {@code iDevolucao} — os controladores chamam apenas os tratadores.</li>
 *   <li>Regra: não contém regras do domínio e nunca referencia Movimentacao, Acervo, Relacionamento nem coleções
 *       do domínio (verificado por ArquiteturaTest).</li>
 *   <li>A família de persistência é escolhida explicitamente pelo ConfiguradorDePersistencia
 *       (em ConfiguracaoDoSistema), não pelo Spring.</li>
 *   <li>Uma área por ator do diagrama de casos de uso, depois do login: estudante ({@code /estudante}),
 *       atendente ({@code /atendente}) e Administrador ({@code /admin}); ver AcessoPorPerfil.</li>
 *   <li>BancosController é a fronteira HTTP com o componente «external» Banco (notificações de pagamento).</li>
 * </ul>
 * <p>As páginas são exibidas no navegador do Computador do Usuário (HTTP local; HTTPS com o perfil {@code https}).</p>
 */
package br.pucpr.ib2b.web;
