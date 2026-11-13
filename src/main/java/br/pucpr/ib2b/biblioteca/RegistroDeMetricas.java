package br.pucpr.ib2b.biblioteca;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Destino das medições das consultas (aqui, em memória; poderia ser um sistema de monitoramento). */
public class RegistroDeMetricas {

    public record Medicao(String termo, Duration duracao, int resultados, Instant em) { }

    private final List<Medicao> medicoes = Collections.synchronizedList(new ArrayList<>());

    public void registrar(Medicao m) { medicoes.add(m); }
    public List<Medicao> medicoes() { return List.copyOf(medicoes); }
}
