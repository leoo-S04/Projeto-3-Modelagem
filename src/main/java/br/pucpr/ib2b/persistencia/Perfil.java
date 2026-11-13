package br.pucpr.ib2b.persistencia;

/** Perfil de execução que escolhe a família de persistência (uma única vez, na montagem do sistema). */
public enum Perfil {
    /** Testes: tudo em memória, nada é gravado em disco. */
    TESTE,
    /** Produção/demonstração: arquivo JSON local (dados/biblioteca.json). */
    PRODUCAO
}
