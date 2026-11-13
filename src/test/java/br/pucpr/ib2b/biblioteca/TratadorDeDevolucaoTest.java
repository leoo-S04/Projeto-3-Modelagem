package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** f5, inspeção e reparo pelos tratadores (com o relógio fixo do cenário). */
class TratadorDeDevolucaoTest {

    /** Empresta o livro 1 para M-100 em 13/11 (vencimento 13/12) e devolve na data indicada. */
    private Cenario emprestarEDevolverEm(LocalDate dataDevolucao) {
        Cenario emprestimo = new Cenario();
        emprestimo.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        emprestimo.tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01");
        // mesmo acervo e movimentação, mas devolução registrada com outro "hoje"
        TratadorDeDevolucao balcao = new TratadorDeDevolucao(emprestimo.acervo, emprestimo.movimentacao,
                emprestimo.relacionamento,
                java.time.Clock.fixed(dataDevolucao.atStartOfDay(Cenario.FUSO).toInstant(), Cenario.FUSO));
        balcao.registrarDevolucao(1, "M-100", "F-01");
        return emprestimo;
    }

    @Test
    void devolucaoAtrasadaRegistraDevolucaoComDiasDeAtraso() {
        Cenario c = emprestarEDevolverEm(LocalDate.of(2026, 12, 16));

        Devolucao d = c.movimentacao.buscarDevolucao(1, "M-100");
        assertEquals(3, d.calcularDiasAtraso());
        assertNull(c.movimentacao.buscarEmprestimo(1, "M-100"));       // empréstimo ativo removido
        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void inspecaoEmBomEstadoLiberaOLivro() {
        Cenario c = emprestarEDevolverEm(LocalDate.of(2026, 12, 1));

        assertTrue(c.tratadorDeDevolucao.inspecionar(1, true));
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void livroDanificadoVaiAoReparoEPodeSerIncinerado() {
        Cenario c = emprestarEDevolverEm(LocalDate.of(2026, 12, 1));

        c.tratadorDeDevolucao.inspecionar(1, false);
        assertEquals(Situacao.REPARANDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
        c.tratadorDeDevolucao.incinerar(1);
        assertEquals(Situacao.INCINERADO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void devolucaoSemEmprestimoRegistradoNaoFazNada() {
        Cenario c = new Cenario();
        assertNull(c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01"));
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void reparoDeLivroDoAcervoERetorno() {
        Cenario c = new Cenario();
        c.tratadorDeDevolucao.enviarParaReparo(1);
        assertEquals(Situacao.REPARANDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
        c.tratadorDeDevolucao.retornarDoReparo(1);
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }
}
