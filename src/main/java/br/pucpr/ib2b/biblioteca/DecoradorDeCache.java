package br.pucpr.ib2b.biblioteca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Papel GoF: ConcreteDecorator (Req. 9) — guarda METADADOS (id e título) por um tempo (TTL). Se o cache falhar,
 * registra e delega ao interno: a consulta continua funcionando, só que sem cache.
 */
public class DecoradorDeCache extends DecoradorDeConsulta {

    private final ArmazenamentoDeCache armazenamento;
    private final Duration ttl;
    private final Clock relogio;
    private final AtomicInteger acertos = new AtomicInteger();
    private final AtomicInteger falhasDoCache = new AtomicInteger();

    public DecoradorDeCache(ConsultaAcervo interno, ArmazenamentoDeCache armazenamento, Duration ttl, Clock relogio) {
        super(interno);
        this.armazenamento = armazenamento;
        this.ttl = ttl;
        this.relogio = relogio;
    }

    @Override
    public List<MetadadosLivro> buscarMetadados(Criterio criterio) {
        Instant agora = relogio.instant();
        try {
            ArmazenamentoDeCache.Entrada e = armazenamento.obter(criterio);
            if (e != null && agora.isBefore(e.expiraEm())) {
                acertos.incrementAndGet();
                return e.metadados();                                   // atendida pelo cache
            }
        } catch (RuntimeException falha) {
            falhasDoCache.incrementAndGet();
            return interno.buscarMetadados(criterio);                   // sem cache, mas funciona
        }
        List<MetadadosLivro> resultado = interno.buscarMetadados(criterio);
        try {
            armazenamento.guardar(criterio, new ArmazenamentoDeCache.Entrada(resultado, agora.plus(ttl)));
        } catch (RuntimeException falha) {
            falhasDoCache.incrementAndGet();
        }
        return resultado;
    }

    public int acertos() { return acertos.get(); }
    public int falhasDoCache() { return falhasDoCache.get(); }
}
