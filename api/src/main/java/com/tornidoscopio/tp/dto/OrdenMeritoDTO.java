package com.tornidoscopio.tp.dto;

import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.TipoDocumento;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenMeritoDTO {
    private String nombreCandidato;
    private String apellidoCandidato;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private Double puntajeTotalObtenido;
    private EstadoCuestionario estadoCuestionario;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinOUltimoIngreso;
    private Integer cantAccesos;
    private Boolean cumpleMinimos; //Añadido de variable para su uso en los métodos y simplificarlos
}
