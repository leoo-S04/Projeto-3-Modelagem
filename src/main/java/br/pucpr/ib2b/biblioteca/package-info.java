/**
 * Componente <b>Biblioteca</b> (diagrama de componentes do Astah).
 *
 * <p>Registra livros, reservas, empréstimos, renovações e devoluções (Trabalho 02, seção 12).</p>
 * <ul>
 *   <li>Interfaces fornecidas: {@code iLivro} (cadastro de livros), {@code iEmprestimo} (f1 a f4),
 *       {@code iDevolucao} (f5, f6 e inspeção).</li>
 *   <li>Interfaces requeridas: {@code iEstudante}, {@code iAtendente}, {@code iMulta}.</li>
 *   <li>Classes: TratadorDeAcervo, Acervo, Livro, Situacao, TratadorDeEmprestimo, TratadorDeDevolucao,
 *       Movimentacao, Relacionamento, Transacao, Reserva, Emprestimo, Devolucao.</li>
 *   <li>Pontos internos de acesso preservados do Trabalho 02 (acessados por delegação):
 *       Acervo, Relacionamento e Movimentacao.</li>
 * </ul>
 * <p>Manifestado pelo artefato {@code biblioteca.jar} (diagrama de implantação).</p>
 */
package br.pucpr.ib2b.biblioteca;
