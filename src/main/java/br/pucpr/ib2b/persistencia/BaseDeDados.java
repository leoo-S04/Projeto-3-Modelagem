package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.biblioteca.Devolucao;
import br.pucpr.ib2b.biblioteca.Emprestimo;
import br.pucpr.ib2b.biblioteca.Livro;
import br.pucpr.ib2b.biblioteca.Reserva;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.Pessoa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cópia de trabalho de uma unidade de trabalho: os objetos de domínio que os repositórios da família leem
 * e alteram. Cada seção corresponde a uma seção do arquivo biblioteca.json.
 */
final class BaseDeDados {
    final Map<String, Pessoa> pessoas = new LinkedHashMap<>();
    final Map<String, Estudante> estudantes = new LinkedHashMap<>();
    final Map<String, Atendente> atendentes = new LinkedHashMap<>();
    final Map<Integer, Livro> livros = new LinkedHashMap<>();
    final List<Reserva> reservas = new ArrayList<>();
    final List<Emprestimo> emprestimos = new ArrayList<>();
    final List<Devolucao> devolucoes = new ArrayList<>();
    int ultimoIdTransacao = 0;
    int ultimoIdPapel = 0;
}
