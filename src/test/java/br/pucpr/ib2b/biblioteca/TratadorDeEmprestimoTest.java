package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.academico.GrupoDeAcademicos;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.GrupoDeColaboradores;
import br.pucpr.ib2b.rh.GrupoDePessoas;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/** Montagem completa dos componentes por interfaces, Relacionamento e f2 (consultar reserva). */
class TratadorDeEmprestimoTest {

    private final Clock relogio = Clock.fixed(
            LocalDate.of(2026, 11, 13).atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(),
            ZoneId.of("America/Sao_Paulo"));

    private Acervo acervo;
    private Movimentacao movimentacao;
    private Relacionamento relacionamento;
    private iEmprestimo tratador;

    @BeforeEach
    void montar() {
        TratadorDePessoa pessoas = new TratadorDePessoa(new GrupoDePessoas());
        TratadorDeEstudante estudantes = new TratadorDeEstudante(new GrupoDeAcademicos(), pessoas);
        TratadorDeAtendente atendentes = new TratadorDeAtendente(new GrupoDeColaboradores(), pessoas);
        pessoas.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        pessoas.inserirPessoa("222", "Bruno", "Alves", "bruno@pucpr.br");
        estudantes.inserirEstudante("111", "M-100");
        atendentes.inserirAtendente("222", "F-01");

        acervo = new Acervo();
        new TratadorDeAcervo(acervo).inserirLivro(1, "Dom Casmurro");
        movimentacao = new Movimentacao();
        relacionamento = new Relacionamento(estudantes, atendentes);
        tratador = new TratadorDeEmprestimo(acervo, movimentacao, relacionamento, relogio);
    }

    @Test
    void relacionamentoEncontraEstudanteEAtendentePelasInterfaces() {
        assertEquals("Ana", relacionamento.buscarEstudante("M-100").getPessoa().getNome());
        assertEquals("Bruno", relacionamento.buscarAtendente("F-01").getPessoa().getNome());
        assertNull(relacionamento.buscarEstudante("M-999"));
    }

    @Test
    void f2ConsultarReservaDevolveAReservaRegistrada() {
        Estudante ana = relacionamento.buscarEstudante("M-100");
        Atendente bruno = relacionamento.buscarAtendente("F-01");
        Reserva r = new Reserva(1, LocalDate.now(relogio), acervo.buscarPorId(1), ana, bruno);
        movimentacao.adicionar(r);

        Reserva consultada = tratador.consultarReserva(1, "M-100");

        assertSame(r, consultada);
        assertEquals(LocalDate.of(2026, 11, 13), consultada.getData());
        assertNull(tratador.consultarReserva(1, "M-999"));
    }
}
