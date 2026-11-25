package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.IItemOpcionDAO;
import com.tornidoscopio.tp.dao.IOpcionRespuestaDAO;
import com.tornidoscopio.tp.dominio.ItemOpcion;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dto.ItemOpcionDTO;
import com.tornidoscopio.tp.dto.OpcionRespuestaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GestorOpcionesRespuesta {

    // GestorOpcionesRespuesta "1" --> "1" IOpcionRespuestaDAO
    private IOpcionRespuestaDAO opcionRespuestaDAO;
    // GestorOpcionesRespuesta "1" --> "1" IItemOpcionDAO
    private IItemOpcionDAO itemOpcionDAO;
    // GestorOpcionesRespuesta "1" --> "1" IAuditor
    private IAuditor auditor;

    public GestorOpcionesRespuesta(IOpcionRespuestaDAO opcionRespuestaDAO, IItemOpcionDAO itemOpcionDAO) {
        this.opcionRespuestaDAO = opcionRespuestaDAO;
        this.itemOpcionDAO = itemOpcionDAO;
    }

    // CU021 - Carga inicial de Opciones de Respuesta para el selector
    public List<OpcionRespuestaDTO> buscar(String criterios) {
        List<OpcionRespuesta> listaOpciones = opcionRespuestaDAO.buscar(criterios);
        List<OpcionRespuestaDTO> listaOpcionRespuestaDTO = new ArrayList<>();

        for (OpcionRespuesta o : listaOpciones) {
            OpcionRespuestaDTO opcDTO = armarOpcionRespuestaDTO(o);
            listaOpcionRespuestaDTO.add(opcDTO);
        }

        return listaOpcionRespuestaDTO;
    }

    public void alta(OpcionRespuestaDTO dto) {
    }

    public void modificar(OpcionRespuestaDTO dto) {
    }

    public void eliminar(Long id) {
    }

    private OpcionRespuestaDTO armarOpcionRespuestaDTO(OpcionRespuesta o) {
        Long id = o.getId();
        String nombre = o.getNombre();
        String descripcion = o.getDescripcion();

        // Busca los ItemOpcion asociados a la OpcionRespuesta
        List<ItemOpcion> listaItems = itemOpcionDAO.buscarPorOpcion(id);
        List<ItemOpcionDTO> listaItemOpcionDTO = new ArrayList<>();

        for (ItemOpcion i : listaItems) {
            ItemOpcionDTO itemDTO = new ItemOpcionDTO(i.getId(), i.getOrdenVisualizacion(), i.getDescripcion());
            listaItemOpcionDTO.add(itemDTO);
        }

        return new OpcionRespuestaDTO(id, nombre, descripcion, listaItemOpcionDTO);
    }

    private OpcionRespuesta mapearOpcionRespuesta(OpcionRespuestaDTO dto) {
        return null;
    }
}
