package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.CompetenciaRequerida;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dominio.Resultado;
import com.tornidoscopio.tp.dominio.ResultadoCompetencia;
import com.tornidoscopio.tp.dto.OrdenMeritoDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración end-to-end del CU028 (Emitir Orden de Mérito) contra PostgreSQL/Neon.
 * Solo se ejecuta cuando está definida la variable de entorno {@code DB_URL}.
 * Al estar anotada con {@link Transactional}, Spring realiza rollback automático al finalizar.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class CU028IntegracionTest {

    @Autowired
    private GestorReportes gestorReportes;

    @Autowired
    private GestorPuestos gestorPuestos;

    @Autowired
    private ICuestionarioDAO cuestionarioDAO;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("CU028 Integración BD - Obtener fechas de evaluación y emitir orden de mérito")
    void flujoCompletoCU028_contraBaseDeDatosReal() {
        List<Candidato> candidatos = em.createQuery(
                "SELECT c FROM Candidato c WHERE c.eliminado = false ORDER BY c.nroCandidato ASC",
                Candidato.class)
                .setMaxResults(2)
                .getResultList();
        assertFalse(candidatos.isEmpty(), "Debe existir al menos un candidato activo en la BD");

        Candidato candidato1 = candidatos.get(0);
        Candidato candidato2 = candidatos.get(candidatos.size() - 1);

        List<Puesto> puestos = em.createQuery(
                "SELECT DISTINCT cr.puesto FROM CompetenciaRequerida cr WHERE cr.puesto.activo = true",
                Puesto.class)
                .setMaxResults(1)
                .getResultList();
        assertFalse(puestos.isEmpty(), "Debe existir al menos un puesto activo con competencias requeridas");
        Puesto puesto = puestos.get(0);

        LocalDateTime fechaEvaluacion = LocalDateTime.now().withNano(0);

        Cuestionario qCompleto = Cuestionario.builder()
                .claveIngreso(generarClavePrueba())
                .estado(EstadoCuestionario.COMPLETO)
                .cantAccesos(1)
                .fechaCreacion(fechaEvaluacion)
                .fechaHoraInicio(fechaEvaluacion.plusMinutes(5))
                .fechaFin(fechaEvaluacion.plusMinutes(25))
                .candidato(candidato1)
                .puesto(puesto)
                .build();
        cuestionarioDAO.crear(qCompleto);

        List<ResultadoCompetencia> detalles = new ArrayList<>();
        Resultado resultado = Resultado.builder()
                .fechaCalculo(fechaEvaluacion.plusMinutes(25))
                .cuestionario(qCompleto)
                .resultadosCompetencia(detalles)
                .build();
        for (CompetenciaRequerida cr : puesto.getCompetenciasRequeridas()) {
            Competencia comp = cr.getCompetencia();
            detalles.add(ResultadoCompetencia.builder()
                    .resultado(resultado)
                    .competencia(comp)
                    .puntajeObtenido(10.0)
                    .puntajeRequerido(cr.getPuntajeMinimoRequerido())
                    .build());
        }
        qCompleto.setResultado(resultado);
        em.persist(resultado);

        Cuestionario qIncompleto = Cuestionario.builder()
                .claveIngreso(generarClavePrueba())
                .estado(EstadoCuestionario.INCOMPLETO)
                .cantAccesos(2)
                .fechaCreacion(fechaEvaluacion)
                .fechaHoraInicio(fechaEvaluacion.plusMinutes(10))
                .fechaUltimoIngreso(fechaEvaluacion.plusMinutes(20))
                .candidato(candidato2)
                .puesto(puesto)
                .build();
        cuestionarioDAO.crear(qIncompleto);

        em.flush();
        em.clear();

        // Paso 4: verificar que GestorPuestos informa candidatos y evaluaciones completas
        List<PuestoDTO> puestosDTO = gestorPuestos.buscarPuestos(puesto.getCodigo());
        PuestoDTO puestoDTO = puestosDTO.stream()
                .filter(dto -> puesto.getId().equals(dto.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(puestoDTO.getCantCandidatos() >= 2);
        assertTrue(puestoDTO.getCantEvaluacionesCompletas() >= 1);

        // Paso 6: obtener fechas sin repetir
        List<LocalDateTime> fechas = gestorReportes.obtenerFechasEvaluaciones(puesto.getId());
        assertTrue(fechas.contains(fechaEvaluacion));

        // Paso 7: emitir orden de mérito para la fecha creada
        List<OrdenMeritoDTO> orden = gestorReportes.generarOrdenMerito(puesto.getId(), List.of(fechaEvaluacion));
        assertEquals(2, orden.size());

        assertEquals(EstadoCuestionario.COMPLETO, orden.get(0).getEstadoCuestionario());
        assertTrue(Boolean.TRUE.equals(orden.get(0).getCumpleMinimos()));
        assertNotNull(orden.get(0).getPuntajeTotalObtenido());

        assertEquals(EstadoCuestionario.INCOMPLETO, orden.get(1).getEstadoCuestionario());
        assertNull(orden.get(1).getPuntajeTotalObtenido());
        assertEquals(2, orden.get(1).getCantAccesos());
    }

    private String generarClavePrueba() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
