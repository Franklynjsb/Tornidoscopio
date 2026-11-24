package com.tornidoscopio.tp.excepciones;

import java.util.Collections;
import java.util.List;

/**
 * CU025 - Flujo Alternativo 9.A: una o más competencias requeridas por el puesto
 * seleccionado no poseen al menos un factor activo con al menos 2 preguntas activas,
 * por lo que no pueden ser evaluadas.
 */
public class CompetenciasNoEvaluablesException extends RuntimeException {

    private final List<String> competenciasNoEvaluables;

    public CompetenciasNoEvaluablesException(List<String> competenciasNoEvaluables) {
        super("Las siguientes competencias no pueden ser evaluadas por no poseer al menos un factor activo con 2 o más preguntas activas: "
                + String.join(", ", competenciasNoEvaluables));
        this.competenciasNoEvaluables = Collections.unmodifiableList(competenciasNoEvaluables);
    }

    public List<String> getCompetenciasNoEvaluables() {
        return competenciasNoEvaluables;
    }
}
