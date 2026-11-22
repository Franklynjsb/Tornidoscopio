package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "competencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competencia")
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo;

    // Competencia "1" o-- "1..*" Factor : compuesta por >
    @OneToMany(mappedBy = "competencia", cascade = CascadeType.ALL)
    private List<Factor> factores;
}
