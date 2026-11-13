package br.pucpr.ib2b.web;

import br.pucpr.ib2b.comum.RelogioAjustavel;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/** Dados exibidos em todas as páginas (a data "de hoje" do relógio ajustável da demonstração). */
@ControllerAdvice(basePackages = "br.pucpr.ib2b.web")
public class ModeloComum {

    private final RelogioAjustavel relogio;

    public ModeloComum(RelogioAjustavel relogio) { this.relogio = relogio; }

    @ModelAttribute("hoje")
    public LocalDate hoje() { return relogio.hoje(); }
}
