-- ============================================================================
-- DATOS DE PRUEBA (SEED): Capit@l Humano (PostgreSQL 16)
-- ============================================================================

-- 1. Empresas
INSERT INTO empresa (id_empresa, nombre, cuit) VALUES
(1, 'TECHCORP ARGENTINA S.A.', '30-71234567-9'),
(2, 'INNOVASOFT S.R.L.', '30-79876543-2')
ON CONFLICT DO NOTHING;

-- 2. Competencias
INSERT INTO competencia (id_competencia, codigo, nombre, descripcion, activo) VALUES
(1, 'COMP-01', 'GESTIÓN DE PROYECTOS', 'Capacidad para planificar, ejecutar y controlar proyectos de software.', TRUE),
(2, 'COMP-02', 'LEALTAD', 'Compromiso e integridad profesional con los objetivos de la organización.', TRUE)
ON CONFLICT DO NOTHING;

-- 3. Factores (con al menos 2 preguntas cada uno para que las competencias sean evaluables)
INSERT INTO factor (id_factor, nombre, descripcion, nro_orden, activo, id_competencia) VALUES
(1, 'GESTIÓN DEL ALCANCE', 'Definición y control de los requerimientos del proyecto.', 1, TRUE, 1),
(2, 'GESTIÓN DEL TIEMPO', 'Estimación y control del cronograma de actividades.', 2, TRUE, 1),
(3, 'CREDIBILIDAD', 'Transparencia y coherencia en el desempeño profesional.', 1, TRUE, 2),
(4, 'COMPROMISO', 'Involucramiento activo con los objetivos colectivos.', 2, TRUE, 2)
ON CONFLICT DO NOTHING;

-- 4. Opciones de Respuesta e Ítems
INSERT INTO opcion_respuesta (id_opcion_resp, nombre, descripcion, activo) VALUES
(1, 'VERDADERO / FALSO', 'Opción dicotómica de verdadero o falso.', TRUE),
(2, 'ESCALA DE VALORACIÓN', 'Escala de 4 niveles desde Malo hasta Excelente.', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO item_opcion (id_item_opcion, id_opcion_resp, orden_visualizacion, descripcion) VALUES
(1, 1, 1, 'VERDADERO'),
(2, 1, 2, 'FALSO'),
(3, 2, 1, 'MALO'),
(4, 2, 2, 'REGULAR'),
(5, 2, 3, 'BUENO'),
(6, 2, 4, 'EXCELENTE')
ON CONFLICT DO NOTHING;

-- 5. Preguntas y sus Ponderaciones (suman 10 puntos por pregunta)
INSERT INTO pregunta (id_pregunta, nombre, enunciado, descripcion, activo, id_factor, id_opcion_resp) VALUES
(1, 'WBS Y ALCANCE', '¿La descomposición de estructura de trabajo (WBS) forma parte de la definición del alcance?', 'Pregunta sobre WBS', TRUE, 1, 1),
(2, 'LÍNEA BASE REQUERIMIENTOS', '¿Cuándo se define un alcance, debe definirse una línea base de los requerimientos?', 'Pregunta sobre línea base', TRUE, 1, 1),
(3, 'DEPENDENCIA FIN A COMIENZO', '¿En el cronograma de un proyecto, una actividad fin a comienzo requiere que la predecesora termine?', 'Pregunta sobre cronograma', TRUE, 2, 1),
(4, 'METODOLOGÍA VALOR GANADO', '¿La metodología de Valor Ganado permite predecir costos según avances a la fecha?', 'Pregunta sobre EVM', TRUE, 2, 1),
(5, 'CONFIANZA PROFESIONAL', '¿Cómo evalúa su cumplimiento íntegro de las normas de confidencialidad en el trabajo?', 'Pregunta sobre credibilidad', TRUE, 3, 2),
(6, 'RESGUARDO DE INTERESES', '¿En qué medida prioriza los intereses de la empresa ante situaciones adversas?', 'Pregunta sobre intereses', TRUE, 3, 2),
(7, 'OBJETIVOS COLECTIVOS', '¿Cómo califica su predisposición para asumir proyectos estratégicos del equipo?', 'Pregunta sobre compromiso', TRUE, 4, 2),
(8, 'COLABORACIÓN EN EQUIPO', '¿En qué grado colabora con sus compañeros para alcanzar metas laborales?', 'Pregunta sobre colaboración', TRUE, 4, 2)
ON CONFLICT DO NOTHING;

-- Ponderaciones para preguntas 1 a 4 (Verdadero = 10, Falso = 0)
INSERT INTO pregunta_opcion (id_pregunta, id_item_opcion, ponderacion) VALUES
(1, 1, 10.0), (1, 2, 0.0),
(2, 1, 10.0), (2, 2, 0.0),
(3, 1, 10.0), (3, 2, 0.0),
(4, 1, 10.0), (4, 2, 0.0),
-- Ponderaciones para preguntas 5 a 8 (Malo = 0, Regular = 1, Bueno = 3, Excelente = 6 -> suma = 10)
(5, 3, 0.0), (5, 4, 1.0), (5, 5, 3.0), (5, 6, 6.0),
(6, 3, 0.0), (6, 4, 1.0), (6, 5, 3.0), (6, 6, 6.0),
(7, 3, 0.0), (7, 4, 1.0), (7, 5, 3.0), (7, 6, 6.0),
(8, 3, 0.0), (8, 4, 1.0), (8, 5, 3.0), (8, 6, 6.0)
ON CONFLICT DO NOTHING;

-- 6. Puesto y Competencias Requeridas
INSERT INTO puesto (id_puesto, codigo, nombre, descripcion, activo, id_empresa) VALUES
(1, 'PUE-01', 'LÍDER DE PROYECTO TIC', 'Responsable de liderar equipos de desarrollo de software.', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO puesto_competencia (id_competencia_requerida, id_puesto, id_competencia, puntaje_minimo) VALUES
(1, 1, 1, 7.0),
(2, 1, 2, 5.0)
ON CONFLICT DO NOTHING;

-- 7. Candidatos de prueba
INSERT INTO candidato (nro_candidato, apellido, nombre, nro_documento, tipo_documento, fecha_nacimiento, genero, nacionalidad, email, escolaridad, eliminado) VALUES
(1001, 'GÓMEZ', 'JUAN PÉREZ', '40123456', 'DNI', '1997-05-14', 'H', 'ARGENTINA', 'juan.gomez@email.com', 'UNIVERSITARIO COMPLETO', FALSE),
(1002, 'MARTÍNEZ', 'MARÍA LAURA', '42987654', 'DNI', '1999-10-22', 'M', 'ARGENTINA', 'maria.martinez@email.com', 'UNIVERSITARIO EN CURSO', FALSE)
ON CONFLICT DO NOTHING;

-- Ajustar secuencias de PostgreSQL
SELECT setval('empresa_id_empresa_seq', (SELECT COALESCE(MAX(id_empresa), 1) FROM empresa));
SELECT setval('puesto_id_puesto_seq', (SELECT COALESCE(MAX(id_puesto), 1) FROM puesto));
SELECT setval('competencia_id_competencia_seq', (SELECT COALESCE(MAX(id_competencia), 1) FROM competencia));
SELECT setval('puesto_competencia_id_competencia_requerida_seq', (SELECT COALESCE(MAX(id_competencia_requerida), 1) FROM puesto_competencia));
SELECT setval('factor_id_factor_seq', (SELECT COALESCE(MAX(id_factor), 1) FROM factor));
SELECT setval('opcion_respuesta_id_opcion_resp_seq', (SELECT COALESCE(MAX(id_opcion_resp), 1) FROM opcion_respuesta));
SELECT setval('item_opcion_id_item_opcion_seq', (SELECT COALESCE(MAX(id_item_opcion), 1) FROM item_opcion));
SELECT setval('pregunta_id_pregunta_seq', (SELECT COALESCE(MAX(id_pregunta), 1) FROM pregunta));
SELECT setval('pregunta_opcion_id_pregunta_opcion_seq', (SELECT COALESCE(MAX(id_pregunta_opcion), 1) FROM pregunta_opcion));
