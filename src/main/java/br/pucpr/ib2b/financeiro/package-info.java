/**
 * Componente <b>Financeiro</b> (diagrama de componentes do Astah).
 *
 * <p>Registra multas e boletos (Trabalho 02, seção 12).</p>
 * <ul>
 *   <li>Interfaces fornecidas: {@code iMulta}, {@code iQuitacao}.</li>
 *   <li>Interfaces requeridas: {@code iPessoa} (consulta de CPF/e-mail), {@code iCobranca} (fornecida pelo Banco).</li>
 *   <li>Strategy (Req. 2): PoliticaDeMulta, MultaDiaria, MultaComTeto, MultaComTolerancia, SeletorDePolitica,
 *       ConfiguracaoAdministrativa, ServicoDeMulta, ServicoDeSimulacao, Multa.</li>
 *   <li>Adapter (Req. 3): iCobranca, AdaptadorBancoA, AdaptadorBancoB, ServicoDeQuitacao, ConfirmacaoPagamento.</li>
 *   <li>Tratador: TratadorDeMulta; boleto: BoletoDeMulta.</li>
 * </ul>
 * <p>O componente «external» Banco (bancos A e B) é simulado pelo subpacote {@code bancos}, dentro de
 * {@code biblioteca.jar}, como o Req. 3 permite. Manifestado pelo artefato {@code biblioteca.jar}.</p>
 */
package br.pucpr.ib2b.financeiro;
