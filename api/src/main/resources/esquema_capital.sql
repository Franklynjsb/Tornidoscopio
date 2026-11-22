-- ============================================================================
-- ESQUEMA DE BASE DE DATOS: Capit@l Humano (PostgreSQL 16)
-- ============================================================================

CREATE TABLE IF NOT EXISTS empresa (
    id_empresa BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    cuit VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS puesto (
    id_puesto BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_empresa BIGINT NOT NULL REFERENCES empresa(id_empresa)
);

CREATE TABLE IF NOT EXISTS competencia (
    id_competencia BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS puesto_competencia (
    id_competencia_requerida BIGSERIAL PRIMARY KEY,
    id_puesto BIGINT NOT NULL REFERENCES puesto(id_puesto),
    id_competencia BIGINT NOT NULL REFERENCES competencia(id_competencia),
    puntaje_minimo DOUBLE PRECISION NOT NULL CHECK (puntaje_minimo >= 0 AND puntaje_minimo <= 10),
    UNIQUE (id_puesto, id_competencia)
);

CREATE TABLE IF NOT EXISTS factor (
    id_factor BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    nro_orden INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_competencia BIGINT NOT NULL REFERENCES competencia(id_competencia),
    UNIQUE (id_competencia, nro_orden)
);

CREATE TABLE IF NOT EXISTS opcion_respuesta (
    id_opcion_resp BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS item_opcion (
    id_item_opcion BIGSERIAL PRIMARY KEY,
    id_opcion_resp BIGINT NOT NULL REFERENCES opcion_respuesta(id_opcion_resp) ON DELETE CASCADE,
    orden_visualizacion INTEGER NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    UNIQUE (id_opcion_resp, orden_visualizacion)
);

CREATE TABLE IF NOT EXISTS pregunta (
    id_pregunta BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    enunciado TEXT NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_factor BIGINT NOT NULL REFERENCES factor(id_factor),
    id_opcion_resp BIGINT NOT NULL REFERENCES opcion_respuesta(id_opcion_resp)
);

CREATE TABLE IF NOT EXISTS pregunta_opcion (
    id_pregunta_opcion BIGSERIAL PRIMARY KEY,
    id_pregunta BIGINT NOT NULL REFERENCES pregunta(id_pregunta) ON DELETE CASCADE,
    id_item_opcion BIGINT NOT NULL REFERENCES item_opcion(id_item_opcion),
    ponderacion DOUBLE PRECISION NOT NULL CHECK (ponderacion >= 0 AND ponderacion <= 10),
    UNIQUE (id_pregunta, id_item_opcion)
);

CREATE TABLE IF NOT EXISTS candidato (
    nro_candidato BIGINT PRIMARY KEY,
    apellido VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    nro_documento VARCHAR(30) NOT NULL,
    tipo_documento VARCHAR(10) NOT NULL CHECK (tipo_documento IN ('DNI', 'LC', 'LE', 'PP')),
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(5) NOT NULL CHECK (genero IN ('H', 'M')),
    nacionalidad VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    escolaridad VARCHAR(150) NOT NULL,
    eliminado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS cuestionario (
    id_cuestionario BIGSERIAL PRIMARY KEY,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'EN_PROCESO', 'COMPLETO', 'INCOMPLETO', 'SIN_CONTESTAR')),
    clave_ingreso VARCHAR(8) NOT NULL UNIQUE,
    cant_accesos INTEGER NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    fecha_ultimo_ingreso TIMESTAMP,
    nro_candidato BIGINT NOT NULL REFERENCES candidato(nro_candidato),
    id_puesto BIGINT NOT NULL REFERENCES puesto(id_puesto)
);

CREATE TABLE IF NOT EXISTS bloque_cuestionario (
    id_bloque BIGSERIAL PRIMARY KEY,
    nro_bloque INTEGER NOT NULL,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    id_cuestionario BIGINT NOT NULL REFERENCES cuestionario(id_cuestionario) ON DELETE CASCADE,
    UNIQUE (id_cuestionario, nro_bloque)
);

CREATE TABLE IF NOT EXISTS respuesta_candidato (
    id_respuesta BIGSERIAL PRIMARY KEY,
    fecha_hora_respuesta TIMESTAMP,
    orden_pregunta INTEGER NOT NULL,
    id_bloque BIGINT NOT NULL REFERENCES bloque_cuestionario(id_bloque) ON DELETE CASCADE,
    id_pregunta BIGINT NOT NULL REFERENCES pregunta(id_pregunta),
    id_item_opcion BIGINT REFERENCES item_opcion(id_item_opcion)
);

CREATE TABLE IF NOT EXISTS resultado (
    id_resultado BIGSERIAL PRIMARY KEY,
    fecha_calculo TIMESTAMP NOT NULL,
    id_cuestionario BIGINT NOT NULL UNIQUE REFERENCES cuestionario(id_cuestionario) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resultado_competencia (
    id_res_comp BIGSERIAL PRIMARY KEY,
    puntaje_obtenido DOUBLE PRECISION NOT NULL,
    puntaje_requerido DOUBLE PRECISION NOT NULL,
    id_resultado BIGINT NOT NULL REFERENCES resultado(id_resultado) ON DELETE CASCADE,
    id_competencia BIGINT NOT NULL REFERENCES competencia(id_competencia)
);
