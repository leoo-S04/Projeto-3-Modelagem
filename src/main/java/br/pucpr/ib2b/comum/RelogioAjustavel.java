package br.pucpr.ib2b.comum;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Relógio injetado nos tratadores. Normalmente segue o relógio base; a tela de administração (e os testes)
 * podem adiantar dias para simular uma devolução atrasada sem esperar.
 */
public class RelogioAjustavel extends Clock {

    private final Clock base;
    private volatile Duration deslocamento = Duration.ZERO;

    public RelogioAjustavel(Clock base) {
        this.base = base;
    }

    /** Relógio de teste: fixo no início do dia indicado. */
    public static RelogioAjustavel fixoEm(LocalDate dia, ZoneId fuso) {
        return new RelogioAjustavel(Clock.fixed(dia.atStartOfDay(fuso).toInstant(), fuso));
    }

    public void avancarDias(long dias) { deslocamento = deslocamento.plusDays(dias); }

    public void definirData(LocalDate dia) {
        LocalDate hojeBase = LocalDate.now(base);
        deslocamento = Duration.ofDays(hojeBase.until(dia, java.time.temporal.ChronoUnit.DAYS));
    }

    public void voltarAoNormal() { deslocamento = Duration.ZERO; }

    public LocalDate hoje() { return LocalDate.now(this); }

    @Override public ZoneId getZone() { return base.getZone(); }
    @Override public Clock withZone(ZoneId zona) { return new RelogioAjustavel(base.withZone(zona)); }
    @Override public Instant instant() { return base.instant().plus(deslocamento); }
}
