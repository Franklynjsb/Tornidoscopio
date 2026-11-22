package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resultado_competencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_res_comp")
    private Long id;

    @Column(name = "puntaje_obtenido", nullable = false)
    private Double puntajeObtenido;

    @Column(name = "puntaje_requerido", nullable = false)
    private Double puntajeRequerido;

    // Resultado "1" *-- "1..*" ResultadoCompetencia : detalla >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resultado", nullable = false)
    private Resultado resultado;

    // ResultadoCompetencia "0..*" --> "1" Competencia : evalúa >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia", nullable = false)
    private Competencia competencia;
}
