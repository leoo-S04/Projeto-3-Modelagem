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
    void f2ConsultarReservaDevolveAReservaRegistradaComADataDeHoje() {
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");

        Reserva consultada = c.tratadorDeEmprestimo.consultarReserva(1, "M-100");

        assertEquals(LocalDate.of(2026, 11, 13), consultada.getData());
        assertNull(c.tratadorDeEmprestimo.consultarReserva(1, "M-999"));
    }

    @Test
    void livroOuEstudanteInexistenteDevolveNullSemAlterarNada() {
        assertNull(c.tratadorDeEmprestimo.reservarLivro(9, "M-100", "F-01"));
        assertNull(c.tratadorDeEmprestimo.reservarLivro(1, "M-999", "F-01"));
        assertFalse(c.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100"));
        assertEquals(Situacao.LIVRE, c.tratadorDeAcervo.consultarLivro(1).getEstado());
    }

    @Test
    void emprestimoDefineVencimentoEmTrintaDias() {
        c.emprestarLivro1();
        assertEquals(LocalDate.of(2026, 12, 13), c.tratadorDeAcervo.consultarLivro(1).getVencimento());
    }
}
