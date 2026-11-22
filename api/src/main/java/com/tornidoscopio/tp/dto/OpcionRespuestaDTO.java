package com.tornidoscopio.tp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionRespuestaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    // OpcionRespuestaDTO "1" *-- "2..*" ItemOpcionDTO
    private List<ItemOpcionDTO> items;
}
