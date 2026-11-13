package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.comum.StatusCadastro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras 3.1 a 3.4 (manter Estudante). Os cenários do Req. 7 estão em R07_CadastroTemplateMethodTest. */
class TratadorDeEstudanteTest {

    private final Cenario c = new Cenario();               // já tem M-100 (Ana, CPF 111)
    private final iEstudante tratador = c.estudantes;

    @Test
    void inserirEstudanteDePessoaCadastradaGrava() {
        c.pessoas.inserirPessoa("333", "Caio", "Reis", "caio@pucpr.br");

        var r = tratador.inserirEstudante("333", "M-300");

        assertEquals(StatusCadastro.NOVO, r.getStatus());
        assertEquals("Caio", r.getEntidade().getPessoa().getNome());
        assertSame(r.getEntidade(), tratador.consultarEstudante("M-300"));
    }

    @Test
    void matriculaEmBrancoERecusada() {
        assertEquals(StatusCadastro.RECUSADO, tratador.inserirEstudante("111", " ").getStatus());
    }

    @Test
    void atualizarEstudanteLigaAOutraPessoaCadastrada() {
        assertTrue(tratador.atualizarEstudante("M-100", "222"));
        assertEquals("Bruno", tratador.consultarEstudante("M-100").getPessoa().getNome());
        assertFalse(tratador.atualizarEstudante("M-100", "999"));
        assertFalse(tratador.atualizarEstudante("M-999", "111"));
    }

    @Test
    void excluirEstudanteCadastrado() {
        assertTrue(tratador.excluirEstudante("M-100"));
        assertNull(tratador.consultarEstudante("M-100"));
    }

    @Test
    void cadaPapelRecebeUmIdDiferente() {
        c.pessoas.inserirPessoa("333", "Caio", "Reis", "caio@pucpr.br");
        int id = tratador.inserirEstudante("333", "M-300").getEntidade().getId();
        assertNotEquals(tratador.consultarEstudante("M-100").getId(), id);
    }
}
