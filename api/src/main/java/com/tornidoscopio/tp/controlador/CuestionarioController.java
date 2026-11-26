package com.tornidoscopio.tp.controlador;

import com.tornidoscopio.tp.dto.BloqueCuestionarioDTO;
import com.tornidoscopio.tp.dto.CuestionarioDTO;
import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import com.tornidoscopio.tp.servicio.GestorCompletarCuestionario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el Caso de Uso CU026 (Completar Cuestionario).
 */
@RestController
@RequestMapping("/api/cuestionarios")
public class CuestionarioController {

    private final GestorCompletarCuestionario gestorCompletarCuestionario;

    public CuestionarioController(GestorCompletarCuestionario gestorCompletarCuestionario) {
        this.gestorCompletarCuestionario = gestorCompletarCuestionario;
    }

    @PostMapping("/iniciar")
    public CuestionarioDTO iniciarCuestionario(@RequestBody IniciarCuestionarioRequest request) {
        return gestorCompletarCuestionario.iniciarCuestionario(request.getClaveIngreso());
    }

    @GetMapping("/instrucciones")
    public Map<String, String> obtenerInstrucciones() {
        return Map.of("instrucciones", gestorCompletarCuestionario.obtenerInstrucciones());
    }

    @PostMapping("/{idCuestionario}/siguiente-bloque")
    public BloqueCuestionarioDTO obtenerSiguienteBloque(@PathVariable Long idCuestionario) {
        return gestorCompletarCuestionario.obtenerSiguienteBloque(idCuestionario);
    }

    @PostMapping("/{idCuestionario}/respuestas")
    public Map<String, Boolean> registrarBloqueRespuestas(
            @PathVariable Long idCuestionario,
            @RequestBody List<RespuestaCandidatoDTO> respuestas) {
        Boolean completo = gestorCompletarCuestionario.registrarBloqueRespuestas(idCuestionario, respuestas);
        return Map.of("completo", completo);
    }

    @PostMapping("/{idCuestionario}/finalizar")
    public ResponseEntity<Void> finalizarCuestionario(@PathVariable Long idCuestionario) {
        gestorCompletarCuestionario.finalizarCuestionario(idCuestionario);
        return ResponseEntity.ok().build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IniciarCuestionarioRequest {
        private String claveIngreso;
    }
}
