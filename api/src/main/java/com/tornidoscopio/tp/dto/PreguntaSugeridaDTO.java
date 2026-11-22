package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaSugeridaDTO {
    private String nombrePregunta;
    private String enunciado;
    private String descripcion;
}
