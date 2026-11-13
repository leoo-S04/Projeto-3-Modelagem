package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoA;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoB;
import br.pucpr.ib2b.persistencia.RepositorioFinanceiro;
import br.pucpr.ib2b.persistencia.RepositorioFinanceiroEmMemoria;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalhoEmMemoria;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 3 — Adapter: cenários de verificação do enunciado. */
class R03_BancosAdapterTest {

    private final Cenario c = new Cenario();
    private final RepositorioFinanceiro repositorio = new RepositorioFinanceiroEmMemoria(new UnidadeDeTrabalhoEmMemoria());
    private final ServicoDeQuitacao quitacao = new ServicoDeQuitacao(repositorio);
    private final SeletorDePolitica seletor = new SeletorDePolitica(repositorio, List.of(new MultaDiaria()));

    /** O mesmo serviço de multas, montado com o adaptador do banco indicado. */
    private ServicoDeMulta servicoCom(iCobranca banco) {
        return new ServicoDeMulta(seletor, banco, repositorio, c.pessoas, c.relogio);
    }

    @Test
    void R03_mesmaSolicitacaoDeSeteReaisViraSetecentosCentavosNoBancoAESeteNoBancoB() {
        ApiBancoA apiA = new ApiBancoA();
        ApiBancoB apiB = new ApiBancoB();
        Multa multa = new Multa(1, "12345678909", 10, 7, new BigDecimal("7.00"), "ORIGINAL", Cenario.HOJE);

        BoletoDeMulta noBancoA = servicoCom(new AdaptadorBancoA(apiA, quitacao)).cobrar(multa);
        BoletoDeMulta noBancoB = servicoCom(new AdaptadorBancoB(apiB, quitacao)).cobrar(multa);

        assertEquals(700L, apiA.ultimoValorCentavos());                       // banco A: centavos
        assertEquals(new BigDecimal("7.00"), apiB.ultimoValor());             // banco B: decimal
        assertEquals("123.456.789-09", apiB.ultimoDocumento());               // banco B: documento formatado
        assertEquals("A", noBancoA.getBanco());
        assertTrue(noBancoA.getIdExterno().startsWith("A-"));                 // nosso número
        assertEquals("B", noBancoB.getBanco());
        assertTrue(noBancoB.getIdExterno().startsWith("TXB"));                // txid
        // sem condicionais por banco: o serviço de multas só conhece o contrato iCobranca
        for (Field f : ServicoDeMulta.class.getDeclaredFields()) {
            assertFalse(f.getType().getSimpleName().contains("BancoA") || f.getType().getSimpleName().contains("BancoB"),
                    "ServicoDeMulta não pode depender de um banco específico: " + f);
        }
    }

    @Test
    void R03_notificacaoRepetidaNaoGeraSegundaQuitacao() {
        ApiBancoA api = new ApiBancoA();
        AdaptadorBancoA adaptador = new AdaptadorBancoA(api, quitacao);
        BoletoDeMulta boleto = servicoCom(adaptador)
                .cobrar(new Multa(1, "111", 10, 7, new BigDecimal("7.00"), "ORIGINAL", Cenario.HOJE));
        String webhook = api.liquidar(boleto.getIdExterno(), LocalDate.of(2026, 12, 20));

        assertEquals("OK", adaptador.receberWebhook(webhook));               // 1ª notificação: quita
        assertEquals("OK", adaptador.receberWebhook(webhook));               // reenvio: responde OK...
        ConfirmacaoPagamento repetida = new ConfirmacaoPagamento("A", boleto.getIdExterno(), "PGA-x",
                new BigDecimal("7.00"), LocalDate.of(2026, 12, 20));
        assertFalse(quitacao.registrarQuitacao(repetida));                    // ...mas não quita de novo

        BoletoDeMulta quitado = repositorio.buscarBoleto(boleto.getIdBoleto());
        assertEquals(BoletoDeMulta.QUITADO, quitado.getSituacao());
        assertEquals("PGA-" + boleto.getIdExterno(), quitado.getIdPagamento()); // pagamento da 1ª notificação
        assertTrue(adaptador.consultarPagamento(boleto.getIdExterno()));
    }

    @Test
    void notificacaoDoBancoBEmXmlTambemETraduzida() {
        ApiBancoB api = new ApiBancoB();
        AdaptadorBancoB adaptador = new AdaptadorBancoB(api, quitacao);
        BoletoDeMulta boleto = servicoCom(adaptador)
                .cobrar(new Multa(1, "111", 10, 7, new BigDecimal("7.00"), "ORIGINAL", Cenario.HOJE));

        adaptador.receberNotificacao(api.pagar(boleto.getIdExterno(), LocalDate.of(2026, 12, 21)));

        assertEquals(BoletoDeMulta.QUITADO, repositorio.buscarBoleto(boleto.getIdBoleto()).getSituacao());
    }

    @Test
    void valorComMaisDeDuasCasasERecusadoAntesDeChamarOBanco() {
        ApiBancoA api = new ApiBancoA();
        assertThrows(ValorInvalidoException.class,
                () -> new AdaptadorBancoA(api, quitacao).registrarBoleto(1, "111", new BigDecimal("7.005"), null));
        assertEquals(-1L, api.ultimoValorCentavos());                         // o banco não foi chamado
    }

    @Test
    void notificacaoDeCobrancaDesconhecidaVaiParaConciliacaoSemQuitar() {
        assertFalse(quitacao.registrarQuitacao(new ConfirmacaoPagamento("A", "A-9999", "P", BigDecimal.ONE, Cenario.HOJE)));
        assertEquals(1, quitacao.pendentesDeConciliacao().size());
    }
}
