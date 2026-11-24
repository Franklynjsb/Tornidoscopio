package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICompetenciaDAO;
import com.tornidoscopio.tp.dao.IFactorDAO;
import com.tornidoscopio.tp.dao.IOpcionRespuestaDAO;
import com.tornidoscopio.tp.dao.IPreguntaDAO;
import com.tornidoscopio.tp.dao.IPreguntaOpcionDAO;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.PreguntaOpcion;
import com.tornidoscopio.tp.dto.PreguntaDTO;
import com.tornidoscopio.tp.dto.PreguntaOpcionDTO;
import com.tornidoscopio.tp.dto.PreguntaSugeridaDTO;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GestorPreguntas {

    // GestorPreguntas "1" --> "1" IPreguntaDAO
    @Autowired
    private IPreguntaDAO preguntaDAO;
    // GestorPreguntas "1" --> "1" IPreguntaOpcionDAO
    @Autowired(required = false)
    private IPreguntaOpcionDAO preguntaOpcionDAO;
    // GestorPreguntas "1" --> "1" IFactorDAO
    @Autowired(required = false)
    private IFactorDAO factorDAO;
    // GestorPreguntas "1" --> "1" ICompetenciaDAO
    @Autowired(required = false)
    private ICompetenciaDAO competenciaDAO;
    // GestorPreguntas "1" --> "1" IOpcionRespuestaDAO
    @Autowired(required = false)
    private IOpcionRespuestaDAO opcionRespuestaDAO;
    // GestorPreguntas "1" --> "1" IAuditor
    @Autowired(required = false)
    private IAuditor auditor;
    // GestorPreguntas "1" --> "1" IServicioIA
    @Autowired(required = false)
    private IServicioIA servicioIA;

    // =========================================================================
    // CU020 - Gestionar Preguntas: Búsqueda de preguntas con filtros
    // =========================================================================
    @Transactional(readOnly = true)
    public List<PreguntaDTO> buscarPreguntas(String competencia, String factor, String nombrePregunta) {
        // 1. Validación Paso 3: al menos uno de los criterios de búsqueda debe estar cargado
        Boolean criteriosValidos = this.validarCriterios(competencia, factor, nombrePregunta);

        if (!criteriosValidos) {
            throw new ReglaNegocioException("Debe ingresar al menos un criterio de búsqueda");
        }

        // 2. Búsqueda en el DAO
        List<Pregunta> listaPreguntas = preguntaDAO.buscar(competencia, factor, nombrePregunta);

        // 3. Construcción de la lista de DTOs
        List<PreguntaDTO> listaPreguntaDTO = new ArrayList<>();

        for (Pregunta p : listaPreguntas) {
            PreguntaDTO dto = this.armarPreguntaDTO(p);
            listaPreguntaDTO.add(dto);
        }

        return listaPreguntaDTO;
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

    public List<String> validarOmisiones(PreguntaDTO p) {
        return null;
    }

    public List<String> validarLongitudMaxima(PreguntaDTO p) {
        return null;
    }

    public Boolean validarPonderacion(List<PreguntaOpcionDTO> opc) {
        return null;
    }

    // =========================================================================
    // CU020 - Gestionar Preguntas: Métodos auxiliares (validarCriterios y armarPreguntaDTO)
    // =========================================================================
    private Boolean validarCriterios(String competencia, String factor, String nombrePregunta) {
        boolean tieneCompetencia = competencia != null && !competencia.trim().isEmpty();
        boolean tieneFactor = factor != null && !factor.trim().isEmpty();
        boolean tieneNombrePregunta = nombrePregunta != null && !nombrePregunta.trim().isEmpty();

        return tieneCompetencia || tieneFactor || tieneNombrePregunta;
    }

    // CU020 - Gestionar Preguntas: Construcción de PreguntaDTO a partir de la entidad Pregunta
    private PreguntaDTO armarPreguntaDTO(Pregunta p) {
        // ---- Extracción de datos básicos de la Pregunta ----
        Long id = p.getId();
        String nombre = p.getNombre();

        // ---- Navegabilidad hacia el Factor ----
        Factor f = p.getFactor();
        String nombreFactor = f.getNombre();

        // ---- Navegabilidad hacia la Competencia ----
        Competencia c = f.getCompetencia();
        String nombreCompetencia = c.getNombre();

        // ---- Creación del DTO ----
        return PreguntaDTO.builder()
                .id(id)
                .nombre(nombre)
                .nombreCompetencia(nombreCompetencia)
                .nombreFactor(nombreFactor)
                .build();
    }

    private String armarMensajeDuplicado(List<Pregunta> existentes, PreguntaDTO p) {
        return null;
    }

    private Pregunta mapearPregunta(PreguntaDTO dto) {
        return null;
    }

    private PreguntaOpcion mapearPreguntaOpcion(Long idPregunta, PreguntaOpcionDTO dto) {
        return null;
    }
}
