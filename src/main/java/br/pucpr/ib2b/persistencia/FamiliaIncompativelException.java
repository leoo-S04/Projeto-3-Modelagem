package br.pucpr.ib2b.persistencia;

/** Lançada quando se tenta criar um repositório de uma família com a unidade de trabalho de outra. */
public class FamiliaIncompativelException extends RuntimeException {
    public FamiliaIncompativelException(String fabrica, UnidadeDeTrabalho uow) {
        super(fabrica + " não aceita a unidade de trabalho " + uow.getClass().getSimpleName()
                + ": não é permitido misturar famílias de persistência");
    }
}
