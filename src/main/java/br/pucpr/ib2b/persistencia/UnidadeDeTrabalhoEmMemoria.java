package br.pucpr.ib2b.persistencia;

/**
 * Papel GoF: ConcreteProduct da família em memória (perfil TESTE). Nada é gravado em disco.
 * confirmar() tira uma "fotografia" da cópia de trabalho; desfazer() volta à última fotografia.
 */
public final class UnidadeDeTrabalhoEmMemoria extends UnidadeDeTrabalhoBase {

    private DocumentoDados fotografia;

    public UnidadeDeTrabalhoEmMemoria() {
        this.dados = new BaseDeDados();
        this.fotografia = MapeadorDeDados.paraDocumento(dados);
    }

    @Override
    public synchronized void confirmar() {
        fotografia = MapeadorDeDados.paraDocumento(dados);
    }

    @Override
    public synchronized void desfazer() {
        dados = MapeadorDeDados.paraBase(fotografia);
    }
}
