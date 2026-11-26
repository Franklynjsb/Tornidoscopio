package com.tornidoscopio.tp.dominio;

import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "cuestionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuestionario {

    private static final int PREGUNTAS_POR_FACTOR = 2;

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
    @OrderBy("nroBloque ASC")
    private List<BloqueCuestionario> bloques;

    // Cuestionario "1" *-- "0..1" Resultado : tiene asociado >
    @OneToOne(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Resultado resultado;

    @Transient
    private List<Pregunta> preguntasSeleccionadas;

    public void registrarIngreso() {
        this.cantAccesos = (this.cantAccesos == null ? 0 : this.cantAccesos) + 1;
        this.fechaUltimoIngreso = LocalDateTime.now();
    }

    public Boolean validarAcceso(Integer limiteAccesos) {
        if (limiteAccesos == null || limiteAccesos <= 0) {
            return true;
        }
        int accesosActuales = this.cantAccesos == null ? 0 : this.cantAccesos;
        return accesosActuales < limiteAccesos;
    }

    /**
     * CU026 - Flujo Alternativo 1.A / Obs (2): verifica si el tiempo transcurrido desde
     * {@code fechaHoraInicio} es menor o igual a {@code tiempoMaxCompletar}.
     */
    public Boolean validarTiempoParaCompletar(LocalTime tiempoMaxCompletar) {
        if (tiempoMaxCompletar == null || this.fechaHoraInicio == null) {
            return true;
        }
        Duration transcurrido = Duration.between(this.fechaHoraInicio, LocalDateTime.now());
        Duration maximo = Duration.ofNanos(tiempoMaxCompletar.toNanoOfDay());
        return transcurrido.compareTo(maximo) <= 0;
    }

    /**
     * CU026 - Flujo Alternativo 1.B: verifica si el tiempo transcurrido desde
     * {@code fechaCreacion} en estado ACTIVO es menor o igual a {@code tiempoMaxComenzar}.
     */
    public Boolean validarTiempoParaComenzar(LocalTime tiempoMaxComenzar) {
        if (tiempoMaxComenzar == null || this.fechaCreacion == null) {
            return true;
        }
        Duration transcurrido = Duration.between(this.fechaCreacion, LocalDateTime.now());
        Duration maximo = Duration.ofNanos(tiempoMaxComenzar.toNanoOfDay());
        return transcurrido.compareTo(maximo) <= 0;
    }

    public void actualizaEstado(EstadoCuestionario e) {
        this.estado = e;
    }

    /**
     * CU026 - Paso 3 / Obs (1): selecciona 2 preguntas aleatorias por cada factor evaluable
     * de cada competencia requerida del puesto, las mezcla aleatoriamente y las agrupa en bloques.
     */
    public List<BloqueCuestionario> generarBloques(Integer cantPorBloque) {
        if (cantPorBloque == null || cantPorBloque <= 0) {
            throw new ReglaNegocioException("La cantidad de preguntas por bloque debe ser mayor a cero.");
        }
        if (this.puesto == null || this.puesto.getCompetenciasRequeridas() == null) {
            throw new ReglaNegocioException("El cuestionario no tiene un puesto con competencias configuradas.");
        }

        List<Pregunta> seleccionadas = new ArrayList<>();
        for (CompetenciaRequerida cr : this.puesto.getCompetenciasRequeridas()) {
            Competencia competencia = cr.getCompetencia();
            if (competencia == null || !Boolean.TRUE.equals(competencia.getActivo())
                    || competencia.getFactores() == null) {
                continue;
            }
            for (Factor factor : competencia.getFactores()) {
                if (factor != null && Boolean.TRUE.equals(factor.getActivo())
                        && factor.cantidadPreguntasActivas() >= PREGUNTAS_POR_FACTOR) {
                    seleccionadas.addAll(factor.obtenerPreguntasAleatorias(PREGUNTAS_POR_FACTOR));
                }
            }
        }

        if (seleccionadas.isEmpty()) {
            throw new ReglaNegocioException(
                    "No existen competencias con factores evaluables (al menos 2 preguntas activas) para generar el cuestionario.");
        }

        this.preguntasSeleccionadas = seleccionadas;
        this.mezclarPreguntas();

        if (this.bloques == null) {
            this.bloques = new ArrayList<>();
        } else {
            this.bloques.clear();
        }

        int total = this.preguntasSeleccionadas.size();
        int nroBloque = 1;
        for (int inicio = 0; inicio < total; inicio += cantPorBloque) {
            int fin = Math.min(inicio + cantPorBloque, total);
            BloqueCuestionario bloque = BloqueCuestionario.builder()
                    .nroBloque(nroBloque++)
                    .completado(false)
                    .cuestionario(this)
                    .respuestas(new ArrayList<>())
                    .build();

            int orden = 1;
            for (int idx = inicio; idx < fin; idx++) {
                RespuestaCandidato respuesta = RespuestaCandidato.builder()
                        .bloqueCuestionario(bloque)
                        .pregunta(this.preguntasSeleccionadas.get(idx))
                        .ordenPregunta(orden++)
                        .build();
                bloque.getRespuestas().add(respuesta);
            }
            this.bloques.add(bloque);
        }

        return this.bloques;
    }

    public BloqueCuestionario obtenerBloquePendiente() {
        if (this.bloques == null || this.bloques.isEmpty()) {
            return null;
        }
        return this.bloques.stream()
                .sorted(Comparator.comparing(BloqueCuestionario::getNroBloque,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .filter(b -> !Boolean.TRUE.equals(b.getCompletado()))
                .findFirst()
                .orElse(null);
    }

    public Boolean esUltimoBloque(BloqueCuestionario b) {
        if (b == null || b.getNroBloque() == null || this.bloques == null || this.bloques.isEmpty()) {
            return false;
        }
        int maxBloque = this.bloques.stream()
                .filter(bloque -> bloque.getNroBloque() != null)
                .mapToInt(BloqueCuestionario::getNroBloque)
                .max()
                .orElse(-1);
        return b.getNroBloque() == maxBloque;
    }

    /**
     * CU026 - Paso 8 / Obs (3): calcula el puntaje obtenido en una competencia promediando
     * los totales de sus factores evaluados (suma de ponderaciones del factor / 2).
     */
    public Double calcularPuntajeCompetencia(Competencia c) {
        if (c == null || this.bloques == null || this.bloques.isEmpty()) {
            return 0.0;
        }

        Map<Object, List<RespuestaCandidato>> respuestasPorFactor = new LinkedHashMap<>();
        for (BloqueCuestionario bloque : this.bloques) {
            if (bloque.getRespuestas() == null) {
                continue;
            }
            for (RespuestaCandidato rc : bloque.getRespuestas()) {
                if (rc.getPregunta() == null || rc.getPregunta().getFactor() == null) {
                    continue;
                }
                Factor factor = rc.getPregunta().getFactor();
                Competencia compPregunta = factor.getCompetencia();
                if (!mismaCompetencia(compPregunta, c)) {
                    continue;
                }
                Object claveFactor = factor.getId() != null ? factor.getId() : factor;
                respuestasPorFactor.computeIfAbsent(claveFactor, k -> new ArrayList<>()).add(rc);
            }
        }

        if (respuestasPorFactor.isEmpty()) {
            return 0.0;
        }

        double sumaTotalesFactores = 0.0;
        for (List<RespuestaCandidato> respuestasFactor : respuestasPorFactor.values()) {
            double sumaPonderaciones = 0.0;
            for (RespuestaCandidato rc : respuestasFactor) {
                sumaPonderaciones += obtenerPonderacionSeleccionada(rc);
            }
            double totalFactor = sumaPonderaciones / 2.0;
            sumaTotalesFactores += totalFactor;
        }

        return sumaTotalesFactores / respuestasPorFactor.size();
    }

    public void asociarResultado(Resultado r) {
        this.resultado = r;
        if (r != null) {
            r.setCuestionario(this);
        }
    }

    private void mezclarPreguntas() {
        if (this.preguntasSeleccionadas != null) {
            Collections.shuffle(this.preguntasSeleccionadas);
        }
    }

    private boolean mismaCompetencia(Competencia c1, Competencia c2) {
        if (c1 == null || c2 == null) {
            return false;
        }
        if (c1.getId() != null && c2.getId() != null) {
            return c1.getId().equals(c2.getId());
        }
        return c1 == c2;
    }

    private double obtenerPonderacionSeleccionada(RespuestaCandidato rc) {
        if (rc == null || rc.getItemOpcion() == null || rc.getPregunta() == null
                || rc.getPregunta().getOpcionesPonderadas() == null) {
            return 0.0;
        }
        ItemOpcion elegido = rc.getItemOpcion();
        for (PreguntaOpcion po : rc.getPregunta().getOpcionesPonderadas()) {
            if (po.getItemOpcion() == null) {
                continue;
            }
            boolean coincide = (elegido.getId() != null && po.getItemOpcion().getId() != null)
                    ? elegido.getId().equals(po.getItemOpcion().getId())
                    : elegido == po.getItemOpcion();
            if (coincide) {
                return po.getPonderacion() != null ? po.getPonderacion() : 0.0;
            }
        }
        return 0.0;
    }
}
