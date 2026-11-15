package br.pucpr.ib2b.biblioteca;

import java.util.List;

/**
 * Resposta da fachada para "consultar empréstimos": uma cópia somente leitura (a fachada não expõe as coleções
 * internas da Movimentacao). Mais recentes primeiro.
 */
public record HistoricoDeEmprestimos(List<EmprestimoDTO> todos) {

    public HistoricoDeEmprestimos {
        todos = List.copyOf(todos);
    }

    public List<EmprestimoDTO> emAndamento() { return todos.stream().filter(EmprestimoDTO::emAndamento).toList(); }

    public List<EmprestimoDTO> devolvidos() { return todos.stream().filter(e -> !e.emAndamento()).toList(); }

    /** Um empréstimo pelo código (ou nulo). */
    public EmprestimoDTO buscar(int idEmprestimo) {
        return todos.stream().filter(e -> e.idEmprestimo() == idEmprestimo).findFirst().orElse(null);
    }
}
