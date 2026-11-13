package br.pucpr.ib2b.biblioteca;

import java.util.List;

/**
 * Papel GoF: Component do Decorator (Req. 9). Contrato único da consulta pública de metadados do acervo,
 * igual para a implementação básica e para qualquer combinação de decoradores.
 */
public interface ConsultaAcervo {
    List<MetadadosLivro> buscarMetadados(Criterio criterio);
}
