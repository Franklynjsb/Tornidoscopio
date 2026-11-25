package com.tornidoscopio.tp.dominio;

import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CuestionarioDomainTest {

    @Test
    void factor_seleccionaPreguntasAleatoriasSoloActivas() {
        Pregunta p1 = Pregunta.builder().id(1L).enunciado("P1").activo(true).build();
        Pregunta p2 = Pregunta.builder().id(2L).enunciado("P2").activo(true).build();
        Pregunta p3 = Pregunta.builder().id(3L).enunciado("P3").activo(true).build();
        Pregunta p4 = Pregunta.builder().id(4L).enunciado("P4").activo(false).build();

        Factor factor = Factor.builder()
                .id(10L)
                .nombre("ALCANCE")
                .activo(true)
                .preguntas(List.of(p1, p2, p3, p4))
                .build();

        List<Pregunta> seleccionadas = factor.obtenerPreguntasAleatorias(2);
        assertEquals(2, seleccionadas.size());
        assertTrue(seleccionadas.stream().allMatch(p -> Boolean.TRUE.equals(p.getActivo())));
        assertEquals(2, seleccionadas.stream().map(Pregunta::getId).distinct().count());
    }

    @Test
    void cuestionario_validaAccesosYTiempos() {
        Cuestionario c = Cuestionario.builder()
                .id(1L)
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(2)
                .fechaCreacion(LocalDateTime.now().minusMinutes(30))
                .fechaHoraInicio(LocalDateTime.now().minusMinutes(20))
                .build();

        assertTrue(c.validarAcceso(3));
        c.registrarIngreso();
        assertEquals(3, c.getCantAccesos());
        assertNotNull(c.getFechaUltimoIngreso());
        assertFalse(c.validarAcceso(3));

        assertTrue(c.validarTiempoParaComenzar(LocalTime.of(1, 0)));
        assertFalse(c.validarTiempoParaComenzar(LocalTime.of(0, 15)));

        assertTrue(c.validarTiempoParaCompletar(LocalTime.of(0, 30)));
        assertFalse(c.validarTiempoParaCompletar(LocalTime.of(0, 10)));
    }

    @Test
    void generarBloques_tomaDosPreguntasPorFactorEvaluableYCalculaPuntaje() {
        ItemOpcion itemSi = ItemOpcion.builder().id(1L).ordenVisualizacion(1).descripcion("SI").build();
        ItemOpcion itemNo = ItemOpcion.builder().id(2L).ordenVisualizacion(2).descripcion("NO").build();
        OpcionRespuesta opcionVF = OpcionRespuesta.builder()
                .id(1L)
                .nombre("VF")
                .activo(true)
                .items(List.of(itemSi, itemNo))
                .build();

        Competencia comp1 = Competencia.builder().id(1L).nombre("GESTION").activo(true).build();
        Factor f1 = Factor.builder().id(1L).nombre("ALCANCE").activo(true).competencia(comp1).build();
        Factor f2 = Factor.builder().id(2L).nombre("TIEMPO").activo(true).competencia(comp1).build();
        Factor fIncompleto = Factor.builder().id(3L).nombre("RIESGOS").activo(true).competencia(comp1).build();

        Pregunta p1 = crearPregunta(1L, "P1", f1, opcionVF, itemSi, 10.0, itemNo, 0.0);
        Pregunta p2 = crearPregunta(2L, "P2", f1, opcionVF, itemSi, 10.0, itemNo, 0.0);
        Pregunta p3 = crearPregunta(3L, "P3", f2, opcionVF, itemSi, 10.0, itemNo, 0.0);
        Pregunta p4 = crearPregunta(4L, "P4", f2, opcionVF, itemSi, 10.0, itemNo, 0.0);
        Pregunta pSoloUna = crearPregunta(5L, "P5", fIncompleto, opcionVF, itemSi, 10.0, itemNo, 0.0);

        f1.setPreguntas(List.of(p1, p2));
        f2.setPreguntas(List.of(p3, p4));
        fIncompleto.setPreguntas(List.of(pSoloUna));
        comp1.setFactores(List.of(f1, f2, fIncompleto));

        Puesto puesto = Puesto.builder()
                .id(1L)
                .nombre("LIDER")
                .activo(true)
                .competenciasRequeridas(List.of(
                        CompetenciaRequerida.builder()
                                .id(1L)
                                .competencia(comp1)
                                .puntajeMinimoRequerido(7.0)
                                .build()
                ))
                .build();

        Cuestionario cuestionario = Cuestionario.builder()
                .id(100L)
                .estado(EstadoCuestionario.ACTIVO)
                .cantAccesos(1)
                .fechaCreacion(LocalDateTime.now())
                .puesto(puesto)
                .build();

        List<BloqueCuestionario> bloques = cuestionario.generarBloques(2);

        // Solo 2 factores tienen >= 2 preguntas activas => 4 preguntas => 2 bloques de 2
        assertEquals(2, bloques.size());
        assertEquals(1, bloques.get(0).getNroBloque());
        assertEquals(2, bloques.get(1).getNroBloque());
        assertFalse(cuestionario.esUltimoBloque(bloques.get(0)));
        assertTrue(cuestionario.esUltimoBloque(bloques.get(1)));
        assertEquals(bloques.get(0), cuestionario.obtenerBloquePendiente());

        // Responder ambos bloques: f1 con SI+SI (20/2 = 10.0), f2 con SI+NO (10/2 = 5.0) => promedio = 7.5
        for (BloqueCuestionario b : bloques) {
            List<RespuestaCandidatoDTO> dtos = b.getRespuestas().stream()
                    .map(rc -> {
                        Long pregId = rc.getPregunta().getId();
                        Long itemElegido = (pregId == 4L) ? itemNo.getId() : itemSi.getId();
                        return new RespuestaCandidatoDTO(pregId, itemElegido);
                    })
                    .toList();
            assertTrue(b.validarRespuestasCompletas(dtos));
            b.registrarRespuestas(dtos);
            b.marcarCompletado();
        }

        assertNull(cuestionario.obtenerBloquePendiente());
        assertEquals(7.5, cuestionario.calcularPuntajeCompetencia(comp1), 0.0001);
    }

    @Test
    void generarBloques_sinFactoresEvaluablesLanzaExcepcion() {
        Competencia comp = Competencia.builder().id(1L).nombre("COMP").activo(true).build();
        Factor factor = Factor.builder().id(1L).nombre("F1").activo(true).competencia(comp)
                .preguntas(List.of(Pregunta.builder().id(1L).activo(true).build()))
                .build();
        comp.setFactores(List.of(factor));

        Puesto puesto = Puesto.builder()
                .id(1L)
                .competenciasRequeridas(List.of(
                        CompetenciaRequerida.builder().competencia(comp).puntajeMinimoRequerido(5.0).build()
                ))
                .build();

        Cuestionario cuestionario = Cuestionario.builder().puesto(puesto).build();
        assertThrows(ReglaNegocioException.class, () -> cuestionario.generarBloques(2));
    }

    @Test
    void resultado_agregaResultadoCompetenciaBidireccionalmente() {
        Resultado resultado = Resultado.builder().fechaCalculo(LocalDateTime.now()).build();
        ResultadoCompetencia rc = ResultadoCompetencia.builder()
                .puntajeObtenido(8.0)
                .puntajeRequerido(7.0)
                .build();

        resultado.agregarResultadoCompetencia(rc);

        assertEquals(1, resultado.getResultadosCompetencia().size());
        assertEquals(resultado, rc.getResultado());
    }

    private Pregunta crearPregunta(Long id, String enunciado, Factor factor, OpcionRespuesta opc,
                                   ItemOpcion i1, double pond1, ItemOpcion i2, double pond2) {
        Pregunta p = Pregunta.builder()
                .id(id)
                .nombre("PREG-" + id)
                .enunciado(enunciado)
                .activo(true)
                .factor(factor)
                .opcionRespuesta(opc)
                .build();
        p.setOpcionesPonderadas(List.of(
                PreguntaOpcion.builder().pregunta(p).itemOpcion(i1).ponderacion(pond1).build(),
                PreguntaOpcion.builder().pregunta(p).itemOpcion(i2).ponderacion(pond2).build()
        ));
        return p;
    }
}
