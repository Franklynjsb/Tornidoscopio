package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetenciaRequeridaDTO {
    private Long idCompetencia;
    private String nombreCompetencia;
    private Double puntajeMinimoRequerido;
}
