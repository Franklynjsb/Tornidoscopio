package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaDTO {
    private Long id;
    private String nombre;
    private String enunciado;
    private String descripcion;
    private String nombreCompetencia;
    private String nombreFactor;
    private Long idFactor;
    private Long idOpcionRespuesta;
}
