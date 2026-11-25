package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "factor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factor")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @Column(nullable = false)
    private Boolean activo;

    // Competencia "1" o-- "1..*" Factor : compuesta por >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia", nullable = false)
    private Competencia competencia;

    // Factor "1" *-- "1..*" Pregunta : contiene >
    @OneToMany(mappedBy = "factor", cascade = CascadeType.ALL)
    private List<Pregunta> preguntas;

    /**
     * CU025 - Cuenta la cantidad de preguntas activas asociadas a este factor.
     *
     * @return cantidad de preguntas con {@code activo == true}
     */
    public Integer cantidadPreguntasActivas() {
        if (preguntas == null) {
            return 0;
        }
        return (int) preguntas.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .count();
    }

    public List<Pregunta> obtenerPreguntasAleatorias(Integer n) {
        return null;
    }
}
