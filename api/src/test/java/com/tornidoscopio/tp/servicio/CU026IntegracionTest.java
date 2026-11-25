package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dto.BloqueCuestionarioDTO;
import com.tornidoscopio.tp.dto.CuestionarioDTO;
import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración end-to-end del CU026 (capa de negocio + capa de datos + JPA/PostgreSQL).
 * Solo se ejecuta cuando está definida la variable de entorno {@code DB_URL}.
 * Usa queries dinámicas para trabajar con cualquier estado de la BD (no depende de seed data fijo).
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class CU026IntegracionTest {

    @Autowired
    private GestorCompletarCuestionario gestorCompletarCuestionario;

    @Autowired
    private ICuestionarioDAO cuestionarioDAO;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("CU026 Integración BD - Iniciar, responder bloques y finalizar cuestionario")
    void flujoCompletoCU026_contraBaseDeDatosReal() {
        // --- 1. Buscar dinámicamente un candidato activo ---
        List<Candidato> candidatos = em.createQuery(
                "SELECT c FROM Candidato c WHERE c.eliminado = false ORDER BY c.nroCandidato ASC",
                Candidato.class)
                .setMaxResults(1)
                .getResultList();
        assertFalse(candidatos.isEmpty(), "Debe existir al menos un candidato activo en la BD");
        Candidato candidato = candidatos.get(0);

        // --- 2. Buscar dinámicamente un puesto que tenga competencias evaluables
        //        (es decir, con al menos un factor activo con >=2 preguntas activas) ---
        List<Puesto> puestosEvaluables = em.createQuery(
                "SELECT DISTINCT cr.puesto FROM CompetenciaRequerida cr " +
                "WHERE EXISTS (" +
                "   SELECT f FROM Factor f " +
                "   WHERE f.competencia = cr.competencia " +
                "   AND f.activo = true " +
                "   AND (SELECT COUNT(p) FROM Pregunta p WHERE p.factor = f AND p.activo = true) >= 2" +
                ")",
                Puesto.class)
                .setMaxResults(1)
                .getResultList();
        assertFalse(puestosEvaluables.isEmpty(),
                "Debe existir al menos un puesto con competencias que tengan factores con >=2 preguntas activas");
        Puesto puesto = puestosEvaluables.get(0);

        // --- 3. Crear cuestionario con clave única (evita colisión de UNIQUE constraint) ---
        String clave = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Cuestionario nuevo = Cuestionario.builder()
                .claveIngreso(clave)
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(0)
                .fechaCreacion(LocalDateTime.now())
                .candidato(candidato)
                .puesto(puesto)
                .build();
        cuestionarioDAO.crear(nuevo);
        em.flush();
        em.clear();

        // --- 4. Iniciar cuestionario ---
        CuestionarioDTO inicio = gestorCompletarCuestionario.iniciarCuestionario(clave);
        assertEquals(EstadoCuestionario.ACTIVO, inicio.getEstado(),
                "El estado devuelto por iniciarCuestionario debe ser ACTIVO");

        // --- 5. Obtener primer bloque ---
        BloqueCuestionarioDTO bloque = gestorCompletarCuestionario.obtenerSiguienteBloque(inicio.getId());
        assertNotNull(bloque, "Debe devolver el primer bloque");
        assertFalse(bloque.getPreguntas().isEmpty(), "El bloque debe contener preguntas");

        // --- 6. Responder todos los bloques con la primera opción disponible ---
        while (true) {
            BloqueCuestionarioDTO bloqueActual = bloque;
            List<RespuestaCandidatoDTO> respuestas = bloqueActual.getPreguntas().stream()
                    .map(p -> new RespuestaCandidatoDTO(p.getId(), p.getOpciones().get(0).getId()))
                    .toList();

            assertTrue(gestorCompletarCuestionario.registrarBloqueRespuestas(inicio.getId(), respuestas),
                    "registrarBloqueRespuestas debe retornar true al guardar exitosamente");

            if (Boolean.TRUE.equals(bloqueActual.getEsUltimo())) {
                break;
            }
            bloque = gestorCompletarCuestionario.obtenerSiguienteBloque(inicio.getId());
        }

        // --- 7. Finalizar y verificar resultado ---
        gestorCompletarCuestionario.finalizarCuestionario(inicio.getId());
        em.flush();
        em.clear();

        Cuestionario finalizado = cuestionarioDAO.buscarPorId(inicio.getId());
        assertEquals(EstadoCuestionario.COMPLETO, finalizado.getEstado(),
                "El cuestionario debe quedar en estado COMPLETO");
        assertNotNull(finalizado.getResultado(),
                "El cuestionario finalizado debe tener un Resultado asociado");
        assertFalse(finalizado.getResultado().getResultadosCompetencia().isEmpty(),
                "El resultado debe tener al menos un ResultadoCompetencia");
    }
}
