package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.biblioteca.Devolucao;
import br.pucpr.ib2b.biblioteca.Emprestimo;
import br.pucpr.ib2b.biblioteca.Livro;
import br.pucpr.ib2b.biblioteca.Reserva;
import br.pucpr.ib2b.biblioteca.Situacao;
import br.pucpr.ib2b.biblioteca.Transacao;
import br.pucpr.ib2b.persistencia.DocumentoDados.*;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;

import java.util.List;

/**
 * Converte a cópia de trabalho (objetos de domínio) no documento gravável e vice-versa.
 * Usado pela família JSON (arquivo) e pela família em memória (fotografia para o desfazer).
 */
final class MapeadorDeDados {

    private MapeadorDeDados() { }

    static DocumentoDados paraDocumento(BaseDeDados b) {
        return new DocumentoDados(
                b.pessoas.values().stream()
                        .map(p -> new PessoaJson(p.getCpf(), p.getNome(), p.getSobrenome(), p.getEmail())).toList(),
                b.estudantes.values().stream()
                        .map(e -> new EstudanteJson(e.getId(), e.getMatricula(), e.getPessoa().getCpf())).toList(),
                b.atendentes.values().stream()
                        .map(a -> new AtendenteJson(a.getId(), a.getMatriculaFuncional(), a.getPessoa().getCpf())).toList(),
                b.livros.values().stream()
                        .map(l -> new LivroJson(l.getIdLivro(), l.getTitulo(), l.getEstado().name(),
                                l.getReservante() == null ? null : l.getReservante().getMatricula(),
                                l.getVencimento(), l.getNumRenovacoes())).toList(),
                b.reservas.stream().map(MapeadorDeDados::transacao).toList(),
                b.emprestimos.stream().map(MapeadorDeDados::transacao).toList(),
                b.devolucoes.stream()
                        .map(d -> new DevolucaoJson(d.getIdTransacao(), d.getData(), d.getLivro().getIdLivro(),
                                d.getEstudante().getMatricula(), matricula(d.getAtendente()),
                                d.getDataDevolucao(), d.getVencimento(), transacao(d.getEmprestimo()))).toList(),
                List.copyOf(b.solicitacoes.values()),
                new ContadoresJson(b.ultimoIdTransacao, b.ultimoIdPapel));
    }

    static BaseDeDados paraBase(DocumentoDados doc) {
        BaseDeDados b = new BaseDeDados();
        for (PessoaJson p : lista(doc.pessoas())) {
            b.pessoas.put(p.cpf(), new Pessoa(p.cpf(), p.nome(), p.sobrenome(), p.email()));
        }
        for (EstudanteJson e : lista(doc.estudantes())) {
            b.estudantes.put(e.matricula(), new Estudante(e.id(), e.matricula(), b.pessoas.get(e.cpf())));
        }
        for (AtendenteJson a : lista(doc.atendentes())) {
            b.atendentes.put(a.matriculaFuncional(), new Atendente(a.id(), a.matriculaFuncional(), b.pessoas.get(a.cpf())));
        }
        for (LivroJson l : lista(doc.livros())) {
            b.livros.put(l.id(), Livro.reconstituir(l.id(), l.titulo(), Situacao.valueOf(l.situacao()),
                    l.reservante() == null ? null : b.estudantes.get(l.reservante()), l.vencimento(), l.numRenovacoes()));
        }
        for (TransacaoJson t : lista(doc.reservas())) {
            b.reservas.add(new Reserva(t.id(), t.data(), b.livros.get(t.idLivro()),
                    b.estudantes.get(t.matriculaEstudante()), atendente(b, t.matriculaAtendente())));
        }
        for (TransacaoJson t : lista(doc.emprestimos())) {
            b.emprestimos.add(emprestimo(b, t));
        }
        for (DevolucaoJson d : lista(doc.devolucoes())) {
            Devolucao dev = new Devolucao(d.id(), d.data(), b.livros.get(d.idLivro()),
                    b.estudantes.get(d.matriculaEstudante()), atendente(b, d.matriculaAtendente()),
                    d.emprestimo() == null ? null : emprestimo(b, d.emprestimo()), d.vencimento());
            dev.registrarDataDevolucao(d.dataDevolucao());
            b.devolucoes.add(dev);
        }
        for (var s : lista(doc.solicitacoes())) {
            b.solicitacoes.put(s.idSolicitacao(), s);
        }
        if (doc.contadores() != null) {
            b.ultimoIdTransacao = doc.contadores().ultimoIdTransacao();
            b.ultimoIdPapel = doc.contadores().ultimoIdPapel();
        }
        return b;
    }

    private static TransacaoJson transacao(Transacao t) {
        if (t == null) return null;
        return new TransacaoJson(t.getIdTransacao(), t.getData(), t.getLivro().getIdLivro(),
                t.getEstudante().getMatricula(), matricula(t.getAtendente()));
    }

    private static Emprestimo emprestimo(BaseDeDados b, TransacaoJson t) {
        return new Emprestimo(t.id(), t.data(), b.livros.get(t.idLivro()),
                b.estudantes.get(t.matriculaEstudante()), atendente(b, t.matriculaAtendente()));
    }

    private static String matricula(Atendente a) { return a == null ? null : a.getMatriculaFuncional(); }

    private static Atendente atendente(BaseDeDados b, String matricula) {
        return matricula == null ? null : b.atendentes.get(matricula);
    }

    /** Seção ausente no arquivo (versão anterior) = seção vazia. */
    private static <T> List<T> lista(List<T> l) { return l == null ? List.of() : l; }
}
