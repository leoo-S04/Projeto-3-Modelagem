package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;
import br.pucpr.ib2b.rh.Atendente;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Serviços do Atendente (Astah: TratadorDeDevolucao): f5, inspeção e reparo. Acessa Acervo, Movimentacao e
 * Relacionamento por delegação; não testa a situação do livro (padrão State). O Clock define a data de hoje.
 * Cada operação é uma transação da unidade de trabalho. A f6 (boleto de multa) entra na Etapa 4.
 */
public class TratadorDeDevolucao implements iDevolucao {

    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final Clock relogio;
    private final UnidadeDeTrabalho transacao;

    public TratadorDeDevolucao(Acervo acervo, Movimentacao movimentacao, Relacionamento relacionamento,
                               Clock relogio, UnidadeDeTrabalho transacao) {
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    /** f5 — regra 9: EMPRESTADO/RENOVADO → DEVOLVIDO; troca o empréstimo ativo por uma Devolucao. */
    @Override
    public Devolucao registrarDevolucao(int idLivro, String idEstudante, String idAtendente) {
        return transacao.executar(() -> {
            Emprestimo emprestimo = movimentacao.buscarEmprestimo(idLivro, idEstudante);
            if (emprestimo == null) return null;                     // guarda [empréstimo registrado]
            Livro livro = acervo.buscarPorId(idLivro);
            Atendente atendente = relacionamento.buscarAtendente(idAtendente);
            LocalDate vencimento = livro.getVencimento();            // copiado antes de o ciclo ser limpo

            livro.registrarDevolucao();
            movimentacao.removerEmprestimo(emprestimo);
            Devolucao devolucao = new Devolucao(movimentacao.gerarIdTransacao(), LocalDate.now(relogio), livro,
                    emprestimo.getEstudante(), atendente, emprestimo, vencimento);
            movimentacao.registrarDevolucao(devolucao);
            acervo.atualizar(livro);
            return devolucao;
        });
    }

    /** DEVOLVIDO → LIVRE (bom estado) ou REPARANDO (danificado). */
    @Override
    public boolean inspecionar(int idLivro, boolean emBomEstado) {
        return noLivro(idLivro, l -> l.inspecionar(emBomEstado));
    }

    /** LIVRE → REPARANDO (dano identificado no acervo). */
    @Override
    public boolean enviarParaReparo(int idLivro) { return noLivro(idLivro, Livro::enviarParaReparo); }

    /** REPARANDO → LIVRE. */
    @Override
    public boolean retornarDoReparo(int idLivro) { return noLivro(idLivro, Livro::retornarDoReparo); }

    /** REPARANDO → INCINERADO (laudo irrecuperável). */
    @Override
    public boolean incinerar(int idLivro) { return noLivro(idLivro, Livro::incinerar); }

    /** Dispara um evento no livro dentro de uma transação; false se o livro não existe. */
    private boolean noLivro(int idLivro, Consumer<Livro> evento) {
        return transacao.executar(() -> {
            Livro livro = acervo.buscarPorId(idLivro);
            if (livro == null) return false;
            evento.accept(livro);
            acervo.atualizar(livro);
            return true;
        });
    }
}
