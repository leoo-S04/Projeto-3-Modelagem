package br.pucpr.ib2b.comum;

/**
 * O que o passo "apresentar" do Template Method devolve: status, a entidade (nova ou existente) e o motivo
 * da recusa, quando houver. É o que a interface web mostra ao usuário.
 */
public final class ResultadoCadastro<T> {

    private final StatusCadastro status;
    private final T entidade;
    private final String motivo;

    private ResultadoCadastro(StatusCadastro status, T entidade, String motivo) {
        this.status = status;
        this.entidade = entidade;
        this.motivo = motivo;
    }

    public static <T> ResultadoCadastro<T> novo(T entidade)      { return new ResultadoCadastro<>(StatusCadastro.NOVO, entidade, null); }
    public static <T> ResultadoCadastro<T> existente(T entidade) { return new ResultadoCadastro<>(StatusCadastro.EXISTENTE, entidade, null); }
    public static <T> ResultadoCadastro<T> recusado(String motivo) { return new ResultadoCadastro<>(StatusCadastro.RECUSADO, null, motivo); }

    public StatusCadastro getStatus() { return status; }
    public T getEntidade() { return entidade; }
    public String getMotivo() { return motivo; }
    public boolean foiRecusado() { return status == StatusCadastro.RECUSADO; }
}
