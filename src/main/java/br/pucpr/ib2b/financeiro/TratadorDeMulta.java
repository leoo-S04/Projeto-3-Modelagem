package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.persistencia.RepositorioFinanceiro;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Tratador do componente Financeiro (Astah: TratadorDeMulta): realiza iMulta e iQuitacao e oferece as
 * operações administrativas (política vigente, simulação). Delega ao ServicoDeMulta (Strategy + Adapter),
 * ao ServicoDeQuitacao (quitação idempotente) e ao SeletorDePolitica.
 */
public class TratadorDeMulta implements iMulta, iQuitacao {

    private final ServicoDeMulta servicoDeMulta;
    private final ServicoDeQuitacao servicoDeQuitacao;
    private final ServicoDeSimulacao simulacao;
    private final SeletorDePolitica seletor;
    private final RepositorioFinanceiro repositorio;

    public TratadorDeMulta(ServicoDeMulta servicoDeMulta, ServicoDeQuitacao servicoDeQuitacao,
                           ServicoDeSimulacao simulacao, SeletorDePolitica seletor, RepositorioFinanceiro repositorio) {
        this.servicoDeMulta = servicoDeMulta;
        this.servicoDeQuitacao = servicoDeQuitacao;
        this.simulacao = simulacao;
        this.seletor = seletor;
        this.repositorio = repositorio;
    }

    @Override
    public BoletoDeMulta gerarMulta(String cpf, int idDevolucao, int diasAtraso) {
        return servicoDeMulta.gerarMulta(cpf, idDevolucao, diasAtraso);
    }

    @Override
    public BoletoDeMulta consultarMulta(int idBoleto) { return repositorio.buscarBoleto(idBoleto); }

    @Override
    public boolean registrarQuitacao(ConfirmacaoPagamento confirmacao) {
        return servicoDeQuitacao.registrarQuitacao(confirmacao);
    }

    // ---- Administração (configuração administrativa) ----
    public String politicaVigente() { return repositorio.configuracao().getPoliticaVigente(); }
    public void definirPoliticaVigente(String id) { seletor.definirPolitica(id); }
    public List<PoliticaDeMulta> politicasDisponiveis() { return seletor.todas(); }
    public Map<String, BigDecimal> simular(int dias) { return simulacao.simular(dias); }
    public String bancoEmUso() { return repositorio.configuracao().getBancoEmUso(); }

    // ---- Consultas para as telas ----
    public List<BoletoDeMulta> listarBoletos() { return repositorio.listarBoletos(); }
    public Multa consultarMultaDoBoleto(int idBoleto) {
        BoletoDeMulta b = repositorio.buscarBoleto(idBoleto);
        return b == null ? null : repositorio.buscarMulta(b.getIdMulta());
    }
}
