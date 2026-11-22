package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "puesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puesto")
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo;

    // Puesto "0..*" --> "1" Empresa : pertenece a >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // Puesto "0..*" -- "1..*" Competencia (Clase de asociación: CompetenciaRequerida)
    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompetenciaRequerida> competenciasRequeridas;
}
