package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "cuestionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuestionario")
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "clave_ingreso", nullable = false, length = 8)
    private String claveIngreso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuestionario estado;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "cant_accesos", nullable = false)
    private Integer cantAccesos;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "fecha_ultimo_ingreso")
    private LocalDateTime fechaUltimoIngreso;

    // Candidato "1" -- "0..*" Cuestionario : vinculado a
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nro_candidato", nullable = false)
    private Candidato candidato;

    // Cuestionario "0..*" --> "1" Puesto : es para >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto", nullable = false)
    private Puesto puesto;

    // Cuestionario "1" *-- "1..*" BloqueCuestionario : estructurado en >
    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BloqueCuestionario> bloques;

    // Cuestionario "1" *-- "0..1" Resultado : tiene asociado >
    @OneToOne(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Resultado resultado;

    public void registrarIngreso() {
    }

    public Boolean validarAcceso(Integer limiteAccesos) {
        return null;
    }

    public Boolean validarTiempoParaCompletar(LocalTime tiempoMaxCompletar) {
        return null;
    }

    public Boolean validarTiempoParaComenzar(LocalTime tiempoMaxComenzar) {
        return null;
    }

    public void actualizaEstado(EstadoCuestionario e) {
    }

    public List<BloqueCuestionario> generarBloques(Integer cantPorBloque) {
        return null;
    }

    public BloqueCuestionario obtenerBloquePendiente() {
        return null;
    }

    public Boolean esUltimoBloque(BloqueCuestionario b) {
        return null;
    }

    public Double calcularPuntajeCompetencia(Competencia c) {
        return null;
    }

    public void asociarResultado(Resultado r) {
    }

    private void mezclarPreguntas() {
    }
}
