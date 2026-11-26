package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IResultadoDAO;
import com.tornidoscopio.tp.dominio.BloqueCuestionario;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.CompetenciaRequerida;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.ItemOpcion;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.PreguntaOpcion;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dominio.Resultado;
import com.tornidoscopio.tp.dto.BloqueCuestionarioDTO;
import com.tornidoscopio.tp.dto.CuestionarioDTO;
import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import com.tornidoscopio.tp.excepciones.EntidadNoEncontradaException;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorCompletarCuestionarioTest {

    @Mock
    private ICuestionarioDAO cuestionarioDAO;

    @Mock
    private IResultadoDAO resultadoDAO;

    private RepositorioConfiguracion repositorioConfiguracion;
    private GestorCompletarCuestionario gestor;

    @BeforeEach
    void setUp() {
        repositorioConfiguracion = new RepositorioConfiguracion();
        repositorioConfiguracion.setCantidadPreguntasPorBloque(2);
        repositorioConfiguracion.setLimiteAccesos(3);
        repositorioConfiguracion.setTiempoMaxCompletar(LocalTime.of(1, 0));
        repositorioConfiguracion.setTiempoMaxComenzar(LocalTime.of(12, 0));
        gestor = new GestorCompletarCuestionario(cuestionarioDAO, resultadoDAO, repositorioConfiguracion);
    }

    @Test
    void iniciarCuestionario_activoValido_registraIngresoYRetornaDTO() {
        Cuestionario cuestionario = Cuestionario.builder()
                .id(1L)
                .claveIngreso("ABC12345")
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(0)
                .fechaCreacion(LocalDateTime.now().minusMinutes(10))
                .build();

        when(cuestionarioDAO.buscarPorClaveIngreso("ABC12345")).thenReturn(cuestionario);

        CuestionarioDTO dto = gestor.iniciarCuestionario("ABC12345");

        assertEquals(1L, dto.getId());
        assertEquals(EstadoCuestionario.ACTIVO, dto.getEstado());
        assertEquals(LocalTime.of(1, 0), dto.getTiempoMaxCompletar());
        assertEquals(1, cuestionario.getCantAccesos());
        assertNotNull(cuestionario.getFechaUltimoIngreso());
        verify(cuestionarioDAO).modificar(cuestionario);
    }

    @Test
    void iniciarCuestionario_claveInexistente_lanzaEntidadNoEncontrada() {
        when(cuestionarioDAO.buscarPorClaveIngreso("NOEXISTE")).thenReturn(null);
        assertThrows(EntidadNoEncontradaException.class, () -> gestor.iniciarCuestionario("NOEXISTE"));
    }

    @Test
    void iniciarCuestionario_superaLimiteAccesos_pasaAIncompletoYLanzaExcepcion() {
        Cuestionario cuestionario = Cuestionario.builder()
                .id(2L)
                .claveIngreso("ACC99999")
                .estado(EstadoCuestionario.EN_PROCESO)
                .cantAccesos(3)
                .fechaCreacion(LocalDateTime.now().minusMinutes(20))
                .fechaHoraInicio(LocalDateTime.now().minusMinutes(10))
                .build();

        when(cuestionarioDAO.buscarPorClaveIngreso("ACC99999")).thenReturn(cuestionario);

        assertThrows(ReglaNegocioException.class, () -> gestor.iniciarCuestionario("ACC99999"));
        assertEquals(EstadoCuestionario.INCOMPLETO, cuestionario.getEstado());
        assertNotNull(cuestionario.getFechaFin());
        verify(cuestionarioDAO).modificar(cuestionario);
    }

    @Test
    void iniciarCuestionario_superaTiempoMaxComenzar_pasaASinContestarYLanzaExcepcion() {
        Cuestionario cuestionario = Cuestionario.builder()
                .id(3L)
                .claveIngreso("SIN00001")
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(0)
                .fechaCreacion(LocalDateTime.now().minusHours(15))
                .build();

        when(cuestionarioDAO.buscarPorClaveIngreso("SIN00001")).thenReturn(cuestionario);

        assertThrows(ReglaNegocioException.class, () -> gestor.iniciarCuestionario("SIN00001"));
        assertEquals(EstadoCuestionario.SIN_CONTESTAR, cuestionario.getEstado());
        assertNotNull(cuestionario.getFechaFin());
        verify(cuestionarioDAO).modificar(cuestionario);
    }

    @Test
    void iniciarCuestionario_enProcesoSuperaTiempoMaxCompletar_pasaAIncompletoYLanzaExcepcion() {
        Cuestionario cuestionario = Cuestionario.builder()
                .id(4L)
                .claveIngreso("INC00001")
                .estado(EstadoCuestionario.EN_PROCESO)
                .cantAccesos(1)
                .fechaCreacion(LocalDateTime.now().minusHours(3))
                .fechaHoraInicio(LocalDateTime.now().minusHours(2))
                .build();

        when(cuestionarioDAO.buscarPorClaveIngreso("INC00001")).thenReturn(cuestionario);

        assertThrows(ReglaNegocioException.class, () -> gestor.iniciarCuestionario("INC00001"));
        assertEquals(EstadoCuestionario.INCOMPLETO, cuestionario.getEstado());
        assertNotNull(cuestionario.getFechaFin());
        verify(cuestionarioDAO).modificar(cuestionario);
    }

    @Test
    void flujoCompletoWizard_generarBloquesResponderYFinalizarConResultado() {
        Cuestionario cuestionario = crearCuestionarioCompletoConDosFactores();
        when(cuestionarioDAO.buscarPorId(10L)).thenReturn(cuestionario);

        // 1. Obtener primer bloque (pasa de ACTIVO a EN_PROCESO)
        BloqueCuestionarioDTO bloque1 = gestor.obtenerSiguienteBloque(10L);
        assertEquals(1, bloque1.getNroBloque());
        assertFalse(bloque1.getEsUltimo());
        assertEquals(2, bloque1.getPreguntas().size());
        assertEquals(EstadoCuestionario.EN_PROCESO, cuestionario.getEstado());
        assertNotNull(cuestionario.getFechaHoraInicio());

        // Verificar orden de opciones de respuesta por ordenVisualizacion
        assertEquals("MALO", bloque1.getPreguntas().get(0).getOpciones().get(0).getDescripcion());
        assertEquals("EXCELENTE", bloque1.getPreguntas().get(0).getOpciones().get(1).getDescripcion());

        // 2. Flujo Alternativo 6.A: falta responder una pregunta del bloque 1
        List<RespuestaCandidatoDTO> incompletas = List.of(
                new RespuestaCandidatoDTO(bloque1.getPreguntas().get(0).getId(),
                        bloque1.getPreguntas().get(0).getOpciones().get(1).getId())
        );
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> gestor.registrarBloqueRespuestas(10L, incompletas));
        assertTrue(ex.getMessage().contains(bloque1.getPreguntas().get(1).getEnunciado()));

        // 3. Responder bloque 1 completo
        List<RespuestaCandidatoDTO> respBloque1 = bloque1.getPreguntas().stream()
                .map(p -> new RespuestaCandidatoDTO(p.getId(), p.getOpciones().get(1).getId()))
                .toList();
        assertTrue(gestor.registrarBloqueRespuestas(10L, respBloque1));

        // 4. Obtener segundo y último bloque (Flujo 8.A / 1.C)
        BloqueCuestionarioDTO bloque2 = gestor.obtenerSiguienteBloque(10L);
        assertEquals(2, bloque2.getNroBloque());
        assertTrue(bloque2.getEsUltimo());
        assertEquals(2, bloque2.getPreguntas().size());

        // 5. Responder bloque 2 completo (EXCELENTE = 10 pts)
        List<RespuestaCandidatoDTO> respBloque2 = bloque2.getPreguntas().stream()
                .map(p -> new RespuestaCandidatoDTO(p.getId(), p.getOpciones().get(1).getId()))
                .toList();
        assertTrue(gestor.registrarBloqueRespuestas(10L, respBloque2));

        // 6. Finalizar cuestionario (Paso 8)
        gestor.finalizarCuestionario(10L);

        assertEquals(EstadoCuestionario.COMPLETO, cuestionario.getEstado());
        assertNotNull(cuestionario.getFechaFin());
        assertNotNull(cuestionario.getResultado());

        ArgumentCaptor<Resultado> resultadoCaptor = ArgumentCaptor.forClass(Resultado.class);
        verify(resultadoDAO).crear(resultadoCaptor.capture());
        Resultado resultado = resultadoCaptor.getValue();
        assertEquals(1, resultado.getResultadosCompetencia().size());
        assertEquals(10.0, resultado.getResultadosCompetencia().get(0).getPuntajeObtenido(), 0.0001);
        assertEquals(7.0, resultado.getResultadosCompetencia().get(0).getPuntajeRequerido(), 0.0001);
    }

    @Test
    void finalizarCuestionario_conBloquesPendientesLanzaExcepcion() {
        BloqueCuestionario bloquePendiente = BloqueCuestionario.builder()
                .id(1L)
                .nroBloque(1)
                .completado(false)
                .respuestas(new ArrayList<>())
                .build();
        Cuestionario cuestionario = Cuestionario.builder()
                .id(20L)
                .estado(EstadoCuestionario.EN_PROCESO)
                .bloques(List.of(bloquePendiente))
                .build();

        when(cuestionarioDAO.buscarPorId(20L)).thenReturn(cuestionario);

        assertThrows(ReglaNegocioException.class, () -> gestor.finalizarCuestionario(20L));
        verify(resultadoDAO, never()).crear(any());
    }

    @Test
    void finalizarCuestionario_omiteCompetenciaSinFactoresEvaluablesYConservaEvaluadaEnCero() {
        ItemOpcion itemCero = ItemOpcion.builder().id(1L).ordenVisualizacion(1).descripcion("MALO").build();
        ItemOpcion itemDiez = ItemOpcion.builder().id(2L).ordenVisualizacion(2).descripcion("EXCELENTE").build();
        OpcionRespuesta escala = OpcionRespuesta.builder()
                .id(1L)
                .nombre("ESCALA")
                .activo(true)
                .items(List.of(itemCero, itemDiez))
                .build();

        Competencia compEvaluable = Competencia.builder().id(1L).nombre("LEALTAD").activo(true).build();
        Factor factorEvaluable = Factor.builder().id(1L).nombre("CREDIBILIDAD").activo(true).competencia(compEvaluable).build();
        factorEvaluable.setPreguntas(List.of(
                crearPregunta(1L, "Pregunta E-1", factorEvaluable, escala, itemCero, itemDiez),
                crearPregunta(2L, "Pregunta E-2", factorEvaluable, escala, itemCero, itemDiez)
        ));
        compEvaluable.setFactores(List.of(factorEvaluable));

        Competencia compNoEvaluable = Competencia.builder().id(2L).nombre("LIDERAZGO").activo(true).build();
        Factor factorInsuficiente = Factor.builder().id(2L).nombre("DELEGACION").activo(true).competencia(compNoEvaluable).build();
        factorInsuficiente.setPreguntas(List.of(
                crearPregunta(3L, "Pregunta NE-1", factorInsuficiente, escala, itemCero, itemDiez)
        ));
        compNoEvaluable.setFactores(List.of(factorInsuficiente));

        Puesto puesto = Puesto.builder()
                .id(1L)
                .nombre("ANALISTA")
                .activo(true)
                .competenciasRequeridas(List.of(
                        CompetenciaRequerida.builder().id(1L).competencia(compEvaluable).puntajeMinimoRequerido(6.0).build(),
                        CompetenciaRequerida.builder().id(2L).competencia(compNoEvaluable).puntajeMinimoRequerido(7.0).build()
                ))
                .build();

        Cuestionario cuestionario = Cuestionario.builder()
                .id(30L)
                .claveIngreso("EDGE0001")
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(1)
                .fechaCreacion(LocalDateTime.now().minusMinutes(5))
                .puesto(puesto)
                .bloques(new ArrayList<>())
                .build();

        when(cuestionarioDAO.buscarPorId(30L)).thenReturn(cuestionario);

        BloqueCuestionarioDTO bloque = gestor.obtenerSiguienteBloque(30L);
        assertEquals(2, bloque.getPreguntas().size());
        assertTrue(bloque.getEsUltimo());

        // Seleccionar explícitamente la opción de ponderación 0.0 para la competencia evaluable
        List<RespuestaCandidatoDTO> respuestas = bloque.getPreguntas().stream()
                .map(p -> new RespuestaCandidatoDTO(p.getId(), itemCero.getId()))
                .toList();
        assertTrue(gestor.registrarBloqueRespuestas(30L, respuestas));

        gestor.finalizarCuestionario(30L);

        ArgumentCaptor<Resultado> resultadoCaptor = ArgumentCaptor.forClass(Resultado.class);
        verify(resultadoDAO).crear(resultadoCaptor.capture());
        Resultado resultado = resultadoCaptor.getValue();
        assertEquals(1, resultado.getResultadosCompetencia().size());
        assertEquals(1L, resultado.getResultadosCompetencia().get(0).getCompetencia().getId());
        assertEquals(0.0, resultado.getResultadosCompetencia().get(0).getPuntajeObtenido(), 0.0001);
        assertEquals(6.0, resultado.getResultadosCompetencia().get(0).getPuntajeRequerido(), 0.0001);
    }

    private Cuestionario crearCuestionarioCompletoConDosFactores() {
        ItemOpcion itemMalo = ItemOpcion.builder().id(1L).ordenVisualizacion(1).descripcion("MALO").build();
        ItemOpcion itemExcelente = ItemOpcion.builder().id(2L).ordenVisualizacion(2).descripcion("EXCELENTE").build();
        OpcionRespuesta escala = OpcionRespuesta.builder()
                .id(1L)
                .nombre("ESCALA")
                .activo(true)
                .items(List.of(itemExcelente, itemMalo))
                .build();

        Competencia competencia = Competencia.builder().id(1L).nombre("LEALTAD").activo(true).build();
        Factor factor1 = Factor.builder().id(1L).nombre("CREDIBILIDAD").activo(true).competencia(competencia).build();
        Factor factor2 = Factor.builder().id(2L).nombre("COMPROMISO").activo(true).competencia(competencia).build();

        factor1.setPreguntas(List.of(
                crearPregunta(1L, "Pregunta F1-1", factor1, escala, itemMalo, itemExcelente),
                crearPregunta(2L, "Pregunta F1-2", factor1, escala, itemMalo, itemExcelente)
        ));
        factor2.setPreguntas(List.of(
                crearPregunta(3L, "Pregunta F2-1", factor2, escala, itemMalo, itemExcelente),
                crearPregunta(4L, "Pregunta F2-2", factor2, escala, itemMalo, itemExcelente)
        ));
        competencia.setFactores(List.of(factor1, factor2));

        Puesto puesto = Puesto.builder()
                .id(1L)
                .nombre("DESARROLLADOR")
                .activo(true)
                .competenciasRequeridas(List.of(
                        CompetenciaRequerida.builder()
                                .id(1L)
                                .competencia(competencia)
                                .puntajeMinimoRequerido(7.0)
                                .build()
                ))
                .build();

        return Cuestionario.builder()
                .id(10L)
                .claveIngreso("TEST1234")
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(1)
                .fechaCreacion(LocalDateTime.now().minusMinutes(5))
                .puesto(puesto)
                .bloques(new ArrayList<>())
                .build();
    }

    private Pregunta crearPregunta(Long id, String enunciado, Factor factor, OpcionRespuesta opc,
                                   ItemOpcion malo, ItemOpcion excelente) {
        Pregunta p = Pregunta.builder()
                .id(id)
                .nombre("P" + id)
                .enunciado(enunciado)
                .activo(true)
                .factor(factor)
                .opcionRespuesta(opc)
                .build();
        p.setOpcionesPonderadas(List.of(
                PreguntaOpcion.builder().pregunta(p).itemOpcion(malo).ponderacion(0.0).build(),
                PreguntaOpcion.builder().pregunta(p).itemOpcion(excelente).ponderacion(10.0).build()
        ));
        return p;
    }
}
