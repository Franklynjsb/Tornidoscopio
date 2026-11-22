package com.tornidoscopio.tp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueCuestionarioDTO {
    private Integer nroBloque;
    private Boolean esUltimo;
    // BloqueCuestionarioDTO "1" *-- "1..*" PreguntaCuestionarioDTO
    private List<PreguntaCuestionarioDTO> preguntas;
}
