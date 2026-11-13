package br.pucpr.ib2b.biblioteca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Papel GoF: Composite (Req. 10). Unidade, seção ou subseção: mantém filhos (grupos ou itens), soma a contagem
 * e concatena os ids recursivamente, e protege as invariantes da árvore ao adicionar.
 */
public class GrupoDoAcervo extends ComponenteDoAcervo {

    private final String nome;
    private final TipoDeGrupo tipo;
    private final List<ComponenteDoAcervo> filhos = new ArrayList<>();

    public GrupoDoAcervo(String nome, TipoDeGrupo tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    /** Soma recursiva: cada filho responde (item = 1; grupo = soma dos seus filhos). */
    @Override
    public int contarLivros() {
        int total = 0;
        for (ComponenteDoAcervo filho : filhos) total += filho.contarLivros();
        return total;
    }

    @Override
    public List<Integer> listarIds() {
        List<Integer> ids = new ArrayList<>();
        for (ComponenteDoAcervo filho : filhos) ids.addAll(filho.listarIds());
        return ids;
    }

    /**
     * Recusa (sem alterar a árvore): o próprio grupo ou um ancestral (ciclo); componente que já tem pai
     * (para mudar de lugar: remover + adicionar); livro que já está em outro grupo desta árvore.
     */
    public void adicionar(ComponenteDoAcervo c) {
        if (c == this || ehAncestralDe(c)) {
            throw new HierarquiaInvalidaException("'" + c.getNome() + "' é ancestral de '" + nome + "' (ciclo)");
        }
        if (c.getPai() != null) {
            throw new HierarquiaInvalidaException("'" + c.getNome() + "' já pertence ao grupo '" + c.getPai().getNome() + "'");
        }
        List<Integer> jaNaArvore = raiz().listarIds();
        for (Integer id : c.listarIds()) {
            if (jaNaArvore.contains(id)) {
                throw new HierarquiaInvalidaException("o livro " + id + " já pertence a outro grupo");
            }
        }
        filhos.add(c);
        c.definirPai(this);
    }

    public void remover(ComponenteDoAcervo c) {
        if (filhos.remove(c)) c.definirPai(null);
    }

    /** true se este grupo está "dentro" de c (c é pai, avô...). Adicionar c aqui criaria um ciclo. */
    private boolean ehAncestralDe(ComponenteDoAcervo c) {
        for (GrupoDoAcervo g = getPai(); g != null; g = g.getPai()) {
            if (g == c) return true;
        }
        return false;
    }

    @Override public String getNome() { return nome; }
    public TipoDeGrupo getTipo() { return tipo; }
    public List<ComponenteDoAcervo> getFilhos() { return Collections.unmodifiableList(filhos); }
}
