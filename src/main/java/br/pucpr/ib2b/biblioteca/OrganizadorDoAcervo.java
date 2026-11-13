package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.persistencia.RepositorioDeOrganizacao;

import java.util.List;

/**
 * Papel GoF: Client do Composite (Req. 10). Navega a partir das unidades (raízes), cria grupos, coloca livros
 * e MOVE itens entre grupos. Mover altera só o pai do item: o Livro e a Movimentacao não recebem nenhuma
 * mensagem. Cada alteração é uma transação; se adicionar no destino falhar, o item volta à origem.
 */
public class OrganizadorDoAcervo {

    private final RepositorioDeOrganizacao repositorio;
    private final Acervo acervo;

    public OrganizadorDoAcervo(RepositorioDeOrganizacao repositorio, Acervo acervo) {
        this.repositorio = repositorio;
        this.acervo = acervo;
    }

    public void criarUnidade(String nome) {
        repositorio.unidade().executar(() -> {
            exigirNomeLivre(nome);
            repositorio.adicionarUnidade(new GrupoDoAcervo(nome, TipoDeGrupo.UNIDADE));
        });
    }

    public void criarGrupo(String nomeDoPai, String nome, TipoDeGrupo tipo) {
        repositorio.unidade().executar(() -> {
            exigirNomeLivre(nome);
            grupo(nomeDoPai).adicionar(new GrupoDoAcervo(nome, tipo));
        });
    }

    /** Coloca um livro do Acervo em um grupo; recusa livro inexistente ou que já está em algum grupo. */
    public void adicionarLivro(String nomeDoGrupo, int idLivro) {
        repositorio.unidade().executar(() -> {
            if (acervo.buscarPorId(idLivro) == null) throw new HierarquiaInvalidaException("livro " + idLivro + " não existe no acervo");
            ItemDoAcervo jaColocado = localizarItem(idLivro);
            if (jaColocado != null) {
                throw new HierarquiaInvalidaException("o livro " + idLivro + " já pertence ao grupo '" + jaColocado.getPai().getNome() + "'");
            }
            grupo(nomeDoGrupo).adicionar(new ItemDoAcervo(idLivro));
        });
    }

    /** mover = remover da origem + adicionar no destino, na mesma transação. Só o pai do item muda. */
    public void mover(int idLivro, String nomeDoDestino) {
        repositorio.unidade().executar(() -> {
            ItemDoAcervo item = localizarItem(idLivro);
            if (item == null) throw new HierarquiaInvalidaException("o livro " + idLivro + " não está em nenhum grupo");
            GrupoDoAcervo destino = grupo(nomeDoDestino);
            GrupoDoAcervo origem = item.getPai();
            origem.remover(item);
            try {
                destino.adicionar(item);
            } catch (HierarquiaInvalidaException e) {
                origem.adicionar(item);           // volta à origem
                throw e;
            }
        });
    }

    /** Contagem uniforme: funciona igual para unidade, seção ou subseção. */
    public int contarLivros(String nomeDoGrupo) { return grupo(nomeDoGrupo).contarLivros(); }

    public List<Integer> listarIds(String nomeDoGrupo) { return grupo(nomeDoGrupo).listarIds(); }

    public List<NoDoAcervoDTO> estrutura() {
        return repositorio.unidades().stream().map(NoDoAcervoDTO::de).toList();
    }

    // ---- auxiliares ----

    private GrupoDoAcervo grupo(String nome) {
        GrupoDoAcervo g = buscarGrupo(nome);
        if (g == null) throw new HierarquiaInvalidaException("grupo '" + nome + "' não existe");
        return g;
    }

    private void exigirNomeLivre(String nome) {
        if (nome == null || nome.isBlank()) throw new HierarquiaInvalidaException("informe o nome do grupo");
        if (buscarGrupo(nome) != null) throw new HierarquiaInvalidaException("já existe um grupo '" + nome + "'");
    }

    private GrupoDoAcervo buscarGrupo(String nome) {
        for (GrupoDoAcervo unidade : repositorio.unidades()) {
            GrupoDoAcervo g = buscarGrupo(unidade, nome);
            if (g != null) return g;
        }
        return null;
    }

    private GrupoDoAcervo buscarGrupo(GrupoDoAcervo g, String nome) {
        if (g.getNome().equals(nome)) return g;
        for (ComponenteDoAcervo filho : g.getFilhos()) {
            if (filho instanceof GrupoDoAcervo sub) {
                GrupoDoAcervo achado = buscarGrupo(sub, nome);
                if (achado != null) return achado;
            }
        }
        return null;
    }

    private ItemDoAcervo localizarItem(int idLivro) {
        for (GrupoDoAcervo unidade : repositorio.unidades()) {
            ItemDoAcervo i = localizarItem(unidade, idLivro);
            if (i != null) return i;
        }
        return null;
    }

    private ItemDoAcervo localizarItem(GrupoDoAcervo g, int idLivro) {
        for (ComponenteDoAcervo filho : g.getFilhos()) {
            if (filho instanceof ItemDoAcervo item && item.getIdLivro() == idLivro) return item;
            if (filho instanceof GrupoDoAcervo sub) {
                ItemDoAcervo achado = localizarItem(sub, idLivro);
                if (achado != null) return achado;
            }
        }
        return null;
    }
}
