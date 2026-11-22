package com.tornidoscopio.tp.dto;

import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuestionarioDTO {
    private Long id;
    private EstadoCuestionario estado;
    private LocalTime tiempoMaxCompletar;
}
