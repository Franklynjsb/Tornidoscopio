package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IResultadoDAO;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.Resultado;
import com.tornidoscopio.tp.dto.OrdenMeritoDTO;
import com.tornidoscopio.tp.dto.ResultadoComparativoDTO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GestorReportes {

    // GestorReportes "1" --> "1" IResultadoDAO
    private IResultadoDAO resultadoDAO;
    // GestorReportes "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;
    // GestorReportes "1" --> "1" ICandidatoDAO
    private ICandidatoDAO candidatoDAO;

    public File exportarResultados(Long idPuesto) {
        return null;
    }

    public List<OrdenMeritoDTO> generarOrdenMerito(Long idPuesto, List<LocalDateTime> fechas) {
        return null;
    }

    public ResultadoComparativoDTO generarReporteComparativo(Long idPuesto, Long idCandidato) {
        return null;
    }

    public List<LocalDateTime> obtenerFechasEvaluaciones(Long idPuesto) {
        return null;
    }

    private OrdenMeritoDTO armarOrdenMeritoDTO(Cuestionario q) {
        return null;
    }

    private ResultadoComparativoDTO armarResultadoComparativoDTO(Resultado r) {
        return null;
    }

    private void insertarSegunOrden(List<OrdenMeritoDTO> lista, OrdenMeritoDTO dto, Boolean cumpleMinimos) {
    }
}
