package br.pucpr.ib2b.comum;

/** Lançada pelos passos validar / verificarPreRequisitos do Template Method; vira ResultadoCadastro RECUSADO. */
public class CadastroRecusadoException extends ExcecaoDeDominio {
    public CadastroRecusadoException(String motivo) {
        super(motivo);
    }
}
