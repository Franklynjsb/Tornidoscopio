package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactorDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer nroOrden;
    private Long idCompetencia;
    private String nombreCompetencia;
}
