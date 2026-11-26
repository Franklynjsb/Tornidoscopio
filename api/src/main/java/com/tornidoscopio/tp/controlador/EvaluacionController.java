package com.tornidoscopio.tp.controlador;

import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dto.CandidatoClaveDTO;
import com.tornidoscopio.tp.dto.CandidatoGestionDTO;
import com.tornidoscopio.tp.dto.CompetenciaRequeridaDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import com.tornidoscopio.tp.servicio.GestorEvaluacion;
import com.tornidoscopio.tp.servicio.GestorPuestos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Controlador REST para el Caso de Uso CU025 (Evaluar a Candidatos)
 * y consultas de Puestos compartidas con el CU028.
 */
@RestController
@RequestMapping("/api")
public class EvaluacionController {

    private final GestorEvaluacion gestorEvaluacion;
    private final GestorPuestos gestorPuestos;

    public EvaluacionController(GestorEvaluacion gestorEvaluacion, GestorPuestos gestorPuestos) {
        this.gestorEvaluacion = gestorEvaluacion;
        this.gestorPuestos = gestorPuestos;
    }

    // =========================================================================
    // Puestos (CU025 Paso 8 y CU028 Paso 3-4)
    // =========================================================================

    @GetMapping("/puestos")
    public List<PuestoDTO> buscarPuestos(
            @RequestParam(required = false, defaultValue = "") String criterios) {
        return gestorPuestos.buscarPuestos(criterios);
    }

    @GetMapping("/puestos/{idPuesto}/competencias-requeridas")
    public List<CompetenciaRequeridaDTO> obtenerCompetenciasRequeridas(@PathVariable Long idPuesto) {
        return gestorPuestos.obtenerCompetenciasRequeridas(idPuesto);
    }

    // =========================================================================
    // CU025 - Evaluar a Candidatos
    // =========================================================================

    @GetMapping("/evaluaciones/candidatos")
    public List<CandidatoGestionDTO> buscarCandidatos(
            @RequestParam(required = false, defaultValue = "") String criterios) {
        return gestorEvaluacion.buscarCandidatos(criterios);
    }

    @PostMapping("/evaluaciones/validar-activos")
    public List<ConflictoCuestionarioDTO> validarCuestionariosActivos(@RequestBody List<Long> idsCandidatos) {
        List<Cuestionario> conflictos = gestorEvaluacion.validarCuestionariosActivos(idsCandidatos);
        return conflictos.stream()
                .map(c -> ConflictoCuestionarioDTO.builder()
                        .idCuestionario(c.getId())
                        .nroCandidato(c.getCandidato() != null ? c.getCandidato().getNroCandidato() : null)
                        .apellido(c.getCandidato() != null ? c.getCandidato().getApellido() : "")
                        .nombre(c.getCandidato() != null ? c.getCandidato().getNombre() : "")
                        .nombrePuesto(c.getPuesto() != null ? c.getPuesto().getNombre() : "")
                        .estado(c.getEstado() != null ? c.getEstado().name() : "")
                        .build())
                .toList();
    }

    @PostMapping("/evaluaciones/generar")
    public List<CandidatoClaveDTO> generarCuestionarios(@RequestBody GenerarCuestionariosRequest request) {
        return gestorEvaluacion.generarCuestionarios(request.getPuesto(), request.getCandidatos());
    }

    @PostMapping("/evaluaciones/confirmar")
    public ResponseEntity<Void> confirmarYGuardarCuestionarios() {
        gestorEvaluacion.confirmarYGuardarCuestionarios();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/evaluaciones/exportar")
    public ResponseEntity<Resource> exportarCandidatosConClaves(@RequestBody ExportarClavesRequest request) throws IOException {
        File archivo = gestorEvaluacion.exportarCandidatosConClaves(request.getIdPuesto(), request.getCandidatos());
        byte[] contenido = Files.readAllBytes(archivo.toPath());
        String nombreArchivo = archivo.getName();
        Files.deleteIfExists(archivo.toPath());

        ByteArrayResource resource = new ByteArrayResource(contenido);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(contenido.length)
                .body(resource);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConflictoCuestionarioDTO {
        private Long idCuestionario;
        private Long nroCandidato;
        private String apellido;
        private String nombre;
        private String nombrePuesto;
        private String estado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarCuestionariosRequest {
        private PuestoDTO puesto;
        private List<CandidatoGestionDTO> candidatos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportarClavesRequest {
        private Long idPuesto;
        private List<CandidatoClaveDTO> candidatos;
    }
}
