package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "respuesta_candidato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaCandidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Long id;

    @Column(name = "fecha_hora_respuesta")
    private LocalDateTime fechaHoraRespuesta;

    @Column(name = "orden_pregunta", nullable = false)
    private Integer ordenPregunta;

    // BloqueCuestionario "1" *-- "1..*" RespuestaCandidato : contiene >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bloque", nullable = false)
    private BloqueCuestionario bloqueCuestionario;

    // RespuestaCandidato "0..*" --> "1" Pregunta : responde a >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    // RespuestaCandidato "0..*" --> "0..1" ItemOpcion : selecciona >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item_opcion")
    private ItemOpcion itemOpcion;
}
