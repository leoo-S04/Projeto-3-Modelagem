package br.pucpr.ib2b.biblioteca;

/** Regras 1.1 a 1.4 (manter Livro). Delega o armazenamento ao Acervo. */
public class TratadorDeAcervo implements iLivro {

    private final Acervo acervo;

    public TratadorDeAcervo(Acervo acervo) {
        this.acervo = acervo;
    }

    /** 1.1: se já cadastrado, devolve o existente; senão cria (LIVRE) e insere no Acervo. */
    @Override
    public Livro inserirLivro(int idLivro, String titulo) {
        Livro existente = acervo.buscarPorId(idLivro);
        if (existente != null) return existente;
        Livro novo = new Livro(idLivro, titulo);
        acervo.adicionar(novo);
        return novo;
    }

    /** 1.3: devolve o livro, ou {@code null} se não cadastrado. */
    @Override
    public Livro consultarLivro(int idLivro) {
        return acervo.buscarPorId(idLivro);
    }

    /** 1.2: só atualiza se cadastrado. */
    @Override
    public boolean atualizarLivro(int idLivro, String titulo) {
        Livro l = acervo.buscarPorId(idLivro);
        if (l == null) return false;
        l.setTitulo(titulo);
        return true;
    }

    /** 1.4: só exclui se cadastrado. A restrição por estado (T11: só LIVRE ou INCINERADO) entra na Etapa 1. */
    @Override
    public boolean excluirLivro(int idLivro) {
        return acervo.remover(idLivro);
    }
}
