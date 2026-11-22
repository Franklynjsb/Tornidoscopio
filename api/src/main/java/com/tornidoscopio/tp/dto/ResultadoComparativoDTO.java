package com.tornidoscopio.tp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoComparativoDTO {
    private String nombrePuesto;
    // ResultadoComparativoDTO "1" --> "1" CandidatoComparativoDTO
    private CandidatoComparativoDTO candidato;
    // ResultadoComparativoDTO "1" *-- "1..*" ResultadoCompetenciaDTO
    private List<ResultadoCompetenciaDTO> resultadosCompetencia;
}
