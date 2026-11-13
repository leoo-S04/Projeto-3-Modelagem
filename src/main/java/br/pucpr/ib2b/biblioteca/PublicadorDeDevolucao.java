package br.pucpr.ib2b.biblioteca;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Papel GoF: Subject (Req. 5). Mantém os ouvintes registrados e os notifica com o evento.
 * Cada ouvinte é ISOLADO: se um lançar exceção, ela é registrada (fila de reenvio) e não se propaga — os
 * demais continuam sendo notificados e a devolução (já confirmada) não é desfeita.
 */
public class PublicadorDeDevolucao {

    /** Falha registrada para reenvio posterior. */
    public record FalhaDeOuvinte(OuvinteDeDevolucao ouvinte, EventoDevolucao evento, String erro) { }

    private final List<OuvinteDeDevolucao> ouvintes = new CopyOnWriteArrayList<>();
    private final List<FalhaDeOuvinte> filaDeReenvio = new CopyOnWriteArrayList<>();

    public void registrar(OuvinteDeDevolucao o) { ouvintes.add(o); }
    public void remover(OuvinteDeDevolucao o) { ouvintes.remove(o); }

    public void publicar(EventoDevolucao evento) {
        for (OuvinteDeDevolucao o : ouvintes) {
            try {
                o.aoDevolver(evento);
            } catch (RuntimeException erro) {
                registrarFalha(o, evento, erro);
            }
        }
    }

    /** Tenta de novo as notificações que falharam; as que falharem outra vez continuam na fila. */
    public void reenviarFalhas() {
        List<FalhaDeOuvinte> pendentes = new ArrayList<>(filaDeReenvio);
        filaDeReenvio.clear();
        for (FalhaDeOuvinte f : pendentes) {
            try {
                f.ouvinte().aoDevolver(f.evento());
            } catch (RuntimeException erro) {
                registrarFalha(f.ouvinte(), f.evento(), erro);
            }
        }
    }

    public List<FalhaDeOuvinte> falhasPendentes() { return List.copyOf(filaDeReenvio); }
    public List<OuvinteDeDevolucao> ouvintes() { return List.copyOf(ouvintes); }

    private void registrarFalha(OuvinteDeDevolucao o, EventoDevolucao evento, RuntimeException erro) {
        filaDeReenvio.add(new FalhaDeOuvinte(o, evento, o.nome() + ": " + erro.getMessage()));
    }
}
