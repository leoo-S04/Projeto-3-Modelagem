package br.pucpr.ib2b.financeiro;

import br.pucpr.ib2b.persistencia.RepositorioFinanceiro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seleção da Strategy pela configuração administrativa (Req. 2): traduz o id gravado ("ORIGINAL", "TETO",
 * "TOLERANCIA") no objeto de política. As políticas são registradas na montagem do sistema; uma nova política
 * é uma nova classe + um registro aqui — sem if por política em lugar nenhum.
 */
public class SeletorDePolitica {

    private final Map<String, PoliticaDeMulta> politicas = new LinkedHashMap<>();
    private final RepositorioFinanceiro repositorio;

    public SeletorDePolitica(RepositorioFinanceiro repositorio, List<PoliticaDeMulta> registradas) {
        this.repositorio = repositorio;
        registradas.forEach(p -> politicas.put(p.id(), p));
    }

    /** A política vigente, conforme a configuração administrativa gravada. */
    public PoliticaDeMulta vigente() { return obter(repositorio.configuracao().getPoliticaVigente()); }

    public PoliticaDeMulta obter(String id) {
        PoliticaDeMulta p = politicas.get(id);
        if (p == null) throw new PoliticaInexistenteException(id);
        return p;
    }

    public List<PoliticaDeMulta> todas() { return new ArrayList<>(politicas.values()); }

    /** Troca a política vigente; id desconhecido é recusado ao salvar (o cálculo nunca encontra id inválido). */
    public void definirPolitica(String id) {
        obter(id);
        repositorio.unidade().executar(() -> repositorio.configuracao().definirPolitica(id));
    }
}
