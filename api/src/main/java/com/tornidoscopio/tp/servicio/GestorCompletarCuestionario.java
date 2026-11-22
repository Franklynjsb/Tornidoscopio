package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IResultadoDAO;
import com.tornidoscopio.tp.dominio.BloqueCuestionario;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.Resultado;
import com.tornidoscopio.tp.dto.BloqueCuestionarioDTO;
import com.tornidoscopio.tp.dto.CuestionarioDTO;
import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorCompletarCuestionario {

    // GestorCompletarCuestionario "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;
    // GestorCompletarCuestionario "1" --> "1" IResultadoDAO
    private IResultadoDAO resultadoDAO;
    // GestorCompletarCuestionario "1" --> "1" RepositorioConfiguracion
    private RepositorioConfiguracion repositorioConfiguracion;

    public CuestionarioDTO iniciarCuestionario(String token) {
        return null;
    }

    public String obtenerInstrucciones() {
        return null;
    }

    public BloqueCuestionarioDTO obtenerSiguienteBloque(Long idCuestionario) {
        return null;
    }

    public Boolean registrarBloqueRespuestas(Long idCuestionario, List<RespuestaCandidatoDTO> r) {
        return null;
    }

    public void finalizarCuestionario(Long idCuestionario) {
    }

    private Resultado calcularResultado(Cuestionario c) {
        return null;
    }

    private CuestionarioDTO armarCuestionarioDTO(Cuestionario c) {
        return null;
    }

    private BloqueCuestionarioDTO armarBloqueDTO(BloqueCuestionario b) {
        return null;
    }
}
