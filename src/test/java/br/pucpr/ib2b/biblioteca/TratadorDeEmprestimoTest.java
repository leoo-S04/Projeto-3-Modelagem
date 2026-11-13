package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Relacionamento e f1–f4 pelo tratador (relógio fixo em 13/11/2026). */
class TratadorDeEmprestimoTest {

    private final Cenario c = new Cenario();

    @Test
    void relacionamentoEncontraEstudanteEAtendentePelasInterfaces() {
        assertEquals("Ana", c.relacionamento().buscarEstudante("M-100").getPessoa().getNome());
        assertEquals("Bruno", c.relacionamento().buscarAtendente("F-01").getPessoa().getNome());
        assertNull(c.relacionamento().buscarEstudante("M-999"));
    }

    @Test
    void f2ConsultarReservaDevolveOComprovanteComADataDeHoje() {
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");

        ComprovanteReserva consultada = c.tratadorDeEmprestimo.consultarReserva(1, "M-100");

        assertEquals(LocalDate.of(2026, 11, 13), consultada.data());
        assertEquals("Dom Casmurro", consultada.titulo());
        assertNull(c.tratadorDeEmprestimo.consultarReserva(1, "M-999"));
    }

    @Test
    void livroOuEstudanteInexistenteERecusadoSemAlterarNada() {
        assertEquals(StatusSolicitacao.RECUSADA, c.tratadorDeEmprestimo.reservarLivro(9, "M-100", "F-01").status());
        assertEquals(StatusSolicitacao.RECUSADA, c.tratadorDeEmprestimo.reservarLivro(1, "M-999", "F-01").status());
        assertEquals(StatusSolicitacao.RECUSADA, c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100").status());
        assertEquals(Situacao.LIVRE, c.tratadorDeEmprestimo.consultarSituacao(1).situacao());
    }

    @Test
    void emprestimoDefineVencimentoEmTrintaDias() {
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        ComprovanteEmprestimo comprovante = c.tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01");
        assertEquals(LocalDate.of(2026, 12, 13), comprovante.vencimento());
    }
}
