package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICompetenciaDAO;
import com.tornidoscopio.tp.dao.IFactorDAO;
import com.tornidoscopio.tp.dao.IOpcionRespuestaDAO;
import com.tornidoscopio.tp.dao.IPreguntaDAO;
import com.tornidoscopio.tp.dao.IPreguntaOpcionDAO;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.ItemOpcion;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
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

    // Longitud máxima según esquema_capital.sql (pregunta.nombre VARCHAR(150)).
    // enunciado y descripcion son TEXT: sin límite.
    private static final int LONGITUD_MAXIMA_NOMBRE = 150;

    // Suma exigida para las ponderaciones de una pregunta
    private static final double SUMA_PONDERACIONES = 10.0;
    private static final double TOLERANCIA = 0.000001;

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

    /**
     * CU021 - Datos válidos: valida duplicados (2.C) y, si no existen, crea la Pregunta (Paso 3).
     *
     * @throws PreguntaDuplicadaException si ya existe una pregunta con ese nombre y/o enunciado
     *                                    para el factor (y por ende la competencia) seleccionado.
     */
    @Transactional
    public PreguntaDTO altaPregunta(PreguntaDTO p) {
        // ---- Validación 2.C: la pregunta ya existe para esa competencia y factor ----
        List<Pregunta> listaExistentes = preguntaDAO.buscarDuplicada(p.getIdFactor(), p.getNombre(), p.getEnunciado());

        if (!listaExistentes.isEmpty()) {
            // 2.C.1 Se clarifica cuál es la validación no superada (nombre, enunciado o ambos)
            String mensaje = armarMensajeDuplicado(listaExistentes, p);
            throw new PreguntaDuplicadaException(mensaje);
        }

        // ---- No existe pregunta duplicada (Paso 3) ----
        Pregunta pregunta = mapearPregunta(p);
        preguntaDAO.crear(pregunta);

        Long idPregunta = pregunta.getId();
        p.setId(idPregunta);

        return p;
    }

    public void modificarPregunta(PreguntaDTO p) {
    }

    public void eliminarPregunta(Long idPregunta) {
    }

    // CU021 - Flujo Alternativo 1.A.1: inicia CU033 - Generar pregunta con IA
    public PreguntaSugeridaDTO generarPreguntaIA(Long idCompetencia, Long idFactor, String descripcion, Long idOpcionRespuesta) {
        Competencia competencia = competenciaDAO.buscarPorId(idCompetencia);
        Factor factor = factorDAO.buscarPorId(idFactor);
        OpcionRespuesta opcion = opcionRespuestaDAO.buscarPorId(idOpcionRespuesta);

        return servicioIA.generarPregunta(competencia, factor, descripcion, opcion);
    }

    // CU021 - Asigna las ponderaciones de cada ItemOpcion a la Pregunta creada
    @Transactional
    public void asignarOpciones(Long idPregunta, List<PreguntaOpcionDTO> opc) {
        for (PreguntaOpcionDTO opcionDTO : opc) {
            PreguntaOpcion po = mapearPreguntaOpcion(idPregunta, opcionDTO);
            preguntaOpcionDAO.crear(po);
        }
    }

    // CU021 - Validación 2.A: omisiones de datos obligatorios
    public List<String> validarOmisiones(PreguntaDTO p) {
        List<String> listaOmisiones = new ArrayList<>();

        if (p.getNombre() == null || p.getNombre().isBlank()) {
            listaOmisiones.add("Debe ingresar el nombre de la pregunta");
        }
        if (p.getEnunciado() == null || p.getEnunciado().isBlank()) {
            listaOmisiones.add("Debe ingresar el enunciado de la pregunta");
        }
        if (p.getIdFactor() == null) {
            listaOmisiones.add("Debe seleccionar la competencia y el factor de la pregunta");
        }
        if (p.getIdOpcionRespuesta() == null) {
            listaOmisiones.add("Debe seleccionar la opción de respuesta de la pregunta");
        }

        return listaOmisiones;
    }

    // CU021 - Validación 2.B: longitud máxima de los campos
    public List<String> validarLongitudMaxima(PreguntaDTO p) {
        List<String> listaExcedidos = new ArrayList<>();

        if (p.getNombre() != null && p.getNombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            listaExcedidos.add("El nombre de la pregunta no puede superar los "
                    + LONGITUD_MAXIMA_NOMBRE + " caracteres");
        }

        return listaExcedidos;
    }

    // CU021 - Validación de ponderaciones: suma = 10 y valores >= 0
    public Boolean validarPonderacion(List<PreguntaOpcionDTO> opc) {
        if (opc == null || opc.isEmpty()) {
            return false;
        }

        double suma = 0;
        for (PreguntaOpcionDTO opcionDTO : opc) {
            Double ponderacion = opcionDTO.getPonderacion();
            if (ponderacion == null || ponderacion < 0) {
                return false;
            }
            suma += ponderacion;
        }

        return Math.abs(suma - SUMA_PONDERACIONES) < TOLERANCIA;
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

    // CU021 - 2.C.1: indica si la validación no superada es el nombre, el enunciado o ambos
    private String armarMensajeDuplicado(List<Pregunta> existentes, PreguntaDTO p) {
        boolean nombreDuplicado = false;
        boolean enunciadoDuplicado = false;

        // Misma comparación que IPreguntaDAO.buscarDuplicada (sin distinguir mayúsculas ni espacios extremos)
        for (Pregunta existente : existentes) {
            if (existente.getNombre().trim().equalsIgnoreCase(p.getNombre().trim())) {
                nombreDuplicado = true;
            }
            if (existente.getEnunciado().trim().equalsIgnoreCase(p.getEnunciado().trim())) {
                enunciadoDuplicado = true;
            }
        }

        String campos;
        if (nombreDuplicado && enunciadoDuplicado) {
            campos = "ese NOMBRE y ese ENUNCIADO";
        } else if (nombreDuplicado) {
            campos = "ese NOMBRE";
        } else {
            campos = "ese ENUNCIADO";
        }

        return "Ya existe una pregunta con " + campos + " para la competencia y factor seleccionados";
    }

    // CU021 - Paso 3: crea la Pregunta con su Factor y su OpcionRespuesta
    private Pregunta mapearPregunta(PreguntaDTO dto) {
        Factor factor = factorDAO.buscarPorId(dto.getIdFactor());
        OpcionRespuesta o = opcionRespuestaDAO.buscarPorId(dto.getIdOpcionRespuesta());

        return Pregunta.builder()
                .nombre(dto.getNombre())
                .enunciado(dto.getEnunciado())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .factor(factor)
                .opcionRespuesta(o)
                .build();
    }

    // CU021 - crea la PreguntaOpcion (idPregunta, idItemOpcion, ponderacion)
    private PreguntaOpcion mapearPreguntaOpcion(Long idPregunta, PreguntaOpcionDTO dto) {
        return PreguntaOpcion.builder()
                .pregunta(Pregunta.builder().id(idPregunta).build())
                .itemOpcion(ItemOpcion.builder().id(dto.getIdItemOpcion()).build())
                .ponderacion(dto.getPonderacion())
                .build();
    }
}
