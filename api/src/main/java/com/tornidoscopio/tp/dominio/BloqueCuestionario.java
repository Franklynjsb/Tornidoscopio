package com.tornidoscopio.tp.dominio;

import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "bloque_cuestionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloque")
    private Long id;

    @Column(name = "nro_bloque", nullable = false)
    private Integer nroBloque;

    @Column(nullable = false)
    private Boolean completado;

    // Cuestionario "1" *-- "1..*" BloqueCuestionario : estructurado en >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false)
    private Cuestionario cuestionario;

    // BloqueCuestionario "1" *-- "1..*" RespuestaCandidato : contiene >
    @OneToMany(mappedBy = "bloqueCuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenPregunta ASC")
    private List<RespuestaCandidato> respuestas;

    /**
     * CU026 - Paso 6: valida que todas las preguntas del bloque cuenten con una
     * opción de respuesta válida seleccionada.
     */
    public Boolean validarRespuestasCompletas(List<RespuestaCandidatoDTO> r) {
        if (respuestas == null || respuestas.isEmpty() || r == null || r.isEmpty()) {
            return false;
        }
        Map<Long, Long> seleccionadas = mapearRespuestasPorPregunta(r);
        for (RespuestaCandidato rc : respuestas) {
            if (rc.getPregunta() == null || rc.getPregunta().getId() == null) {
                return false;
            }
            Long itemOpcionId = seleccionadas.get(rc.getPregunta().getId());
            if (itemOpcionId == null || buscarItemOpcionEnPregunta(rc.getPregunta(), itemOpcionId) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * CU026 - Paso 7: registra la opción elegida y la fecha/hora de respuesta en cada
     * {@link RespuestaCandidato} del bloque.
     */
    public void registrarRespuestas(List<RespuestaCandidatoDTO> r) {
        if (respuestas == null || respuestas.isEmpty()) {
            throw new ReglaNegocioException("El bloque no contiene preguntas.");
        }
        Map<Long, Long> seleccionadas = mapearRespuestasPorPregunta(r);
        LocalDateTime ahora = LocalDateTime.now();

        for (RespuestaCandidato rc : respuestas) {
            Pregunta pregunta = rc.getPregunta();
            Long itemOpcionId = pregunta != null ? seleccionadas.get(pregunta.getId()) : null;
            if (itemOpcionId == null) {
                throw new ReglaNegocioException("Falta responder la pregunta: "
                        + (pregunta != null ? pregunta.getEnunciado() : ""));
            }
            ItemOpcion itemSeleccionado = buscarItemOpcionEnPregunta(pregunta, itemOpcionId);
            if (itemSeleccionado == null) {
                throw new ReglaNegocioException("La opción de respuesta seleccionada no pertenece a la pregunta: "
                        + pregunta.getEnunciado());
            }
            rc.setItemOpcion(itemSeleccionado);
            rc.setFechaHoraRespuesta(ahora);
        }
    }

    public void marcarCompletado() {
        this.completado = true;
    }

    private Map<Long, Long> mapearRespuestasPorPregunta(List<RespuestaCandidatoDTO> r) {
        Map<Long, Long> mapa = new HashMap<>();
        if (r == null) {
            return mapa;
        }
        for (RespuestaCandidatoDTO dto : r) {
            if (dto != null && dto.getPreguntaId() != null && dto.getItemOpcionId() != null) {
                mapa.put(dto.getPreguntaId(), dto.getItemOpcionId());
            }
        }
        return mapa;
    }

    private ItemOpcion buscarItemOpcionEnPregunta(Pregunta pregunta, Long itemOpcionId) {
        if (pregunta == null || pregunta.getOpcionRespuesta() == null
                || pregunta.getOpcionRespuesta().getItems() == null) {
            return null;
        }
        return pregunta.getOpcionRespuesta().getItems().stream()
                .filter(item -> item.getId() != null && item.getId().equals(itemOpcionId))
                .findFirst()
                .orElse(null);
    }
}
