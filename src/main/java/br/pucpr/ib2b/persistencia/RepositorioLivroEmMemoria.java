package br.pucpr.ib2b.persistencia;

/** Papel GoF: ConcreteProduct da família em memória (perfil TESTE). Só aceita a unidade de trabalho da própria família. */
public final class RepositorioLivroEmMemoria extends RepositorioLivroBase {
    public RepositorioLivroEmMemoria(UnidadeDeTrabalhoEmMemoria uow) { super(uow); }
}
