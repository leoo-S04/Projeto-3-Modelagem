package br.pucpr.ib2b.persistencia;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Papel GoF: ConcreteFactory da família JSON (perfil PRODUCAO). Todos os produtos usam o mesmo arquivo
 * biblioteca.json, por meio da mesma unidade de trabalho.
 */
public final class FabricaJson implements FabricaDePersistencia {

    private final Path arquivo;

    public FabricaJson(Path arquivo) {
        this.arquivo = Objects.requireNonNull(arquivo, "Informe o caminho do arquivo JSON (ex.: dados/biblioteca.json)");
    }

    @Override
    public UnidadeDeTrabalho criarUnidadeDeTrabalho() { return new UnidadeDeTrabalhoJson(arquivo); }

    @Override
    public RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho uow) {
        return new RepositorioLivroJson(daFamilia(uow));
    }

    @Override
    public RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho uow) {
        return new RepositorioMovimentacaoJson(daFamilia(uow));
    }

    @Override
    public RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho uow) {
        return new RepositorioRelacionamentoJson(daFamilia(uow));
    }

    /** Impede misturar famílias: só aceita a unidade de trabalho JSON. */
    private UnidadeDeTrabalhoJson daFamilia(UnidadeDeTrabalho uow) {
        if (uow instanceof UnidadeDeTrabalhoJson daMesmaFamilia) return daMesmaFamilia;
        throw new FamiliaIncompativelException("FabricaJson", uow);
    }
}
