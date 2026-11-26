package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IResultadoDAO;
import com.tornidoscopio.tp.dominio.BloqueCuestionario;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.CompetenciaRequerida;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.ItemOpcion;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.RespuestaCandidato;
import com.tornidoscopio.tp.dominio.Resultado;
import com.tornidoscopio.tp.dominio.ResultadoCompetencia;
import com.tornidoscopio.tp.dto.BloqueCuestionarioDTO;
import com.tornidoscopio.tp.dto.CuestionarioDTO;
import com.tornidoscopio.tp.dto.ItemOpcionCuestionarioDTO;
import com.tornidoscopio.tp.dto.PreguntaCuestionarioDTO;
import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import com.tornidoscopio.tp.excepciones.EntidadNoEncontradaException;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestor responsable del Caso de Uso 026: Completar Cuestionario.
 */
@Service
public class GestorCompletarCuestionario {

    // GestorCompletarCuestionario "1" --> "1" ICuestionarioDAO
    private final ICuestionarioDAO cuestionarioDAO;
    // GestorCompletarCuestionario "1" --> "1" IResultadoDAO
    private final IResultadoDAO resultadoDAO;
    // GestorCompletarCuestionario "1" --> "1" RepositorioConfiguracion
    private final RepositorioConfiguracion repositorioConfiguracion;

    @Autowired
    public GestorCompletarCuestionario(
            ICuestionarioDAO cuestionarioDAO,
            IResultadoDAO resultadoDAO,
            RepositorioConfiguracion repositorioConfiguracion) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.resultadoDAO = resultadoDAO;
        this.repositorioConfiguracion = repositorioConfiguracion;
    }

    /**
     * CU026 - Paso 1 y Flujos Alternativos 1.A, 1.B y 1.C:
     * valida la clave de ingreso, el estado del cuestionario, el límite de accesos
     * y los tiempos máximos configurados antes de registrar el ingreso del candidato.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public CuestionarioDTO iniciarCuestionario(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ReglaNegocioException("Debe ingresar la clave de acceso al cuestionario.");
        }

        Cuestionario c = cuestionarioDAO.buscarPorClaveIngreso(token.trim());
        if (c == null) {
            throw new EntidadNoEncontradaException("No existe un cuestionario para la clave ingresada.");
        }

        if (c.getEstado() != EstadoCuestionario.ACTIVO && c.getEstado() != EstadoCuestionario.EN_PROCESO) {
            throw new ReglaNegocioException(
                    "El cuestionario no se encuentra en estado Activo o En Proceso (estado actual: "
                    + c.getEstado() + ").");
        }

        // Precondición 3: límite de accesos permitidos
        if (!Boolean.TRUE.equals(c.validarAcceso(repositorioConfiguracion.getLimiteAccesos()))) {
            c.actualizaEstado(EstadoCuestionario.INCOMPLETO);
            c.setFechaFin(LocalDateTime.now());
            cuestionarioDAO.modificar(c);
            throw new ReglaNegocioException(
                    "Se ha superado la cantidad máxima de accesos permitidos. El cuestionario pasó a estado Incompleto.");
        }

        if (c.getEstado() == EstadoCuestionario.ACTIVO) {
            // Flujo Alternativo 1.B: tiempo máximo en estado ACTIVO
            if (!Boolean.TRUE.equals(c.validarTiempoParaComenzar(repositorioConfiguracion.getTiempoMaxComenzar()))) {
                c.actualizaEstado(EstadoCuestionario.SIN_CONTESTAR);
                c.setFechaFin(LocalDateTime.now());
                cuestionarioDAO.modificar(c);
                throw new ReglaNegocioException(
                        "Se ha superado el tiempo máximo permitido para comenzar el cuestionario. El mismo pasó a estado Sin Contestar.");
            }
        } else {
            // Flujo Alternativo 1.A: tiempo máximo para completar desde el primer inicio
            if (!Boolean.TRUE.equals(c.validarTiempoParaCompletar(repositorioConfiguracion.getTiempoMaxCompletar()))) {
                c.actualizaEstado(EstadoCuestionario.INCOMPLETO);
                c.setFechaFin(LocalDateTime.now());
                cuestionarioDAO.modificar(c);
                throw new ReglaNegocioException(
                        "Se ha superado el tiempo máximo permitido para completar el cuestionario. El mismo pasó a estado Incompleto.");
            }
        }

        c.registrarIngreso();
        cuestionarioDAO.modificar(c);

        return armarCuestionarioDTO(c);
    }

    /**
     * CU026 - Paso 1: obtiene las instrucciones configuradas para el cuestionario.
     */
    public String obtenerInstrucciones() {
        return repositorioConfiguracion.obtenerInstrucciones();
    }

    /**
     * CU026 - Pasos 3, 4 y Flujos Alternativos 1.C / 8.A:
     * si el cuestionario está ACTIVO genera los bloques con preguntas aleatorias y pasa a EN_PROCESO;
     * si ya está EN_PROCESO recupera el próximo bloque pendiente sin responder.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public BloqueCuestionarioDTO obtenerSiguienteBloque(Long idCuestionario) {
        Cuestionario c = buscarCuestionarioObligatorio(idCuestionario);

        if (c.getEstado() == EstadoCuestionario.ACTIVO) {
            if (!Boolean.TRUE.equals(c.validarTiempoParaComenzar(repositorioConfiguracion.getTiempoMaxComenzar()))) {
                c.actualizaEstado(EstadoCuestionario.SIN_CONTESTAR);
                c.setFechaFin(LocalDateTime.now());
                cuestionarioDAO.modificar(c);
                throw new ReglaNegocioException(
                        "Se ha superado el tiempo máximo permitido para comenzar el cuestionario. El mismo pasó a estado Sin Contestar.");
            }
            c.generarBloques(repositorioConfiguracion.obtenerCantidadPreguntasPorBloque());
            c.setFechaHoraInicio(LocalDateTime.now());
            c.actualizaEstado(EstadoCuestionario.EN_PROCESO);
            cuestionarioDAO.modificar(c);
        } else if (c.getEstado() == EstadoCuestionario.EN_PROCESO) {
            if (!Boolean.TRUE.equals(c.validarTiempoParaCompletar(repositorioConfiguracion.getTiempoMaxCompletar()))) {
                c.actualizaEstado(EstadoCuestionario.INCOMPLETO);
                c.setFechaFin(LocalDateTime.now());
                cuestionarioDAO.modificar(c);
                throw new ReglaNegocioException(
                        "Se ha superado el tiempo máximo permitido para completar el cuestionario. El mismo pasó a estado Incompleto.");
            }
        } else {
            throw new ReglaNegocioException(
                    "El cuestionario no se encuentra en estado Activo o En Proceso (estado actual: "
                    + c.getEstado() + ").");
        }

        BloqueCuestionario pendiente = c.obtenerBloquePendiente();
        if (pendiente == null) {
            throw new ReglaNegocioException("No existen bloques pendientes por responder en el cuestionario.");
        }

        return armarBloqueDTO(pendiente);
    }

    /**
     * CU026 - Pasos 5, 6, 6.A y 7:
     * valida que todas las preguntas del bloque pendiente hayan sido respondidas,
     * persiste las respuestas y marca el bloque como completado.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public Boolean registrarBloqueRespuestas(Long idCuestionario, List<RespuestaCandidatoDTO> r) {
        Cuestionario c = buscarCuestionarioObligatorio(idCuestionario);

        if (c.getEstado() != EstadoCuestionario.EN_PROCESO) {
            throw new ReglaNegocioException(
                    "Solo se pueden registrar respuestas cuando el cuestionario está En Proceso.");
        }

        if (!Boolean.TRUE.equals(c.validarTiempoParaCompletar(repositorioConfiguracion.getTiempoMaxCompletar()))) {
            c.actualizaEstado(EstadoCuestionario.INCOMPLETO);
            c.setFechaFin(LocalDateTime.now());
            cuestionarioDAO.modificar(c);
            throw new ReglaNegocioException(
                    "Se ha superado el tiempo máximo permitido para completar el cuestionario. El mismo pasó a estado Incompleto.");
        }

        BloqueCuestionario bloqueActual = c.obtenerBloquePendiente();
        if (bloqueActual == null) {
            throw new ReglaNegocioException("No existen bloques pendientes por responder.");
        }

        if (!Boolean.TRUE.equals(bloqueActual.validarRespuestasCompletas(r))) {
            List<String> faltantes = obtenerPreguntasSinResponder(bloqueActual, r);
            if (!faltantes.isEmpty()) {
                throw new ReglaNegocioException(
                        "Debe responder todas las preguntas del bloque. Preguntas sin responder: "
                        + String.join("; ", faltantes));
            }
            throw new ReglaNegocioException(
                    "Una o más opciones de respuesta seleccionadas no pertenecen a su pregunta.");
        }

        bloqueActual.registrarRespuestas(r);
        bloqueActual.marcarCompletado();
        cuestionarioDAO.modificar(c);

        return true;
    }

    /**
     * CU026 - Paso 8 / Obs (3):
     * calcula y persiste los resultados del cuestionario por competencia y cambia su estado a COMPLETO.
     */
    @Transactional
    public void finalizarCuestionario(Long idCuestionario) {
        Cuestionario c = buscarCuestionarioObligatorio(idCuestionario);

        if (c.getEstado() != EstadoCuestionario.EN_PROCESO) {
            throw new ReglaNegocioException(
                    "Solo se puede finalizar un cuestionario que se encuentre En Proceso.");
        }

        if (c.obtenerBloquePendiente() != null) {
            throw new ReglaNegocioException(
                    "No se puede finalizar el cuestionario porque aún existen bloques sin responder.");
        }

        Resultado resultado = calcularResultado(c);
        c.asociarResultado(resultado);
        c.setFechaFin(LocalDateTime.now());
        c.actualizaEstado(EstadoCuestionario.COMPLETO);

        resultadoDAO.crear(resultado);
        cuestionarioDAO.modificar(c);
    }

    private Cuestionario buscarCuestionarioObligatorio(Long idCuestionario) {
        if (idCuestionario == null) {
            throw new ReglaNegocioException("Debe indicar el identificador del cuestionario.");
        }
        Cuestionario c = cuestionarioDAO.buscarPorId(idCuestionario);
        if (c == null) {
            throw new EntidadNoEncontradaException("No se encontró el cuestionario con id " + idCuestionario + ".");
        }
        return c;
    }

    private Resultado calcularResultado(Cuestionario c) {
        Resultado resultado = Resultado.builder()
                .fechaCalculo(LocalDateTime.now())
                .resultadosCompetencia(new ArrayList<>())
                .build();

        Set<Object> competenciasEvaluadas = obtenerCompetenciasEvaluadas(c);
        if (c.getPuesto() != null && c.getPuesto().getCompetenciasRequeridas() != null) {
            for (CompetenciaRequerida cr : c.getPuesto().getCompetenciasRequeridas()) {
                Competencia competencia = cr.getCompetencia();
                if (competencia == null || !Boolean.TRUE.equals(competencia.getActivo())) {
                    continue;
                }
                Object claveCompetencia = obtenerClaveCompetencia(competencia);
                if (!competenciasEvaluadas.contains(claveCompetencia)) {
                    // CU026 Obs (1): si la competencia no tuvo factores evaluables en el cuestionario, no se evalúa
                    continue;
                }
                Double puntajeObtenido = c.calcularPuntajeCompetencia(competencia);
                Double puntajeRequerido = 0.0;
                if (cr.getPuntajeMinimoRequerido() != null) {
                    puntajeRequerido = cr.getPuntajeMinimoRequerido();
                }

                ResultadoCompetencia rc = ResultadoCompetencia.builder()
                        .competencia(competencia)
                        .puntajeObtenido(puntajeObtenido)
                        .puntajeRequerido(puntajeRequerido)
                        .build();
                resultado.agregarResultadoCompetencia(rc);
            }
        }

        return resultado;
    }

    private Set<Object> obtenerCompetenciasEvaluadas(Cuestionario c) {
        Set<Object> evaluadas = new HashSet<>();
        if (c.getBloques() == null) {
            return evaluadas;
        }
        for (BloqueCuestionario bloque : c.getBloques()) {
            if (bloque.getRespuestas() == null) {
                continue;
            }
            for (RespuestaCandidato rc : bloque.getRespuestas()) {
                if (rc.getPregunta() == null || rc.getPregunta().getFactor() == null) {
                    continue;
                }
                Competencia competencia = rc.getPregunta().getFactor().getCompetencia();
                if (competencia != null) {
                    evaluadas.add(obtenerClaveCompetencia(competencia));
                }
            }
        }
        return evaluadas;
    }

    private Object obtenerClaveCompetencia(Competencia competencia) {
        if (competencia.getId() != null) {
            return competencia.getId();
        }
        return competencia;
    }

    private CuestionarioDTO armarCuestionarioDTO(Cuestionario c) {
        return CuestionarioDTO.builder()
                .id(c.getId())
                .estado(c.getEstado())
                .tiempoMaxCompletar(repositorioConfiguracion.getTiempoMaxCompletar())
                .build();
    }

    private BloqueCuestionarioDTO armarBloqueDTO(BloqueCuestionario b) {
        boolean esUltimo = b.getCuestionario() != null
                && Boolean.TRUE.equals(b.getCuestionario().esUltimoBloque(b));

        List<PreguntaCuestionarioDTO> preguntasDTO = new ArrayList<>();
        if (b.getRespuestas() != null) {
            List<RespuestaCandidato> ordenadas = b.getRespuestas().stream()
                    .sorted(Comparator.comparing(RespuestaCandidato::getOrdenPregunta,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            for (RespuestaCandidato rc : ordenadas) {
                Pregunta pregunta = rc.getPregunta();
                if (pregunta == null) {
                    continue;
                }
                List<ItemOpcionCuestionarioDTO> opcionesDTO = new ArrayList<>();
                if (pregunta.getOpcionRespuesta() != null
                        && pregunta.getOpcionRespuesta().getItems() != null) {
                    pregunta.getOpcionRespuesta().getItems().stream()
                            .sorted(Comparator.comparing(ItemOpcion::getOrdenVisualizacion,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                            .forEach(item -> opcionesDTO.add(ItemOpcionCuestionarioDTO.builder()
                                    .id(item.getId())
                                    .descripcion(item.getDescripcion())
                                    .build()));
                }

                preguntasDTO.add(PreguntaCuestionarioDTO.builder()
                        .id(pregunta.getId())
                        .enunciado(pregunta.getEnunciado())
                        .opciones(opcionesDTO)
                        .build());
            }
        }

        return BloqueCuestionarioDTO.builder()
                .nroBloque(b.getNroBloque())
                .esUltimo(esUltimo)
                .preguntas(preguntasDTO)
                .build();
    }

    private List<String> obtenerPreguntasSinResponder(BloqueCuestionario bloque, List<RespuestaCandidatoDTO> respuestas) {
        if (bloque == null || bloque.getRespuestas() == null) {
            return List.of();
        }
        Set<Long> respondidas = new HashSet<>();
        if (respuestas != null) {
            for (RespuestaCandidatoDTO dto : respuestas) {
                if (dto != null && dto.getPreguntaId() != null && dto.getItemOpcionId() != null) {
                    respondidas.add(dto.getPreguntaId());
                }
            }
        }
        return bloque.getRespuestas().stream()
                .sorted(Comparator.comparing(RespuestaCandidato::getOrdenPregunta,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .filter(rc -> rc.getPregunta() == null
                        || rc.getPregunta().getId() == null
                        || !respondidas.contains(rc.getPregunta().getId()))
                .map(rc -> {
                    if (rc.getPregunta() != null) {
                        return rc.getPregunta().getEnunciado();
                    }
                    return "Pregunta";
                })
                .toList();
    }
}
