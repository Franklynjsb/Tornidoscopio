package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IPuestoDAO;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dto.CandidatoClaveDTO;
import com.tornidoscopio.tp.dto.CandidatoGestionDTO;
import com.tornidoscopio.tp.dto.CompetenciaRequeridaDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración end-to-end del CU025 (capa de negocio + capa de datos + JPA/PostgreSQL).
 * <p>
 * Solo se ejecuta cuando está definida la variable de entorno {@code DB_URL} (junto con
 * {@code DB_USER} y {@code DB_PASSWORD}). Al estar anotada con {@link Transactional},
 * Spring realiza rollback automático al finalizar cada test para no modificar datos
 * en la base compartida.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class CU025IntegracionTest {

    @Autowired
    private GestorEvaluacion gestorEvaluacion;

    @Autowired
    private GestorPuestos gestorPuestos;

    @Autowired
    private ICandidatoDAO candidatoDAO;

    @Autowired
    private ICuestionarioDAO cuestionarioDAO;

    @Autowired
    private IPuestoDAO puestoDAO;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("CU025 Integración BD - Flujo completo: buscar, validar, generar, guardar, detectar conflicto 6.A y exportar .xls")
    void flujoCompletoCU025_contraBaseDeDatosReal() throws Exception {
        // 1. Paso 3: Buscar candidatos activos en BD (sin filtro trae todos los no eliminados)
        List<CandidatoGestionDTO> todosCandidatos = gestorEvaluacion.buscarCandidatos("");
        assertFalse(todosCandidatos.isEmpty(),
                "La BD debe tener al menos un candidato cargado (ejecutá seed_capital.sql si está vacía)");

        // Elegimos un candidato que actualmente no tenga cuestionario activo/en proceso
        CandidatoGestionDTO candidatoLibre = todosCandidatos.stream()
                .filter(c -> gestorEvaluacion.validarCuestionariosActivos(List.of(c.getNroCandidato())).isEmpty())
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Todos los candidatos de la BD ya tienen un cuestionario activo o en proceso"));

        // Verificar también la búsqueda filtrada por apellido y por número de candidato
        List<CandidatoGestionDTO> porApellido = gestorEvaluacion.buscarCandidatos(candidatoLibre.getApellido());
        assertTrue(porApellido.stream().anyMatch(c -> c.getNroCandidato().equals(candidatoLibre.getNroCandidato())));

        List<CandidatoGestionDTO> porNumero = gestorEvaluacion.buscarCandidatos(String.valueOf(candidatoLibre.getNroCandidato()));
        assertTrue(porNumero.stream().anyMatch(c -> c.getNroCandidato().equals(candidatoLibre.getNroCandidato())));

        // 2. Paso 8: Buscar puestos activos en BD
        List<PuestoDTO> puestos = gestorPuestos.buscarPuestos("");
        assertFalse(puestos.isEmpty(),
                "La BD debe tener al menos un puesto cargado (ejecutá seed_capital.sql si está vacía)");

        PuestoDTO puestoSeleccionado = puestos.get(0);
        assertNotNull(puestoSeleccionado.getNombreEmpresa(), "Debe traer el nombre de la empresa vía JOIN FETCH");

        // 3. Paso 9: Obtener competencias requeridas y validar factores evaluables (>= 2 preguntas activas)
        List<CompetenciaRequeridaDTO> competencias = gestorPuestos.obtenerCompetenciasRequeridas(puestoSeleccionado.getId());
        assertFalse(competencias.isEmpty(), "El puesto debe tener competencias requeridas evaluables");

        // 4. Paso 11: Generar cuestionario en memoria (todavía no persiste)
        List<CandidatoClaveDTO> clavesGeneradas = gestorEvaluacion.generarCuestionarios(
                puestoSeleccionado,
                List.of(candidatoLibre)
        );
        assertEquals(1, clavesGeneradas.size());
        String clave = clavesGeneradas.get(0).getClaveIngreso();
        assertEquals(8, clave.length());
        assertTrue(clave.matches(".*[A-Z].*") && clave.matches(".*[0-9].*"));

        // Antes de confirmar, todavía no existe en BD
        assertNull(cuestionarioDAO.buscarPorClaveIngreso(clave));

        // 5. Paso 12-13: Confirmar y guardar en BD
        gestorEvaluacion.confirmarYGuardarCuestionarios();
        em.flush();
        em.clear();

        // Verificar que quedó persistido en estado ACTIVO y con la clave única
        Cuestionario persistido = cuestionarioDAO.buscarPorClaveIngreso(clave);
        assertNotNull(persistido, "El cuestionario debe existir en la BD luego de confirmar");
        assertEquals(EstadoCuestionario.ACTIVO, persistido.getEstado());
        assertEquals(0, persistido.getCantAccesos());
        assertEquals(candidatoLibre.getNroCandidato(), persistido.getCandidato().getNroCandidato());
        assertEquals(puestoSeleccionado.getId(), persistido.getPuesto().getId());

        // 6. Verificar Flujo Alternativo 6.A: ahora el mismo candidato SÍ debe reportar conflicto
        List<Cuestionario> conflictos = gestorEvaluacion.validarCuestionariosActivos(List.of(candidatoLibre.getNroCandidato()));
        assertFalse(conflictos.isEmpty(), "El candidato recién evaluado debe aparecer con cuestionario ACTIVO");
        assertEquals(clave, conflictos.get(0).getClaveIngreso());

        // 7. Paso 14-17: Exportar a Excel (.xls)
        File archivoExcel = gestorEvaluacion.exportarCandidatosConClaves(puestoSeleccionado.getId(), clavesGeneradas);
        try {
            assertTrue(archivoExcel.exists());
            assertTrue(archivoExcel.getName().endsWith(".xls"));
            String contenido = Files.readString(archivoExcel.toPath(), StandardCharsets.UTF_8);
            assertTrue(contenido.contains(clave));
            assertTrue(contenido.contains(candidatoLibre.getApellido()));
        } finally {
            Files.deleteIfExists(archivoExcel.toPath());
        }
    }
}
