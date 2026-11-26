package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IResultadoDAO;
import com.tornidoscopio.tp.dominio.*;
import com.tornidoscopio.tp.dto.OrdenMeritoDTO;
import com.tornidoscopio.tp.dto.ResultadoComparativoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.ArrayList;

/**
 * CU028 - Emitir orden de mérito: obtiene las evaluaciones (fechas) de un puesto y
 * genera el listado de candidatos ordenado por mérito.
 */

@Service
public class GestorReportes {

    // GestorReportes "1" --> "1" IResultadoDAO
    private IResultadoDAO resultadoDAO;
    // GestorReportes "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;
    // GestorReportes "1" --> "1" ICandidatoDAO
    private ICandidatoDAO candidatoDAO;

    /* Inyecta el DAO de cuestionarios */
    @Autowired
    public GestorReportes(ICuestionarioDAO cuestionarioDAO) {
        this.cuestionarioDAO = cuestionarioDAO;
    }

    public File exportarResultados(Long idPuesto) {
        return null;
    }

    /**
     * CU028 - Pasos 7 a 9: genera el listado de orden de mérito de un puesto.
     * Orden del listado:
     * 1) Candidatos COMPLETO que alcanzan los puntajes mínimos, por puntaje descendente.
     * 2) Candidatos COMPLETO que no alcanzan los mínimos, por puntaje descendente.
     * 3) Resto de candidatos (INCOMPLETO, SIN_CONTESTAR, etc.), agrupados por estado.
     * @param idPuesto id del puesto evaluado
     * @param fechas   fechas de creación de las evaluaciones elegidas por el consultor
     * @return lista de {@link OrdenMeritoDTO} ya ordenada
     */
    // readOnly = true: solo lee, y mantiene abierta la sesión para el lazy loading
    // de q.getResultado() y getResultadosCompetencia()
    @Transactional(readOnly = true)
    public List<OrdenMeritoDTO> generarOrdenMerito(Long idPuesto, List<LocalDateTime> fechas) {
        List<Cuestionario> listaCuestionarios = cuestionarioDAO.buscarPorPuesto(idPuesto);
        List<OrdenMeritoDTO> listaOrdenMeritoDTO = new ArrayList<>();

        for (Cuestionario q : listaCuestionarios) {
            // Solo interesan los cuestionarios de las evaluaciones seleccionadas
            if (!fechas.contains(q.getFechaCreacion())) {
                continue;
            }
            OrdenMeritoDTO ordenDTO = armarOrdenMeritoDTO(q);
            // Un candidato cumple los mínimos si su cuestionario está COMPLETO y en
            // TODAS las competencias obtuvo al menos el puntaje requerido por el puesto
            boolean cumpleMinimos = false;
            if (q.getEstado() == EstadoCuestionario.COMPLETO) {
                cumpleMinimos = true;
                for (ResultadoCompetencia rc : q.getResultado().getResultadosCompetencia()) {
                    if (rc.getPuntajeObtenido() < rc.getPuntajeRequerido()) {
                        cumpleMinimos = false;
                    }
                }
            }
            // Lo inserta directamente en la posición que le corresponde
            insertarSegunOrden(listaOrdenMeritoDTO, ordenDTO, cumpleMinimos);
        }
        return listaOrdenMeritoDTO;
    }

    public ResultadoComparativoDTO generarReporteComparativo(Long idPuesto, Long idCandidato) {
        return null;
    }

    /**
     * CU028 - Paso 6: devuelve las fechas de creación (sin repetir) de las evaluaciones
     * de un puesto. Si la lista viene vacía, el puesto no tiene evaluaciones (flujo 6.A).
     */
    @Transactional(readOnly = true)
    public List<LocalDateTime> obtenerFechasEvaluaciones(Long idPuesto) {
        List<Cuestionario> listaCuestionarios = cuestionarioDAO.buscarPorPuesto(idPuesto);
        List<LocalDateTime> listaFechas = new ArrayList<>();
        for (Cuestionario q : listaCuestionarios) {
            LocalDateTime fechaCreacion = q.getFechaCreacion();
            // Los cuestionarios de una misma evaluación comparten la fecha de creación,
            // por eso se agrega solo una vez
            if (!listaFechas.contains(fechaCreacion)) {
                listaFechas.add(fechaCreacion);
            }
        }
        return listaFechas;
    }

    /**
     * Arma el DTO de una fila del listado a partir de un cuestionario.
     * (1) El puntaje total solo se calcula si el cuestionario está COMPLETO; en los demás
     * estados queda en null (no se muestra).
     * (2) La fecha a mostrar es la de fin si está COMPLETO, o la del último ingreso si no.
     */
    private OrdenMeritoDTO armarOrdenMeritoDTO(Cuestionario q) {
        Candidato ca = q.getCandidato();
        EstadoCuestionario estado = q.getEstado();

        Double puntajeTotal = null;              // (1) solo se muestra si está COMPLETO
        LocalDateTime fechaFinOUltimoIngreso;    // (2) fin si COMPLETO, último ingreso si no
        // Valor inicial: el valor real lo calcula generarOrdenMerito y se asigna
        // en insertarSegunOrden mediante setCumpleMinimos
        Boolean cumpleMinimos = false;

        if (estado == EstadoCuestionario.COMPLETO) {
            fechaFinOUltimoIngreso = q.getFechaFin();
            // El puntaje total es la suma de los puntajes obtenidos en cada competencia
            puntajeTotal = 0.0;
            for (ResultadoCompetencia rc : q.getResultado().getResultadosCompetencia()) {
                puntajeTotal += rc.getPuntajeObtenido();
            }
        } else {
            fechaFinOUltimoIngreso = q.getFechaUltimoIngreso();
        }
        return new OrdenMeritoDTO(
                ca.getNombre(),
                ca.getApellido(),
                ca.getTipoDocumento(),
                ca.getNroDocumento(),
                puntajeTotal,
                estado,
                q.getFechaHoraInicio(),
                fechaFinOUltimoIngreso,
                q.getCantAccesos(),
                cumpleMinimos);
    }

    private ResultadoComparativoDTO armarResultadoComparativoDTO(Resultado r) {
        return null;
    }

    /**
     * Inserta un candidato en la lista en la posición que le corresponde, de modo que la
     * lista quede siempre ordenada. Criterios, en este orden:
     * Grupo: 0 = COMPLETO que cumple mínimos, 1 = COMPLETO que no cumple, 2 = resto.
     * Estado del cuestionario (agrupa al resto de candidatos por estado).
     * Puntaje obtenido, de mayor a menor.
     * @param cumpleMinimos si el candidato alcanzó los puntajes mínimos del puesto;
     * se guarda en el DTO para poder comparar contra los ya insertados
     */

    private void insertarSegunOrden(List<OrdenMeritoDTO> lista, OrdenMeritoDTO dto, Boolean cumpleMinimos) {
        // Se guarda en el DTO: al recorrer la lista, los DTOs ya insertados necesitan
        // conocer su grupo para compararlos con el nuevo
        dto.setCumpleMinimos(cumpleMinimos);
        // Devuelve el grupo de un DTO (menor número = va antes en el listado)
        ToIntFunction<OrdenMeritoDTO> grupo = d -> {
            if (d.getEstadoCuestionario() != EstadoCuestionario.COMPLETO) {
                return 2; // INCOMPLETO, SIN_CONTESTAR, etc.
            }
            if (Boolean.TRUE.equals(d.getCumpleMinimos())) {
                return 0; // COMPLETO que cumple mínimos (orden de mérito)
            }
            return 1; // COMPLETO que no alcanza los mínimos
        };

        // Comparador con tres criterios encadenados: grupo, estado y puntaje
        Comparator<OrdenMeritoDTO> orden = Comparator
                .comparingInt(grupo)
                // Entre los COMPLETO todos valen 0 (no los separa); el resto se agrupa por estado
                .thenComparingInt(d -> {
                    if (d.getEstadoCuestionario() == EstadoCuestionario.COMPLETO) {
                        return 0;
                    }
                    return d.getEstadoCuestionario().ordinal();
                })
                // Puntaje descendente; si no tiene puntaje (null) se toma como 0
                .thenComparingDouble(d -> {
                    if (d.getPuntajeTotalObtenido() == null) {
                        return 0.0;
                    }
                    return -d.getPuntajeTotalObtenido();   // el signo menos = puntaje descendente
                });
        // Avanza hasta encontrar el primer elemento que debe ir después del nuevo
        int i = 0;
        while (i < lista.size() && orden.compare(lista.get(i), dto) <= 0) {
            i++;
        }
        // Inserta en esa posición, manteniendo la lista ordenada
        lista.add(i, dto);
    }
}