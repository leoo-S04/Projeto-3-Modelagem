package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.Sistema;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.persistencia.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 4 — Facade: cenário de verificação do enunciado e tratamento de falhas. */
class R04_FachadaDeCirculacaoTest {

    private final Cenario c = new Cenario();

    @Test
    void R04_efetivarEmprestimoValidaReservaRemoveReservaCriaEmprestimoEAtualizaLivroSemExporColecoes() {
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        assertNotNull(c.movimentacao().buscarReservaAtiva(1));

        ComprovanteEmprestimo comprovante = c.tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01");

        // comprovante (DTO) com os dados do empréstimo — não é coleção nem objeto do domínio
        assertEquals(1, comprovante.idLivro());
        assertEquals("M-100", comprovante.matriculaEstudante());
        assertEquals(LocalDate.of(2026, 12, 13), comprovante.vencimento());
        // remove a reserva ativa, cria o empréstimo e atualiza o Livro
        assertNull(c.movimentacao().buscarReservaAtiva(1));
        assertEquals(comprovante.idEmprestimo(), c.movimentacao().buscarEmprestimoAtivo(1).getIdTransacao());
        assertEquals(Situacao.EMPRESTADO, c.tratadorDeEmprestimo.consultarSituacao(1).situacao());
        // nenhuma operação da fachada nem dos tratadores expõe coleções ou os pontos internos
        for (Class<?> fronteira : List.of(iCirculacao.class, iEmprestimo.class, iDevolucao.class)) {
            for (Method m : fronteira.getMethods()) {
                Class<?> tipo = m.getReturnType();
                assertFalse(Collection.class.isAssignableFrom(tipo) || Map.class.isAssignableFrom(tipo)
                        || tipo == Acervo.class || tipo == Movimentacao.class || tipo == Relacionamento.class,
                        fronteira.getSimpleName() + "." + m.getName() + " expõe " + tipo.getSimpleName());
            }
        }
    }

    @Test
    void reservaAusenteOuDeOutroEstudanteERecusadaSemAlterarNada() {
        assertThrows(ReservaInvalidaException.class, () -> c.tratadorDeEmprestimo.emprestarLivro(1, "M-100", "F-01"));

        c.pessoas.inserirPessoa("333", "Caio", "Reis", "caio@pucpr.br");
        c.estudantes.inserirEstudante("333", "M-300");
        c.tratadorDeEmprestimo.reservarLivro(1, "M-100", "F-01");
        assertThrows(ReservaInvalidaException.class, () -> c.tratadorDeEmprestimo.emprestarLivro(1, "M-300", "F-01"));

        assertEquals(Situacao.RESERVADO, c.tratadorDeEmprestimo.consultarSituacao(1).situacao());
        assertNotNull(c.movimentacao().buscarReservaAtiva(1));
        assertNull(c.movimentacao().buscarEmprestimoAtivo(1));
    }

    @Test
    void falhaNoMeioDaOperacaoDesfazTudo() {
        // Família em memória cujo repositório de movimentação falha ao registrar o empréstimo (passo 7)
        FabricaComFalha fabrica = new FabricaComFalha();
        Sistema s = Sistema.montar(fabrica, RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO));
        s.tratadorDePessoa.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        s.tratadorDeEstudante.inserirEstudante("111", "M-100");
        s.tratadorDeAcervo.inserirLivro(1, "Dom Casmurro");
        s.tratadorDeEmprestimo.reservarLivro(1, "M-100", null);
        fabrica.falharAoRegistrarEmprestimo = true;

        assertThrows(IllegalStateException.class, () -> s.tratadorDeEmprestimo.emprestarLivro(1, "M-100", null));

        // o livro já tinha mudado e a reserva já tinha sido removida — tudo foi desfeito
        assertEquals(Situacao.RESERVADO, s.tratadorDeEmprestimo.consultarSituacao(1).situacao());
        assertNotNull(s.tratadorDeEmprestimo.consultarReserva(1, "M-100"));
        assertNull(s.movimentacao().buscarEmprestimoAtivo(1));
    }

    /** Fábrica de teste: família em memória com um repositório de movimentação que pode falhar. */
    static class FabricaComFalha implements FabricaDePersistencia {
        private final FabricaEmMemoria real = new FabricaEmMemoria();
        boolean falharAoRegistrarEmprestimo = false;

        @Override public UnidadeDeTrabalho criarUnidadeDeTrabalho() { return real.criarUnidadeDeTrabalho(); }
        @Override public RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho u) { return real.criarRepositorioLivro(u); }
        @Override public RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho u) { return real.criarRepositorioRelacionamento(u); }
        @Override public RepositorioDeSolicitacoes criarRepositorioDeSolicitacoes(UnidadeDeTrabalho u) { return real.criarRepositorioDeSolicitacoes(u); }
        @Override public RepositorioFinanceiro criarRepositorioFinanceiro(UnidadeDeTrabalho u) { return real.criarRepositorioFinanceiro(u); }
        @Override public RepositorioDeOrganizacao criarRepositorioDeOrganizacao(UnidadeDeTrabalho u) { return real.criarRepositorioDeOrganizacao(u); }

        @Override
        public RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho u) {
            RepositorioMovimentacao r = real.criarRepositorioMovimentacao(u);
            return new RepositorioMovimentacao() {
                public int gerarIdTransacao() { return r.gerarIdTransacao(); }
                public void salvarReserva(Reserva x) { r.salvarReserva(x); }
                public boolean removerReserva(Reserva x) { return r.removerReserva(x); }
                public List<Reserva> reservas() { return r.reservas(); }
                public void salvarEmprestimo(Emprestimo x) {
                    if (falharAoRegistrarEmprestimo) throw new IllegalStateException("falha simulada ao gravar");
                    r.salvarEmprestimo(x);
                }
                public boolean removerEmprestimo(Emprestimo x) { return r.removerEmprestimo(x); }
                public List<Emprestimo> emprestimos() { return r.emprestimos(); }
                public void salvarDevolucao(Devolucao x) { r.salvarDevolucao(x); }
                public List<Devolucao> devolucoes() { return r.devolucoes(); }
                public UnidadeDeTrabalho unidade() { return r.unidade(); }
            };
        }
    }
}
