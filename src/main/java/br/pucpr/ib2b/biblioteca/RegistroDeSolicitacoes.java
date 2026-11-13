package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.persistencia.RepositorioDeSolicitacoes;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apoio do Invoker (Command, Req. 6): garante que o mesmo identificador não seja executado duas vezes.
 * - reservarExecucao(id) é ATÔMICA (ConcurrentHashMap.putIfAbsent): entre duas submissões simultâneas do mesmo
 *   id, só uma "vence"; a outra recebe false.
 * - Os resultados concluídos são gravados no repositório (seção "solicitacoes" do JSON), para que um reenvio
 *   depois de reiniciar a aplicação também não execute de novo.
 */
public class RegistroDeSolicitacoes {

    private final RepositorioDeSolicitacoes repositorio;
    /** Ids já reservados nesta execução da aplicação. Nunca são removidos após concluir (evita corrida). */
    private final ConcurrentHashMap<UUID, Boolean> reservados = new ConcurrentHashMap<>();

    public RegistroDeSolicitacoes(RepositorioDeSolicitacoes repositorio) { this.repositorio = repositorio; }

    /** true = este chamador pode executar; false = id já executado ou em execução. */
    public boolean reservarExecucao(UUID id) {
        if (repositorio.buscar(id) != null) return false;           // já concluída (inclusive antes de reiniciar)
        return reservados.putIfAbsent(id, Boolean.TRUE) == null;    // inserção atômica com chave única
    }

    /** Grava o resultado (EXECUTADA ou RECUSADA), que será reaproveitado nas repetições. */
    public void concluir(ResultadoSolicitacao resultado) {
        repositorio.unidade().executar(() -> repositorio.salvar(resultado));
    }

    /** Falha técnica (não de regra): libera o id para uma nova tentativa. */
    public void liberar(UUID id) { reservados.remove(id); }

    public Optional<ResultadoSolicitacao> resultadoDe(UUID id) { return Optional.ofNullable(repositorio.buscar(id)); }
}
