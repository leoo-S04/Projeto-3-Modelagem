package br.pucpr.ib2b.biblioteca;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Livros devolvidos aguardando inspeção. A inspeção apenas é AGENDADA aqui; quem decide (bom estado ou
 * reparo) é o atendente, pela operação inspecionar (o Livro continua DEVOLVIDO até lá).
 */
public class AgendaDeInspecao {

    public record ItemDeInspecao(int idLivro, String titulo, LocalDate devolvidoEm, int diasAtraso) { }

    private final Map<Integer, ItemDeInspecao> pendentes = new ConcurrentHashMap<>();

    public void agendar(ItemDeInspecao item) { pendentes.put(item.idLivro(), item); }
    public void concluir(int idLivro) { pendentes.remove(idLivro); }
    public boolean estaAgendado(int idLivro) { return pendentes.containsKey(idLivro); }
    public List<ItemDeInspecao> pendentes() { return new ArrayList<>(pendentes.values()); }
}
