package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.comum.ExcecaoDeDominio;

/**
 * Papel GoF: Invoker (Command, Req. 6). Recebe a solicitação pronta e dispara a execução, sem conhecer as
 * regras. Garante que o mesmo id não execute duas vezes e guarda o resultado para as repetições.
 */
public class ExecutorDeSolicitacoes {

    private final RegistroDeSolicitacoes registro;

    public ExecutorDeSolicitacoes(RegistroDeSolicitacoes registro) { this.registro = registro; }

    public ResultadoSolicitacao submeter(SolicitacaoDeCirculacao s) {
        if (!registro.reservarExecucao(s.getId())) {                     // repetição: não executa de novo
            return registro.resultadoDe(s.getId()).orElse(ResultadoSolicitacao.emProcessamento(s));
        }
        ResultadoSolicitacao resultado;
        try {
            resultado = s.executar();                                    // regras ficam no receptor/Livro
        } catch (ExcecaoDeDominio recusa) {                              // ex.: 3ª renovação
            resultado = ResultadoSolicitacao.recusada(s, recusa.getMessage());
        } catch (RuntimeException falhaTecnica) {
            registro.liberar(s.getId());                                 // permite nova tentativa
            throw falhaTecnica;
        }
        registro.concluir(resultado);                                    // reaproveitado nas repetições
        return resultado;
    }
}
