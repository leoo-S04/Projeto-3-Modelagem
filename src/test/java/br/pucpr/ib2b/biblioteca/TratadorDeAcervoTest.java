package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.comum.StatusCadastro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 1.1 a 1.4 (manter Livro). */
class TratadorDeAcervoTest {

    private final Cenario c = new Cenario();                // já tem o livro 1 "Dom Casmurro"
    private final iLivro tratador = c.tratadorDeAcervo;

    @Test
    void livroNovoNasceLivreNoAcervo() {
        var r = tratador.inserirLivro(2, "Memórias Póstumas");

        assertEquals(StatusCadastro.NOVO, r.getStatus());
        assertEquals(Situacao.LIVRE, r.getEntidade().getEstado());
        assertSame(r.getEntidade(), tratador.consultarLivro(2));
    }

    @Test
    void inserirLivroJaCadastradoDevolveOExistenteSemDuplicar() {
        var r = tratador.inserirLivro(1, "Outro título");

        assertEquals(StatusCadastro.EXISTENTE, r.getStatus());
        assertEquals("Dom Casmurro", r.getEntidade().getTitulo());
        assertEquals(1, c.acervo().listar().size());
    }

    @Test
    void livroSemTituloERecusado() {
        assertEquals(StatusCadastro.RECUSADO, tratador.inserirLivro(5, "").getStatus());
        assertNull(tratador.consultarLivro(5));
    }

    @Test
    void atualizarEExcluirLivro() {
        assertTrue(tratador.atualizarLivro(1, "Dom Casmurro (2ª ed.)"));
        assertEquals("Dom Casmurro (2ª ed.)", tratador.consultarLivro(1).getTitulo());
        assertTrue(tratador.excluirLivro(1));
        assertNull(tratador.consultarLivro(1));
    }

    @Test
    void operacoesSobreLivroInexistente() {
        assertNull(tratador.consultarLivro(9));
        assertFalse(tratador.atualizarLivro(9, "X"));
        assertFalse(tratador.excluirLivro(9));
    }

    @Test
    void livroEmprestadoNaoPodeSerExcluido() {
        c.emprestarLivro1();

        assertThrows(OperacaoInvalidaException.class, () -> tratador.excluirLivro(1));
        assertNotNull(tratador.consultarLivro(1));
    }

    @Test
    void listarNaoPermiteAlterarOAcervoPorFora() {
        assertThrows(UnsupportedOperationException.class, () -> c.acervo().listar().clear());
        assertEquals(1, c.acervo().listar().size());
    }
}
