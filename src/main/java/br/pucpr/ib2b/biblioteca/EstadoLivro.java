package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;

import java.time.LocalDate;

/**
 * Papel GoF: State. Declara todos os eventos da máquina de estados do Livro (nomes do Astah).
 * A implementação padrão RECUSA o evento sem alterar o livro; cada estado concreto sobrescreve
 * apenas as transições que saem dele, com a guarda e a ação no corpo do método.
 */
public abstract class EstadoLivro {

    void reservarLivro(Livro l, Estudante e)                   { throw recusa("reservarLivro"); }
    void emprestarLivro(Livro l, Estudante e, LocalDate data)  { throw recusa("emprestarLivro"); }
    void renovarEmprestimo(Livro l)                            { throw recusa("renovarEmprestimo"); }
    void registrarDevolucao(Livro l)                           { throw recusa("registrarDevolucao"); }
    void inspecionar(Livro l, boolean emBomEstado)             { throw recusa("inspecionar"); }
    void enviarParaReparo(Livro l)                             { throw recusa("enviarParaReparo"); }
    void retornarDoReparo(Livro l)                             { throw recusa("retornarDoReparo"); }
    void incinerar(Livro l)                                    { throw recusa("incinerar"); }
    void excluirLivro(Livro l)                                 { throw recusa("excluirLivro"); }

    /** Valor gravado na persistência e exibido na interface. */
    public abstract Situacao situacao();

    protected OperacaoInvalidaException recusa(String evento) {
        return new OperacaoInvalidaException(situacao(), evento);
    }

    /** Reconstitui o objeto de estado a partir da Situacao gravada (persistência, Etapa 2). */
    public static EstadoLivro para(Situacao s) {
        return switch (s) {
            case LIVRE -> Livre.INSTANCIA;
            case RESERVADO -> Reservado.INSTANCIA;
            case EMPRESTADO -> Emprestado.INSTANCIA;
            case RENOVADO -> Renovado.INSTANCIA;
            case DEVOLVIDO -> Devolvido.INSTANCIA;
            case REPARANDO -> Reparando.INSTANCIA;
            case INCINERADO -> Incinerado.INSTANCIA;
        };
    }
}
