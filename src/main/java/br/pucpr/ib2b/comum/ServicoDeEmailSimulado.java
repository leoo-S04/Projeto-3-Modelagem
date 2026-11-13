package br.pucpr.ib2b.comum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * E-mail simulado: guarda as mensagens "enviadas" para a demonstração. {@link #simularFalha(boolean)} faz os
 * próximos envios lançarem exceção — usado para mostrar que a falha de um ouvinte não desfaz a devolução.
 */
public class ServicoDeEmailSimulado implements ServicoDeEmail {

    public record Mensagem(String para, String assunto, String texto) { }

    private final List<Mensagem> enviadas = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean falhar = false;

    @Override
    public void enviar(String para, String assunto, String texto) {
        if (falhar) throw new IllegalStateException("Servidor de e-mail indisponível");
        enviadas.add(new Mensagem(para, assunto, texto));
    }

    public void simularFalha(boolean falhar) { this.falhar = falhar; }
    public boolean estaFalhando() { return falhar; }
    public List<Mensagem> enviadas() { return List.copyOf(enviadas); }
}
