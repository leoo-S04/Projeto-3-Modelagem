package br.pucpr.ib2b.persistencia;

/** Papel GoF: ConcreteProduct da família em memória (perfil TESTE). Só aceita a unidade de trabalho da própria família. */
public final class RepositorioRelacionamentoEmMemoria extends RepositorioRelacionamentoBase {
    public RepositorioRelacionamentoEmMemoria(UnidadeDeTrabalhoEmMemoria uow) { super(uow); }
}
