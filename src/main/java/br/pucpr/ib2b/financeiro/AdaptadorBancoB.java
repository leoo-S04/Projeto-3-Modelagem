package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.financeiro.bancos.ApiBancoB;
import br.pucpr.ib2b.financeiro.bancos.CobrancaB;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Papel GoF: Adapter (de objeto) — Req. 3. Converte iCobranca para a API do banco B (saída) e traduz a
 * notificação XML do banco B em ConfirmacaoPagamento (entrada). Conhecimento do banco B: valor decimal,
 * documento formatado, txid, situação em texto ("CONCLUIDA" = pago) e formato XML.
 */
public class AdaptadorBancoB implements iCobranca {

    private final ApiBancoB api;               // Adaptee
    private final iQuitacao receptor;

    public AdaptadorBancoB(ApiBancoB api, iQuitacao receptor) {
        this.api = api;
        this.receptor = receptor;
    }

    @Override
    public String registrarBoleto(int idBoleto, String cpf, BigDecimal valor, String email) {
        CobrancaB cobranca = api.abrirCobranca(paraDocumento(cpf), valor.setScale(2, RoundingMode.UNNECESSARY),
                "MULTA-" + idBoleto);
        return cobranca.txid();
    }

    @Override
    public boolean consultarPagamento(String txid) { return traduzir(api.statusCobranca(txid)); }

    @Override public String codigoDoBanco() { return "B"; }

    /** Entrada: notificação XML do banco B → ConfirmacaoPagamento → iQuitacao. */
    public String receberNotificacao(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            if (!traduzir(texto(doc, "situacao"))) return "IGNORADO";
            ConfirmacaoPagamento c = new ConfirmacaoPagamento("B", texto(doc, "txid"), texto(doc, "e2eid"),
                    new BigDecimal(texto(doc, "valor")), LocalDate.parse(texto(doc, "pagoEm")));
            receptor.registrarQuitacao(c);
            return "OK";
        } catch (Exception e) {
            throw new IllegalArgumentException("Notificação do banco B inválida", e);
        }
    }

    /** 12345678909 → 123.456.789-09 (o banco B exige o documento formatado). */
    private String paraDocumento(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private boolean traduzir(String situacao) { return "CONCLUIDA".equals(situacao); }

    private static String texto(Document doc, String tag) {
        return doc.getElementsByTagName(tag).item(0).getTextContent().trim();
    }
}
