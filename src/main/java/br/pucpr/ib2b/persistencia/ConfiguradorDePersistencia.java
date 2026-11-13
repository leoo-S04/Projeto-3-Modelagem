package br.pucpr.ib2b.persistencia;

import java.nio.file.Path;

/**
 * Ponto ÚNICO onde a família de persistência é escolhida, pelo perfil de execução (Req. 8).
 * Chamado uma vez, na montagem do sistema; daí em diante ninguém sabe qual família está em uso.
 * Não usa recursos do Spring (@Profile): a escolha é explícita, aqui.
 */
public final class ConfiguradorDePersistencia {

    private ConfiguradorDePersistencia() { }

    /**
     * @param perfil      TESTE (memória) ou PRODUCAO (arquivo JSON)
     * @param arquivoJson caminho do arquivo da família JSON (ignorado no perfil TESTE)
     */
    public static FabricaDePersistencia fabricaPara(Perfil perfil, Path arquivoJson) {
        if (perfil == null) {
            // Perfil ausente: interrompe em vez de escolher uma família por omissão (Soluções, Problema 8)
            throw new IllegalArgumentException("Perfil de persistência não informado (use TESTE ou PRODUCAO)");
        }
        return switch (perfil) {
            case TESTE -> new FabricaEmMemoria();
            case PRODUCAO -> new FabricaJson(arquivoJson);
        };
    }
}
