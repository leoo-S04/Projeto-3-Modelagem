package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.rh.GrupoDePessoas;
import br.pucpr.ib2b.rh.TratadorDePessoa;
import br.pucpr.ib2b.rh.iPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 3.1 a 3.4 (manter Estudante) e a restrição de pessoa já cadastrada. */
class TratadorDeEstudanteTest {

    private iPessoa pessoas;
    private iEstudante tratador;

    @BeforeEach
    void preparar() {
        pessoas = new TratadorDePessoa(new GrupoDePessoas());
        tratador = new TratadorDeEstudante(new GrupoDeAcademicos(), pessoas);
        pessoas.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        pessoas.inserirPessoa("333", "Caio", "Reis", "caio@pucpr.br");
    }

    @Test
    void inserirEstudanteDePessoaCadastradaGrava() {
        Estudante e = tratador.inserirEstudante("111", "M-100");

        assertEquals("Ana", e.getPessoa().getNome());
        assertSame(e, tratador.consultarEstudante("M-100"));
    }

    @Test
    void inserirEstudanteDeCpfNaoCadastradoNaoGravaNada() {
        assertNull(tratador.inserirEstudante("999", "M-200"));
        assertNull(tratador.consultarEstudante("M-200"));
    }

    @Test
    void inserirEstudanteDuplicadoDevolveOExistente() {
        Estudante primeiro = tratador.inserirEstudante("111", "M-100");

        assertSame(primeiro, tratador.inserirEstudante("111", "M-100"));
    }

    @Test
    void atualizarEstudanteLigaAOutraPessoaCadastrada() {
        tratador.inserirEstudante("111", "M-100");

        assertTrue(tratador.atualizarEstudante("M-100", "333"));
        assertEquals("Caio", tratador.consultarEstudante("M-100").getPessoa().getNome());
        assertFalse(tratador.atualizarEstudante("M-100", "999"));
        assertFalse(tratador.atualizarEstudante("M-999", "111"));
    }

    @Test
    void excluirEstudanteCadastrado() {
        tratador.inserirEstudante("111", "M-100");

        assertTrue(tratador.excluirEstudante("M-100"));
        assertNull(tratador.consultarEstudante("M-100"));
    }
}
