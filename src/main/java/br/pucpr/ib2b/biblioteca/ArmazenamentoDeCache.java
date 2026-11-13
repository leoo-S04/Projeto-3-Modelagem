package br.pucpr.ib2b.biblioteca;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Onde o cache guarda as entradas. Pode falhar (ex.: cache externo fora do ar); o decorador trata. */
public interface ArmazenamentoDeCache {

    record Entrada(List<MetadadosLivro> metadados, Instant expiraEm) { }

    Entrada obter(Criterio criterio);
    void guardar(Criterio criterio, Entrada entrada);

    /** Implementação padrão, em memória. */
    static ArmazenamentoDeCache emMemoria() {
        Map<Criterio, Entrada> mapa = new ConcurrentHashMap<>();
        return new ArmazenamentoDeCache() {
            @Override public Entrada obter(Criterio c) { return mapa.get(c); }
            @Override public void guardar(Criterio c, Entrada e) { mapa.put(c, e); }
        };
    }
}
