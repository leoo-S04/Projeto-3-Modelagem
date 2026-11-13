/**
 * Componente <b>Biblioteca</b> (diagrama de componentes do Astah).
 *
 * <p>Registra livros, reservas, empréstimos, renovações e devoluções (Trabalho 02, seção 12).</p>
 * <ul>
 *   <li>Interfaces fornecidas: {@code iLivro} (cadastro de livros), {@code iEmprestimo} (f1 a f4),
 *       {@code iDevolucao} (f5, f6, inspeção e reparo).</li>
 *   <li>Interfaces requeridas: {@code iEstudante}, {@code iAtendente}, {@code iMulta}.</li>
 *   <li>Pontos internos de acesso preservados do Trabalho 02 (acessados por delegação):
 *       Acervo, Relacionamento e Movimentacao.</li>
 *   <li>State (Req. 1): Livro, EstadoLivro e os 7 estados. Facade (Req. 4): iCirculacao, ServicoDeCirculacao.
 *       Observer (Req. 5): PublicadorDeDevolucao e ouvintes. Command (Req. 6): SolicitacaoDeCirculacao,
 *       ExecutorDeSolicitacoes. Decorator (Req. 9): ConsultaAcervo e decoradores. Composite (Req. 10):
 *       ComponenteDoAcervo, ItemDoAcervo, GrupoDoAcervo.</li>
 * </ul>
 * <p>Manifestado pelo artefato {@code biblioteca.jar} (diagrama de implantação).</p>
 */
package br.pucpr.ib2b.biblioteca;
