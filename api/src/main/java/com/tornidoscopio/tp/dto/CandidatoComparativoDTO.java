package com.tornidoscopio.tp.dto;

import com.tornidoscopio.tp.dominio.TipoDocumento;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatoComparativoDTO {
    private String nombre;
    private String apellido;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private Long nroCandidato;
    private LocalDateTime fechaFinalizacion;
}
