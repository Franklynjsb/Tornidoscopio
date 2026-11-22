package com.tornidoscopio.tp.dto;

import com.tornidoscopio.tp.dominio.TipoDocumento;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatoClaveDTO {
    private String nombreCandidato;
    private String apellidoCandidato;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private String claveIngreso;
}
