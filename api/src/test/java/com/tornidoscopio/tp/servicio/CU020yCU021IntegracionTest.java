package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.ItemOpcion;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dto.CompetenciaDTO;
import com.tornidoscopio.tp.dto.FactorDTO;
import com.tornidoscopio.tp.dto.OpcionRespuestaDTO;
import com.tornidoscopio.tp.dto.PreguntaDTO;
import com.tornidoscopio.tp.dto.PreguntaOpcionDTO;
import com.tornidoscopio.tp.excepciones.PreguntaDuplicadaException;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración end-to-end de los CU020 (Gestionar Preguntas) y CU021 (Dar de Alta Pregunta)
 * contra PostgreSQL. Solo se ejecuta cuando está definida la variable de entorno {@code DB_URL}.
 * Al estar anotada con {@link Transactional}, Spring realiza rollback automático al finalizar.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class CU020yCU021IntegracionTest {

    @Autowired
    private GestorPreguntas gestorPreguntas;

    @Autowired
    private GestorCompetencias gestorCompetencias;

    @Autowired
    private GestorFactores gestorFactores;

    @Autowired
    private GestorOpcionesRespuesta gestorOpcionesRespuesta;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("CU020 y CU021 Integración BD - Dar de alta pregunta con opciones, detectar duplicado y buscarla con filtros")
    void flujoCompletoCU020yCU021_contraBaseDeDatosReal() {
        // 1. CU021: Carga inicial de Competencias y Opciones de Respuesta
        List<CompetenciaDTO> competencias = gestorCompetencias.buscar("");
        assertFalse(competencias.isEmpty(), "Debe existir al menos una competencia activa en la BD");

        List<OpcionRespuestaDTO> opcionesRespuesta = gestorOpcionesRespuesta.buscar("");
        assertFalse(opcionesRespuesta.isEmpty(), "Debe existir al menos una opción de respuesta activa en la BD");

        // Buscar un factor activo en la BD para asociar la pregunta
        List<Factor> factoresBD = em.createQuery(
                        "SELECT f FROM Factor f JOIN FETCH f.competencia c WHERE f.activo = true AND c.activo = true",
                        Factor.class)
                .setMaxResults(1)
                .getResultList();
        assertFalse(factoresBD.isEmpty(), "Debe existir al menos un factor activo con competencia activa en la BD");
        Factor factor = factoresBD.get(0);

        List<FactorDTO> factoresDeCompetencia = gestorFactores.buscarPorCompetencia(factor.getCompetencia().getId());
        assertTrue(factoresDeCompetencia.stream().anyMatch(f -> f.getId().equals(factor.getId())));

        // Buscar una OpcionRespuesta que tenga al menos un ItemOpcion cargado
        List<ItemOpcion> itemsBD = em.createQuery(
                        "SELECT i FROM ItemOpcion i JOIN FETCH i.opcionRespuesta o WHERE o.activo = true ORDER BY i.ordenVisualizacion ASC",
                        ItemOpcion.class)
                .getResultList();
        assertFalse(itemsBD.isEmpty(), "Debe existir al menos un ItemOpcion en la BD");
        OpcionRespuesta opcionElegida = itemsBD.get(0).getOpcionRespuesta();
        List<ItemOpcion> itemsDeOpcion = itemsBD.stream()
                .filter(i -> i.getOpcionRespuesta().getId().equals(opcionElegida.getId()))
                .toList();

        // 2. CU021: Validar y dar de alta una nueva pregunta única
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String nombreUnico = "PREGUNTA TEST " + sufijo;
        String enunciadoUnico = "¿Enunciado de prueba integración " + sufijo + "?";

        PreguntaDTO nuevaPregunta = PreguntaDTO.builder()
                .nombre(nombreUnico)
                .enunciado(enunciadoUnico)
                .descripcion("Descripción de prueba")
                .idFactor(factor.getId())
                .idOpcionRespuesta(opcionElegida.getId())
                .build();

        assertTrue(gestorPreguntas.validarOmisiones(nuevaPregunta).isEmpty());
        assertTrue(gestorPreguntas.validarLongitudMaxima(nuevaPregunta).isEmpty());

        List<PreguntaOpcionDTO> ponderaciones = new ArrayList<>();
        for (int i = 0; i < itemsDeOpcion.size(); i++) {
            double puntos = (i == 0) ? 10.0 : 0.0;
            ponderaciones.add(new PreguntaOpcionDTO(itemsDeOpcion.get(i).getId(), puntos));
        }
        assertTrue(gestorPreguntas.validarPonderacion(ponderaciones));

        PreguntaDTO creada = gestorPreguntas.altaPregunta(nuevaPregunta);
        assertNotNull(creada.getId(), "La pregunta creada debe tener un ID asignado por la BD");

        gestorPreguntas.asignarOpciones(creada.getId(), ponderaciones);
        em.flush();
        em.clear();

        // 3. CU021 Flujo 2.C: Intentar dar de alta la misma pregunta debe lanzar PreguntaDuplicadaException
        assertThrows(
                PreguntaDuplicadaException.class,
                () -> gestorPreguntas.altaPregunta(PreguntaDTO.builder()
                        .nombre(nombreUnico)
                        .enunciado("¿Otro enunciado?")
                        .idFactor(factor.getId())
                        .idOpcionRespuesta(opcionElegida.getId())
                        .build())
        );

        // 4. CU020 Paso 3: Sin criterios debe lanzar ReglaNegocioException
        assertThrows(
                ReglaNegocioException.class,
                () -> gestorPreguntas.buscarPreguntas("", "", "")
        );

        // 5. CU020: Buscar la pregunta recién creada por competencia, factor y nombre
        List<PreguntaDTO> encontradas = gestorPreguntas.buscarPreguntas(
                factor.getCompetencia().getNombre(),
                factor.getNombre(),
                nombreUnico
        );

        assertEquals(1, encontradas.size());
        PreguntaDTO encontrada = encontradas.get(0);
        assertEquals(creada.getId(), encontrada.getId());
        assertEquals(nombreUnico, encontrada.getNombre());
        assertEquals(factor.getNombre(), encontrada.getNombreFactor());
        assertEquals(factor.getCompetencia().getNombre(), encontrada.getNombreCompetencia());
    }
}
