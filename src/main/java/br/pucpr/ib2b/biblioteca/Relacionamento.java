package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;
import br.pucpr.ib2b.academico.iEstudante;
import br.pucpr.ib2b.rh.Atendente;
import br.pucpr.ib2b.rh.iAtendente;

/**
 * Ponto interno único de acesso da Biblioteca a estudantes e atendentes (Trabalho 02: "Relacionamento").
 * Usa as interfaces iEstudante e iAtendente, que o componente Biblioteca requer (diagrama de componentes),
 * sem acessar diretamente as classes dos componentes Academico e RH.
 */
public class Relacionamento {

    private final iEstudante estudantes;
    private final iAtendente atendentes;

    public Relacionamento(iEstudante estudantes, iAtendente atendentes) {
        this.estudantes = estudantes;
        this.atendentes = atendentes;
    }

    /** Estudante com essa matrícula (idEstudante), ou {@code null} se não cadastrado. */
    public Estudante buscarEstudante(String matricula) {
        return estudantes.consultarEstudante(matricula);
    }

    /** Atendente com essa matrícula funcional (idAtendente), ou {@code null} se não cadastrado. */
    public Atendente buscarAtendente(String matriculaFuncional) {
        return atendentes.consultarAtendente(matriculaFuncional);
    }
}
