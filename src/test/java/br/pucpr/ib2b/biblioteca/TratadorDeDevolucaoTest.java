package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** f5, inspeção e reparo pelos tratadores (empréstimo em 13/11, vencimento 13/12). */
class TratadorDeDevolucaoTest {

    private final Cenario c = new Cenario();

    /** Empresta o livro 1 e devolve depois de "dias" dias. */
    private void emprestarEDevolverApos(long dias) {
        c.emprestarLivro1();
        c.relogio.avancarDias(dias);
        c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01");
    }

    @Test
    void devolucaoAtrasadaRegistraDevolucaoComDiasDeAtraso() {
        emprestarEDevolverApos(33);                                   // 16/12: 3 dias após o vencimento

        Devolucao d = c.movimentacao().buscarDevolucao(1, "M-100");
        assertEquals(3, d.calcularDiasAtraso());
        assertNull(c.movimentacao().buscarEmprestimo(1, "M-100"));    // empréstimo ativo removido
        assertEquals(Situacao.DEVOLVIDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void inspecaoEmBomEstadoLiberaOLivro() {
        emprestarEDevolverApos(10);

        assertTrue(c.tratadorDeDevolucao.inspecionar(1, true));
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void livroDanificadoVaiAoReparoEPodeSerIncinerado() {
        emprestarEDevolverApos(10);

        c.tratadorDeDevolucao.inspecionar(1, false);
        assertEquals(Situacao.REPARANDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
        c.tratadorDeDevolucao.incinerar(1);
        assertEquals(Situacao.INCINERADO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void devolucaoSemEmprestimoRegistradoNaoFazNada() {
        assertNull(c.tratadorDeDevolucao.registrarDevolucao(1, "M-100", "F-01"));
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void reparoDeLivroDoAcervoERetorno() {
        c.tratadorDeDevolucao.enviarParaReparo(1);
        assertEquals(Situacao.REPARANDO, c.tratadorDeAcervo.consultarLivro(1).getEstado());
        c.tratadorDeDevolucao.retornarDoReparo(1);
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }
}
