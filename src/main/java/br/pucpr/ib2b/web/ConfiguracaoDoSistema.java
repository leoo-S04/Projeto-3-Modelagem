package br.pucpr.ib2b.web;

import br.pucpr.ib2b.Sistema;
import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.RegistroDeMetricas;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import br.pucpr.ib2b.comum.ServicoDeEmailSimulado;
import br.pucpr.ib2b.financeiro.AdaptadorBancoA;
import br.pucpr.ib2b.financeiro.AdaptadorBancoB;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoA;
import br.pucpr.ib2b.financeiro.bancos.ApiBancoB;
import br.pucpr.ib2b.persistencia.ConfiguradorDePersistencia;
import br.pucpr.ib2b.persistencia.Perfil;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;

/**
 * Liga a camada web ao sistema. A família de persistência é escolhida EXPLICITAMENTE pelo nosso
 * ConfiguradorDePersistencia (Abstract Factory, Req. 8), a partir da propriedade biblioteca.perfil — o Spring
 * não substitui o padrão (nada de @Profile). Para a web, só são publicados os tratadores e as ferramentas da
 * demonstração (relógio, e-mail simulado, métricas, simulador dos bancos externos).
 */
@Configuration
public class ConfiguracaoDoSistema {

    @Bean
    public RelogioAjustavel relogio() {
        return new RelogioAjustavel(Clock.system(ZoneId.of("America/Sao_Paulo")));
    }

    @Bean
    public Sistema sistema(@Value("${biblioteca.perfil}") String perfil,
                           @Value("${biblioteca.arquivo}") String arquivo,
                           @Value("${biblioteca.exemplo:}") String exemplo,
                           RelogioAjustavel relogio) {
        Perfil escolhido = Perfil.valueOf(perfil.trim().toUpperCase());
        Path destino = Path.of(arquivo);
        if (escolhido == Perfil.PRODUCAO) copiarExemploSeNaoHouverDados(destino, exemplo);
        return Sistema.montar(ConfiguradorDePersistencia.fabricaPara(escolhido, destino), relogio);
    }

    /** Primeira execução: começa com os dados de demonstração (dados/exemplo.json), se existirem. */
    private static void copiarExemploSeNaoHouverDados(Path destino, String exemplo) {
        if (Files.exists(destino) || exemplo == null || exemplo.isBlank() || !Files.exists(Path.of(exemplo))) return;
        try {
            if (destino.toAbsolutePath().getParent() != null) Files.createDirectories(destino.toAbsolutePath().getParent());
            Files.copy(Path.of(exemplo), destino);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível copiar " + exemplo + " para " + destino, e);
        }
    }

    // ---- Tratadores: a única porta da web para o domínio ----
    @Bean public TratadorDePessoa tratadorDePessoa(Sistema s) { return s.tratadorDePessoa; }
    @Bean public TratadorDeEstudante tratadorDeEstudante(Sistema s) { return s.tratadorDeEstudante; }
    @Bean public TratadorDeAtendente tratadorDeAtendente(Sistema s) { return s.tratadorDeAtendente; }
    @Bean public TratadorDeAcervo tratadorDeAcervo(Sistema s) { return s.tratadorDeAcervo; }
    @Bean public TratadorDeEmprestimo tratadorDeEmprestimo(Sistema s) { return s.tratadorDeEmprestimo; }
    @Bean public TratadorDeDevolucao tratadorDeDevolucao(Sistema s) { return s.tratadorDeDevolucao; }
    @Bean public TratadorDeMulta tratadorDeMulta(Sistema s) { return s.tratadorDeMulta; }

    // ---- Ferramentas da demonstração (não são regras do domínio) ----
    @Bean public ServicoDeEmailSimulado email(Sistema s) { return s.email(); }
    @Bean public RegistroDeMetricas registroDeMetricas(Sistema s) { return s.registroDeMetricas(); }

    // ---- Bancos externos (simulados): APIs e pontos de entrada das notificações ----
    @Bean public ApiBancoA apiBancoA(Sistema s) { return s.apiBancoA(); }
    @Bean public ApiBancoB apiBancoB(Sistema s) { return s.apiBancoB(); }
    @Bean public AdaptadorBancoA adaptadorBancoA(Sistema s) { return s.adaptadorBancoA(); }
    @Bean public AdaptadorBancoB adaptadorBancoB(Sistema s) { return s.adaptadorBancoB(); }
}
