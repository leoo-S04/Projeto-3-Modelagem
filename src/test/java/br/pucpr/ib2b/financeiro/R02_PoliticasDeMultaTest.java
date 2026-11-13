package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 2 — Strategy: cenários de verificação do enunciado. */
class R02_PoliticasDeMultaTest {

    private static BigDecimal reais(String v) { return new BigDecimal(v); }

    @Test
    void R02_cincoDiasDeAtrasoValemCincoReaisNaOriginalETresNaTolerancia() {
        assertEquals(reais("5.00"), new MultaDiaria().calcular(5));          // 5 × 1,00
        assertEquals(reais("3.00"), new MultaComTolerancia().calcular(5));   // (5 − 2) × 1,00
        assertEquals(reais("5.00"), new MultaComTeto().calcular(5));         // não atinge o teto
    }

    @Test
    void R02_semAtrasoOValorEZero() {
        for (PoliticaDeMulta p : new Cenario().sistema.tratadorDeMulta.politicasDisponiveis()) {
            assertEquals(reais("0.00"), p.calcular(0), p.id());
        }
    }

    @Test
    void tetoLimitaEmVinteReais() {
        assertEquals(reais("20.00"), new MultaComTeto().calcular(25));
    }

    @Test
    void simulacaoUsaOMesmoCalculoSemCriarMulta() {
        Cenario c = new Cenario();
        Map<String, BigDecimal> simulado = c.sistema.tratadorDeMulta.simular(5);

        assertEquals(Map.of("ORIGINAL", reais("5.00"), "TETO", reais("5.00"), "TOLERANCIA", reais("3.00")), simulado);
        assertTrue(c.sistema.tratadorDeMulta.listarBoletos().isEmpty());
    }

    @Test
    void trocaDePoliticaNaoRecalculaMultasJaEmitidas() {
        Cenario c = new Cenario();
        c.tratadorDeAcervo.inserirLivro(2, "Quincas Borba");
        c.emprestarLivro1();
        c.tratadorDeEmprestimo.reservarLivro(2, "M-100", "F-01");
        c.tratadorDeEmprestimo.emprestarLivro(2, "M-100", "F-01");
        c.relogio.avancarDias(35);                                            // 5 dias após o vencimento
        c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01");        // política ORIGINAL → 5,00

        c.sistema.tratadorDeMulta.definirPoliticaVigente("TOLERANCIA");
        c.tratadorDeDevolucao.registrarDevolucao(2, "M-100", "F-01");        // política TOLERANCIA → 3,00

        var boletos = c.sistema.tratadorDeMulta.listarBoletos();
        Multa primeira = c.sistema.tratadorDeMulta.consultarMultaDoBoleto(boletos.get(0).getIdBoleto());
        Multa segunda = c.sistema.tratadorDeMulta.consultarMultaDoBoleto(boletos.get(1).getIdBoleto());
        assertEquals(reais("5.00"), primeira.valor());                        // não foi recalculada
        assertEquals("ORIGINAL", primeira.politicaAplicada());
        assertEquals(reais("3.00"), segunda.valor());
        assertEquals("TOLERANCIA", segunda.politicaAplicada());
    }

    @Test
    void politicaInexistenteERecusadaAoSalvarAConfiguracao() {
        Cenario c = new Cenario();
        assertThrows(PoliticaInexistenteException.class, () -> c.sistema.tratadorDeMulta.definirPoliticaVigente("XYZ"));
        assertEquals("ORIGINAL", c.sistema.tratadorDeMulta.politicaVigente());
    }
}
