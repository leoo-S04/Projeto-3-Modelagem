package br.pucpr.ib2b.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Boleto de cobrança de uma multa (Astah: BoletoDeMulta), registrado no banco em uso por meio do adaptador.
 * Guarda o banco e o identificador externo (nosso número / txid) para localizar a cobrança na notificação.
 */
public class BoletoDeMulta {

    public static final String EMITIDO = "EMITIDO";
    public static final String QUITADO = "QUITADO";

    private final int idBoleto;
    private final int idMulta;
    private final String cpf;
    private final int diasAtraso;
    private final BigDecimal valorTotal;
    private final LocalDate dataEmissao;
    private final String banco;
    private final String idExterno;
    private String situacao;
    private LocalDate dataPagamento;
    private String idPagamento;

    public BoletoDeMulta(int idBoleto, int idMulta, String cpf, int diasAtraso, BigDecimal valorTotal,
                         LocalDate dataEmissao, String banco, String idExterno) {
        this.idBoleto = idBoleto;
        this.idMulta = idMulta;
        this.cpf = cpf;
        this.diasAtraso = diasAtraso;
        this.valorTotal = valorTotal;
        this.dataEmissao = dataEmissao;
        this.banco = banco;
        this.idExterno = idExterno;
        this.situacao = EMITIDO;
    }

    /** Reconstituição a partir da persistência. */
    public static BoletoDeMulta reconstituir(int idBoleto, int idMulta, String cpf, int diasAtraso, BigDecimal valor,
                                            LocalDate emissao, String banco, String idExterno, String situacao,
                                            LocalDate dataPagamento, String idPagamento) {
        BoletoDeMulta b = new BoletoDeMulta(idBoleto, idMulta, cpf, diasAtraso, valor, emissao, banco, idExterno);
        b.situacao = situacao;
        b.dataPagamento = dataPagamento;
        b.idPagamento = idPagamento;
        return b;
    }

    /**
     * Quitação condicional: só quita se ainda estiver EMITIDO. Devolve false se já estava quitado — é isso que
     * impede uma notificação repetida de gerar uma segunda quitação. Chamado dentro de uma transação exclusiva.
     */
    boolean quitarSeAberto(LocalDate dataPagamento, String idPagamento) {
        if (QUITADO.equals(situacao)) return false;
        this.situacao = QUITADO;
        this.dataPagamento = dataPagamento;
        this.idPagamento = idPagamento;
        return true;
    }

    public int getIdBoleto() { return idBoleto; }
    public int getIdMulta() { return idMulta; }
    public String getCpf() { return cpf; }
    public int getDiasAtraso() { return diasAtraso; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public String getBanco() { return banco; }
    public String getIdExterno() { return idExterno; }
    public String getSituacao() { return situacao; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public String getIdPagamento() { return idPagamento; }
}
