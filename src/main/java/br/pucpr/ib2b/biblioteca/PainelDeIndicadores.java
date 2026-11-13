package br.pucpr.ib2b.biblioteca;

import java.util.concurrent.atomic.AtomicInteger;

/** Indicadores de devolução (total, atrasadas, dias de atraso acumulados), alimentados pelo ouvinte. */
public class PainelDeIndicadores {

    private final AtomicInteger devolucoes = new AtomicInteger();
    private final AtomicInteger atrasadas = new AtomicInteger();
    private final AtomicInteger diasDeAtraso = new AtomicInteger();

    public void registrar(int diasAtraso) {
        devolucoes.incrementAndGet();
        if (diasAtraso > 0) {
            atrasadas.incrementAndGet();
            diasDeAtraso.addAndGet(diasAtraso);
        }
    }

    public int totalDeDevolucoes() { return devolucoes.get(); }
    public int devolucoesAtrasadas() { return atrasadas.get(); }
    public int totalDeDiasDeAtraso() { return diasDeAtraso.get(); }
}
