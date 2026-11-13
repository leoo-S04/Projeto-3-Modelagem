package br.pucpr.ib2b.persistencia;

import java.time.LocalDate;
import java.util.List;

/**
 * Formato do arquivo biblioteca.json (uma seção por tipo de dado). Só dados simples: as referências entre
 * objetos são gravadas pelos identificadores (CPF, matrícula, id do livro) e a situação do livro como texto.
 */
public record DocumentoDados(
        List<PessoaJson> pessoas,
        List<EstudanteJson> estudantes,
        List<AtendenteJson> atendentes,
        List<LivroJson> livros,
        List<TransacaoJson> reservas,
        List<TransacaoJson> emprestimos,
        List<DevolucaoJson> devolucoes,
        ContadoresJson contadores) {

    public record PessoaJson(String cpf, String nome, String sobrenome, String email) { }

    public record EstudanteJson(int id, String matricula, String cpf) { }

    public record AtendenteJson(int id, String matriculaFuncional, String cpf) { }

    /** "situacao": "DEVOLVIDO" — reconstituída com EstadoLivro.para(Situacao) na leitura. */
    public record LivroJson(int id, String titulo, String situacao, String reservante, LocalDate vencimento,
                     int numRenovacoes) { }

    public record TransacaoJson(int id, LocalDate data, int idLivro, String matriculaEstudante,
                         String matriculaAtendente) { }

    public record DevolucaoJson(int id, LocalDate data, int idLivro, String matriculaEstudante, String matriculaAtendente,
                         LocalDate dataDevolucao, LocalDate vencimento, TransacaoJson emprestimo) { }

    public record ContadoresJson(int ultimoIdTransacao, int ultimoIdPapel) { }
}
