package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Papel GoF: Facade (Req. 4). Coordena Relacionamento, Movimentacao, Acervo e Livro em operações completas.
 * Cada operação roda dentro de uma transação da unidade de trabalho: se um passo falhar no meio, tudo o que
 * foi feito antes é desfeito. A fachada não substitui essas classes — apenas as coordena; as regras de estado
 * continuam no Livro (State).
 */
public class ServicoDeCirculacao implements iCirculacao {

    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final Clock relogio;
    private final UnidadeDeTrabalho transacao;

    public ServicoDeCirculacao(Acervo acervo, Movimentacao movimentacao, Relacionamento relacionamento,
                               Clock relogio, UnidadeDeTrabalho transacao) {
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    /** f1 (regra 5): Relacionamento → Acervo → Livro.reservarLivro → Movimentacao.registrarReserva. */
    @Override
    public ComprovanteReserva reservar(int idLivro, String idEstudante, String idAtendente) {
        return transacao.executar(() -> {
            Livro livro = acervo.buscarPorId(idLivro);
            Estudante estudante = relacionamento.buscarEstudante(idEstudante);
            if (livro == null || estudante == null) return null;

            livro.reservarLivro(estudante);                                  // State decide
            Reserva reserva = new Reserva(movimentacao.gerarIdTransacao(), hoje(), livro, estudante,
                    relacionamento.buscarAtendente(idAtendente));
            movimentacao.registrarReserva(reserva);
            acervo.atualizar(livro);
            return ComprovanteReserva.de(reserva);
        });
    }

    /** f2 (regra 6). */
    @Override
    public ComprovanteReserva consultarReserva(int idLivro, String idEstudante) {
        Reserva r = movimentacao.buscarReserva(idLivro, idEstudante);
        return r == null ? null : ComprovanteReserva.de(r);
    }

    /**
     * f3 (regra 7) — operação atômica: valida a reserva → livro.emprestarLivro → remove a reserva →
     * registra o empréstimo → atualiza o livro → confirma. Qualquer exceção desfaz tudo.
     */
    @Override
    public ComprovanteEmprestimo efetivarEmprestimo(int idLivro, String idEstudante, String idAtendente) {
        return transacao.executar(() -> {
            Estudante estudante = relacionamento.buscarEstudante(idEstudante);   // 1
            Reserva reserva = movimentacao.buscarReservaAtiva(idLivro);          // 2
            exigirReservaDo(estudante, reserva, idEstudante);                     // 3 valida a reserva
            Livro livro = acervo.buscarPorId(idLivro);                            // 4
            livro.emprestarLivro(estudante, hoje());                              // 5 RESERVADO → EMPRESTADO
            movimentacao.removerReserva(reserva);                                 // 6 remove a reserva ativa
            Emprestimo emprestimo = new Emprestimo(movimentacao.gerarIdTransacao(), hoje(), livro, estudante,
                    relacionamento.buscarAtendente(idAtendente));
            movimentacao.registrarEmprestimo(emprestimo);                         // 7 cria o empréstimo
            acervo.atualizar(livro);                                              // 8 atualiza o livro
            return ComprovanteEmprestimo.de(emprestimo, livro.getVencimento());   // 9 confirmar() ao sair
        });
    }

    /** f4 (regra 8): a guarda numRenovacoes < 2 está no estado do livro. */
    @Override
    public ComprovanteRenovacao renovar(int idLivro, String idEstudante) {
        return transacao.executar(() -> {
            if (movimentacao.buscarEmprestimo(idLivro, idEstudante) == null) return null;
            Livro livro = acervo.buscarPorId(idLivro);
            livro.renovarEmprestimo();
            acervo.atualizar(livro);
            return new ComprovanteRenovacao(idLivro, livro.getTitulo(), livro.getVencimento(), livro.getNumRenovacoes());
        });
    }

    /** f5 (regra 9): troca o empréstimo ativo por uma Devolucao; o livro fica DEVOLVIDO até a inspeção. */
    @Override
    public ComprovanteDevolucao devolver(int idLivro, String idEstudante, String idAtendente) {
        return transacao.executar(() -> {
            Emprestimo emprestimo = movimentacao.buscarEmprestimo(idLivro, idEstudante);
            if (emprestimo == null) return null;                                  // [empréstimo registrado]
            Livro livro = acervo.buscarPorId(idLivro);
            LocalDate vencimento = livro.getVencimento();                         // copiado antes de limpar o ciclo

            livro.registrarDevolucao();
            movimentacao.removerEmprestimo(emprestimo);
            Devolucao devolucao = new Devolucao(movimentacao.gerarIdTransacao(), hoje(), livro,
                    emprestimo.getEstudante(), relacionamento.buscarAtendente(idAtendente), emprestimo, vencimento);
            movimentacao.registrarDevolucao(devolucao);
            acervo.atualizar(livro);
            return ComprovanteDevolucao.de(devolucao);
        });
    }

    /** Devolução mais recente do livro por esse estudante (para a f6, emitir boleto). */
    @Override
    public ComprovanteDevolucao consultarDevolucao(int idLivro, String idEstudante) {
        Devolucao d = movimentacao.buscarDevolucao(idLivro, idEstudante);
        return d == null ? null : ComprovanteDevolucao.de(d);
    }

    @Override
    public SituacaoLivroDTO registrarInspecao(int idLivro, boolean emBomEstado) {
        return noLivro(idLivro, l -> l.inspecionar(emBomEstado));
    }

    @Override public SituacaoLivroDTO enviarParaReparo(int idLivro) { return noLivro(idLivro, Livro::enviarParaReparo); }
    @Override public SituacaoLivroDTO retornarDoReparo(int idLivro) { return noLivro(idLivro, Livro::retornarDoReparo); }
    @Override public SituacaoLivroDTO incinerar(int idLivro) { return noLivro(idLivro, Livro::incinerar); }

    @Override
    public SituacaoLivroDTO consultarSituacao(int idLivro) {
        Livro l = acervo.buscarPorId(idLivro);
        return l == null ? null : SituacaoLivroDTO.de(l);
    }

    // ---- auxiliares ----

    /** Regra 7: precisa existir reserva ativa do livro, e ela precisa ser do mesmo estudante. */
    private void exigirReservaDo(Estudante estudante, Reserva reserva, String idEstudante) {
        if (estudante == null) throw new ReservaInvalidaException("estudante " + idEstudante + " não cadastrado");
        if (reserva == null) throw new ReservaInvalidaException("não há reserva ativa para este livro");
        if (!reserva.getEstudante().getMatricula().equals(estudante.getMatricula())) {
            throw new ReservaInvalidaException("a reserva é de outro estudante");
        }
    }

    private SituacaoLivroDTO noLivro(int idLivro, Consumer<Livro> evento) {
        return transacao.executar(() -> {
            Livro livro = acervo.buscarPorId(idLivro);
            if (livro == null) return null;
            evento.accept(livro);
            acervo.atualizar(livro);
            return SituacaoLivroDTO.de(livro);
        });
    }

    private LocalDate hoje() { return LocalDate.now(relogio); }
}
