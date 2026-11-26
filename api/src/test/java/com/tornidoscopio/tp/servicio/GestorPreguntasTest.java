package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICompetenciaDAO;
import com.tornidoscopio.tp.dao.IFactorDAO;
import com.tornidoscopio.tp.dao.IOpcionRespuestaDAO;
import com.tornidoscopio.tp.dao.IPreguntaDAO;
import com.tornidoscopio.tp.dao.IPreguntaOpcionDAO;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.PreguntaOpcion;
import com.tornidoscopio.tp.dto.PreguntaDTO;
import com.tornidoscopio.tp.dto.PreguntaOpcionDTO;
import com.tornidoscopio.tp.dto.PreguntaSugeridaDTO;
import com.tornidoscopio.tp.excepciones.PreguntaDuplicadaException;
import com.tornidoscopio.tp.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorPreguntasTest {

    @Mock
    private IPreguntaDAO preguntaDAO;

    @Mock
    private IPreguntaOpcionDAO preguntaOpcionDAO;

    @Mock
    private IFactorDAO factorDAO;

    @Mock
    private ICompetenciaDAO competenciaDAO;

    @Mock
    private IOpcionRespuestaDAO opcionRespuestaDAO;

    @Mock
    private IServicioIA servicioIA;

    @InjectMocks
    private GestorPreguntas gestorPreguntas;

    private Competencia competencia;
    private Factor factorAlcance;
    private Factor factorTiempo;
    private OpcionRespuesta opcionRespuesta;
    private Pregunta pregunta1;
    private Pregunta pregunta2;

    @BeforeEach
    void setUp() {
        competencia = Competencia.builder()
                .id(1L)
                .codigo("COMP001")
                .nombre("GESTIÓN DE PROYECTOS")
                .descripcion("Capacidad para planificar y controlar proyectos.")
                .activo(true)
                .build();

        factorAlcance = Factor.builder()
                .id(10L)
                .nombre("GESTIÓN DEL ALCANCE")
                .descripcion("Definición y control del alcance.")
                .nroOrden(1)
                .activo(true)
                .competencia(competencia)
                .build();

        factorTiempo = Factor.builder()
                .id(11L)
                .nombre("GESTIÓN DEL TIEMPO")
                .descripcion("Definición y control del cronograma.")
                .nroOrden(2)
                .activo(true)
                .competencia(competencia)
                .build();

        opcionRespuesta = OpcionRespuesta.builder()
                .id(1L)
                .nombre("VERDADERO/FALSO")
                .descripcion("Opción dicotómica")
                .activo(true)
                .build();

        pregunta1 = Pregunta.builder()
                .id(100L)
                .nombre("WBS EN ALCANCE")
                .enunciado("¿La descomposición de estructura de trabajo (WBS) forma parte de la definición del alcance?")
                .descripcion("Evalúa conocimientos básicos de WBS")
                .activo(true)
                .factor(factorAlcance)
                .opcionRespuesta(opcionRespuesta)
                .build();

        pregunta2 = Pregunta.builder()
                .id(101L)
                .nombre("ACTIVIDAD FIN A COMIENZO")
                .enunciado("En el cronograma de un proyecto, ¿qué significa que una actividad sea del tipo Fin a Comienzo?")
                .descripcion("Dependencias en cronograma")
                .activo(true)
                .factor(factorTiempo)
                .opcionRespuesta(opcionRespuesta)
                .build();
    }

    // =========================================================================
    // CU020 - Gestionar Preguntas
    // =========================================================================

    @Test
    @DisplayName("CU020 Paso 3 - buscarPreguntas lanza ReglaNegocioException si todos los criterios son nulos o vacíos")
    void buscarPreguntas_sinCriterios_lanzaExcepcion() {
        ReglaNegocioException exNulos = assertThrows(
                ReglaNegocioException.class,
                () -> gestorPreguntas.buscarPreguntas(null, null, null)
        );
        assertEquals("Debe ingresar al menos un criterio de búsqueda", exNulos.getMessage());

        ReglaNegocioException exVacios = assertThrows(
                ReglaNegocioException.class,
                () -> gestorPreguntas.buscarPreguntas("", "   ", "\t")
        );
        assertEquals("Debe ingresar al menos un criterio de búsqueda", exVacios.getMessage());

        verifyNoInteractions(preguntaDAO);
    }

    @Test
    @DisplayName("CU020 Paso 3 y 4 - buscarPreguntas filtra sólo por competencia y arma PreguntaDTO navegando Factor y Competencia")
    void buscarPreguntas_soloPorCompetencia_retornaListaDTO() {
        when(preguntaDAO.buscar("GESTIÓN DE PROYECTOS", null, null))
                .thenReturn(List.of(pregunta1, pregunta2));

        List<PreguntaDTO> resultado = gestorPreguntas.buscarPreguntas("GESTIÓN DE PROYECTOS", null, null);

        assertEquals(2, resultado.size());

        PreguntaDTO dto1 = resultado.get(0);
        assertEquals(100L, dto1.getId());
        assertEquals("WBS EN ALCANCE", dto1.getNombre());
        assertEquals("GESTIÓN DEL ALCANCE", dto1.getNombreFactor());
        assertEquals("GESTIÓN DE PROYECTOS", dto1.getNombreCompetencia());

        PreguntaDTO dto2 = resultado.get(1);
        assertEquals(101L, dto2.getId());
        assertEquals("ACTIVIDAD FIN A COMIENZO", dto2.getNombre());
        assertEquals("GESTIÓN DEL TIEMPO", dto2.getNombreFactor());
        assertEquals("GESTIÓN DE PROYECTOS", dto2.getNombreCompetencia());

        verify(preguntaDAO).buscar("GESTIÓN DE PROYECTOS", null, null);
    }

    @Test
    @DisplayName("CU020 Paso 3 y 4 - buscarPreguntas acepta búsqueda sólo por factor")
    void buscarPreguntas_soloPorFactor_retornaListaDTO() {
        when(preguntaDAO.buscar("", "GESTIÓN DEL ALCANCE", ""))
                .thenReturn(List.of(pregunta1));

        List<PreguntaDTO> resultado = gestorPreguntas.buscarPreguntas("", "GESTIÓN DEL ALCANCE", "");

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).getId());
        assertEquals("WBS EN ALCANCE", resultado.get(0).getNombre());
        assertEquals("GESTIÓN DEL ALCANCE", resultado.get(0).getNombreFactor());
        assertEquals("GESTIÓN DE PROYECTOS", resultado.get(0).getNombreCompetencia());
        verify(preguntaDAO).buscar("", "GESTIÓN DEL ALCANCE", "");
    }

    @Test
    @DisplayName("CU020 Paso 3 y 4 - buscarPreguntas acepta búsqueda sólo por nombre de pregunta")
    void buscarPreguntas_soloPorNombrePregunta_retornaListaDTO() {
        when(preguntaDAO.buscar(null, null, "WBS"))
                .thenReturn(List.of(pregunta1));

        List<PreguntaDTO> resultado = gestorPreguntas.buscarPreguntas(null, null, "WBS");

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).getId());
        assertEquals("WBS EN ALCANCE", resultado.get(0).getNombre());
        verify(preguntaDAO).buscar(null, null, "WBS");
    }

    @Test
    @DisplayName("CU020 Paso 3 y 4 - buscarPreguntas combina los tres criterios y retorna lista vacía si no hay coincidencias")
    void buscarPreguntas_tresCriteriosSinCoincidencias_retornaListaVacia() {
        when(preguntaDAO.buscar("GESTIÓN DE PROYECTOS", "GESTIÓN DEL ALCANCE", "INEXISTENTE"))
                .thenReturn(List.of());

        List<PreguntaDTO> resultado = gestorPreguntas.buscarPreguntas(
                "GESTIÓN DE PROYECTOS",
                "GESTIÓN DEL ALCANCE",
                "INEXISTENTE"
        );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(preguntaDAO).buscar("GESTIÓN DE PROYECTOS", "GESTIÓN DEL ALCANCE", "INEXISTENTE");
    }

    // =========================================================================
    // CU021 - Dar de Alta Pregunta
    // =========================================================================

    @Test
    @DisplayName("CU021 Flujo 2.A - validarOmisiones detecta todos los campos obligatorios faltantes")
    void validarOmisiones_detectaCamposObligatorios() {
        PreguntaDTO vacia = PreguntaDTO.builder().build();
        List<String> omisiones = gestorPreguntas.validarOmisiones(vacia);
        assertEquals(4, omisiones.size());

        PreguntaDTO completa = PreguntaDTO.builder()
                .nombre("PREGUNTA VALIDA")
                .enunciado("¿Enunciado válido?")
                .idFactor(10L)
                .idOpcionRespuesta(1L)
                .build();
        assertTrue(gestorPreguntas.validarOmisiones(completa).isEmpty());
    }

    @Test
    @DisplayName("CU021 Flujo 2.B - validarLongitudMaxima detecta si el nombre supera los 150 caracteres")
    void validarLongitudMaxima_detectaNombreExcedido() {
        String nombre151 = "A".repeat(151);
        PreguntaDTO excedida = PreguntaDTO.builder().nombre(nombre151).build();

        List<String> errores = gestorPreguntas.validarLongitudMaxima(excedida);
        assertEquals(1, errores.size());
        assertTrue(errores.get(0).contains("150"));

        PreguntaDTO valida = PreguntaDTO.builder().nombre("A".repeat(150)).build();
        assertTrue(gestorPreguntas.validarLongitudMaxima(valida).isEmpty());
    }

    @Test
    @DisplayName("CU021 - validarPonderacion exige suma igual a 10 y valores mayores o iguales a 0")
    void validarPonderacion_verificaSuma10YValoresNoNegativos() {
        List<PreguntaOpcionDTO> validas = List.of(
                new PreguntaOpcionDTO(1L, 10.0),
                new PreguntaOpcionDTO(2L, 0.0)
        );
        assertTrue(gestorPreguntas.validarPonderacion(validas));

        List<PreguntaOpcionDTO> sumaIncorrecta = List.of(
                new PreguntaOpcionDTO(1L, 5.0),
                new PreguntaOpcionDTO(2L, 3.0)
        );
        assertFalse(gestorPreguntas.validarPonderacion(sumaIncorrecta));

        List<PreguntaOpcionDTO> conNegativo = List.of(
                new PreguntaOpcionDTO(1L, 12.0),
                new PreguntaOpcionDTO(2L, -2.0)
        );
        assertFalse(gestorPreguntas.validarPonderacion(conNegativo));

        assertFalse(gestorPreguntas.validarPonderacion(null));
        assertFalse(gestorPreguntas.validarPonderacion(List.of()));
    }

    @Test
    @DisplayName("CU021 Flujo 2.C - altaPregunta lanza PreguntaDuplicadaException indicando si duplica nombre, enunciado o ambos")
    void altaPregunta_duplicada_lanzaExcepcionClarificandoCampo() {
        PreguntaDTO dtoNombreDuplicado = PreguntaDTO.builder()
                .idFactor(10L)
                .nombre("WBS EN ALCANCE")
                .enunciado("¿Otro enunciado distinto?")
                .idOpcionRespuesta(1L)
                .build();

        when(preguntaDAO.buscarDuplicada(10L, "WBS EN ALCANCE", "¿Otro enunciado distinto?"))
                .thenReturn(List.of(pregunta1));

        PreguntaDuplicadaException exNombre = assertThrows(
                PreguntaDuplicadaException.class,
                () -> gestorPreguntas.altaPregunta(dtoNombreDuplicado)
        );
        assertTrue(exNombre.getMessage().contains("ese NOMBRE"));
        assertFalse(exNombre.getMessage().contains("ENUNCIADO"));

        PreguntaDTO dtoAmbosDuplicados = PreguntaDTO.builder()
                .idFactor(10L)
                .nombre("WBS EN ALCANCE")
                .enunciado(pregunta1.getEnunciado())
                .idOpcionRespuesta(1L)
                .build();

        when(preguntaDAO.buscarDuplicada(10L, "WBS EN ALCANCE", pregunta1.getEnunciado()))
                .thenReturn(List.of(pregunta1));

        PreguntaDuplicadaException exAmbos = assertThrows(
                PreguntaDuplicadaException.class,
                () -> gestorPreguntas.altaPregunta(dtoAmbosDuplicados)
        );
        assertTrue(exAmbos.getMessage().contains("ese NOMBRE y ese ENUNCIADO"));

        verify(preguntaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CU021 Paso 3 - altaPregunta y asignarOpciones crean la pregunta activa y sus ponderaciones")
    void altaPregunta_y_asignarOpciones_exito() {
        PreguntaDTO nuevaDTO = PreguntaDTO.builder()
                .idFactor(10L)
                .idOpcionRespuesta(1L)
                .nombre("MATRIZ RACI")
                .enunciado("¿La matriz RACI define roles y responsabilidades?")
                .descripcion("Evalúa matriz de responsabilidades")
                .build();

        when(preguntaDAO.buscarDuplicada(10L, "MATRIZ RACI", "¿La matriz RACI define roles y responsabilidades?"))
                .thenReturn(List.of());
        when(factorDAO.buscarPorId(10L)).thenReturn(factorAlcance);
        when(opcionRespuestaDAO.buscarPorId(1L)).thenReturn(opcionRespuesta);
        doAnswer(invocation -> {
            Pregunta p = invocation.getArgument(0);
            p.setId(200L);
            return null;
        }).when(preguntaDAO).crear(any(Pregunta.class));

        PreguntaDTO creada = gestorPreguntas.altaPregunta(nuevaDTO);

        assertEquals(200L, creada.getId());
        ArgumentCaptor<Pregunta> pregCaptor = ArgumentCaptor.forClass(Pregunta.class);
        verify(preguntaDAO).crear(pregCaptor.capture());
        Pregunta persistida = pregCaptor.getValue();
        assertTrue(persistida.getActivo());
        assertEquals("MATRIZ RACI", persistida.getNombre());
        assertEquals(factorAlcance, persistida.getFactor());
        assertEquals(opcionRespuesta, persistida.getOpcionRespuesta());

        List<PreguntaOpcionDTO> opciones = List.of(
                new PreguntaOpcionDTO(101L, 10.0),
                new PreguntaOpcionDTO(102L, 0.0)
        );
        gestorPreguntas.asignarOpciones(creada.getId(), opciones);

        ArgumentCaptor<PreguntaOpcion> poCaptor = ArgumentCaptor.forClass(PreguntaOpcion.class);
        verify(preguntaOpcionDAO, times(2)).crear(poCaptor.capture());
        List<PreguntaOpcion> ponderacionesGuardadas = poCaptor.getAllValues();
        assertEquals(200L, ponderacionesGuardadas.get(0).getPregunta().getId());
        assertEquals(101L, ponderacionesGuardadas.get(0).getItemOpcion().getId());
        assertEquals(10.0, ponderacionesGuardadas.get(0).getPonderacion());
    }

    @Test
    @DisplayName("CU021 Flujo 1.A.1 (CU033) - generarPreguntaIA delega en IServicioIA con las entidades buscadas")
    void generarPreguntaIA_delegaEnServicioIA() {
        PreguntaSugeridaDTO sugerida = new PreguntaSugeridaDTO("SUGERIDA", "¿Enunciado IA?", "Desc IA");
        when(competenciaDAO.buscarPorId(1L)).thenReturn(competencia);
        when(factorDAO.buscarPorId(10L)).thenReturn(factorAlcance);
        when(opcionRespuestaDAO.buscarPorId(1L)).thenReturn(opcionRespuesta);
        when(servicioIA.generarPregunta(competencia, factorAlcance, "Contexto", opcionRespuesta))
                .thenReturn(sugerida);

        PreguntaSugeridaDTO resultado = gestorPreguntas.generarPreguntaIA(1L, 10L, "Contexto", 1L);

        assertNotNull(resultado);
        assertEquals("SUGERIDA", resultado.getNombrePregunta());
        verify(servicioIA).generarPregunta(competencia, factorAlcance, "Contexto", opcionRespuesta);
    }
}
