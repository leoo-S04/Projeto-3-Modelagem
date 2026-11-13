package br.pucpr.ib2b.biblioteca;

import java.util.List;

/** Fotografia (DTO) de um grupo da organização do acervo, para a tela exibir a árvore. */
public record NoDoAcervoDTO(String nome, TipoDeGrupo tipo, int totalDeLivros, List<Integer> idsDiretos,
                            List<NoDoAcervoDTO> subgrupos) {

    static NoDoAcervoDTO de(GrupoDoAcervo g) {
        List<Integer> ids = g.getFilhos().stream()
                .filter(c -> c instanceof ItemDoAcervo).map(c -> ((ItemDoAcervo) c).getIdLivro()).toList();
        List<NoDoAcervoDTO> sub = g.getFilhos().stream()
                .filter(c -> c instanceof GrupoDoAcervo).map(c -> de((GrupoDoAcervo) c)).toList();
        return new NoDoAcervoDTO(g.getNome(), g.getTipo(), g.contarLivros(), ids, sub);
    }
}
