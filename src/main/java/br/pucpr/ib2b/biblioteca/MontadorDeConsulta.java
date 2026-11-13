package br.pucpr.ib2b.biblioteca;

import java.time.Clock;
import java.time.Duration;

/**
 * Monta a cadeia de decoradores conforme a instalação (Req. 9). A ordem importa: a medição fica POR FORA do
 * cache, para medir também as consultas atendidas por ele: Medicao(Cache(Basica)).
 */
public final class MontadorDeConsulta {

    /** O que cada instalação quer: medição, cache e validade do cache. */
    public record ConfigInstalacao(boolean medicao, boolean cache, Duration ttl) {
        public static ConfigInstalacao completa() { return new ConfigInstalacao(true, true, Duration.ofMinutes(5)); }
    }

    private MontadorDeConsulta() { }

    public static ConsultaAcervo montar(ConfigInstalacao config, ConsultaAcervo basica, ArmazenamentoDeCache armazenamento,
                                        RegistroDeMetricas metricas, Clock relogio) {
        ConsultaAcervo cadeia = basica;
        if (config.cache()) cadeia = new DecoradorDeCache(cadeia, armazenamento, config.ttl(), relogio);
        if (config.medicao()) cadeia = new DecoradorDeMedicao(cadeia, metricas, relogio);
        return cadeia;
    }
}
