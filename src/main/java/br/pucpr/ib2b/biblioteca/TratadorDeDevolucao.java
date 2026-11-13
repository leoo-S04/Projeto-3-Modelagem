package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.rh.Atendente;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Serviços do Atendente (Astah: TratadorDeDevolucao): f5, inspeção e reparo. Acessa Acervo, Movimentacao e
 * Relacionamento por delegação; não testa a situação do livro (padrão State). O Clock define a data de hoje.
 * A f6 (boleto de multa) entra na Etapa 4.
 */
public class TratadorDeDevolucao implements iDevolucao {

    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final Clock relogio;

    public TratadorDeDevolucao(Acervo acervo, Movimentacao movimentacao, Relacionamento relacionamento,
                               Clock relogio) {
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
    }

    /** f5 — regra 9: EMPRESTADO/RENOVADO → DEVOLVIDO; troca o empréstimo ativo por uma Devolucao. */
    @Override
    public Devolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente) {
        Emprestimo emprestimo = movimentacao.buscarEmprestimo(idLivro, idEstudante);
        if (emprestimo == null) return null;                     // guarda [empréstimo registrado]
        Livro livro = emprestimo.getLivro();
        Atendente atendente = relacionamento.buscarAtendente(idAtendente);
        LocalDate vencimento = livro.getVencimento();            // copiado antes de o ciclo ser limpo

        livro.registrarDevolucao();
        movimentacao.remover(emprestimo);
        Devolucao devolucao = new Devolucao(movimentacao.gerarIdTransacao(), LocalDate.now(relogio), livro,
                emprestimo.getEstudante(), atendente, emprestimo, vencimento);
        movimentacao.adicionar(devolucao);
        return devolucao;
    }

    /** DEVOLVIDO → LIVRE (bom estado) ou REPARANDO (danificado). */
    @Override
    public boolean inspecionar(int idLivro, boolean emBomEstado) {
        Livro livro = acervo.buscarPorId(idLivro);
        if (livro == null) return false;
        livro.inspecionar(emBomEstado);
        return true;
    }

    /** LIVRE → REPARANDO (dano identificado no acervo). */
    @Override
    public boolean enviarParaReparo(int idLivro) {
        Livro livro = acervo.buscarPorId(idLivro);
        if (livro == null) return false;
        livro.enviarParaReparo();
        return true;
    }

    /** REPARANDO → LIVRE. */
    @Override
    public boolean retornarDoReparo(int idLivro) {
        Livro livro = acervo.buscarPorId(idLivro);
        if (livro == null) return false;
        livro.retornarDoReparo();
        return true;
    }

    /** REPARANDO → INCINERADO (laudo irrecuperável). */
    @Override
    public boolean incinerar(int idLivro) {
        Livro livro = acervo.buscarPorId(idLivro);
        if (livro == null) return false;
        livro.incinerar();
        return true;
    }
}
