package br.pucpr.ib2b.rh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 4.1 a 4.4 (manter Atendente) e a restrição de pessoa já cadastrada. */
class TratadorDeAtendenteTest {

    private iPessoa pessoas;
    private iAtendente tratador;

    @BeforeEach
    void preparar() {
        pessoas = new TratadorDePessoa(new GrupoDePessoas());
        tratador = new TratadorDeAtendente(new GrupoDeColaboradores(), pessoas);
        pessoas.inserirPessoa("222", "Bruno", "Alves", "bruno@pucpr.br");
    }

    @Test
    void inserirAtendenteDePessoaCadastradaGrava() {
        Atendente a = tratador.inserirAtendente("222", "F-01");

        assertEquals("222", a.getPessoa().getCpf());
        assertSame(a, tratador.consultarAtendente("F-01"));
    }

    @Test
    void inserirAtendenteDeCpfNaoCadastradoNaoGravaNada() {
        assertNull(tratador.inserirAtendente("999", "F-02"));
        assertNull(tratador.consultarAtendente("F-02"));
    }

    @Test
    void inserirAtendenteDuplicadoDevolveOExistente() {
        Atendente primeiro = tratador.inserirAtendente("222", "F-01");

        assertSame(primeiro, tratador.inserirAtendente("222", "F-01"));
    }

    @Test
    void atualizarAtendenteParaCpfNaoCadastradoDevolveFalse() {
        tratador.inserirAtendente("222", "F-01");

        assertFalse(tratador.atualizarAtendente("F-01", "999"));
        assertEquals("222", tratador.consultarAtendente("F-01").getPessoa().getCpf());
    }

    @Test
    void excluirAtendenteCadastrado() {
        tratador.inserirAtendente("222", "F-01");

        assertTrue(tratador.excluirAtendente("F-01"));
        assertFalse(tratador.excluirAtendente("F-01"));
    }
}
