package com.tornidoscopio.tp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaCuestionarioDTO {
    private Long id;
    private String enunciado;
    // PreguntaCuestionarioDTO "1" *-- "2..*" ItemOpcionCuestionarioDTO
    private List<ItemOpcionCuestionarioDTO> opciones;
}
