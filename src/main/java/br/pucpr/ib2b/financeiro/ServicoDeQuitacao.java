package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.persistencia.RepositorioFinanceiro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recebe as confirmações de pagamento (já traduzidas pelos adaptadores) e quita o boleto UMA vez.
 * A garantia contra notificação repetida é daqui, não do adaptador: localiza o boleto por (banco, idExterno) e
 * chama quitarSeAberto dentro de uma transação exclusiva (UnidadeDeTrabalho.executar é synchronized).
 */
public class ServicoDeQuitacao implements iQuitacao {

    private final RepositorioFinanceiro repositorio;
    /** Notificações de cobranças desconhecidas: guardadas para conciliação manual, sem quitar nada. */
    private final List<ConfirmacaoPagamento> conciliacao = Collections.synchronizedList(new ArrayList<>());

    public ServicoDeQuitacao(RepositorioFinanceiro repositorio) { this.repositorio = repositorio; }

    @Override
    public boolean registrarQuitacao(ConfirmacaoPagamento c) {
        return repositorio.unidade().executar(() -> {
            BoletoDeMulta boleto = repositorio.buscarBoletoExterno(c.banco(), c.idExterno());
            if (boleto == null) {
                conciliacao.add(c);
                return false;
            }
            return boleto.quitarSeAberto(c.dataPagamento(), c.idPagamento());   // false se já quitado
        });
    }

    public List<ConfirmacaoPagamento> pendentesDeConciliacao() { return List.copyOf(conciliacao); }
}
