package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String enunciado;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo;

    // Factor "1" *-- "1..*" Pregunta : contiene >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factor", nullable = false)
    private Factor factor;

    // Pregunta "0..*" --> "1" OpcionRespuesta : usa >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_resp", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    // Pregunta "1" *-- "2..*" PreguntaOpcion : contiene >
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreguntaOpcion> opcionesPonderadas;
}
