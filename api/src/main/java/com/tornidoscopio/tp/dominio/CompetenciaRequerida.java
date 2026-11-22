package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puesto_competencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetenciaRequerida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competencia_requerida")
    private Long id;

    @Column(name = "puntaje_minimo", nullable = false)
    private Double puntajeMinimoRequerido;

    // (Puesto, Competencia) .. CompetenciaRequerida
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto", nullable = false)
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia", nullable = false)
    private Competencia competencia;
}
