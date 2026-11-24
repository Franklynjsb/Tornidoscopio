package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dominio.TipoDocumento;
import com.tornidoscopio.tp.dto.CandidatoClaveDTO;
import com.tornidoscopio.tp.dto.CandidatoGestionDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import com.tornidoscopio.tp.excepciones.EntidadNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorEvaluacionTest {

    @Mock
    private ICandidatoDAO candidatoDAO;

    @Mock
    private ICuestionarioDAO cuestionarioDAO;

    @InjectMocks
    private GestorEvaluacion gestorEvaluacion;

    private Candidato candidato1;
    private Candidato candidato2;
    private PuestoDTO puestoDTO;

    @BeforeEach
    void setUp() {
        candidato1 = Candidato.builder()
                .nroCandidato(1001L)
                .apellido("GÓMEZ")
                .nombre("JUAN PÉREZ")
                .tipoDocumento(TipoDocumento.DNI)
                .nroDocumento("40123456")
                .eliminado(false)
                .build();

        candidato2 = Candidato.builder()
                .nroCandidato(1002L)
                .apellido("MARTÍNEZ")
                .nombre("MARÍA LAURA")
                .tipoDocumento(TipoDocumento.DNI)
                .nroDocumento("42987654")
                .eliminado(false)
                .build();

        puestoDTO = PuestoDTO.builder()
                .id(1L)
                .codigo("PUE-01")
                .nombre("LÍDER DE PROYECTO TIC")
                .descripcion("Responsable de liderar equipos de software.")
                .idEmpresa(1L)
                .nombreEmpresa("TECHCORP ARGENTINA S.A.")
                .build();
    }

    @Test
    @DisplayName("CU025 Paso 3 - buscarCandidatos retorna DTOs mapeados correctamente")
    void buscarCandidatos_devuelveDTOs() {
        when(candidatoDAO.buscar("GÓMEZ")).thenReturn(List.of(candidato1));

        List<CandidatoGestionDTO> resultado = gestorEvaluacion.buscarCandidatos("GÓMEZ");

        assertEquals(1, resultado.size());
        CandidatoGestionDTO dto = resultado.get(0);
        assertEquals(1001L, dto.getNroCandidato());
        assertEquals("GÓMEZ", dto.getApellido());
        assertEquals("JUAN PÉREZ", dto.getNombre());
        assertEquals(TipoDocumento.DNI, dto.getTipoDocumento());
        assertEquals("40123456", dto.getNroDocumento());
        verify(candidatoDAO).buscar("GÓMEZ");
    }

    @Test
    @DisplayName("CU025 Paso 6 - validarCuestionariosActivos con lista vacía no consulta al DAO")
    void validarCuestionariosActivos_listaVacia() {
        assertTrue(gestorEvaluacion.validarCuestionariosActivos(List.of()).isEmpty());
        assertTrue(gestorEvaluacion.validarCuestionariosActivos(null).isEmpty());
        verifyNoInteractions(cuestionarioDAO);
    }

    @Test
    @DisplayName("CU025 Flujo 6.A - validarCuestionariosActivos retorna cuestionarios conflictivos")
    void validarCuestionariosActivos_conConflictos() {
        Cuestionario activo = Cuestionario.builder()
                .id(10L)
                .candidato(candidato1)
                .estado(EstadoCuestionario.ACTIVO)
                .build();
        List<Long> ids = List.of(1001L, 1002L);
        when(cuestionarioDAO.buscarActivosOEnProceso(ids)).thenReturn(List.of(activo));

        List<Cuestionario> conflictos = gestorEvaluacion.validarCuestionariosActivos(ids);

        assertEquals(1, conflictos.size());
        assertEquals(1001L, conflictos.get(0).getCandidato().getNroCandidato());
        assertEquals(EstadoCuestionario.ACTIVO, conflictos.get(0).getEstado());
    }

    @Test
    @DisplayName("CU025 Paso 11 - generarCuestionarios genera claves válidas de 8 caracteres con letras y dígitos sin persistir aún")
    void generarCuestionarios_exitoSinPersistirAun() {
        when(candidatoDAO.buscarPorNroCandidato(1001L)).thenReturn(candidato1);
        when(candidatoDAO.buscarPorNroCandidato(1002L)).thenReturn(candidato2);
        when(cuestionarioDAO.buscarPorClaveIngreso(anyString())).thenReturn(null);

        List<CandidatoGestionDTO> seleccionados = List.of(
                CandidatoGestionDTO.builder().nroCandidato(1001L).build(),
                CandidatoGestionDTO.builder().nroCandidato(1002L).build()
        );

        List<CandidatoClaveDTO> resultado = gestorEvaluacion.generarCuestionarios(puestoDTO, seleccionados);

        assertEquals(2, resultado.size());
        for (CandidatoClaveDTO dto : resultado) {
            String clave = dto.getClaveIngreso();
            assertNotNull(clave);
            assertEquals(8, clave.length(), "La clave debe tener longitud 8");
            assertTrue(clave.matches(".*[A-Z].*"), "La clave debe contener al menos una letra");
            assertTrue(clave.matches(".*[0-9].*"), "La clave debe contener al menos un dígito");
            assertTrue(clave.matches("[A-Z0-9]{8}"), "La clave debe ser alfanumérica");
        }
        assertNotEquals(resultado.get(0).getClaveIngreso(), resultado.get(1).getClaveIngreso());
        verify(cuestionarioDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CU025 Paso 11 - generarCuestionarios reintenta clave si ya existe en BD")
    void generarCuestionarios_reintentaClaveDuplicadaEnBD() {
        when(candidatoDAO.buscarPorNroCandidato(1001L)).thenReturn(candidato1);
        when(cuestionarioDAO.buscarPorClaveIngreso(anyString()))
                .thenReturn(Cuestionario.builder().id(99L).build())
                .thenReturn(null);

        List<CandidatoClaveDTO> resultado = gestorEvaluacion.generarCuestionarios(
                puestoDTO,
                List.of(CandidatoGestionDTO.builder().nroCandidato(1001L).build())
        );

        assertEquals(1, resultado.size());
        verify(cuestionarioDAO, times(2)).buscarPorClaveIngreso(anyString());
    }

    @Test
    @DisplayName("CU025 - generarCuestionarios valida puesto y candidatos obligatorios")
    void generarCuestionarios_validacionesEntrada() {
        List<CandidatoGestionDTO> lista = List.of(CandidatoGestionDTO.builder().nroCandidato(1001L).build());

        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.generarCuestionarios(null, lista));
        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.generarCuestionarios(PuestoDTO.builder().build(), lista));
        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.generarCuestionarios(puestoDTO, null));
        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.generarCuestionarios(puestoDTO, List.of()));
    }

    @Test
    @DisplayName("CU025 - generarCuestionarios lanza excepción si un candidato no existe o está eliminado")
    void generarCuestionarios_candidatoNoEncontradoOEliminado() {
        Candidato eliminado = Candidato.builder().nroCandidato(1003L).eliminado(true).build();
        when(candidatoDAO.buscarPorNroCandidato(9999L)).thenReturn(null);
        when(candidatoDAO.buscarPorNroCandidato(1003L)).thenReturn(eliminado);

        assertThrows(EntidadNoEncontradaException.class, () -> gestorEvaluacion.generarCuestionarios(
                puestoDTO, List.of(CandidatoGestionDTO.builder().nroCandidato(9999L).build())));

        assertThrows(EntidadNoEncontradaException.class, () -> gestorEvaluacion.generarCuestionarios(
                puestoDTO, List.of(CandidatoGestionDTO.builder().nroCandidato(1003L).build())));
    }

    @Test
    @DisplayName("CU025 Paso 12-13 - confirmarYGuardarCuestionarios persiste en estado ACTIVO y limpia pendientes")
    void confirmarYGuardarCuestionarios_persisteYLimpia() {
        when(candidatoDAO.buscarPorNroCandidato(1001L)).thenReturn(candidato1);
        when(candidatoDAO.buscarPorNroCandidato(1002L)).thenReturn(candidato2);
        when(cuestionarioDAO.buscarPorClaveIngreso(anyString())).thenReturn(null);

        List<CandidatoClaveDTO> claves = gestorEvaluacion.generarCuestionarios(
                puestoDTO,
                List.of(
                        CandidatoGestionDTO.builder().nroCandidato(1001L).build(),
                        CandidatoGestionDTO.builder().nroCandidato(1002L).build()
                )
        );

        gestorEvaluacion.confirmarYGuardarCuestionarios();

        ArgumentCaptor<Cuestionario> captor = ArgumentCaptor.forClass(Cuestionario.class);
        verify(cuestionarioDAO, times(2)).crear(captor.capture());

        List<Cuestionario> guardados = captor.getAllValues();
        assertEquals(2, guardados.size());
        assertEquals(EstadoCuestionario.ACTIVO, guardados.get(0).getEstado());
        assertEquals(0, guardados.get(0).getCantAccesos());
        assertNotNull(guardados.get(0).getFechaCreacion());
        assertEquals(guardados.get(0).getFechaCreacion(), guardados.get(1).getFechaCreacion());
        assertEquals(claves.get(0).getClaveIngreso(), guardados.get(0).getClaveIngreso());
        assertEquals(claves.get(1).getClaveIngreso(), guardados.get(1).getClaveIngreso());
        assertEquals(1L, guardados.get(0).getPuesto().getId());

        // Si se invoca nuevamente sin generar cuestionarios, debe fallar
        assertThrows(IllegalStateException.class, () -> gestorEvaluacion.confirmarYGuardarCuestionarios());
    }

    @Test
    @DisplayName("CU025 - confirmarYGuardarCuestionarios falla si no se generaron cuestionarios")
    void confirmarYGuardarCuestionarios_sinPendientes() {
        assertThrows(IllegalStateException.class, () -> gestorEvaluacion.confirmarYGuardarCuestionarios());
    }

    @Test
    @DisplayName("CU025 Paso 14-17 - exportarCandidatosConClaves genera archivo .xls con formato de nombre y contenido válidos")
    void exportarCandidatosConClaves_generaArchivoExcel() throws Exception {
        when(candidatoDAO.buscarPorNroCandidato(1001L)).thenReturn(candidato1);
        when(cuestionarioDAO.buscarPorClaveIngreso(anyString())).thenReturn(null);

        List<CandidatoClaveDTO> claves = gestorEvaluacion.generarCuestionarios(
                puestoDTO,
                List.of(CandidatoGestionDTO.builder().nroCandidato(1001L).build())
        );

        File archivo = gestorEvaluacion.exportarCandidatosConClaves(1L, claves);
        try {
            assertNotNull(archivo);
            assertTrue(archivo.exists());
            assertTrue(
                    archivo.getName().matches("^\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-LÍDER DE PROYECTO TIC\\.xls$"),
                    "Nombre inesperado: " + archivo.getName()
            );

            String contenido = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
            assertTrue(contenido.contains("GÓMEZ"));
            assertTrue(contenido.contains("JUAN PÉREZ"));
            assertTrue(contenido.contains("DNI"));
            assertTrue(contenido.contains("40123456"));
            assertTrue(contenido.contains(claves.get(0).getClaveIngreso()));
        } finally {
            Files.deleteIfExists(archivo.toPath());
        }
    }

    @Test
    @DisplayName("CU025 - exportarCandidatosConClaves resuelve nombre de puesto desde DAO si no hay puesto en memoria")
    void exportarCandidatosConClaves_resuelvePuestoDesdeDAO() throws Exception {
        Puesto puestoBD = Puesto.builder().id(5L).nombre("DESARROLLADOR JAVA").build();
        when(cuestionarioDAO.buscarPorPuesto(5L)).thenReturn(
                List.of(Cuestionario.builder().puesto(puestoBD).build())
        );

        List<CandidatoClaveDTO> claves = List.of(
                CandidatoClaveDTO.builder()
                        .apellidoCandidato("LÓPEZ")
                        .nombreCandidato("ANA")
                        .tipoDocumento(TipoDocumento.DNI)
                        .nroDocumento("38111222")
                        .claveIngreso("A1B2C3D4")
                        .build()
        );

        File archivo = gestorEvaluacion.exportarCandidatosConClaves(5L, claves);
        try {
            assertTrue(archivo.exists());
            assertTrue(archivo.getName().endsWith("-DESARROLLADOR JAVA.xls"));
        } finally {
            Files.deleteIfExists(archivo.toPath());
        }
    }

    @Test
    @DisplayName("CU025 - exportarCandidatosConClaves valida parámetros obligatorios")
    void exportarCandidatosConClaves_parametrosInvalidos() {
        List<CandidatoClaveDTO> claves = List.of(CandidatoClaveDTO.builder().claveIngreso("AB12CD34").build());
        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.exportarCandidatosConClaves(null, claves));
        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.exportarCandidatosConClaves(1L, null));
        assertThrows(IllegalArgumentException.class, () -> gestorEvaluacion.exportarCandidatosConClaves(1L, List.of()));
    }
}
