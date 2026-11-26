package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Resultado;
import com.tornidoscopio.tp.dominio.ResultadoCompetencia;
import com.tornidoscopio.tp.dominio.TipoDocumento;
import com.tornidoscopio.tp.dto.OrdenMeritoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorReportesTest {

    @Mock
    private ICuestionarioDAO cuestionarioDAO;

    @InjectMocks
    private GestorReportes gestorReportes;

    @Test
    @DisplayName("CU028 Paso 6 - obtenerFechasEvaluaciones agrupa fechas sin repetir y retorna vacío si no hay")
    void obtenerFechasEvaluaciones_agrupaSinRepetir() {
        LocalDateTime fecha1 = LocalDateTime.of(2026, 11, 20, 10, 0);
        LocalDateTime fecha2 = LocalDateTime.of(2026, 11, 22, 15, 30);

        Cuestionario q1 = Cuestionario.builder().id(1L).fechaCreacion(fecha1).build();
        Cuestionario q2 = Cuestionario.builder().id(2L).fechaCreacion(fecha1).build();
        Cuestionario q3 = Cuestionario.builder().id(3L).fechaCreacion(fecha2).build();

        when(cuestionarioDAO.buscarPorPuesto(10L)).thenReturn(List.of(q1, q2, q3));
        when(cuestionarioDAO.buscarPorPuesto(99L)).thenReturn(List.of());

        List<LocalDateTime> fechas = gestorReportes.obtenerFechasEvaluaciones(10L);
        assertEquals(List.of(fecha1, fecha2), fechas);
        assertTrue(gestorReportes.obtenerFechasEvaluaciones(99L).isEmpty());
        assertTrue(gestorReportes.obtenerFechasEvaluaciones(null).isEmpty());
        verify(cuestionarioDAO).buscarPorPuesto(10L);
        verify(cuestionarioDAO).buscarPorPuesto(99L);
    }

    @Test
    @DisplayName("CU028 Paso 7 - generarOrdenMerito ordena por cumplimiento de mínimos, puntaje descendente y estado")
    void generarOrdenMerito_ordenaCorrectamenteYFiltraPorFecha() {
        LocalDateTime fechaSeleccionada = LocalDateTime.of(2026, 11, 20, 10, 0);
        LocalDateTime otraFecha = LocalDateTime.of(2026, 11, 10, 9, 0);

        // 1) COMPLETO que cumple mínimos con puntaje total 15.0 (7.5 + 7.5)
        Cuestionario qCumpleMenor = crearCuestionarioCompleto(
                1L, "Ana", "Gomez", "11111111", fechaSeleccionada,
                List.of(new double[]{7.5, 6.0}, new double[]{7.5, 6.0})
        );

        // 2) COMPLETO que cumple mínimos con puntaje total 18.0 (9.0 + 9.0) -> debe ir primero
        Cuestionario qCumpleMayor = crearCuestionarioCompleto(
                2L, "Bruno", "Perez", "22222222", fechaSeleccionada,
                List.of(new double[]{9.0, 6.0}, new double[]{9.0, 6.0})
        );

        // 3) COMPLETO que NO cumple mínimos en una competencia (10.0 + 5.0 = 15.0, pero 5.0 < 6.0)
        Cuestionario qNoCumple = crearCuestionarioCompleto(
                3L, "Carla", "Diaz", "33333333", fechaSeleccionada,
                List.of(new double[]{10.0, 6.0}, new double[]{5.0, 6.0})
        );

        // 4) INCOMPLETO -> puntaje null, fechaFinOUltimoIngreso = fechaUltimoIngreso
        LocalDateTime inicioInc = LocalDateTime.of(2026, 11, 20, 11, 0);
        LocalDateTime ultIngresoInc = LocalDateTime.of(2026, 11, 20, 11, 45);
        Cuestionario qIncompleto = Cuestionario.builder()
                .id(4L)
                .fechaCreacion(fechaSeleccionada)
                .estado(EstadoCuestionario.INCOMPLETO)
                .fechaHoraInicio(inicioInc)
                .fechaUltimoIngreso(ultIngresoInc)
                .cantAccesos(2)
                .candidato(crearCandidato("Diego", "Lopez", "44444444"))
                .build();

        // 5) SIN_CONTESTAR -> agrupado en el resto de estados
        Cuestionario qSinContestar = Cuestionario.builder()
                .id(5L)
                .fechaCreacion(fechaSeleccionada)
                .estado(EstadoCuestionario.SIN_CONTESTAR)
                .cantAccesos(0)
                .candidato(crearCandidato("Elena", "Ruiz", "55555555"))
                .build();

        // 6) De otra evaluación -> debe ser ignorado
        Cuestionario qOtraFecha = crearCuestionarioCompleto(
                6L, "Franco", "Sosa", "66666666", otraFecha,
                List.of(new double[]{10.0, 6.0}, new double[]{10.0, 6.0})
        );

        when(cuestionarioDAO.buscarPorPuesto(10L)).thenReturn(List.of(
                qIncompleto, qCumpleMenor, qOtraFecha, qNoCumple, qSinContestar, qCumpleMayor
        ));

        List<OrdenMeritoDTO> resultado = gestorReportes.generarOrdenMerito(10L, List.of(fechaSeleccionada));

        assertEquals(5, resultado.size());

        // Grupo 0: COMPLETO que cumple mínimos (ordenado por puntaje descendente)
        assertEquals("22222222", resultado.get(0).getNroDocumento());
        assertEquals(18.0, resultado.get(0).getPuntajeTotalObtenido(), 0.0001);
        assertTrue(resultado.get(0).getCumpleMinimos());
        assertEquals(qCumpleMayor.getFechaFin(), resultado.get(0).getFechaFinOUltimoIngreso());

        assertEquals("11111111", resultado.get(1).getNroDocumento());
        assertEquals(15.0, resultado.get(1).getPuntajeTotalObtenido(), 0.0001);
        assertTrue(resultado.get(1).getCumpleMinimos());

        // Grupo 1: COMPLETO que no cumple mínimos
        assertEquals("33333333", resultado.get(2).getNroDocumento());
        assertEquals(15.0, resultado.get(2).getPuntajeTotalObtenido(), 0.0001);
        assertFalse(resultado.get(2).getCumpleMinimos());

        // Grupo 2: Resto agrupado por estado, sin puntaje mostrado (Obs. 1)
        assertEquals(EstadoCuestionario.INCOMPLETO, resultado.get(3).getEstadoCuestionario());
        assertNull(resultado.get(3).getPuntajeTotalObtenido());
        assertEquals(inicioInc, resultado.get(3).getFechaInicio());
        assertEquals(ultIngresoInc, resultado.get(3).getFechaFinOUltimoIngreso());
        assertEquals(2, resultado.get(3).getCantAccesos());

        assertEquals(EstadoCuestionario.SIN_CONTESTAR, resultado.get(4).getEstadoCuestionario());
        assertNull(resultado.get(4).getPuntajeTotalObtenido());
    }

    @Test
    @DisplayName("CU028 - Maneja fechas nulas/vacías y cuestionario COMPLETO sin Resultado sin lanzar NPE")
    void generarOrdenMerito_entradasNulasYSinResultado_noLanzaNPE() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 20, 10, 0);

        assertTrue(gestorReportes.generarOrdenMerito(null, List.of(fecha)).isEmpty());
        assertTrue(gestorReportes.generarOrdenMerito(10L, null).isEmpty());
        assertTrue(gestorReportes.generarOrdenMerito(10L, List.of()).isEmpty());
        verifyNoInteractions(cuestionarioDAO);

        Cuestionario completoSinResultado = Cuestionario.builder()
                .id(1L)
                .fechaCreacion(fecha)
                .estado(EstadoCuestionario.COMPLETO)
                .cantAccesos(1)
                .candidato(crearCandidato("Lucia", "Mendez", "77777777"))
                .resultado(null)
                .build();

        when(cuestionarioDAO.buscarPorPuesto(10L)).thenReturn(List.of(completoSinResultado));

        List<OrdenMeritoDTO> resultado = gestorReportes.generarOrdenMerito(10L, List.of(fecha));
        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).getCumpleMinimos());
        assertEquals(0.0, resultado.get(0).getPuntajeTotalObtenido(), 0.0001);
    }

    private Candidato crearCandidato(String nombre, String apellido, String dni) {
        return Candidato.builder()
                .nombre(nombre)
                .apellido(apellido)
                .tipoDocumento(TipoDocumento.DNI)
                .nroDocumento(dni)
                .build();
    }

    private Cuestionario crearCuestionarioCompleto(
            Long id,
            String nombre,
            String apellido,
            String dni,
            LocalDateTime fechaCreacion,
            List<double[]> puntajes) {
        List<ResultadoCompetencia> detalles = new ArrayList<>();
        Resultado resultado = Resultado.builder()
                .id(id)
                .fechaCalculo(fechaCreacion.plusHours(1))
                .resultadosCompetencia(detalles)
                .build();

        for (int i = 0; i < puntajes.size(); i++) {
            double[] par = puntajes.get(i);
            detalles.add(ResultadoCompetencia.builder()
                    .resultado(resultado)
                    .competencia(Competencia.builder().id((long) (i + 1)).nombre("COMP-" + (i + 1)).build())
                    .puntajeObtenido(par[0])
                    .puntajeRequerido(par[1])
                    .build());
        }

        return Cuestionario.builder()
                .id(id)
                .fechaCreacion(fechaCreacion)
                .estado(EstadoCuestionario.COMPLETO)
                .fechaHoraInicio(fechaCreacion.plusMinutes(10))
                .fechaFin(fechaCreacion.plusMinutes(40))
                .cantAccesos(1)
                .candidato(crearCandidato(nombre, apellido, dni))
                .resultado(resultado)
                .build();
    }
}
