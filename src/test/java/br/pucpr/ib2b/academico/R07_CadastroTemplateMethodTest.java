package br.pucpr.ib2b.academico;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.comum.StatusCadastro;
import br.pucpr.ib2b.persistencia.FabricaEmMemoria;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 7 — Template Method: cenários de verificação do enunciado. */
class R07_CadastroTemplateMethodTest {

    /** Unidade de trabalho espiã: conta quantas vezes houve gravação (executar = gravar e confirmar). */
    static class UnidadeEspia implements UnidadeDeTrabalho {
        final UnidadeDeTrabalho real = new FabricaEmMemoria().criarUnidadeDeTrabalho();
        int gravacoes = 0;
        @Override public void confirmar() { real.confirmar(); }
        @Override public void desfazer() { real.desfazer(); }
        @Override public <T> T executar(Supplier<T> op) { gravacoes++; return real.executar(op); }
    }

    private final Cenario c = new Cenario();   // pessoa 111 (Ana) e estudante M-100 já cadastrados

    @Test
    void R07_estudanteDuplicadoRetornaOExistente() {
        var original = c.estudantes.consultarEstudante("M-100");

        var resultado = c.estudantes.inserirEstudante("111", "M-100");

        assertEquals(StatusCadastro.EXISTENTE, resultado.getStatus());
        assertSame(original, resultado.getEntidade());
    }

    @Test
    void R07_estudanteComCpfNaoCadastradoFalhaAntesDeQualquerGravacao() {
        UnidadeEspia espia = new UnidadeEspia();
        var repositorio = new FabricaEmMemoria().criarRepositorioRelacionamento(espia.real);
        TratadorDeEstudante tratador = new TratadorDeEstudante(new GrupoDeAcademicos(repositorio), c.pessoas, espia);

        var resultado = tratador.inserirEstudante("999", "M-999");     // CPF 999 não está no RH

        assertEquals(StatusCadastro.RECUSADO, resultado.getStatus());
        assertTrue(resultado.getMotivo().contains("pessoa não cadastrada"));
        assertEquals(0, espia.gravacoes);                              // armazenar nunca foi chamado
        assertNull(repositorio.buscarEstudante("M-999"));
    }

    @Test
    void metodoModeloNaoPodeSerSobrescrito() throws NoSuchMethodException {
        var metodo = br.pucpr.ib2b.comum.TratadorDeCadastro.class.getMethod("inserir", Object.class);
        assertTrue(java.lang.reflect.Modifier.isFinal(metodo.getModifiers()));
    }
}
