package com.tornidoscopio.tp.controlador;

import com.tornidoscopio.tp.dto.OrdenMeritoDTO;
import com.tornidoscopio.tp.servicio.GestorReportes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para el Caso de Uso CU028 (Emitir Orden de Mérito).
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final GestorReportes gestorReportes;

    public ReporteController(GestorReportes gestorReportes) {
        this.gestorReportes = gestorReportes;
    }

    @GetMapping("/puestos/{idPuesto}/fechas")
    public List<LocalDateTime> obtenerFechasEvaluaciones(@PathVariable Long idPuesto) {
        return gestorReportes.obtenerFechasEvaluaciones(idPuesto);
    }

    @PostMapping("/puestos/{idPuesto}/orden-merito")
    public List<OrdenMeritoDTO> generarOrdenMerito(
            @PathVariable Long idPuesto,
            @RequestBody GenerarOrdenMeritoRequest request) {
        return gestorReportes.generarOrdenMerito(idPuesto, request.getFechas());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarOrdenMeritoRequest {
        private List<LocalDateTime> fechas;
    }
}
