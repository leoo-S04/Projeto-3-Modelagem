package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 10 — Composite: cenários de verificação do enunciado. */
class R10_OrganizacaoDoAcervoTest {

    private final Cenario c = new Cenario();
    private TratadorDeAcervo acervo;

    /** Unidade "Central" com as seções "Literatura" (livros 1, 2, 3) e "Ciencias" (livros 4, 5). */
    @BeforeEach
    void montarUnidade() {
        acervo = c.tratadorDeAcervo;
        for (int id = 2; id <= 5; id++) acervo.inserirLivro(id, "Livro " + id);
        acervo.criarUnidade("Central");
        acervo.criarGrupo("Central", "Literatura", TipoDeGrupo.SECAO);
        acervo.criarGrupo("Central", "Ciencias", TipoDeGrupo.SECAO);
        for (int id : new int[]{1, 2, 3}) acervo.adicionarLivroAoGrupo("Literatura", id);
        for (int id : new int[]{4, 5}) acervo.adicionarLivroAoGrupo("Ciencias", id);
    }

    @Test
    void R10_unidadeComDuasSecoesDeTresEDoisLivrosInformaCinco() {
        assertEquals(5, acervo.contarLivros("Central"));        // grupo: soma recursiva
        assertEquals(3, acervo.contarLivros("Literatura"));
        assertEquals(2, acervo.contarLivros("Ciencias"));
    }

    @Test
    void R10_moverLivroEntreSecoesNaoAlteraIdEstadoNemEmprestimo() {
        c.emprestarLivro1();                                      // livro 1 EMPRESTADO, vence 13/12/2026
        Livro antes = acervo.consultarLivro(1);

        acervo.moverLivro(1, "Ciencias");

        assertEquals(2, acervo.contarLivros("Literatura"));
        assertEquals(3, acervo.contarLivros("Ciencias"));
        assertEquals(5, acervo.contarLivros("Central"));
        Livro depois = acervo.consultarLivro(1);
        assertEquals(1, depois.getIdLivro());                    // mesmo id
        assertEquals(Situacao.EMPRESTADO, depois.getEstado());    // mesmo estado
        assertEquals(LocalDate.of(2026, 12, 13), depois.getVencimento());
        assertEquals(antes.getVencimento(), depois.getVencimento());
        assertNotNull(c.movimentacao().buscarEmprestimoAtivo(1)); // empréstimo intacto
    }

    @Test
    void profundidadeVariavelComSubsecao() {
        acervo.inserirLivro(6, "Livro 6");
        acervo.criarGrupo("Literatura", "Poesia", TipoDeGrupo.SUBSECAO);
        acervo.adicionarLivroAoGrupo("Poesia", 6);

        assertEquals(4, acervo.contarLivros("Literatura"));
        assertEquals(6, acervo.contarLivros("Central"));
    }

    @Test
    void grupoRecusaLivroQueJaPertenceAOutroGrupo() {
        assertThrows(HierarquiaInvalidaException.class, () -> acervo.adicionarLivroAoGrupo("Ciencias", 1));
        assertEquals(5, acervo.contarLivros("Central"));
    }

    @Test
    void grupoRecusaCiclos() {
        GrupoDoAcervo unidade = new GrupoDoAcervo("U", TipoDeGrupo.UNIDADE);
        GrupoDoAcervo secao = new GrupoDoAcervo("S", TipoDeGrupo.SECAO);
        unidade.adicionar(secao);

        assertThrows(HierarquiaInvalidaException.class, () -> secao.adicionar(unidade));   // ancestral
        assertThrows(HierarquiaInvalidaException.class, () -> unidade.adicionar(unidade)); // ele mesmo
        assertNull(unidade.getPai());
    }

    @Test
    void grupoEItemRespondemAsMesmasOperacoes() {
        ComponenteDoAcervo item = new ItemDoAcervo(7);
        ComponenteDoAcervo grupo = new GrupoDoAcervo("G", TipoDeGrupo.SECAO);
        ((GrupoDoAcervo) grupo).adicionar(item);

        assertEquals(1, item.contarLivros());
        assertEquals(1, grupo.contarLivros());
        assertEquals(item.listarIds(), grupo.listarIds());
    }
}
