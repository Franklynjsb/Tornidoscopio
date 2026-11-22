package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOpcionDTO {
    private Long id;
    private Integer ordenVisualizacion;
    private String descripcion;
}
