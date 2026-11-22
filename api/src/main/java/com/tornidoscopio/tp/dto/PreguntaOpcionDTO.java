package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaOpcionDTO {
    private Long idItemOpcion;
    private Double ponderacion;
}
