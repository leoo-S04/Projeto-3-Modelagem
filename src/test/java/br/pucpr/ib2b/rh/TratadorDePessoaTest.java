package br.pucpr.ib2b.rh;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.comum.StatusCadastro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 2.1 a 2.4 (manter Pessoa). */
class TratadorDePessoaTest {

    private final iPessoa tratador = new Cenario().pessoas;   // já tem 111 (Ana) e 222 (Bruno)

    @Test
    void inserirPessoaNovaGravaNoGrupo() {
        var r = tratador.inserirPessoa("444", "Davi", "Melo", "davi@pucpr.br");

        assertEquals(StatusCadastro.NOVO, r.getStatus());
        assertSame(r.getEntidade(), tratador.consultarPessoa("444"));
    }

    @Test
    void inserirCpfJaCadastradoDevolveAExistenteSemDuplicar() {
        var r = tratador.inserirPessoa("111", "Outro", "Nome", "x@x.com");

        assertEquals(StatusCadastro.EXISTENTE, r.getStatus());
        assertEquals("Ana", r.getEntidade().getNome());         // dados originais preservados
        assertTrue(tratador.excluirPessoa("111"));
        assertNull(tratador.consultarPessoa("111"));            // havia só uma
    }

    @Test
    void cpfInvalidoERecusado() {
        var r = tratador.inserirPessoa("12a", "X", "Y", "z@z.com");
        assertEquals(StatusCadastro.RECUSADO, r.getStatus());
        assertNull(tratador.consultarPessoa("12a"));
    }

    @Test
    void atualizarPessoaCadastradaMudaOsDados() {
        assertTrue(tratador.atualizarPessoa("111", "Ana", "Lima", "ana.lima@pucpr.br"));
        assertEquals("Lima", tratador.consultarPessoa("111").getSobrenome());
    }

    @Test
    void atualizarOuExcluirPessoaInexistenteDevolveFalse() {
        assertFalse(tratador.atualizarPessoa("999", "X", "Y", "z@z.com"));
        assertFalse(tratador.excluirPessoa("999"));
        assertNull(tratador.consultarPessoa("999"));
    }
}
