package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoImportacionDTO {
    private Integer candidatosCreados;
    private Integer candidatosActualizados;
    private Integer candidatosEliminados;
}
