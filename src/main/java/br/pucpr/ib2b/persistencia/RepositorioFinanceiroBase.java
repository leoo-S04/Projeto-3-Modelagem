package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import br.pucpr.ib2b.financeiro.ConfiguracaoAdministrativa;
import br.pucpr.ib2b.financeiro.Multa;

import java.util.ArrayList;
import java.util.List;

/** Implementação comum: multas, boletos e configuração na cópia de trabalho. */
abstract class RepositorioFinanceiroBase implements RepositorioFinanceiro {

    private final UnidadeDeTrabalhoBase uow;

    protected RepositorioFinanceiroBase(UnidadeDeTrabalhoBase uow) { this.uow = uow; }

    @Override public int gerarIdMulta() { return uow.proximoIdMulta(); }
    @Override public void salvarMulta(Multa m) { uow.dados().multas.put(m.idMulta(), m); }
    @Override public Multa buscarMulta(int idMulta) { return uow.dados().multas.get(idMulta); }
    @Override public Multa buscarMultaDaDevolucao(int idDevolucao) {
        return uow.dados().multas.values().stream().filter(m -> m.idDevolucao() == idDevolucao).findFirst().orElse(null);
    }

    @Override public int gerarIdBoleto() { return uow.proximoIdBoleto(); }
    @Override public void salvarBoleto(BoletoDeMulta b) { uow.dados().boletos.put(b.getIdBoleto(), b); }
    @Override public BoletoDeMulta buscarBoleto(int idBoleto) { return uow.dados().boletos.get(idBoleto); }
    @Override public BoletoDeMulta buscarBoletoDaMulta(int idMulta) {
        return uow.dados().boletos.values().stream().filter(b -> b.getIdMulta() == idMulta).findFirst().orElse(null);
    }
    @Override public BoletoDeMulta buscarBoletoExterno(String banco, String idExterno) {
        return uow.dados().boletos.values().stream()
                .filter(b -> b.getBanco().equals(banco) && b.getIdExterno().equals(idExterno))
                .findFirst().orElse(null);
    }
    @Override public List<BoletoDeMulta> listarBoletos() { return new ArrayList<>(uow.dados().boletos.values()); }

    @Override public ConfiguracaoAdministrativa configuracao() { return uow.dados().configuracao; }

    @Override public UnidadeDeTrabalho unidade() { return uow; }
}
