package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetenciaDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
}
