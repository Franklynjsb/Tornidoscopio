package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pregunta_opcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaOpcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta_opcion")
    private Long id;

    @Column(nullable = false)
    private Double ponderacion;

    // Pregunta "1" *-- "2..*" PreguntaOpcion : contiene >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    // PreguntaOpcion "*" --> "1" ItemOpcion : apunta a >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item_opcion", nullable = false)
    private ItemOpcion itemOpcion;
}
