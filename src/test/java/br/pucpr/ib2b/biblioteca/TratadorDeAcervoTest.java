package br.pucpr.ib2b.biblioteca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 1.1 a 1.4 (manter Livro). */
class TratadorDeAcervoTest {

    private Acervo acervo;
    private iLivro tratador;

    @BeforeEach
    void preparar() {
        acervo = new Acervo();
        tratador = new TratadorDeAcervo(acervo);
    }

    @Test
    void livroNovoNasceLivreNoAcervo() {
        Livro l = tratador.inserirLivro(1, "Dom Casmurro");

        assertEquals(Situacao.LIVRE, l.getEstado());
        assertSame(l, tratador.consultarLivro(1));
    }

    @Test
    void inserirLivroJaCadastradoDevolveOExistenteSemDuplicar() {
        Livro primeiro = tratador.inserirLivro(1, "Dom Casmurro");

        Livro segundo = tratador.inserirLivro(1, "Outro título");

        assertSame(primeiro, segundo);
        assertEquals("Dom Casmurro", segundo.getTitulo());
        assertEquals(1, acervo.listar().size());
    }

    @Test
    void atualizarEExcluirLivro() {
        tratador.inserirLivro(1, "Dom Casmurro");

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
    void listarNaoPermiteAlterarOAcervoPorFora() {
        tratador.inserirLivro(1, "Dom Casmurro");

        assertThrows(UnsupportedOperationException.class, () -> acervo.listar().clear());
        assertEquals(1, acervo.listar().size());
    }
}
