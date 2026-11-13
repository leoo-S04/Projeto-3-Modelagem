package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.persistencia.RepositorioFinanceiro;
import br.pucpr.ib2b.rh.Pessoa;
import br.pucpr.ib2b.rh.iPessoa;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Papel GoF: Context do Strategy (Req. 2) e Client do Adapter (Req. 3).
 * Calcula a multa com a política VIGENTE sem saber qual é (nenhum if por política) e cobra pelo contrato
 * institucional iCobranca sem saber qual banco atende (nenhum if por banco).
 */
public class ServicoDeMulta {

    private final SeletorDePolitica seletor;
    private final iCobranca cobranca;            // adaptador do banco escolhido na montagem
    private final RepositorioFinanceiro repositorio;
    private final iPessoa pessoas;               // Financeiro requer iPessoa (consulta de CPF / e-mail)
    private final Clock relogio;

    public ServicoDeMulta(SeletorDePolitica seletor, iCobranca cobranca, RepositorioFinanceiro repositorio,
                          iPessoa pessoas, Clock relogio) {
        this.seletor = seletor;
        this.cobranca = cobranca;
        this.repositorio = repositorio;
        this.pessoas = pessoas;
        this.relogio = relogio;
    }

    /**
     * Gera a multa de uma devolução atrasada e emite o boleto. Idempotente por devolução: se a multa dessa
     * devolução já existe, devolve o boleto existente (segunda via) sem calcular nem cobrar de novo.
     * @return o boleto, ou {@code null} se o valor calculado for zero (sem multa)
     */
    public BoletoDeMulta gerarMulta(String cpf, int idDevolucao, int diasAtraso) {
        return repositorio.unidade().executar(() -> {
            Multa existente = repositorio.buscarMultaDaDevolucao(idDevolucao);
            if (existente != null) return repositorio.buscarBoletoDaMulta(existente.idMulta());

            PoliticaDeMulta politica = seletor.vigente();                 // Strategy escolhida pela configuração
            BigDecimal valor = politica.calcular(diasAtraso);
            if (valor.signum() == 0) return null;                         // ex.: atraso dentro da tolerância

            Multa multa = new Multa(repositorio.gerarIdMulta(), cpf, idDevolucao, diasAtraso, valor,
                    politica.id(), hoje());                               // grava valor e política aplicada
            repositorio.salvarMulta(multa);
            return cobrar(multa);
        });
    }

    /** Emite o boleto pelo adaptador configurado (A ou B — o serviço não sabe qual). */
    public BoletoDeMulta cobrar(Multa multa) {
        int idBoleto = repositorio.gerarIdBoleto();
        Pessoa pessoa = pessoas.consultarPessoa(multa.cpf());
        String idExterno = cobranca.registrarBoleto(idBoleto, multa.cpf(), multa.valor(),
                pessoa == null ? null : pessoa.getEmail());
        BoletoDeMulta boleto = new BoletoDeMulta(idBoleto, multa.idMulta(), multa.cpf(), multa.diasAtraso(),
                multa.valor(), hoje(), cobranca.codigoDoBanco(), idExterno);
        repositorio.salvarBoleto(boleto);
        return boleto;
    }

    private LocalDate hoje() { return LocalDate.now(relogio); }
}
