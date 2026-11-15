package br.pucpr.ib2b.web;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Como mostrar o prazo de um empréstimo na tela: quanto do prazo já passou (0 a 100%) e a cor
 * (verde = no prazo, laranja = vence em até 3 dias, vermelho = atrasado). Só apresentação: a regra do prazo é
 * do domínio (vencimento calculado pelo Livro).
 */
public record Prazo(int percentual, String cor, String texto) {

    static Prazo de(LocalDate inicio, LocalDate vencimento, LocalDate hoje) {
        if (inicio == null || vencimento == null) return new Prazo(0, "verde", "");
        long total = Math.max(1, ChronoUnit.DAYS.between(inicio, vencimento));
        long passados = ChronoUnit.DAYS.between(inicio, hoje);
        int percentual = (int) Math.max(0, Math.min(100, passados * 100 / total));
        long faltam = ChronoUnit.DAYS.between(hoje, vencimento);
        if (faltam < 0) return new Prazo(100, "vermelho", "Atrasado há " + (-faltam) + " dia(s)");
        if (faltam == 0) return new Prazo(percentual, "laranja", "Vence hoje");
        if (faltam <= 3) return new Prazo(percentual, "laranja", "Vence em " + faltam + " dia(s)");
        return new Prazo(percentual, "verde", "Faltam " + faltam + " dias");
    }
}
