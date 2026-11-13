package br.pucpr.ib2b.persistencia;

/** Papel GoF: ConcreteProduct da família em memória (perfil TESTE). Só aceita a unidade de trabalho da própria família. */
public final class RepositorioMovimentacaoEmMemoria extends RepositorioMovimentacaoBase {
    public RepositorioMovimentacaoEmMemoria(UnidadeDeTrabalhoEmMemoria uow) { super(uow); }
}
