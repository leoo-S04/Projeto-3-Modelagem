package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.financeiro.BoletoDeMulta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 5 — Observer: cenários de verificação. Empréstimo em 13/11/2026 → vencimento 13/12/2026. */
class R05_ReacoesADevolucaoTest {

    private final Cenario c = new Cenario();

    /** Empresta o livro 1 e devolve depois de "dias" dias. */
    private ComprovanteDevolucao devolverApos(int dias) {
        c.emprestarLivro1();
        c.relogio.avancarDias(dias);
        return c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01");
    }

    @Test
    void R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores() {
        ComprovanteDevolucao d = devolverApos(33);                          // 16/12: 3 dias de atraso
        assertEquals(3, d.diasAtraso());

        assertTrue(c.sistema.agendaDeInspecao().estaAgendado(1));            // inspeção agendada
        List<BoletoDeMulta> boletos = c.sistema.tratadorDeMulta.listarBoletos();
        assertEquals(1, boletos.size());                                     // multa solicitada ao Financeiro
        assertEquals(new BigDecimal("3.00"), boletos.get(0).getValorTotal());
        assertEquals(1, c.sistema.painelDeIndicadores().devolucoesAtrasadas());   // indicadores
        assertEquals(3, c.sistema.painelDeIndicadores().totalDeDiasDeAtraso());
        assertEquals(1, c.sistema.email().enviadas().size());                // e-mail ao estudante
    }

    @Test
    void R05_devolucaoNoPrazoNaoGeraMulta() {
        devolverApos(10);

        assertTrue(c.sistema.tratadorDeMulta.listarBoletos().isEmpty());
        assertEquals(1, c.sistema.painelDeIndicadores().totalDeDevolucoes());
        assertEquals(0, c.sistema.painelDeIndicadores().devolucoesAtrasadas());
        assertNull(c.tratadorDeDevolucao.emitirBoletoDeMulta(1, "M-100"));   // f6: sem atraso, sem boleto
    }

    @Test
    void R05_livroPermaneceDevolvidoAteAInspecao() {
        devolverApos(33);
        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeEmprestimo.consultarSituacao(1).situacao());

        c.tratadorDeDevolucao.inspecionar(1, true);

        assertEquals(Situacao.LIVRE, c.tratadorDeEmprestimo.consultarSituacao(1).situacao());
        assertFalse(c.sistema.agendaDeInspecao().estaAgendado(1));
    }

    @Test
    void falhaDoEmailNaoAfetaOsDemaisOuvintesNemDesfazADevolucao() {
        c.sistema.email().simularFalha(true);

        devolverApos(33);

        assertNotNull(c.movimentacao().buscarDevolucao(1, "M-100"));         // devolução confirmada
        assertEquals(1, c.sistema.tratadorDeMulta.listarBoletos().size());   // multa gerada
        assertTrue(c.sistema.agendaDeInspecao().estaAgendado(1));
        assertEquals(1, c.sistema.publicador().falhasPendentes().size());    // falha registrada para reenvio

        c.sistema.email().simularFalha(false);
        c.sistema.publicador().reenviarFalhas();
        assertEquals(1, c.sistema.email().enviadas().size());
        assertTrue(c.sistema.publicador().falhasPendentes().isEmpty());
    }

    @Test
    void f6EmiteSegundaViaDoMesmoBoleto() {
        devolverApos(33);
        BoletoDeMulta boletoDoOuvinte = c.sistema.tratadorDeMulta.listarBoletos().get(0);

        BoletoDeMulta segundaVia = c.tratadorDeDevolucao.emitirBoletoDeMulta(1, "M-100");

        assertEquals(boletoDoOuvinte.getIdBoleto(), segundaVia.getIdBoleto());
        assertEquals(1, c.sistema.tratadorDeMulta.listarBoletos().size());   // nenhum boleto novo
    }

    @Test
    void novoOuvinteEIncluidoSemAlterarOTratador() {
        List<EventoDevolucao> recebidos = new ArrayList<>();
        c.sistema.publicador().registrar(recebidos::add);                    // nova reação, só na montagem

        devolverApos(33);

        assertEquals(1, recebidos.size());
        assertEquals(3, recebidos.get(0).diasAtraso());
    }
}
