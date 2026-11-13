package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.rh.Pessoa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Padrão State: cada transição da máquina de estados do Livro e as recusas sem alteração. */
class EstadoLivroTest {

    private final Estudante ana = new Estudante(1, "M-100", new Pessoa("111", "Ana", "Souza", "ana@pucpr.br"));
    private final Estudante caio = new Estudante(2, "M-300", new Pessoa("333", "Caio", "Reis", "caio@pucpr.br"));
    private final LocalDate hoje = LocalDate.of(2026, 11, 13);

    private Livro emprestadoParaAna() {
        Livro l = new Livro(1, "Dom Casmurro");
        l.reservarLivro(ana);
        l.emprestarLivro(ana, hoje);
        return l;
    }

    private Livro devolvido() {
        Livro l = emprestadoParaAna();
        l.registrarDevolucao();
        return l;
    }

    @Test
    void cicloCompletoLivreAteLivreDeNovo() {
        Livro l = new Livro(1, "Dom Casmurro");
        assertEquals(Situacao.LIVRE, l.getEstado());

        l.reservarLivro(ana);                                   // T2
        assertEquals(Situacao.RESERVADO, l.getEstado());
        assertSame(ana, l.getReservante());

        l.emprestarLivro(ana, hoje);                            // T3: vencimento = 13/11 + 30
        assertEquals(Situacao.EMPRESTADO, l.getEstado());
        assertEquals(LocalDate.of(2026, 12, 13), l.getVencimento());

        l.renovarEmprestimo();                                  // T4
        assertEquals(Situacao.RENOVADO, l.getEstado());
        assertEquals(LocalDate.of(2027, 1, 12), l.getVencimento());
        assertEquals(1, l.getNumRenovacoes());

        l.renovarEmprestimo();                                  // T5 (autotransição)
        assertEquals(Situacao.RENOVADO, l.getEstado());
        assertEquals(LocalDate.of(2027, 2, 11), l.getVencimento());
        assertEquals(2, l.getNumRenovacoes());

        l.registrarDevolucao();                                 // T6
        assertEquals(Situacao.DEVOLVIDO, l.getEstado());

        l.inspecionar(true);                                    // T7 / limparCiclo
        assertEquals(Situacao.LIVRE, l.getEstado());
        assertNull(l.getReservante());
        assertNull(l.getVencimento());
        assertEquals(0, l.getNumRenovacoes());
    }

    @Test
    void terceiraRenovacaoRecusadaSemAlterarOLivro() {
        Livro l = emprestadoParaAna();
        l.renovarEmprestimo();
        l.renovarEmprestimo();
        LocalDate vencimentoAntes = l.getVencimento();

        assertThrows(RenovacaoNegadaException.class, l::renovarEmprestimo);

        assertEquals(vencimentoAntes, l.getVencimento());
        assertEquals(2, l.getNumRenovacoes());
        assertEquals(Situacao.RENOVADO, l.getEstado());
    }

    @Test
    void devolucaoSemRenovacaoTambemVaiParaDevolvido() {
        Livro l = emprestadoParaAna();
        l.registrarDevolucao();
        assertEquals(Situacao.DEVOLVIDO, l.getEstado());
    }

    @Test
    void inspecaoComDanoVaiParaReparoERetornaLivre() {
        Livro l = devolvido();

        l.inspecionar(false);                                   // T8
        assertEquals(Situacao.REPARANDO, l.getEstado());

        l.retornarDoReparo();                                   // T10 / limparCiclo
        assertEquals(Situacao.LIVRE, l.getEstado());
        assertNull(l.getVencimento());
    }

    @Test
    void livroSemReparoPossivelEIncineradoEPodeSerExcluido() {
        Livro l = devolvido();
        l.inspecionar(false);

        l.incinerar();                                          // laudo irrecuperável
        assertEquals(Situacao.INCINERADO, l.getEstado());
        assertDoesNotThrow(l::excluirLivro);                    // T11
        assertThrows(OperacaoInvalidaException.class, () -> l.reservarLivro(ana));
    }

    @Test
    void livreComDanoNoAcervoVaiParaReparo() {
        Livro l = new Livro(1, "Dom Casmurro");
        l.enviarParaReparo();                                   // T9
        assertEquals(Situacao.REPARANDO, l.getEstado());
    }

    @Test
    void emprestimoParaEstudanteDiferenteDoReservanteERecusado() {
        Livro l = new Livro(1, "Dom Casmurro");
        l.reservarLivro(ana);

        assertThrows(OperacaoInvalidaException.class, () -> l.emprestarLivro(caio, hoje));

        assertEquals(Situacao.RESERVADO, l.getEstado());
        assertNull(l.getVencimento());
    }

    @Test
    void reservaSemEstudanteCadastradoERecusada() {
        Livro l = new Livro(1, "Dom Casmurro");
        assertThrows(OperacaoInvalidaException.class, () -> l.reservarLivro(null));
        assertEquals(Situacao.LIVRE, l.getEstado());
    }

    @Test
    void eventosForaDoLugarSaoRecusadosSemAlterarOLivro() {
        Livro livre = new Livro(1, "A");
        assertThrows(OperacaoInvalidaException.class, livre::renovarEmprestimo);
        assertThrows(OperacaoInvalidaException.class, () -> livre.inspecionar(true));
        assertEquals(Situacao.LIVRE, livre.getEstado());

        Livro devolvido = devolvido();
        assertThrows(OperacaoInvalidaException.class, () -> devolvido.reservarLivro(caio));
        assertEquals(Situacao.DEVOLVIDO, devolvido.getEstado());

        Livro emprestado = emprestadoParaAna();
        assertThrows(OperacaoInvalidaException.class, emprestado::excluirLivro);
        assertThrows(OperacaoInvalidaException.class, emprestado::enviarParaReparo);
        assertEquals(Situacao.EMPRESTADO, emprestado.getEstado());

        Livro reparando = new Livro(2, "B");
        reparando.enviarParaReparo();
        assertThrows(OperacaoInvalidaException.class, () -> reparando.reservarLivro(ana));
        assertEquals(Situacao.REPARANDO, reparando.getEstado());
    }

    @ParameterizedTest
    @EnumSource(Situacao.class)
    void estadoEReconstituidoAPartirDaSituacaoGravada(Situacao gravada) {
        assertEquals(gravada, EstadoLivro.para(gravada).situacao());
    }
}
