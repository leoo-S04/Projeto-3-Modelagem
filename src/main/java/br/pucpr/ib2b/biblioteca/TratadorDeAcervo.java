package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.comum.ResultadoCadastro;
import br.pucpr.ib2b.comum.TratadorDeCadastro;
import br.pucpr.ib2b.persistencia.UnidadeDeTrabalho;

/**
 * Papel GoF: ConcreteClass do Template Method (Req. 7). Regras 1.1 a 1.4 (manter Livro), delegando ao Acervo.
 * Livro não tem pré-requisito: usa o gancho vazio herdado.
 */
public class TratadorDeAcervo extends TratadorDeCadastro<Livro, DadosLivro> implements iLivro {

    private final Acervo acervo;

    public TratadorDeAcervo(Acervo acervo, UnidadeDeTrabalho transacao) {
        super(transacao);
        this.acervo = acervo;
    }

    /** 1.1: preserva a operação do Astah; delega ao método-modelo. O livro novo nasce LIVRE. */
    @Override
    public ResultadoCadastro<Livro> inserirLivro(int idLivro, String titulo) {
        return inserir(new DadosLivro(idLivro, titulo));
    }

    // ---- Passos variáveis do Template Method ----
    @Override protected Livro buscarExistente(DadosLivro d) { return acervo.buscarPorId(d.idLivro()); }

    @Override protected void validar(DadosLivro d) {
        exigir(d.idLivro() > 0, "id do livro deve ser positivo");
        exigir(preenchido(d.titulo()), "título é obrigatório");
    }

    @Override protected Livro criar(DadosLivro d) { return new Livro(d.idLivro(), d.titulo()); }

    @Override protected void armazenar(Livro l) { acervo.adicionar(l); }

    // ---- Demais operações de iLivro ----
    /** 1.3: devolve o livro, ou {@code null} se não cadastrado. */
    @Override
    public Livro consultarLivro(int idLivro) { return acervo.buscarPorId(idLivro); }

    /** 1.2: só atualiza se cadastrado. */
    @Override
    public boolean atualizarLivro(int idLivro, String titulo) {
        return transacao.executar(() -> {
            Livro l = acervo.buscarPorId(idLivro);
            if (l == null) return false;
            l.setTitulo(titulo);
            acervo.atualizar(l);
            return true;
        });
    }

    /** 1.4 + T11: só exclui se cadastrado; o estado aceita a exclusão apenas em LIVRE ou INCINERADO. */
    @Override
    public boolean excluirLivro(int idLivro) {
        return transacao.executar(() -> {
            Livro l = acervo.buscarPorId(idLivro);
            if (l == null) return false;
            l.excluirLivro();                 // OperacaoInvalidaException nos demais estados
            return acervo.remover(idLivro);
        });
    }
}
