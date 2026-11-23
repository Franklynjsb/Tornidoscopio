package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.IPreguntaDAO;
import com.tornidoscopio.tp.dao.IPreguntaOpcionDAO;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.PreguntaOpcion;
import com.tornidoscopio.tp.dto.PreguntaDTO;
import com.tornidoscopio.tp.dto.PreguntaOpcionDTO;
import com.tornidoscopio.tp.dto.PreguntaSugeridaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorPreguntas {

    // GestorPreguntas "1" --> "1" IPreguntaDAO
    private IPreguntaDAO preguntaDAO;
    // GestorPreguntas "1" --> "1" IPreguntaOpcionDAO
    private IPreguntaOpcionDAO preguntaOpcionDAO;
    // GestorPreguntas "1" --> "1" IAuditor
    private IAuditor auditor;
    // GestorPreguntas ..> IServicioIA
    private IServicioIA servicioIA;

    public List<PreguntaDTO> buscarPreguntas(String competencia, String factor, String nombrePregunta) {
        return null;
    }

    public PreguntaDTO altaPregunta(PreguntaDTO p) {
        return null;
    }

    public void modificarPregunta(PreguntaDTO p) {
    }

    public void eliminarPregunta(Long idPregunta) {
    }

    public PreguntaSugeridaDTO generarPreguntaIA(Long idCompetencia, Long idFactor, String descripcion, Long idOpcionRespuesta) {
        return null;
    }

    public void asignarOpciones(Long idPregunta, List<PreguntaOpcionDTO> opc) {
    }

    public Boolean validarPonderacion(List<PreguntaOpcionDTO> opc) {
        return null;
    }

    private Boolean validarCriterios(String competencia, String factor, String nombrePregunta) {
        return null;
    }

    private PreguntaDTO armarPreguntaDTO(Pregunta p) {
        return null;
    }

    private Pregunta mapearPregunta(PreguntaDTO dto) {
        return null;
    }

    private PreguntaOpcion mapearPreguntaOpcion(Long idPregunta, PreguntaOpcionDTO dto) {
        return null;
    }
}
