package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.comum.StatusCadastro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 4.1 a 4.4 (manter Atendente) e a restrição de pessoa já cadastrada. */
class TratadorDeAtendenteTest {

    private final Cenario c = new Cenario();               // já tem F-01 (Bruno, CPF 222)
    private final iAtendente tratador = c.atendentes;

    @Test
    void inserirAtendenteDePessoaCadastradaGrava() {
        var r = tratador.inserirAtendente("111", "F-02");

        assertEquals(StatusCadastro.NOVO, r.getStatus());
        assertEquals("111", r.getEntidade().getPessoa().getCpf());
        assertSame(r.getEntidade(), tratador.consultarAtendente("F-02"));
    }

    @Test
    void inserirAtendenteDeCpfNaoCadastradoNaoGravaNada() {
        var r = tratador.inserirAtendente("999", "F-03");

        assertEquals(StatusCadastro.RECUSADO, r.getStatus());
        assertTrue(r.getMotivo().contains("pessoa não cadastrada"));
        assertNull(tratador.consultarAtendente("F-03"));
    }

    @Test
    void inserirAtendenteDuplicadoDevolveOExistente() {
        var r = tratador.inserirAtendente("222", "F-01");

        assertEquals(StatusCadastro.EXISTENTE, r.getStatus());
        assertSame(tratador.consultarAtendente("F-01"), r.getEntidade());
    }

    @Test
    void atualizarAtendenteParaCpfNaoCadastradoDevolveFalse() {
        assertFalse(tratador.atualizarAtendente("F-01", "999"));
        assertEquals("222", tratador.consultarAtendente("F-01").getPessoa().getCpf());
    }

    @Test
    void excluirAtendenteCadastrado() {
        assertTrue(tratador.excluirAtendente("F-01"));
        assertFalse(tratador.excluirAtendente("F-01"));
    }
}
