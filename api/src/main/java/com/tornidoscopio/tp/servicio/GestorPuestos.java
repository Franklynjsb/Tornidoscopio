package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IEmpresaDAO;
import com.tornidoscopio.tp.dao.IPuestoDAO;
import com.tornidoscopio.tp.dominio.*;
import com.tornidoscopio.tp.dto.CompetenciaRequeridaDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import com.tornidoscopio.tp.excepciones.CompetenciasNoEvaluablesException;
import com.tornidoscopio.tp.excepciones.EntidadNoEncontradaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gestor de lógica de negocio para puestos o funciones.
 * Incluye las operaciones requeridas por el CU025 (búsqueda de puestos y
 * obtención/validación de competencias requeridas evaluables).
 */
@Service
public class GestorPuestos {

    private static final int MINIMO_PREGUNTAS_POR_FACTOR = 2;

    // GestorPuestos "1" --> "1" IPuestoDAO
    private IPuestoDAO puestoDAO;
    // GestorPuestos "1" --> "1" IEmpresaDAO
    private IEmpresaDAO empresaDAO;
    // GestorPuestos "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;
    // GestorPuestos "1" --> "1" IAuditor
    private IAuditor auditor;

    @Autowired
    public GestorPuestos(IPuestoDAO puestoDAO, ICuestionarioDAO cuestionarioDAO) {
        this.puestoDAO = puestoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
    }

    /**
     * CU025 - Paso 8: busca puestos activos que coincidan con el criterio
     * y los retorna mapeados como {@link PuestoDTO}.
     *
     * @param criterios texto de búsqueda (código, nombre o empresa); si es vacío retorna todos los puestos activos
     * @return lista de puestos encontrados
     */
    @Transactional(readOnly = true)
    public List<PuestoDTO> buscarPuestos(String criterios) {
        List<Puesto> listaPuestos = puestoDAO.buscar(criterios);
        return listaPuestos.stream()
                .map(this::armarPuestoDTO)
                .toList();
    }

    public void nuevoPuesto(PuestoDTO p) {
    }

    public void modificarPuesto(PuestoDTO p) {
    }

    public void eliminarPuesto(Long idPuesto) {
    }

    public void definirCompetencias(Long idPuesto, List<CompetenciaRequeridaDTO> c) {
    }

    public Boolean tieneCuestionariosGenerados(Long idPuesto) {
        return null;
    }

    /**
     * CU025 - Paso 9 y Flujo Alternativo 9.A: recupera las competencias requeridas
     * de un puesto y valida que cada una posea al menos un factor activo con 2 o más
     * preguntas activas.
     *
     * @param idPuesto identificador del puesto seleccionado
     * @return lista de competencias requeridas con su puntaje mínimo requerido
     * @throws IllegalArgumentException          si {@code idPuesto} es nulo
     * @throws EntidadNoEncontradaException      si el puesto no existe o está inactivo
     * @throws CompetenciasNoEvaluablesException si alguna competencia no posee un factor evaluable (9.A)
     */
    @Transactional(readOnly = true)
    public List<CompetenciaRequeridaDTO> obtenerCompetenciasRequeridas(Long idPuesto) {
        if (idPuesto == null) {
            throw new IllegalArgumentException("Debe seleccionar un puesto");
        }

        Puesto puesto = Optional.ofNullable(puestoDAO.buscarPorId(idPuesto))
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontró un puesto activo con id " + idPuesto));

        List<CompetenciaRequerida> listaCompReq = Optional.ofNullable(puesto.getCompetenciasRequeridas())
                .orElse(List.of());

        List<String> listaSinFactor = validarFactoresEvaluables(listaCompReq);
        if (!listaSinFactor.isEmpty()) {
            throw new CompetenciasNoEvaluablesException(listaSinFactor);
        }

        return listaCompReq.stream()
                .map(this::armarCompetenciaRequeridaDTO)
                .toList();
    }

    /**
     * CU025 - Validación 9.A: verifica que cada competencia requerida posea al menos
     * un factor activo con al menos {@value #MINIMO_PREGUNTAS_POR_FACTOR} preguntas activas.
     *
     * @param list lista de competencias requeridas del puesto
     * @return nombres de las competencias que no pueden ser evaluadas (lista vacía si todas son válidas)
     */
    private List<String> validarFactoresEvaluables(List<CompetenciaRequerida> list) {
        List<String> listaSinFactor = new ArrayList<>();

        for (CompetenciaRequerida cr : list) {
            Competencia comp = cr.getCompetencia();
            if (comp == null) {
                continue;
            }

            List<Factor> listaFactores = Optional.ofNullable(comp.getFactores()).orElse(List.of());

            boolean tieneFactorEvaluable = !Boolean.FALSE.equals(comp.getActivo())
                    && listaFactores.stream()
                    .filter(f -> Boolean.TRUE.equals(f.getActivo()))
                    .anyMatch(f -> {
                        Integer cantActivas = f.cantidadPreguntasActivas();
                        return cantActivas != null && cantActivas >= MINIMO_PREGUNTAS_POR_FACTOR;
                    });

            if (!tieneFactorEvaluable) {
                listaSinFactor.add(comp.getNombre());
            }
        }

        return listaSinFactor;
    }

    /**
     * Convierte un Puesto en un PuestoDTO para mostrarlo en pantalla.
     * Además de los datos del puesto, calcula dos cantidades a partir de sus cuestionarios
     * CU028 - Paso 4, columnas "Candidatos" y "Evaluaciones Completas":
     * cantCandidatos: total de cuestionarios del puesto (uno por candidato evaluado).
     * cantEvaluacionesCompletas: cuántos de esos cuestionarios están en estado COMPLETO.
     * @param p puesto a convertir
     * @return DTO con los datos del puesto y sus cantidades
     */

    private PuestoDTO armarPuestoDTO(Puesto p) {
        // Un cuestionario por cada candidato evaluado en este puesto
        List<Cuestionario> listaCuestionarios = cuestionarioDAO.buscarPorPuesto(p.getId());
        int cantCandidatos = listaCuestionarios.size();
        // Se cuentan solo los cuestionarios que el candidato terminó de responder
        int cantEvaluacionesCompletas = 0;
        for (Cuestionario q : listaCuestionarios) {
            if (q.getEstado() == EstadoCuestionario.COMPLETO) {
                cantEvaluacionesCompletas = cantEvaluacionesCompletas + 1;
            }
        }
        // La empresa puede ser null: en ese caso se evita un NullPointerException
        // y los datos de la empresa quedan en null
        Long idEmpresa = null;
        String nombreEmpresa = null;
        if (p.getEmpresa() != null) {
            idEmpresa = p.getEmpresa().getId();
            nombreEmpresa = p.getEmpresa().getNombre();
        }
        return PuestoDTO.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .idEmpresa(idEmpresa)
                .nombreEmpresa(nombreEmpresa)
                .cantCandidatos(cantCandidatos)
                .cantEvaluacionesCompletas(cantEvaluacionesCompletas)
                .build();
    }

    private Puesto mapearPuesto(PuestoDTO dto) {
        return null;
    }

    /**
     * Convierte una CompetenciaRequerida (competencia de un puesto con su puntaje
     * mínimo) en un CompetenciaRequeridaDTO.
     * Toma el id y el nombre de la competencia, y el puntaje mínimo que el puesto exige en ella.
     */
    private CompetenciaRequeridaDTO armarCompetenciaRequeridaDTO(CompetenciaRequerida cr) {
        Competencia comp = cr.getCompetencia();
        return CompetenciaRequeridaDTO.builder()
                .idCompetencia(comp.getId())
                .nombreCompetencia(comp.getNombre())
                // Puntaje mínimo que debe alcanzar un candidato en esta competencia para el puesto
                .puntajeMinimoRequerido(cr.getPuntajeMinimoRequerido())
                .build();
    }
}
