package br.pucpr.ib2b.financeiro;

/**
 * Configuração administrativa da instalação (gravada na seção "configuracao" do JSON):
 * política de multa vigente e banco usado para cobrança.
 */
public class ConfiguracaoAdministrativa {

    private String politicaVigente;
    private String bancoEmUso;

    public ConfiguracaoAdministrativa(String politicaVigente, String bancoEmUso) {
        this.politicaVigente = politicaVigente;
        this.bancoEmUso = bancoEmUso;
    }

    /** Padrão: política original (R$ 1,00/dia) e banco A. */
    public static ConfiguracaoAdministrativa padrao() { return new ConfiguracaoAdministrativa("ORIGINAL", "A"); }

    public String getPoliticaVigente() { return politicaVigente; }
    public String getBancoEmUso() { return bancoEmUso; }

    /** A validação do id (política existente) é feita pelo SeletorDePolitica antes de chamar este método. */
    void definirPolitica(String id) { this.politicaVigente = id; }
    void definirBanco(String codigo) { this.bancoEmUso = codigo; }
}
