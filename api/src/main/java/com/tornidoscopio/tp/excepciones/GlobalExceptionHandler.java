package com.tornidoscopio.tp.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaNegocio(ReglaNegocioException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "REGLA_NEGOCIO", ex.getMessage(), null);
    }

    @ExceptionHandler(PreguntaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> manejarPreguntaDuplicada(PreguntaDuplicadaException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, "PREGUNTA_DUPLICADA", ex.getMessage(), null);
    }

    @ExceptionHandler(CompetenciasNoEvaluablesException.class)
    public ResponseEntity<Map<String, Object>> manejarCompetenciasNoEvaluables(CompetenciasNoEvaluablesException ex) {
        return construirRespuesta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "COMPETENCIAS_NO_EVALUABLES",
                ex.getMessage(),
                ex.getCompetenciasNoEvaluables()
        );
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarEntidadNoEncontrada(EntidadNoEncontradaException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage(), null);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(RuntimeException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", ex.getMessage(), null);
    }

    @ExceptionHandler(ExportacionExcelException.class)
    public ResponseEntity<Map<String, Object>> manejarExportacionExcel(ExportacionExcelException ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_EXPORTACION", ex.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(
            HttpStatus status,
            String codigo,
            String mensaje,
            List<String> detalles) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("codigo", codigo);
        body.put("mensaje", mensaje);
        if (detalles != null && !detalles.isEmpty()) {
            body.put("detalles", detalles);
        }
        return ResponseEntity.status(status).body(body);
    }
}
