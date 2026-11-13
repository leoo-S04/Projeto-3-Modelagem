package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.biblioteca.ResultadoSolicitacao;
import br.pucpr.ib2b.financeiro.Multa;

import java.math.BigDecimal;
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
        List<ResultadoSolicitacao> solicitacoes,
        List<Multa> multas,
        List<BoletoJson> cobrancas,
        ConfiguracaoJson configuracao,
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

    /** Boleto registrado no banco em uso (seção "cobrancas"). */
    public record BoletoJson(int idBoleto, int idMulta, String cpf, int diasAtraso, BigDecimal valor,
                             LocalDate dataEmissao, String banco, String idExterno, String situacao,
                             LocalDate dataPagamento, String idPagamento) { }

    /** Configuração administrativa: política de multa vigente e banco em uso. */
    public record ConfiguracaoJson(String politicaVigente, String bancoEmUso) { }

    public record ContadoresJson(int ultimoIdTransacao, int ultimoIdPapel, int ultimoIdMulta, int ultimoIdBoleto) { }
}
