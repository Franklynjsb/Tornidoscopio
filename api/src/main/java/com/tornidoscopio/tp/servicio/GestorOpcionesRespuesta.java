package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.IItemOpcionDAO;
import com.tornidoscopio.tp.dao.IOpcionRespuestaDAO;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dto.OpcionRespuestaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorOpcionesRespuesta {

    // GestorOpcionesRespuesta "1" --> "1" IOpcionRespuestaDAO
    private IOpcionRespuestaDAO opcionRespuestaDAO;
    // GestorOpcionesRespuesta "1" --> "1" IItemOpcionDAO
    private IItemOpcionDAO itemOpcionDAO;
    // GestorOpcionesRespuesta "1" --> "1" IAuditor
    private IAuditor auditor;

    public List<OpcionRespuestaDTO> buscar(String criterios) {
        return null;
    }

    public void alta(OpcionRespuestaDTO dto) {
    }

    public void modificar(OpcionRespuestaDTO dto) {
    }

    public void eliminar(Long id) {
    }

    private OpcionRespuestaDTO armarOpcionRespuestaDTO(OpcionRespuesta o) {
        return null;
    }

    private OpcionRespuesta mapearOpcionRespuesta(OpcionRespuestaDTO dto) {
        return null;
    }
}
