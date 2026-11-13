package br.pucpr.ib2b.persistencia;

/** Papel GoF: ConcreteFactory da família em memória (perfil TESTE). Só cria produtos em memória. */
public final class FabricaEmMemoria implements FabricaDePersistencia {

    @Override
    public UnidadeDeTrabalho criarUnidadeDeTrabalho() { return new UnidadeDeTrabalhoEmMemoria(); }

    @Override
    public RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho uow) {
        return new RepositorioLivroEmMemoria(daFamilia(uow));
    }

    @Override
    public RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho uow) {
        return new RepositorioMovimentacaoEmMemoria(daFamilia(uow));
    }

    @Override
    public RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho uow) {
        return new RepositorioRelacionamentoEmMemoria(daFamilia(uow));
    }

    /** Impede misturar famílias: só aceita a unidade de trabalho em memória. */
    private UnidadeDeTrabalhoEmMemoria daFamilia(UnidadeDeTrabalho uow) {
        if (uow instanceof UnidadeDeTrabalhoEmMemoria daMesmaFamilia) return daMesmaFamilia;
        throw new FamiliaIncompativelException("FabricaEmMemoria", uow);
    }
}
