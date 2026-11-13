package br.pucpr.ib2b.biblioteca;

import java.util.List;

/** Papel GoF: ConcreteComponent (Decorator, Req. 9). A implementação original: lê o Acervo. Não é alterada. */
public class ConsultaAcervoBasica implements ConsultaAcervo {

    private final Acervo acervo;

    public ConsultaAcervoBasica(Acervo acervo) { this.acervo = acervo; }

    @Override
    public List<MetadadosLivro> buscarMetadados(Criterio criterio) {
        return acervo.listar().stream()
                .filter(l -> l.getTitulo().toLowerCase().contains(criterio.termo()))
                .map(l -> new MetadadosLivro(l.getIdLivro(), l.getTitulo()))
                .toList();
    }
}
