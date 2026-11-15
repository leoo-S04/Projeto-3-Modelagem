package br.pucpr.ib2b.web;

import java.io.Serializable;

/**
 * Quem está usando o sistema (guardado na sessão depois do login). Os três perfis são os atores do diagrama de
 * casos de uso: estudante, atendente e Administrador.
 *
 * @param perfil        ator do caso de uso
 * @param identificador matrícula (estudante), matrícula funcional (atendente) ou usuário (administrador)
 * @param nome          nome para exibir no topo da página
 * @param cpf           CPF da pessoa (usado para achar as multas do estudante); nulo para o administrador
 */
public record Usuario(Perfil perfil, String identificador, String nome, String cpf) implements Serializable {

    public enum Perfil {
        ESTUDANTE("Estudante", "/estudante"),
        ATENDENTE("Atendente", "/atendente"),
        ADMINISTRADOR("Administrador", "/admin");

        private final String rotulo;
        private final String inicio;

        Perfil(String rotulo, String inicio) {
            this.rotulo = rotulo;
            this.inicio = inicio;
        }

        public String rotulo() { return rotulo; }

        /** Página inicial do perfil. */
        public String inicio() { return inicio; }
    }

    static final String CHAVE_NA_SESSAO = "usuario";
}
