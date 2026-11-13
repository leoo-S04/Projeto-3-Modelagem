package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import br.pucpr.ib2b.financeiro.ConfiguracaoAdministrativa;
import br.pucpr.ib2b.financeiro.Multa;

import java.util.List;

/** Papel GoF: AbstractProduct — multas, boletos (seção "cobrancas") e configuração administrativa. */
public interface RepositorioFinanceiro {
    int gerarIdMulta();
    void salvarMulta(Multa m);
    Multa buscarMulta(int idMulta);
    Multa buscarMultaDaDevolucao(int idDevolucao);

    int gerarIdBoleto();
    void salvarBoleto(BoletoDeMulta b);
    BoletoDeMulta buscarBoleto(int idBoleto);
    BoletoDeMulta buscarBoletoDaMulta(int idMulta);
    BoletoDeMulta buscarBoletoExterno(String banco, String idExterno);
    List<BoletoDeMulta> listarBoletos();

    ConfiguracaoAdministrativa configuracao();

    UnidadeDeTrabalho unidade();
}
