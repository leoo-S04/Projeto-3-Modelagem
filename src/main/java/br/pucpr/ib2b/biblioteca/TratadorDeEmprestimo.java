package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;
import br.pucpr.ib2b.rh.Atendente;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Serviços f1 a f4 do Estudante (Astah: TratadorDeEmprestimo). Acessa Acervo, Movimentacao e Relacionamento
 * por delegação. NÃO testa a situação do livro: dispara o evento no Livro e o estado decide (padrão State).
 * Cada operação é uma transação: se algo falhar, a unidade de trabalho desfaz tudo.
 * Recusa do estado → a OperacaoInvalidaException sobe com a mensagem; livro/estudante inexistente → null/false.
 */
public class TratadorDeEmprestimo implements iEmprestimo {

    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final Clock relogio;
    private final UnidadeDeTrabalho transacao;

    public TratadorDeEmprestimo(Acervo acervo, Movimentacao movimentacao, Relacionamento relacionamento,
                                Clock relogio, UnidadeDeTrabalho transacao) {
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    /** f1 — regra 5: o estado do livro decide se a reserva é aceita; só depois a Reserva é registrada. */
    @Override
    public Reserva reservarLivro(int idLivro, String idEstudante, String idAtendente) {
        return transacao.executar(() -> {
            Livro livro = acervo.buscarPorId(idLivro);
            Estudante estudante = relacionamento.buscarEstudante(idEstudante);
            if (livro == null || estudante == null) return null;
            Atendente atendente = relacionamento.buscarAtendente(idAtendente);

            livro.reservarLivro(estudante);                // LIVRE → RESERVADO, ou exceção sem alterar nada
            Reserva reserva = new Reserva(movimentacao.gerarIdTransacao(), hoje(), livro, estudante, atendente);
            movimentacao.registrarReserva(reserva);
            acervo.atualizar(livro);
            return reserva;
        });
    }

    /** f2 — regra 6: devolve a reserva registrada na Movimentacao, ou {@code null}. */
    @Override
    public Reserva consultarReserva(int idLivro, String idEstudante) {
        return movimentacao.buscarReserva(idLivro, idEstudante);
    }

    /** f3 — regra 7: RESERVADO → EMPRESTADO para o mesmo estudante; remove a reserva e registra o empréstimo. */
    @Override
    public Emprestimo emprestarLivro(int idLivro, String idEstudante, String idAtendente) {
        return transacao.executar(() -> {
            Livro livro = acervo.buscarPorId(idLivro);
            Estudante estudante = relacionamento.buscarEstudante(idEstudante);
            if (livro == null || estudante == null) return null;
            Atendente atendente = relacionamento.buscarAtendente(idAtendente);

            livro.emprestarLivro(estudante, hoje());       // guarda: reserva do mesmo estudante
            Reserva reserva = movimentacao.buscarReserva(idLivro, idEstudante);
            if (reserva != null) movimentacao.removerReserva(reserva);
            Emprestimo emprestimo = new Emprestimo(movimentacao.gerarIdTransacao(), hoje(), livro, estudante, atendente);
            movimentacao.registrarEmprestimo(emprestimo);
            acervo.atualizar(livro);
            return emprestimo;
        });
    }

    /** f4 — regra 8: renova se houver empréstimo registrado; a guarda numRenovacoes < 2 fica no estado. */
    @Override
    public boolean renovarEmprestimo(int idLivro, String idEstudante) {
        return transacao.executar(() -> {
            Emprestimo emprestimo = movimentacao.buscarEmprestimo(idLivro, idEstudante);
            if (emprestimo == null) return false;
            Livro livro = acervo.buscarPorId(idLivro);
            livro.renovarEmprestimo();                     // RenovacaoNegadaException na 3ª tentativa
            acervo.atualizar(livro);
            return true;
        });
    }

    private LocalDate hoje() { return LocalDate.now(relogio); }
}
