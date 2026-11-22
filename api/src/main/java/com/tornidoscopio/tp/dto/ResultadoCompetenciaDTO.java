package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoCompetenciaDTO {
    private String nombreCompetencia;
    private Double puntajeObtenido;
    private Double puntajeRequerido;
}
