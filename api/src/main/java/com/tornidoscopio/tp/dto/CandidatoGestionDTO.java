package com.tornidoscopio.tp.dto;

import com.tornidoscopio.tp.dominio.TipoDocumento;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatoGestionDTO {
    private Long nroCandidato;
    private String apellido;
    private String nombre;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
}
