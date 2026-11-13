package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.biblioteca.MontadorDeConsulta.ConfigInstalacao;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 9 — Decorator: cenários de verificação do enunciado. */
class R09_ConsultaComDecoradoresTest {

    private final Cenario c = new Cenario();                      // livro 1 "Dom Casmurro"
    private final RegistroDeMetricas metricas = new RegistroDeMetricas();

    /** Conta quantas vezes a implementação básica foi realmente chamada. */
    private final AtomicInteger chamadasABasica = new AtomicInteger();
    private final ConsultaAcervo basicaContada = criterio -> {
        chamadasABasica.incrementAndGet();
        return new ConsultaAcervoBasica(c.acervo()).buscarMetadados(criterio);
    };

    @Test
    void R09_segundaConsultaDeMetadadosUsaCacheEAindaGeraMedicao() {
        // Medicao(Cache(Basica)): medição por fora
        ConsultaAcervo consulta = MontadorDeConsulta.montar(ConfigInstalacao.completa(), basicaContada,
                ArmazenamentoDeCache.emMemoria(), metricas, c.relogio);

        List<MetadadosLivro> primeira = consulta.buscarMetadados(new Criterio("Dom Casmurro"));
        List<MetadadosLivro> segunda = consulta.buscarMetadados(new Criterio("  dom casmurro "));   // mesma chave

        assertEquals(List.of(new MetadadosLivro(1, "Dom Casmurro")), primeira);
        assertEquals(primeira, segunda);
        assertEquals(1, chamadasABasica.get());                  // a 2ª veio do cache
        assertEquals(2, metricas.medicoes().size());             // e mesmo assim foi medida
        assertInstanceOf(DecoradorDeMedicao.class, consulta);
    }

    @Test
    void R09_cacheAntigoNuncaAutorizaReservarLivroQueDeixouDeEstarLivre() {
        // a consulta pública (com cache) vê o livro 1
        assertEquals(1, c.tratadorDeAcervo.buscarMetadados("dom").size());
        // o livro deixa de estar LIVRE (Ana reserva e leva)
        c.emprestarLivro1();
        // o cache ainda devolve os metadados antigos...
        assertEquals(1, c.tratadorDeAcervo.buscarMetadados("dom").size());
        // ...mas eles NÃO têm situação (só id e título): não servem para decidir reserva
        assertEquals(List.of("idLivro", "titulo"),
                Arrays.stream(MetadadosLivro.class.getRecordComponents()).map(rc -> rc.getName()).toList());
        // a reserva consulta o estado atual do Livro (Acervo → State) e é recusada
        c.pessoas.inserirPessoa("333", "Caio", "Reis", "caio@pucpr.br");
        c.estudantes.inserirEstudante("333", "M-300");
        var resultado = c.tratadorDeEmprestimo.reservarLivro(1, "M-300", "F-01");
        assertEquals(StatusSolicitacao.RECUSADA, resultado.status());
        assertTrue(resultado.mensagem().contains("EMPRESTADO"));
    }

    @Test
    void falhaDoCacheNaoDerrubaAConsulta() {
        ArmazenamentoDeCache quebrado = new ArmazenamentoDeCache() {
            public Entrada obter(Criterio criterio) { throw new IllegalStateException("cache fora do ar"); }
            public void guardar(Criterio criterio, Entrada e) { throw new IllegalStateException("cache fora do ar"); }
        };
        DecoradorDeCache cache = new DecoradorDeCache(basicaContada, quebrado, Duration.ofMinutes(5), c.relogio);
        ConsultaAcervo consulta = new DecoradorDeMedicao(cache, metricas, c.relogio);

        assertEquals(1, consulta.buscarMetadados(new Criterio("dom")).size());
        assertEquals(1, consulta.buscarMetadados(new Criterio("dom")).size());
        assertEquals(2, chamadasABasica.get());                  // sem cache, mas funcionando
        assertEquals(2, cache.falhasDoCache());
        assertEquals(2, metricas.medicoes().size());
    }

    @Test
    void entradaExpiradaVoltaAConsultarABasica() {
        DecoradorDeCache cache = new DecoradorDeCache(basicaContada, ArmazenamentoDeCache.emMemoria(),
                Duration.ofMinutes(5), c.relogio);
        cache.buscarMetadados(new Criterio("dom"));
        c.relogio.avancarDias(1);                                // passou do TTL
        cache.buscarMetadados(new Criterio("dom"));
        assertEquals(2, chamadasABasica.get());
        assertEquals(0, cache.acertos());
    }

    @Test
    void cadaInstalacaoCombinaOsRecursos() {
        ConsultaAcervo soMedicao = MontadorDeConsulta.montar(new ConfigInstalacao(true, false, Duration.ZERO),
                basicaContada, ArmazenamentoDeCache.emMemoria(), metricas, c.relogio);
        ConsultaAcervo nenhum = MontadorDeConsulta.montar(new ConfigInstalacao(false, false, Duration.ZERO),
                basicaContada, ArmazenamentoDeCache.emMemoria(), metricas, c.relogio);

        assertInstanceOf(DecoradorDeMedicao.class, soMedicao);
        assertSame(basicaContada, nenhum);                       // implementação original, sem decoradores
    }
}
