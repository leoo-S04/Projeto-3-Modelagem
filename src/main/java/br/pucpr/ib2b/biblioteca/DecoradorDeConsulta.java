package br.pucpr.ib2b.biblioteca;

import java.util.List;

/**
 * Papel GoF: Decorator (Req. 9). Realiza o mesmo contrato e guarda uma referência ao componente "interno"
 * (a básica ou outro decorador). Por padrão só delega; cada decorador concreto acrescenta algo antes/depois.
 */
public abstract class DecoradorDeConsulta implements ConsultaAcervo {

    protected final ConsultaAcervo interno;

    protected DecoradorDeConsulta(ConsultaAcervo interno) { this.interno = interno; }

    @Override
    public List<MetadadosLivro> buscarMetadados(Criterio criterio) { return interno.buscarMetadados(criterio); }
}
