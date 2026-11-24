package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IPuestoDAO;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.CompetenciaRequerida;
import com.tornidoscopio.tp.dominio.Empresa;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dto.CompetenciaRequeridaDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import com.tornidoscopio.tp.excepciones.CompetenciasNoEvaluablesException;
import com.tornidoscopio.tp.excepciones.EntidadNoEncontradaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorPuestosTest {

    @Mock
    private IPuestoDAO puestoDAO;

    @Mock
    private ICuestionarioDAO cuestionarioDAO;

    @InjectMocks
    private GestorPuestos gestorPuestos;

    @Test
    @DisplayName("CU025 Paso 8 - buscarPuestos mapea puestos activos con su empresa a PuestoDTO")
    void buscarPuestos_mapeaCorrectamente() {
        Empresa empresa = Empresa.builder().id(1L).nombre("TECHCORP ARGENTINA S.A.").build();
        Puesto puesto = Puesto.builder()
                .id(1L)
                .codigo("PUE-01")
                .nombre("LÍDER DE PROYECTO TIC")
                .descripcion("Líder técnico")
                .activo(true)
                .empresa(empresa)
                .build();

        when(puestoDAO.buscar("LÍDER")).thenReturn(List.of(puesto));

        List<PuestoDTO> resultado = gestorPuestos.buscarPuestos("LÍDER");

        assertEquals(1, resultado.size());
        PuestoDTO dto = resultado.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("PUE-01", dto.getCodigo());
        assertEquals("LÍDER DE PROYECTO TIC", dto.getNombre());
        assertEquals(1L, dto.getIdEmpresa());
        assertEquals("TECHCORP ARGENTINA S.A.", dto.getNombreEmpresa());
        verify(puestoDAO).buscar("LÍDER");
    }

    @Test
    @DisplayName("CU025 Paso 9 - obtenerCompetenciasRequeridas retorna competencias cuando todas son evaluables")
    void obtenerCompetenciasRequeridas_todasEvaluables() {
        Pregunta p1 = Pregunta.builder().id(1L).activo(true).build();
        Pregunta p2 = Pregunta.builder().id(2L).activo(true).build();

        Factor factorValido = Factor.builder()
                .id(1L)
                .nombre("GESTIÓN DEL ALCANCE")
                .activo(true)
                .preguntas(List.of(p1, p2))
                .build();

        Competencia comp = Competencia.builder()
                .id(1L)
                .nombre("GESTIÓN DE PROYECTOS")
                .activo(true)
                .factores(List.of(factorValido))
                .build();

        CompetenciaRequerida cr = CompetenciaRequerida.builder()
                .id(1L)
                .competencia(comp)
                .puntajeMinimoRequerido(7.0)
                .build();

        Puesto puesto = Puesto.builder()
                .id(1L)
                .nombre("LÍDER DE PROYECTO TIC")
                .activo(true)
                .competenciasRequeridas(List.of(cr))
                .build();

        when(puestoDAO.buscarPorId(1L)).thenReturn(puesto);

        List<CompetenciaRequeridaDTO> resultado = gestorPuestos.obtenerCompetenciasRequeridas(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdCompetencia());
        assertEquals("GESTIÓN DE PROYECTOS", resultado.get(0).getNombreCompetencia());
        assertEquals(7.0, resultado.get(0).getPuntajeMinimoRequerido());
    }

    @Test
    @DisplayName("CU025 Flujo 9.A - obtenerCompetenciasRequeridas lanza excepción listando competencias sin factor evaluable")
    void obtenerCompetenciasRequeridas_lanzaExcepcionSiAlgunaNoEsEvaluable() {
        // Competencia 1: factor activo con solo 1 pregunta activa y 1 inactiva -> NO evaluable
        Factor factorInsuficiente = Factor.builder()
                .id(1L)
                .activo(true)
                .preguntas(List.of(
                        Pregunta.builder().id(1L).activo(true).build(),
                        Pregunta.builder().id(2L).activo(false).build()
                ))
                .build();
        Competencia compInvalida1 = Competencia.builder()
                .id(1L)
                .nombre("COMUNICACIÓN")
                .activo(true)
                .factores(List.of(factorInsuficiente))
                .build();

        // Competencia 2: factor con 2 preguntas activas pero el factor está inactivo -> NO evaluable
        Factor factorInactivo = Factor.builder()
                .id(2L)
                .activo(false)
                .preguntas(List.of(
                        Pregunta.builder().id(3L).activo(true).build(),
                        Pregunta.builder().id(4L).activo(true).build()
                ))
                .build();
        Competencia compInvalida2 = Competencia.builder()
                .id(2L)
                .nombre("LIDERAZGO")
                .activo(true)
                .factores(List.of(factorInactivo))
                .build();

        // Competencia 3: válida
        Factor factorValido = Factor.builder()
                .id(3L)
                .activo(true)
                .preguntas(List.of(
                        Pregunta.builder().id(5L).activo(true).build(),
                        Pregunta.builder().id(6L).activo(true).build()
                ))
                .build();
        Competencia compValida = Competencia.builder()
                .id(3L)
                .nombre("LEALTAD")
                .activo(true)
                .factores(List.of(factorValido))
                .build();

        Puesto puesto = Puesto.builder()
                .id(1L)
                .activo(true)
                .competenciasRequeridas(List.of(
                        CompetenciaRequerida.builder().competencia(compInvalida1).puntajeMinimoRequerido(6.0).build(),
                        CompetenciaRequerida.builder().competencia(compInvalida2).puntajeMinimoRequerido(8.0).build(),
                        CompetenciaRequerida.builder().competencia(compValida).puntajeMinimoRequerido(5.0).build()
                ))
                .build();

        when(puestoDAO.buscarPorId(1L)).thenReturn(puesto);

        CompetenciasNoEvaluablesException ex = assertThrows(
                CompetenciasNoEvaluablesException.class,
                () -> gestorPuestos.obtenerCompetenciasRequeridas(1L)
        );

        assertEquals(List.of("COMUNICACIÓN", "LIDERAZGO"), ex.getCompetenciasNoEvaluables());
        assertTrue(ex.getMessage().contains("COMUNICACIÓN"));
        assertTrue(ex.getMessage().contains("LIDERAZGO"));
        assertFalse(ex.getMessage().contains("LEALTAD"));
    }

    @Test
    @DisplayName("CU025 - obtenerCompetenciasRequeridas lanza excepción si el puesto no existe, está inactivo o id es nulo")
    void obtenerCompetenciasRequeridas_puestoInvalido() {
        when(puestoDAO.buscarPorId(99L)).thenReturn(null);
        when(puestoDAO.buscarPorId(2L)).thenReturn(Puesto.builder().id(2L).activo(false).build());

        assertThrows(IllegalArgumentException.class, () -> gestorPuestos.obtenerCompetenciasRequeridas(null));
        assertThrows(EntidadNoEncontradaException.class, () -> gestorPuestos.obtenerCompetenciasRequeridas(99L));
        assertThrows(EntidadNoEncontradaException.class, () -> gestorPuestos.obtenerCompetenciasRequeridas(2L));
    }
}
