package br.pucpr.ib2b.persistencia;

/** Papel GoF: ConcreteProduct da família JSON (perfil PRODUCAO). Só aceita a unidade de trabalho da própria família. */
public final class RepositorioFinanceiroJson extends RepositorioFinanceiroBase {
    public RepositorioFinanceiroJson(UnidadeDeTrabalhoJson uow) { super(uow); }
}
