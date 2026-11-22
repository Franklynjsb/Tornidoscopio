package com.tornidoscopio.tp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaCandidatoDTO {
    private Long preguntaId;
    private Long itemOpcionId;
}
