package com.tornidoscopio.tp.controlador;

import com.tornidoscopio.tp.dto.CompetenciaDTO;
import com.tornidoscopio.tp.dto.FactorDTO;
import com.tornidoscopio.tp.dto.OpcionRespuestaDTO;
import com.tornidoscopio.tp.dto.PreguntaDTO;
import com.tornidoscopio.tp.dto.PreguntaOpcionDTO;
import com.tornidoscopio.tp.dto.PreguntaSugeridaDTO;
import com.tornidoscopio.tp.servicio.GestorCompetencias;
import com.tornidoscopio.tp.servicio.GestorFactores;
import com.tornidoscopio.tp.servicio.GestorOpcionesRespuesta;
import com.tornidoscopio.tp.servicio.GestorPreguntas;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para los Casos de Uso CU020 (Gestionar Preguntas)
 * y CU021 (Dar de Alta Pregunta, incluyendo selectores y CU033 Generar con IA).
 */
@RestController
@RequestMapping("/api")
public class PreguntaController {

    private final GestorPreguntas gestorPreguntas;
    private final GestorCompetencias gestorCompetencias;
    private final GestorFactores gestorFactores;
    private final GestorOpcionesRespuesta gestorOpcionesRespuesta;

    public PreguntaController(
            GestorPreguntas gestorPreguntas,
            GestorCompetencias gestorCompetencias,
            GestorFactores gestorFactores,
            GestorOpcionesRespuesta gestorOpcionesRespuesta) {
        this.gestorPreguntas = gestorPreguntas;
        this.gestorCompetencias = gestorCompetencias;
        this.gestorFactores = gestorFactores;
        this.gestorOpcionesRespuesta = gestorOpcionesRespuesta;
    }

    // =========================================================================
    // Selectores para CU020 y CU021 (Competencias, Factores y Opciones de Respuesta)
    // =========================================================================

    @GetMapping("/competencias")
    public List<CompetenciaDTO> buscarCompetencias(
            @RequestParam(required = false, defaultValue = "") String criterios) {
        return gestorCompetencias.buscar(criterios);
    }

    @GetMapping("/competencias/{idCompetencia}/factores")
    public List<FactorDTO> buscarFactoresPorCompetencia(@PathVariable Long idCompetencia) {
        return gestorFactores.buscarPorCompetencia(idCompetencia);
    }

    @GetMapping("/opciones-respuesta")
    public List<OpcionRespuestaDTO> buscarOpcionesRespuesta(
            @RequestParam(required = false, defaultValue = "") String criterios) {
        return gestorOpcionesRespuesta.buscar(criterios);
    }

    // =========================================================================
    // CU020 - Gestionar Preguntas: Búsqueda con filtros
    // =========================================================================

    @GetMapping("/preguntas")
    public List<PreguntaDTO> buscarPreguntas(
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) String factor,
            @RequestParam(required = false) String nombrePregunta) {
        return gestorPreguntas.buscarPreguntas(competencia, factor, nombrePregunta);
    }

    // =========================================================================
    // CU021 - Dar de Alta Pregunta (y Flujo Alternativo 1.A.1 Generar con IA)
    // =========================================================================

    @PostMapping("/preguntas/generar-ia")
    public ResponseEntity<PreguntaSugeridaDTO> generarPreguntaIA(@RequestBody GenerarPreguntaIARequest request) {
        PreguntaSugeridaDTO sugerida = gestorPreguntas.generarPreguntaIA(
                request.getIdCompetencia(),
                request.getIdFactor(),
                request.getDescripcion(),
                request.getIdOpcionRespuesta()
        );
        return ResponseEntity.ok(sugerida);
    }

    @PostMapping("/preguntas")
    public ResponseEntity<?> altaPregunta(@RequestBody AltaPreguntaRequest request) {
        PreguntaDTO preguntaDTO = request.getPregunta();
        List<PreguntaOpcionDTO> ponderaciones = request.getOpciones();

        // 1. Validación 2.A: omisiones de datos obligatorios
        List<String> listaOmisiones = gestorPreguntas.validarOmisiones(preguntaDTO);
        if (!listaOmisiones.isEmpty()) {
            return respuestaValidacion("OMISIONES", "Faltan datos obligatorios", listaOmisiones);
        }

        // 2. Validación 2.B: longitud máxima de los campos
        List<String> listaExcedidos = gestorPreguntas.validarLongitudMaxima(preguntaDTO);
        if (!listaExcedidos.isEmpty()) {
            return respuestaValidacion("LONGITUD_MAXIMA", "Uno o más campos superan la longitud máxima", listaExcedidos);
        }

        // 3. Validación de ponderaciones (suma = 10 y valores >= 0)
        Boolean ponderacionValida = gestorPreguntas.validarPonderacion(ponderaciones);
        if (!Boolean.TRUE.equals(ponderacionValida)) {
            return respuestaValidacion(
                    "PONDERACION_INVALIDA",
                    "Las ponderaciones deben sumar 10 puntos y ser mayores o iguales a 0",
                    List.of("Las ponderaciones deben sumar 10 puntos y ser mayores o iguales a 0")
            );
        }

        // 4. Paso 3 (y Validación 2.C duplicados dentro de altaPregunta) + asignarOpciones
        PreguntaDTO creada = gestorPreguntas.altaPregunta(preguntaDTO);
        gestorPreguntas.asignarOpciones(creada.getId(), ponderaciones);

        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    private ResponseEntity<Map<String, Object>> respuestaValidacion(
            String codigo,
            String mensaje,
            List<String> detalles) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("codigo", codigo);
        body.put("mensaje", mensaje);
        body.put("detalles", detalles);
        return ResponseEntity.badRequest().body(body);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AltaPreguntaRequest {
        private PreguntaDTO pregunta;
        private List<PreguntaOpcionDTO> opciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarPreguntaIARequest {
        private Long idCompetencia;
        private Long idFactor;
        private String descripcion;
        private Long idOpcionRespuesta;
    }
}
