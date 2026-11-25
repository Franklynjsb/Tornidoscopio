package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dto.CandidatoClaveDTO;
import com.tornidoscopio.tp.dto.CandidatoGestionDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import com.tornidoscopio.tp.excepciones.EntidadNoEncontradaException;
import com.tornidoscopio.tp.excepciones.ExportacionExcelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Gestor responsable del Caso de Uso 025: Evaluar a Candidatos.
 * Orquesta la búsqueda de candidatos, la validación de cuestionarios activos o en proceso,
 * la generación de cuestionarios en estado ACTIVO con claves aleatorias de 8 caracteres,
 * su persistencia confirmada y la exportación a archivo Excel (.xls).
 */
@Service
public class GestorEvaluacion {

    private static final int LONGITUD_CLAVE = 8;
    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITOS = "0123456789";
    private static final String ALFANUMERICOS = LETRAS + DIGITOS;
    private static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private final SecureRandom random = new SecureRandom();

    // GestorEvaluacion "1" --> "1" ICandidatoDAO
    private ICandidatoDAO candidatoDAO;
    // GestorEvaluacion "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;

    // Estado en memoria entre Paso 11 (generarCuestionarios) y Paso 12-13 (confirmarYGuardarCuestionarios)
    private List<Cuestionario> listaCuestionarios = new ArrayList<>();
    private Puesto puestoActual;
    private String nombrePuestoExportacion;

    @Autowired
    public GestorEvaluacion(ICandidatoDAO candidatoDAO, ICuestionarioDAO cuestionarioDAO) {
        this.candidatoDAO = candidatoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
    }

    /**
     * CU025 - Paso 3: busca candidatos activos según los criterios ingresados
     * y los devuelve mapeados como {@link CandidatoGestionDTO}.
     *
     * @param criterios texto de filtrado (apellido, nombre o número de candidato)
     * @return lista de candidatos que cumplen con el criterio
     */
    @Transactional(readOnly = true)
    public List<CandidatoGestionDTO> buscarCandidatos(String criterios) {
        List<Candidato> listaCandidatos = candidatoDAO.buscar(criterios);
        return listaCandidatos.stream()
                .map(this::armarCandidatoGestionDTO)
                .toList();
    }

    /**
     * CU025 - Paso 6 y Flujo Alternativo 6.A: verifica si alguno de los candidatos
     * seleccionados posee un cuestionario en estado {@code ACTIVO} o {@code EN_PROCESO}.
     *
     * @param ids lista de números de candidato seleccionados para evaluar
     * @return lista de cuestionarios conflictivos (vacía si ningún candidato tiene conflictos)
     */
    @Transactional(readOnly = true)
    public List<Cuestionario> validarCuestionariosActivos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return cuestionarioDAO.buscarActivosOEnProceso(ids);
    }

    /**
     * CU025 - Paso 10-11: genera en memoria los cuestionarios en estado {@code ACTIVO}
     * para los candidatos seleccionados y el puesto indicado, asignando a cada uno una
     * clave aleatoria única de 8 caracteres (letras y dígitos).
     * Los cuestionarios no se persisten hasta invocar {@link #confirmarYGuardarCuestionarios()}.
     *
     * @param p puesto seleccionado para la evaluación
     * @param c lista de candidatos seleccionados a evaluar
     * @return lista de candidatos con sus respectivas claves de ingreso generadas
     * @throws IllegalArgumentException     si no se seleccionó un puesto o la lista de candidatos está vacía
     * @throws EntidadNoEncontradaException si algún candidato no existe o fue eliminado
     */
    @Transactional(readOnly = true)
    public List<CandidatoClaveDTO> generarCuestionarios(PuestoDTO p, List<CandidatoGestionDTO> c) {
        if (p == null || p.getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una función o puesto");
        }
        if (c == null || c.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar uno o más candidatos");
        }

        Puesto puesto = mapearPuesto(p);
        this.puestoActual = puesto;
        this.listaCuestionarios = new ArrayList<>();

        List<CandidatoClaveDTO> listaCandidatoClaveDTO = new ArrayList<>();
        Set<String> clavesGeneradasEnLote = new HashSet<>();
        LocalDateTime fechaCreacion = LocalDateTime.now();

        for (CandidatoGestionDTO candidatoGestDTO : c) {
            Candidato cand = mapearCandidato(candidatoGestDTO);
            String claveIngreso = generarClaveUnica(clavesGeneradasEnLote);

            Cuestionario cuest = Cuestionario.builder()
                    .candidato(cand)
                    .puesto(puesto)
                    .estado(EstadoCuestionario.ACTIVO)
                    .claveIngreso(claveIngreso)
                    .fechaCreacion(fechaCreacion)
                    .cantAccesos(0)
                    .build();

            this.listaCuestionarios.add(cuest);

            CandidatoClaveDTO candClaveDTO = armarCandidatoClaveDTO(cand);
            candClaveDTO.setClaveIngreso(claveIngreso);
            listaCandidatoClaveDTO.add(candClaveDTO);
        }

        return listaCandidatoClaveDTO;
    }

    /**
     * CU025 - Paso 12-13: confirma la evaluación y persiste todos los cuestionarios
     * generados previamente en {@link #generarCuestionarios(PuestoDTO, List)} de forma atómica.
     *
     * @throws IllegalStateException si no hay cuestionarios generados pendientes de guardar
     */
    @Transactional
    public void confirmarYGuardarCuestionarios() {
        if (this.listaCuestionarios == null || this.listaCuestionarios.isEmpty()) {
            throw new IllegalStateException("No hay cuestionarios generados pendientes de confirmación");
        }

        for (Cuestionario cuest : this.listaCuestionarios) {
            cuestionarioDAO.crear(cuest);
        }

        this.listaCuestionarios.clear();
    }

    /**
     * CU025 - Paso 14-17: genera un archivo Excel (.xls) con la lista de candidatos
     * evaluados y sus claves de ingreso.
     * El nombre del archivo sigue el formato {@code aaaa-mm-dd-hh-mm-NombreFunción.xls}.
     *
     * @param idPuesto identificador del puesto evaluado
     * @param c        lista de candidatos con sus claves de ingreso
     * @return archivo {@link File} generado en disco listo para descarga o guardado
     * @throws IllegalArgumentException  si {@code idPuesto} es nulo o la lista está vacía
     * @throws ExportacionExcelException si ocurre un error de E/S al crear el archivo
     */
    @Transactional(readOnly = true)
    public File exportarCandidatosConClaves(Long idPuesto, List<CandidatoClaveDTO> c) {
        if (idPuesto == null) {
            throw new IllegalArgumentException("Debe indicar el puesto para exportar las claves");
        }
        if (c == null || c.isEmpty()) {
            throw new IllegalArgumentException("No hay candidatos con claves para exportar");
        }

        this.nombrePuestoExportacion = resolverNombrePuesto(idPuesto);
        String nombreArchivo = generarNombreArchivo();
        Path rutaArchivo = Path.of(System.getProperty("java.io.tmpdir"), nombreArchivo);

        try (BufferedWriter writer = Files.newBufferedWriter(rutaArchivo, StandardCharsets.UTF_8)) {
            writer.write(construirContenidoExcelXml(c));
        } catch (IOException e) {
            throw new ExportacionExcelException("Error al generar el archivo Excel: " + nombreArchivo, e);
        }

        return rutaArchivo.toFile();
    }

    public void actualizarCuestionariosVencidos() {
    }

    private CandidatoGestionDTO armarCandidatoGestionDTO(Candidato c) {
        return CandidatoGestionDTO.builder()
                .nroCandidato(c.getNroCandidato())
                .apellido(c.getApellido())
                .nombre(c.getNombre())
                .tipoDocumento(c.getTipoDocumento())
                .nroDocumento(c.getNroDocumento())
                .build();
    }

    private CandidatoClaveDTO armarCandidatoClaveDTO(Candidato c) {
        return CandidatoClaveDTO.builder()
                .nombreCandidato(c.getNombre())
                .apellidoCandidato(c.getApellido())
                .tipoDocumento(c.getTipoDocumento())
                .nroDocumento(c.getNroDocumento())
                .build();
    }

    private Puesto mapearPuesto(PuestoDTO dto) {
        return Puesto.builder()
                .id(dto.getId())
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();
    }

    private Candidato mapearCandidato(CandidatoGestionDTO dto) {
        if (dto == null || dto.getNroCandidato() == null) {
            throw new IllegalArgumentException("Los datos del candidato seleccionado son inválidos");
        }
        return Optional.ofNullable(candidatoDAO.buscarPorNroCandidato(dto.getNroCandidato()))
                .filter(cand -> !Boolean.TRUE.equals(cand.getEliminado()))
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontró el candidato activo con número " + dto.getNroCandidato()));
    }

    /**
     * Genera una clave aleatoria de 8 caracteres garantizando que contenga
     * al menos una letra y al menos un dígito (Requerimiento Especial CU025).
     */
    private String generarClaveIngreso() {
        char[] clave = new char[LONGITUD_CLAVE];
        clave[0] = LETRAS.charAt(random.nextInt(LETRAS.length()));
        clave[1] = DIGITOS.charAt(random.nextInt(DIGITOS.length()));

        for (int i = 2; i < LONGITUD_CLAVE; i++) {
            clave[i] = ALFANUMERICOS.charAt(random.nextInt(ALFANUMERICOS.length()));
        }

        // Mezcla aleatoria (Fisher-Yates) para que la letra y el dígito no queden en posiciones fijas
        for (int i = clave.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = clave[i];
            clave[i] = clave[j];
            clave[j] = tmp;
        }

        return new String(clave);
    }

    private String generarClaveUnica(Set<String> clavesGeneradasEnLote) {
        String clave;
        do {
            clave = generarClaveIngreso();
        } while (clavesGeneradasEnLote.contains(clave)
                || cuestionarioDAO.buscarPorClaveIngreso(clave) != null);

        clavesGeneradasEnLote.add(clave);
        return clave;
    }

    /**
     * Genera el nombre del archivo Excel según el formato exigido por el CU025:
     * {@code aaaa-mm-dd-hh-mm-NombreFunción.xls}.
     */
    private String generarNombreArchivo() {
        String timestamp = LocalDateTime.now().format(FORMATO_FECHA_ARCHIVO);
        String nombreFuncion = (this.nombrePuestoExportacion == null || this.nombrePuestoExportacion.isBlank())
                ? "Evaluacion"
                : this.nombrePuestoExportacion.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        return timestamp + "-" + nombreFuncion + ".xls";
    }

    private String resolverNombrePuesto(Long idPuesto) {
        if (this.puestoActual != null
                && idPuesto.equals(this.puestoActual.getId())
                && this.puestoActual.getNombre() != null
                && !this.puestoActual.getNombre().isBlank()) {
            return this.puestoActual.getNombre();
        }

        List<Cuestionario> cuestionariosDelPuesto = cuestionarioDAO.buscarPorPuesto(idPuesto);
        if (cuestionariosDelPuesto != null) {
            Optional<String> nombreDesdeBD = cuestionariosDelPuesto.stream()
                    .map(Cuestionario::getPuesto)
                    .filter(p -> p != null && p.getNombre() != null && !p.getNombre().isBlank())
                    .map(Puesto::getNombre)
                    .findFirst();
            if (nombreDesdeBD.isPresent()) {
                return nombreDesdeBD.get();
            }
        }

        return "Puesto_" + idPuesto;
    }

    /**
     * Construye un documento Excel 2003 XML (SpreadsheetML) compatible con Microsoft Excel
     * y LibreOffice Calc sin dependencias externas adicionales.
     */
    private String construirContenidoExcelXml(List<CandidatoClaveDTO> candidatos) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n");
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
        sb.append(" <Styles>\n");
        sb.append("  <Style ss:ID=\"Header\">\n");
        sb.append("   <Font ss:Bold=\"1\"/>\n");
        sb.append("  </Style>\n");
        sb.append(" </Styles>\n");
        sb.append(" <Worksheet ss:Name=\"Candidatos y Claves\">\n");
        sb.append("  <Table>\n");
        sb.append("   <Row ss:StyleID=\"Header\">\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Apellido</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Nombre</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Tipo Documento</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Nro Documento</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Clave de Ingreso</Data></Cell>\n");
        sb.append("   </Row>\n");

        for (CandidatoClaveDTO dto : candidatos) {
            sb.append("   <Row>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">").append(escaparXml(dto.getApellidoCandidato())).append("</Data></Cell>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">").append(escaparXml(dto.getNombreCandidato())).append("</Data></Cell>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">").append(escaparXml(dto.getTipoDocumento() != null ? dto.getTipoDocumento().name() : "")).append("</Data></Cell>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">").append(escaparXml(dto.getNroDocumento())).append("</Data></Cell>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">").append(escaparXml(dto.getClaveIngreso())).append("</Data></Cell>\n");
            sb.append("   </Row>\n");
        }

        sb.append("  </Table>\n");
        sb.append(" </Worksheet>\n");
        sb.append("</Workbook>\n");
        return sb.toString();
    }

    private String escaparXml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
