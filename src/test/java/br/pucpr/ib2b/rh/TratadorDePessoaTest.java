package br.pucpr.ib2b.rh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 2.1 a 2.4 (manter Pessoa). */
class TratadorDePessoaTest {

    private iPessoa tratador;

    @BeforeEach
    void preparar() {
        tratador = new TratadorDePessoa(new GrupoDePessoas());
    }

    @Test
    void inserirPessoaNovaGravaNoGrupo() {
        Pessoa p = tratador.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");

        assertEquals("Ana", p.getNome());
        assertSame(p, tratador.consultarPessoa("111"));
    }

    @Test
    void inserirCpfJaCadastradoDevolveAExistenteSemDuplicar() {
        Pessoa primeira = tratador.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");

        Pessoa segunda = tratador.inserirPessoa("111", "Outro", "Nome", "x@x.com");

        assertSame(primeira, segunda);
        assertEquals("Ana", segunda.getNome());          // dados originais preservados
        assertTrue(tratador.excluirPessoa("111"));
        assertNull(tratador.consultarPessoa("111"));      // havia só uma: após excluir, não sobra cópia
    }

    @Test
    void atualizarPessoaCadastradaMudaOsDados() {
        tratador.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");

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
