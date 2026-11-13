package br.pucpr.ib2b.biblioteca;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

/**
 * Papel GoF: ConcreteDecorator (Req. 9) — mede o tempo de cada consulta. Colocado POR FORA do cache, mede
 * também as consultas atendidas pelo cache. Falha ao registrar a métrica nunca derruba a consulta.
 */
public class DecoradorDeMedicao extends DecoradorDeConsulta {

    private final RegistroDeMetricas metricas;
    private final Clock relogio;

    public DecoradorDeMedicao(ConsultaAcervo interno, RegistroDeMetricas metricas, Clock relogio) {
        super(interno);
        this.metricas = metricas;
        this.relogio = relogio;
    }

    @Override
    public List<MetadadosLivro> buscarMetadados(Criterio criterio) {
        long inicio = System.nanoTime();
        List<MetadadosLivro> resultado = interno.buscarMetadados(criterio);   // delega (cache ou básica)
        try {
            metricas.registrar(new RegistroDeMetricas.Medicao(criterio.termo(),
                    Duration.ofNanos(System.nanoTime() - inicio), resultado.size(), relogio.instant()));
        } catch (RuntimeException falhaNaMetrica) {
            // a medição é opcional: a consulta continua
        }
        return resultado;
    }
}
