-- ============================================================================
-- SCRIPT DE DATOS DE PRUEBA - CAPIT@L HUMANO
-- ============================================================================

BEGIN;

-- 1. EMPRESAS (10)
INSERT INTO empresa (id_empresa, nombre, cuit) VALUES
(1, 'TECHCORP ARGENTINA S.A.', '30-71000001-9'),
(2, 'INNOVASOFT S.R.L.', '30-71000002-7'),
(3, 'PAMPA CLOUD SYSTEMS S.A.', '30-71000003-5'),
(4, 'LITORAL DATA LABS S.R.L.', '30-71000004-3'),
(5, 'ANDES DIGITAL SOLUTIONS S.A.', '30-71000005-1'),
(6, 'SUDESTE INGENIERÍA TIC S.A.', '30-71000006-0'),
(7, 'NEXUS ARQUITECTURA S.R.L.', '30-71000007-8'),
(8, 'PATAGONIA ANALYTICS S.A.', '30-71000008-6'),
(9, 'CÓRDOBA DEV STUDIO S.R.L.', '30-71000009-4'),
(10, 'SANTA FE SISTEMAS S.A.', '30-71000010-8');

-- 2. PUESTOS (30: 27 activos + 3 inactivos)
INSERT INTO puesto (id_puesto, codigo, nombre, descripcion, activo, id_empresa) VALUES
(1,  'PST-001', 'DESARROLLADOR BACKEND JAVA',        'DESARROLLO DE SERVICIOS Y APIS REST CON JAVA Y SPRING BOOT.', TRUE, 1),
(2,  'PST-002', 'DESARROLLADOR FRONTEND REACT',      'CONSTRUCCIÓN DE INTERFACES WEB CON REACT Y TYPESCRIPT.', TRUE, 2),
(3,  'PST-003', 'ANALISTA FUNCIONAL',                'RELEVAMIENTO DE REQUERIMIENTOS Y ESPECIFICACIÓN DE CASOS DE USO.', TRUE, 3),
(4,  'PST-004', 'ARQUITECTO DE SOFTWARE',            'DEFINICIÓN DE ARQUITECTURA, PATRONES Y ESTÁNDARES TÉCNICOS.', TRUE, 4),
(5,  'PST-005', 'INGENIERO DEVOPS',                  'AUTOMATIZACIÓN DE CI/CD, CONTENEDORES Y OBSERVABILIDAD.', TRUE, 5),
(6,  'PST-006', 'QA AUTOMATION',                     'DISEÑO Y EJECUCIÓN DE PRUEBAS AUTOMATIZADAS E INTEGRACIÓN.', TRUE, 6),
(7,  'PST-007', 'DATA ENGINEER',                     'DISEÑO DE PIPELINES ETL Y MODELADO DE DATA WAREHOUSE.', TRUE, 7),
(8,  'PST-008', 'DATA SCIENTIST',                    'MODELADO ESTADÍSTICO Y ENTRENAMIENTO DE MODELOS PREDICTIVOS.', TRUE, 8),
(9,  'PST-009', 'SCRUM MASTER',                      'FACILITACIÓN DE CEREMONIAS ÁGILES Y REMOCIÓN DE IMPEDIMENTOS.', TRUE, 9),
(10, 'PST-010', 'PRODUCT OWNER',                     'GESTIÓN DE BACKLOG Y PRIORIZACIÓN DE VALOR DE NEGOCIO.', TRUE, 10),
(11, 'PST-011', 'TECH LEAD',                         'LIDERAZGO TÉCNICO DE CÉLULAS DE DESARROLLO MULTIDISCIPLINARIAS.', TRUE, 1),
(12, 'PST-012', 'DBA POSTGRESQL',                    'ADMINISTRACIÓN, TUNING Y ALTA DISPONIBILIDAD DE BASES DE DATOS.', TRUE, 2),
(13, 'PST-013', 'ESPECIALISTA CIBERSEGURIDAD',       'AUDITORÍA DE VULNERABILIDADES Y SEGURIDAD EN APLICACIONES.', TRUE, 3),
(14, 'PST-014', 'SOPORTE TÉCNICO N2',                'RESOLUCIÓN DE INCIDENTES COMPLEJOS Y ESCALAMIENTO.', TRUE, 4),
(15, 'PST-015', 'ADMINISTRADOR DE REDES',            'OPERACIÓN DE INFRAESTRUCTURA DE RED Y CONECTIVIDAD.', TRUE, 5),
(16, 'PST-016', 'DESARROLLADOR MOBILE FLUTTER',      'DESARROLLO DE APLICACIONES MÓVILES MULTIPLATAFORMA.', TRUE, 6),
(17, 'PST-017', 'INGENIERO CLOUD AWS',               'DISEÑO Y OPERACIÓN DE INFRAESTRUCTURA CLOUD EN AWS.', TRUE, 7),
(18, 'PST-018', 'ANALISTA BI',                       'CONSTRUCCIÓN DE TABLEROS EJECUTIVOS Y MÉTRICAS DE NEGOCIO.', TRUE, 8),
(19, 'PST-019', 'DISEÑADOR UX/UI',                   'INVESTIGACIÓN DE USUARIOS Y PROTOTIPADO DE INTERFACES.', TRUE, 9),
(20, 'PST-020', 'GERENTE DE PROYECTOS IT',           'PLANIFICACIÓN, PRESUPUESTO Y SEGUIMIENTO DE PORTAFOLIO IT.', TRUE, 10),
(21, 'PST-021', 'ANALISTA DE SISTEMAS',              'ANÁLISIS INTEGRAL DE PROCESOS Y MODELADO DE DATOS.', TRUE, 1),
(22, 'PST-022', 'DESARROLLADOR FULLSTACK',           'DESARROLLO END-TO-END DE APLICACIONES WEB EMPRESARIALES.', TRUE, 2),
(23, 'PST-023', 'INGENIERO SRE',                     'CONFIABILIDAD, SLOS Y RESPUESTA ANTE INCIDENTES PRODUCTIVOS.', TRUE, 3),
(24, 'PST-024', 'CONSULTOR SAP',                     'IMPLEMENTACIÓN Y PARAMETRIZACIÓN DE MÓDULOS ERP.', TRUE, 4),
(25, 'PST-025', 'AUDITOR IT',                        'CONTROL DE CUMPLIMIENTO, PROCESOS Y SEGURIDAD DE LA INFORMACIÓN.', TRUE, 5),
(26, 'PST-026', 'DESARROLLADOR PYTHON',              'DESARROLLO DE SERVICIOS BACKEND Y AUTOMATIZACIÓN CON PYTHON.', TRUE, 6),
(27, 'PST-027', 'INGENIERO MACHINE LEARNING',        'DESPLIEGUE Y MONITOREO DE MODELOS DE ML EN PRODUCCIÓN.', TRUE, 7),
(28, 'PST-028', 'LÍDER DE CALIDAD',                  'DEFINICIÓN DE ESTRATEGIA DE CALIDAD Y ASEGURAMIENTO.', FALSE, 8),
(29, 'PST-029', 'ESPECIALISTA EN INTEGRACIONES',     'ORQUESTACIÓN DE MENSAJERÍA Y BUSES DE INTEGRACIÓN.', FALSE, 9),
(30, 'PST-030', 'ADMINISTRADOR LINUX',               'ADMINISTRACIÓN DE SERVIDORES LINUX Y AUTOMATIZACIÓN BASH.', FALSE, 10);

-- 3. COMPETENCIAS (15: 14 activas evaluables + 1 inactiva)
INSERT INTO competencia (id_competencia, codigo, nombre, descripcion, activo) VALUES
(1,  'CMP-001', 'TRABAJO EN EQUIPO',        'CAPACIDAD PARA COLABORAR ACTIVAMENTE HACIA OBJETIVOS COMUNES.', TRUE),
(2,  'CMP-002', 'LIDERAZGO',                'HABILIDAD PARA GUIAR, MOTIVAR Y DESARROLLAR EQUIPOS DE TRABAJO.', TRUE),
(3,  'CMP-003', 'RESOLUCIÓN DE PROBLEMAS',  'CAPACIDAD PARA ANALIZAR CAUSAS RAÍZ Y PROPONER SOLUCIONES EFECTIVAS.', TRUE),
(4,  'CMP-004', 'COMUNICACIÓN EFECTIVA',    'CLARIDAD Y EMPATÍA AL TRANSMITIR IDEAS TÉCNICAS Y DE NEGOCIO.', TRUE),
(5,  'CMP-005', 'ADAPTABILIDAD',            'FLEXIBILIDAD ANTE CAMBIOS DE PRIORIDADES, CONTEXTO O TECNOLOGÍA.', TRUE),
(6,  'CMP-006', 'PENSAMIENTO ANALÍTICO',    'DESCOMPOSICIÓN SISTEMÁTICA DE PROBLEMAS COMPLEJOS EN PARTES.', TRUE),
(7,  'CMP-007', 'ORIENTACIÓN A RESULTADOS', 'ENFOQUE CONSTANTE EN EL CUMPLIMIENTO DE METAS Y CALIDAD.', TRUE),
(8,  'CMP-008', 'GESTIÓN DEL TIEMPO',       'PLANIFICACIÓN Y PRIORIZACIÓN EFICIENTE DE TAREAS Y PLAZOS.', TRUE),
(9,  'CMP-009', 'INNOVACIÓN Y CREATIVIDAD', 'GENERACIÓN DE PROPUESTAS ORIGINALES DE MEJORA Y DISEÑO.', TRUE),
(10, 'CMP-010', 'TOMA DE DECISIONES',       'ELECCIÓN FUNDAMENTADA DE ALTERNATIVAS BAJO INCERTIDUMBRE.', TRUE),
(11, 'CMP-011', 'NEGOCIACIÓN',              'BÚSQUEDA DE ACUERDOS SOSTENIBLES CON CLIENTES Y EQUIPOS.', TRUE),
(12, 'CMP-012', 'PROACTIVIDAD',             'INICIATIVA PARA ANTICIPARSE A RIESGOS Y OPORTUNIDADES.', TRUE),
(13, 'CMP-013', 'TOLERANCIA A LA PRESIÓN',  'ESTABILIDAD Y DESEMPEÑO SOSTENIDO EN ESCENARIOS CRÍTICOS.', TRUE),
(14, 'CMP-014', 'APRENDIZAJE CONTINUO',     'ACTUALIZACIÓN CONSTANTE DE CONOCIMIENTOS Y PRÁCTICAS.', TRUE),
(15, 'CMP-015', 'ORIENTACIÓN AL CLIENTE',   'VOCACIÓN DE SERVICIO Y FOCO EN LA EXPERIENCIA DEL CLIENTE.', FALSE);

-- 4. PUESTO_COMPETENCIA (4 competencias distintas por puesto)
INSERT INTO puesto_competencia (id_competencia_requerida, id_puesto, id_competencia, puntaje_minimo)
SELECT
    row_number() OVER () AS id_competencia_requerida,
    p.id_puesto,
    ((p.id_puesto * 3 + k.idx * 4) % 14) + 1 AS id_competencia,
    ROUND((5.0 + ((p.id_puesto + k.idx * 2) % 4) * 0.75)::numeric, 2)::double precision AS puntaje_minimo
FROM puesto p
CROSS JOIN (VALUES (0), (1), (2), (3)) AS k(idx);

-- 5. FACTORES (2 factores activos por cada competencia activa = 28 factores evaluables + 1 incompleto en CMP-015)
INSERT INTO factor (id_factor, nombre, descripcion, nro_orden, activo, id_competencia) VALUES
(1,  'COOPERACIÓN INTERPERSONAL',   'DISPOSICIÓN A COLABORAR CON PARES DE DISTINTAS ÁREAS.', 1, TRUE, 1),
(2,  'SINERGIA DE EQUIPO',          'APORTE ACTIVO AL CLIMA Y RENDIMIENTO GRUPAL.', 2, TRUE, 1),
(3,  'VISIÓN Y DIRECCIÓN',          'CAPACIDAD PARA ALINEAR AL EQUIPO CON LA ESTRATEGIA.', 1, TRUE, 2),
(4,  'DELEGACIÓN Y EMPOWERMENT',    'ASIGNACIÓN EFECTIVA DE RESPONSABILIDADES Y AUTONOMÍA.', 2, TRUE, 2),
(5,  'DIAGNÓSTICO DE INCIDENTES',   'IDENTIFICACIÓN PRECISA DE CAUSAS EN FALLAS TÉCNICAS.', 1, TRUE, 3),
(6,  'IMPLEMENTACIÓN DE SOLUCIONES','EJECUCIÓN PRÁCTICA Y VERIFICACIÓN DE CORRECCIONES.', 2, TRUE, 3),
(7,  'CLARIDAD EXPOSITIVA',         'CAPACIDAD DE EXPLICAR CONCEPTOS COMPLEJOS CON SENCILLEZ.', 1, TRUE, 4),
(8,  'ESCUCHA ACTIVA',              'ATENCIÓN Y COMPRENSIÓN REAL DE LOS INTERLOCUTORES.', 2, TRUE, 4),
(9,  'GESTIÓN DEL CAMBIO',          'ASIMILACIÓN ÁGIL DE NUEVAS HERRAMIENTAS Y PROCESOS.', 1, TRUE, 5),
(10, 'RESILIENCIA OPERATIVA',       'MANTENIMIENTO DEL RITMO ANTE REQUISITOS CAMBIANTES.', 2, TRUE, 5),
(11, 'DESCOMPOSICIÓN LÓGICA',       'ESTRUCTURACIÓN ORDENADA DE REQUERIMIENTOS COMPLEJOS.', 1, TRUE, 6),
(12, 'EVALUACIÓN DE RIESGOS',       'DETECCIÓN TEMPRANA DE IMPACTO Y DEPENDENCIAS.', 2, TRUE, 6),
(13, 'CUMPLIMIENTO DE OBJETIVOS',   'ORIENTACIÓN FIRME AL LOGRO DE ENTREGABLES.', 1, TRUE, 7),
(14, 'CALIDAD EN LA ENTREGA',       'ATENCIÓN AL DETALLE Y ESTÁNDARES DE EXCELENCIA.', 2, TRUE, 7),
(15, 'PRIORIZACIÓN DE TAREAS',      'CRITERIO PARA ORDENAR PENDIENTES SEGÚN URGENCIA E IMPACTO.', 1, TRUE, 8),
(16, 'ESTIMACIÓN Y PLAZOS',         'PRECISIÓN EN COMPROMISOS TEMPORALES DE ENTREGA.', 2, TRUE, 8),
(17, 'IDEACIÓN DE MEJORAS',         'GENERACIÓN DE ALTERNATIVAS NOVEDOSAS DE PRODUCTO O PROCESO.', 1, TRUE, 9),
(18, 'EXPERIMENTACIÓN SEGURA',      'VALIDACIÓN RÁPIDA DE PROTOTIPOS E HIPÓTESIS.', 2, TRUE, 9),
(19, 'ANÁLISIS DE ALTERNATIVAS',    'COMPARACIÓN OBJETIVA DE OPCIONES TÉCNICAS Y DE NEGOCIO.', 1, TRUE, 10),
(20, 'OPORTUNIDAD EN LA DECISIÓN',  'CAPACIDAD DE DECIDIR A TIEMPO SIN PARÁLISIS.', 2, TRUE, 10),
(21, 'GESTIÓN DE INTERESES',        'IDENTIFICACIÓN DE PUNTOS COMUNES ENTRE PARTES.', 1, TRUE, 11),
(22, 'CIERRE DE ACUERDOS',          'FORMALIZACIÓN DE COMPROMISOS CLAROS Y SOSTENIBLES.', 2, TRUE, 11),
(23, 'ANTICIPACIÓN DE PROBLEMAS',   'DETECCIÓN TEMPRANA DE DESVÍOS ANTES QUE ESCALEN.', 1, TRUE, 12),
(24, 'INICIATIVA PROPIA',           'IMPULSO AUTÓNOMO DE MEJORAS SIN ESPERAR INSTRUCCIONES.', 2, TRUE, 12),
(25, 'SERENIDAD EN CRISIS',         'CONTROL EMOCIONAL DURANTE CAÍDAS O ENTREGAS CRÍTICAS.', 1, TRUE, 13),
(26, 'SOSTENIBILIDAD DEL FOCO',     'PRECISIÓN TÉCNICA BAJO ALTA DEMANDA.', 2, TRUE, 13),
(27, 'ACTUALIZACIÓN TECNOLÓGICA',   'INTERÉS CONSTANTE EN NUEVAS TECNOLOGÍAS Y BUENAS PRÁCTICAS.', 1, TRUE, 14),
(28, 'RETROALIMENTACIÓN ACTIVA',    'INCORPORACIÓN CONSTRUCTIVA DE FEEDBACK EN EL TRABAJO.', 2, TRUE, 14),
(29, 'ATENCIÓN DE RECLAMOS',        'RESPUESTA EMPÁTICA Y RESOLUTIVA HACIA EL CLIENTE.', 1, TRUE, 15);

-- 6. OPCIONES DE RESPUESTA E ÍTEMS (4 escalas únicas, 16 ítems)
INSERT INTO opcion_respuesta (id_opcion_resp, nombre, descripcion, activo) VALUES
(1, 'VERDADERO / FALSO',     'OPCIÓN DICOTÓMICA DE VERDADERO O FALSO.', TRUE),
(2, 'ESCALA DE VALORACIÓN',  'ESCALA DE 4 NIVELES DESDE MALO HASTA EXCELENTE.', TRUE),
(3, 'ESCALA DE FRECUENCIA',  'ESCALA DE 5 NIVELES DESDE NUNCA HASTA SIEMPRE.', TRUE),
(4, 'NIVEL DE ACUERDO',      'ESCALA LIKERT DE 5 NIVELES DE ACUERDO.', TRUE);

INSERT INTO item_opcion (id_item_opcion, id_opcion_resp, orden_visualizacion, descripcion) VALUES
(1,  1, 1, 'VERDADERO'),
(2,  1, 2, 'FALSO'),
(3,  2, 1, 'MALO'),
(4,  2, 2, 'REGULAR'),
(5,  2, 3, 'BUENO'),
(6,  2, 4, 'EXCELENTE'),
(7,  3, 1, 'NUNCA'),
(8,  3, 2, 'RARA VEZ'),
(9,  3, 3, 'A VECES'),
(10, 3, 4, 'FRECUENTEMENTE'),
(11, 3, 5, 'SIEMPRE'),
(12, 4, 1, 'TOTALMENTE EN DESACUERDO'),
(13, 4, 2, 'EN DESACUERDO'),
(14, 4, 3, 'NEUTRAL'),
(15, 4, 4, 'DE ACUERDO'),
(16, 4, 5, 'TOTALMENTE DE ACUERDO');

-- 7. PREGUNTAS (3 preguntas activas únicas por cada factor 1..28 = 84 + 1 en factor 29 = 85 preguntas)
INSERT INTO pregunta (id_pregunta, nombre, enunciado, descripcion, activo, id_factor, id_opcion_resp)
SELECT
    ((f.id_factor - 1) * 3 + q.num) AS id_pregunta,
    'PREG-' || LPAD(((f.id_factor - 1) * 3 + q.num)::text, 3, '0') || ' ' || LEFT(f.nombre, 20) AS nombre,
    '¿CON QUÉ FRECUENCIA O GRADO DEMUESTRA ' || f.nombre || ' EN EL ESCENARIO PRÁCTICO #' || q.num || ' DE SU ROL?' AS enunciado,
    'PREGUNTA DE EVALUACIÓN PARA EL FACTOR ' || f.nombre || ' (VARIANTE ' || q.num || ').' AS descripcion,
    TRUE AS activo,
    f.id_factor,
    ((f.id_factor + q.num) % 4) + 1 AS id_opcion_resp
FROM factor f
CROSS JOIN (VALUES (1), (2), (3)) AS q(num)
WHERE f.id_factor <= 28;

INSERT INTO pregunta (id_pregunta, nombre, enunciado, descripcion, activo, id_factor, id_opcion_resp) VALUES
(85, 'PREG-085 RECLAMO CLIENTE', '¿PRIORIZA LA CONTENCIÓN Y SOLUCIÓN ANTE UN RECLAMO CRÍTICO DE CLIENTE?', 'PREGUNTA ÚNICA DE FACTOR INCOMPLETO.', TRUE, 29, 1);

-- 8. PREGUNTA_OPCION (Ponderaciones únicas por pregunta que suman 10.0)
INSERT INTO pregunta_opcion (id_pregunta, id_item_opcion, ponderacion)
SELECT
    p.id_pregunta,
    io.id_item_opcion,
    CASE p.id_opcion_resp
        WHEN 1 THEN CASE io.orden_visualizacion WHEN 1 THEN 10.0 ELSE 0.0 END
        WHEN 2 THEN CASE io.orden_visualizacion WHEN 1 THEN 0.0 WHEN 2 THEN 1.0 WHEN 3 THEN 3.0 ELSE 6.0 END
        WHEN 3 THEN CASE io.orden_visualizacion WHEN 1 THEN 0.0 WHEN 2 THEN 1.0 WHEN 3 THEN 2.0 WHEN 4 THEN 3.0 ELSE 4.0 END
        WHEN 4 THEN CASE io.orden_visualizacion WHEN 1 THEN 0.0 WHEN 2 THEN 1.0 WHEN 3 THEN 2.0 WHEN 4 THEN 3.0 ELSE 4.0 END
    END AS ponderacion
FROM pregunta p
JOIN item_opcion io ON io.id_opcion_resp = p.id_opcion_resp;

-- 9. CANDIDATOS (1000 candidatos únicos con DNI y email irrepetibles)
WITH nombres AS (
    SELECT ARRAY[
        'JUAN', 'MARÍA', 'LUCAS', 'SOFÍA', 'MATEO', 'VALENTINA', 'NICOLÁS', 'CAMILA', 'TOMÁS', 'MARTINA',
        'SANTIAGO', 'AGUSTINA', 'FEDERICO', 'PAULA', 'FACUNDO', 'JULIETA', 'IGNACIO', 'CARLA', 'RODRIGO', 'FLORENCIA'
    ] AS arr_nom,
    ARRAY[
        'GÓMEZ', 'RODRÍGUEZ', 'FERNÁNDEZ', 'LÓPEZ', 'MARTÍNEZ', 'PÉREZ', 'GARCÍA', 'SÁNCHEZ', 'ROMERO', 'SOSA',
        'TORRES', 'ÁLVAREZ', 'RUIZ', 'DÍAZ', 'CASTRO', 'MORALES', 'HERRERA', 'SILVA', 'SUÁREZ', 'BENÍTEZ',
        'ACOSTA', 'MENDOZA', 'CABRERA', 'ORTIZ', 'FLORES'
    ] AS arr_ape,
    ARRAY['UNIVERSITARIO COMPLETO', 'UNIVERSITARIO EN CURSO', 'TERCIARIO COMPLETO', 'POSGRADO'] AS arr_esc
)
INSERT INTO candidato (
    nro_candidato, apellido, nombre, nro_documento, tipo_documento,
    fecha_nacimiento, genero, nacionalidad, email, escolaridad, eliminado
)
SELECT
    1000 + g AS nro_candidato,
    n.arr_ape[((g - 1) % 25) + 1] AS apellido,
    n.arr_nom[(((g - 1) / 25) % 20) + 1] AS nombre,
    (30000000 + g)::text AS nro_documento,
    CASE WHEN g % 20 = 0 THEN 'PP' WHEN g % 15 = 0 THEN 'LC' WHEN g % 12 = 0 THEN 'LE' ELSE 'DNI' END AS tipo_documento,
    DATE '1980-01-01' + ((g * 17) % 9000) AS fecha_nacimiento,
    CASE WHEN (((g - 1) / 25) % 20) % 2 = 0 THEN 'H' ELSE 'M' END AS genero,
    'ARGENTINA' AS nacionalidad,
    'candidato.' || (1000 + g)::text || '@capitalhumano.test' AS email,
    n.arr_esc[(g % 4) + 1] AS escolaridad,
    (g % 33 = 0) AS eliminado
FROM generate_series(1, 1000) AS g
CROSS JOIN nombres n;

-- 10. CUESTIONARIOS, BLOQUES, RESPUESTAS Y RESULTADOS CONSISTENTES (1000 cuestionarios = 1 por candidato)
DO $$
DECLARE
    v_idx INTEGER;
    v_nro_cand BIGINT;
    v_id_puesto BIGINT;
    v_estado VARCHAR(20);
    v_clave VARCHAR(8);
    v_f_crea TIMESTAMP;
    v_f_ini TIMESTAMP;
    v_f_fin TIMESTAMP;
    v_f_ult TIMESTAMP;
    v_accesos INTEGER;
    v_id_cuest BIGINT;
    v_id_bloque BIGINT;
    v_id_res BIGINT;
    v_nro_bloque INTEGER;
    v_orden_preg INTEGER;
    v_preg_rec RECORD;
    v_item_elegido BIGINT;
    v_comp_rec RECORD;
    v_fac_rec RECORD;
    v_suma_pond DOUBLE PRECISION;
    v_suma_fac DOUBLE PRECISION;
    v_cant_fac INTEGER;
    v_puntaje_comp DOUBLE PRECISION;
    v_chars CONSTANT TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    v_letters CONSTANT TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ';
    v_digits CONSTANT TEXT := '23456789';
BEGIN
    FOR v_idx IN 1..1000 LOOP
        v_nro_cand := 1000 + v_idx;
        v_id_puesto := ((v_idx - 1) % 27) + 1;

        -- Agrupar evaluaciones en lotes por puesto y fecha para probar CU028
        v_f_crea := TIMESTAMP '2026-10-01 09:00:00'
                    + ((v_idx % 5) * INTERVAL '7 days')
                    + ((v_id_puesto - 1) * INTERVAL '1 hour');

        -- Clave única determinística de 8 caracteres (letras + dígitos)
        v_clave :=
            substr(v_letters, ((v_idx * 7) % length(v_letters)) + 1, 1) ||
            substr(v_digits,  ((v_idx * 3) % length(v_digits))  + 1, 1) ||
            LPAD(upper(to_hex(100000 + v_idx)), 6, '0');

        IF v_idx <= 500 THEN
            v_estado := 'COMPLETO';
            v_accesos := (v_idx % 3) + 1;
            v_f_ini := v_f_crea + INTERVAL '2 hours';
            v_f_ult := v_f_ini + INTERVAL '15 minutes';
            v_f_fin := v_f_ini + ((20 + (v_idx % 25)) * INTERVAL '1 minute');
        ELSIF v_idx <= 650 THEN
            v_estado := 'EN_PROCESO';
            v_accesos := 1;
            v_f_ini := v_f_crea + INTERVAL '3 hours';
            v_f_ult := v_f_ini + INTERVAL '10 minutes';
            v_f_fin := NULL;
        ELSIF v_idx <= 800 THEN
            v_estado := 'ACTIVO';
            v_accesos := 0;
            v_f_ini := NULL;
            v_f_ult := NULL;
            v_f_fin := NULL;
        ELSIF v_idx <= 900 THEN
            v_estado := 'INCOMPLETO';
            v_accesos := 3;
            v_f_ini := v_f_crea + INTERVAL '1 hour';
            v_f_ult := v_f_ini + INTERVAL '40 minutes';
            v_f_fin := v_f_crea + INTERVAL '2 days';
        ELSE
            v_estado := 'SIN_CONTESTAR';
            v_accesos := 0;
            v_f_ini := NULL;
            v_f_ult := NULL;
            v_f_fin := v_f_crea + INTERVAL '3 days';
        END IF;

        INSERT INTO cuestionario (
            estado, clave_ingreso, cant_accesos, fecha_creacion,
            fecha_inicio, fecha_fin, fecha_ultimo_ingreso, nro_candidato, id_puesto
        ) VALUES (
            v_estado, v_clave, v_accesos, v_f_crea,
            v_f_ini, v_f_fin, v_f_ult, v_nro_cand, v_id_puesto
        )
        RETURNING id_cuestionario INTO v_id_cuest;

        -- Solo generan bloques los cuestionarios COMPLETO, EN_PROCESO e INCOMPLETO
        IF v_estado IN ('COMPLETO', 'EN_PROCESO', 'INCOMPLETO') THEN
            v_nro_bloque := 0;
            v_orden_preg := 0;

            FOR v_preg_rec IN
                SELECT pr.id_pregunta, pr.id_opcion_resp
                FROM puesto_competencia pc
                JOIN factor fa ON fa.id_competencia = pc.id_competencia AND fa.activo = TRUE
                JOIN LATERAL (
                    SELECT p2.id_pregunta, p2.id_opcion_resp
                    FROM pregunta p2
                    WHERE p2.id_factor = fa.id_factor AND p2.activo = TRUE
                    ORDER BY ((p2.id_pregunta * 13 + v_idx) % 97), p2.id_pregunta
                    LIMIT 2
                ) pr ON TRUE
                WHERE pc.id_puesto = v_id_puesto
                ORDER BY ((pr.id_pregunta * 17 + v_idx) % 101), pr.id_pregunta
            LOOP
                IF v_orden_preg % 4 = 0 THEN
                    v_nro_bloque := v_nro_bloque + 1;
                    INSERT INTO bloque_cuestionario (nro_bloque, completado, id_cuestionario)
                    VALUES (
                        v_nro_bloque,
                        (v_estado = 'COMPLETO') OR (v_estado = 'EN_PROCESO' AND v_nro_bloque = 1),
                        v_id_cuest
                    )
                    RETURNING id_bloque INTO v_id_bloque;
                END IF;

                v_orden_preg := v_orden_preg + 1;

                IF (v_estado = 'COMPLETO') OR (v_estado = 'EN_PROCESO' AND v_nro_bloque = 1) THEN
                    -- Elegir un item válido de la opción de respuesta de esa pregunta
                    SELECT io.id_item_opcion INTO v_item_elegido
                    FROM item_opcion io
                    WHERE io.id_opcion_resp = v_preg_rec.id_opcion_resp
                    ORDER BY ((io.orden_visualizacion + v_idx + v_orden_preg) %
                              (SELECT COUNT(*) FROM item_opcion io2 WHERE io2.id_opcion_resp = v_preg_rec.id_opcion_resp)) DESC,
                             io.orden_visualizacion DESC
                    LIMIT 1;

                    INSERT INTO respuesta_candidato (
                        fecha_hora_respuesta, orden_pregunta, id_bloque, id_pregunta, id_item_opcion
                    ) VALUES (
                        v_f_ini + (v_orden_preg * INTERVAL '1 minute'),
                        ((v_orden_preg - 1) % 4) + 1,
                        v_id_bloque,
                        v_preg_rec.id_pregunta,
                        v_item_elegido
                    );
                ELSE
                    -- Pregunta pendiente sin responder aún
                    INSERT INTO respuesta_candidato (
                        fecha_hora_respuesta, orden_pregunta, id_bloque, id_pregunta, id_item_opcion
                    ) VALUES (
                        NULL,
                        ((v_orden_preg - 1) % 4) + 1,
                        v_id_bloque,
                        v_preg_rec.id_pregunta,
                        NULL
                    );
                END IF;
            END LOOP;

            -- Calcular y persistir Resultado + ResultadoCompetencia para COMPLETO
            IF v_estado = 'COMPLETO' THEN
                INSERT INTO resultado (fecha_calculo, id_cuestionario)
                VALUES (v_f_fin, v_id_cuest)
                RETURNING id_resultado INTO v_id_res;

                FOR v_comp_rec IN
                    SELECT pc.id_competencia, pc.puntaje_minimo
                    FROM puesto_competencia pc
                    WHERE pc.id_puesto = v_id_puesto
                LOOP
                    v_suma_fac := 0.0;
                    v_cant_fac := 0;

                    FOR v_fac_rec IN
                        SELECT f.id_factor
                        FROM factor f
                        WHERE f.id_competencia = v_comp_rec.id_competencia AND f.activo = TRUE
                    LOOP
                        SELECT COALESCE(SUM(po.ponderacion), 0.0) INTO v_suma_pond
                        FROM bloque_cuestionario bc
                        JOIN respuesta_candidato rc ON rc.id_bloque = bc.id_bloque
                        JOIN pregunta pr ON pr.id_pregunta = rc.id_pregunta
                        JOIN pregunta_opcion po ON po.id_pregunta = pr.id_pregunta AND po.id_item_opcion = rc.id_item_opcion
                        WHERE bc.id_cuestionario = v_id_cuest
                          AND pr.id_factor = v_fac_rec.id_factor;

                        v_suma_fac := v_suma_fac + (v_suma_pond / 2.0);
                        v_cant_fac := v_cant_fac + 1;
                    END LOOP;

                    IF v_cant_fac > 0 THEN
                        v_puntaje_comp := ROUND((v_suma_fac / v_cant_fac)::numeric, 2)::double precision;
                        INSERT INTO resultado_competencia (
                            puntaje_obtenido, puntaje_requerido, id_resultado, id_competencia
                        ) VALUES (
                            v_puntaje_comp, v_comp_rec.puntaje_minimo, v_id_res, v_comp_rec.id_competencia
                        );
                    END IF;
                END LOOP;
            END IF;
        END IF;
    END LOOP;
END $$;

-- 11. SINCRONIZAR TODAS LAS SECUENCIAS DE POSTGRESQL
SELECT setval('empresa_id_empresa_seq', (SELECT COALESCE(MAX(id_empresa), 1) FROM empresa));
SELECT setval('puesto_id_puesto_seq', (SELECT COALESCE(MAX(id_puesto), 1) FROM puesto));
SELECT setval('competencia_id_competencia_seq', (SELECT COALESCE(MAX(id_competencia), 1) FROM competencia));
SELECT setval('puesto_competencia_id_competencia_requerida_seq', (SELECT COALESCE(MAX(id_competencia_requerida), 1) FROM puesto_competencia));
SELECT setval('factor_id_factor_seq', (SELECT COALESCE(MAX(id_factor), 1) FROM factor));
SELECT setval('opcion_respuesta_id_opcion_resp_seq', (SELECT COALESCE(MAX(id_opcion_resp), 1) FROM opcion_respuesta));
SELECT setval('item_opcion_id_item_opcion_seq', (SELECT COALESCE(MAX(id_item_opcion), 1) FROM item_opcion));
SELECT setval('pregunta_id_pregunta_seq', (SELECT COALESCE(MAX(id_pregunta), 1) FROM pregunta));
SELECT setval('pregunta_opcion_id_pregunta_opcion_seq', (SELECT COALESCE(MAX(id_pregunta_opcion), 1) FROM pregunta_opcion));
SELECT setval('cuestionario_id_cuestionario_seq', (SELECT COALESCE(MAX(id_cuestionario), 1) FROM cuestionario));
SELECT setval('bloque_cuestionario_id_bloque_seq', (SELECT COALESCE(MAX(id_bloque), 1) FROM bloque_cuestionario));
SELECT setval('respuesta_candidato_id_respuesta_seq', (SELECT COALESCE(MAX(id_respuesta), 1) FROM respuesta_candidato));
SELECT setval('resultado_id_resultado_seq', (SELECT COALESCE(MAX(id_resultado), 1) FROM resultado));
SELECT setval('resultado_competencia_id_res_comp_seq', (SELECT COALESCE(MAX(id_res_comp), 1) FROM resultado_competencia));

COMMIT;
