package br.pucpr.ib2b.comum;

/** Envio de e-mail (infraestrutura). Nesta versão é simulado; pode falhar (ex.: servidor fora do ar). */
public interface ServicoDeEmail {
    void enviar(String para, String assunto, String texto);
}
