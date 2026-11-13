package br.pucpr.ib2b.comum;

import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;

/**
 * Papel GoF: AbstractClass do Template Method (Req. 7).
 * {@link #inserir} é o método-modelo: {@code final}, fixa a ordem dos passos para os 4 cadastros
 * (Livro, Pessoa, Estudante, Atendente). As subclasses só implementam os passos que variam.
 *
 * @param <T> entidade cadastrada (Livro, Pessoa, Estudante, Atendente)
 * @param <D> dados de entrada do formulário (DadosLivro, DadosPessoa...)
 */
public abstract class TratadorDeCadastro<T, D> {

    protected final UnidadeDeTrabalho transacao;

    protected TratadorDeCadastro(UnidadeDeTrabalho transacao) {
        this.transacao = transacao;
    }

    /** Método-modelo: buscar existente → validar → verificar pré-requisitos → criar → armazenar → apresentar. */
    public final ResultadoCadastro<T> inserir(D dados) {
        T existente = buscarExistente(dados);                     // 1. já cadastrado? devolve o existente
        if (existente != null) return apresentar(existente, false);
        try {
            validar(dados);                                       // 2. campos obrigatórios e formato
            verificarPreRequisitos(dados);                        // 3. gancho (ex.: pessoa já cadastrada)
        } catch (CadastroRecusadoException e) {
            return ResultadoCadastro.recusado(e.getMessage());    //    recusa ANTES de qualquer gravação
        }
        T novo = criar(dados);                                    // 4. monta o objeto
        transacao.executar(() -> armazenar(novo));                // 5. grava e confirma (unidade de trabalho)
        return apresentar(novo, true);                            // 6. resultado para a tela
    }

    // ---- Operações primitivas: cada cadastro implementa ----
    protected abstract T buscarExistente(D dados);
    protected abstract void validar(D dados);
    protected abstract T criar(D dados);
    protected abstract void armazenar(T entidade);

    /** Gancho (hook): vazio por padrão; Estudante e Atendente exigem a pessoa cadastrada. */
    protected void verificarPreRequisitos(D dados) { }

    /** Passo comum: monta o resultado mostrado ao usuário. */
    protected ResultadoCadastro<T> apresentar(T entidade, boolean novo) {
        return novo ? ResultadoCadastro.novo(entidade) : ResultadoCadastro.existente(entidade);
    }

    /** Auxiliar para os passos validar / verificarPreRequisitos. */
    protected static void exigir(boolean condicao, String motivo) {
        if (!condicao) throw new CadastroRecusadoException(motivo);
    }

    protected static boolean preenchido(String s) { return s != null && !s.isBlank(); }
}
