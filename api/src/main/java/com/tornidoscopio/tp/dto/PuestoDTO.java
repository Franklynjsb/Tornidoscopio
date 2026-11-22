package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuestoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Long idEmpresa;
    private String nombreEmpresa;
    private Integer cantCandidatos;
    private Integer cantEvaluacionesCompletas;
}
