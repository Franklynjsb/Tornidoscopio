package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "candidato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidato {

    @Id
    @Column(name = "nro_candidato")
    private Long nroCandidato;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "nro_documento", nullable = false)
    private String nroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(nullable = false)
    private String nacionalidad;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String escolaridad;

    @Column(nullable = false)
    private Boolean eliminado;

    // Candidato "1" -- "0..*" Cuestionario : vinculado a
    @OneToMany(mappedBy = "candidato")
    private List<Cuestionario> cuestionarios;

    public Cuestionario getCuestionario(Long idPuesto) {
        return null;
    }
}
