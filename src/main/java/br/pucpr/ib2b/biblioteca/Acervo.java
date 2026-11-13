package br.pucpr.ib2b.biblioteca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ponto interno de acesso aos livros (Astah: AcervoDeLivros, renomeado para Acervo).
 * TratadorDeAcervo e os tratadores de circulação acessam os livros por delegação a esta classe.
 */
public class Acervo {

    private final List<Livro> livros = new ArrayList<>();

    public void adicionar(Livro livro) { livros.add(livro); }

    public boolean remover(int idLivro) {
        return livros.removeIf(l -> l.getIdLivro() == idLivro);
    }

    /** Devolve o livro com esse id, ou {@code null} se não estiver no acervo. */
    public Livro buscarPorId(int idLivro) {
        for (Livro l : livros) {
            if (l.getIdLivro() == idLivro) return l;
        }
        return null;
    }

    /** Cópia somente leitura: quem chama não consegue alterar a coleção interna. */
    public List<Livro> listar() {
        return Collections.unmodifiableList(new ArrayList<>(livros));
    }
}
