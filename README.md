# Capit@l Humano — Gestión de Evaluaciones de Perfiles (Tornidoscopio)

Trabajo Práctico Integrador de las cátedras **Desarrollo de Software** y **Diseño de Sistemas de Información** (UTN FRSF - 2026).

---

## 1. Requisitos del Entorno

Antes de comenzar a desarrollar, asegurate de tener instalado:
- **Java 21 (JDK 21)** *(Importante: el proyecto está configurado para Java 21 y Spring Boot 3.4.3).*
- **Maven** (opcional, se puede usar el wrapper `./mvnw` incluido en `/api`).
- **PostgreSQL 16**.
- **Node.js 20+** (para el desarrollo del frontend en Next.js dentro de `/frontend`).

---

## 2. Estructura del Repositorio

Siguiendo los requisitos de entrega de la cátedra, el repositorio se divide en dos carpetas principales:

```text
Tornidoscopio-main/
├── api/                        # Backend en Spring Boot (Java 21)
│   ├── src/main/java/com/tornidoscopio/tp/
│   │   ├── config/             # Configuración de Spring Security, CORS y Swagger (OpenAPI)
│   │   ├── controlador/        # Controladores REST (@RestController) que consume el frontend
│   │   ├── dao/                # Interfaces e implementaciones del Patrón DAO (acceso a datos)
│   │   ├── dominio/            # Entidades de dominio (@Entity) y Enums
│   │   ├── dto/                # Objetos de Transferencia de Datos (DTOs)
│   │   ├── excepciones/        # Excepciones de negocio y manejo global (@RestControllerAdvice)
│   │   ├── servicio/           # Lógica de negocio (Gestores, Importador, Servicios externos)
│   │   └── TpApplication.java  # Clase principal de Spring Boot
│   └── src/main/resources/
│       ├── application.properties
│       ├── esquema_capital.sql # Script DDL de creación de tablas
│       └── seed_capital.sql    # Script DML con datos iniciales de prueba
└── frontend/                   # Aplicación Web en Next.js
```

---

## 3. Casos de Uso a Implementar

De acuerdo con el enunciado de Desarrollo de Software, debemos implementar los siguientes Casos de Uso (y aquellos complementarios necesarios para su ejecución):

| Código | Caso de Uso | Gestor / Clase Responsable (`servicio`) |
| :--- | :--- | :--- |
| **CU 01** | Autenticar usuario (Consultor y Candidato) | `GestorAutenticacion` |
| **CU 06** | Importar candidatos (CSV) | `ImportadorCandidatos` |
| **CU 09** | Dar de alta puesto | `GestorPuestos` |
| **CU 13** | Dar de alta competencia | `GestorCompetencias` |
| **CU 17** | Dar de alta factor | `GestorFactores` |
| **CU 20** | Gestionar preguntas | `GestorPreguntas` |
| **CU 21** | Dar de alta pregunta | `GestorPreguntas` |
| **CU 22** | Modificar pregunta | `GestorPreguntas` |
| **CU 23** | Dar de baja pregunta | `GestorPreguntas` |
| **CU 24** | Gestionar opciones de respuestas | `GestorOpcionesRespuesta` |
| **CU 29** | Dar de alta opción de respuesta | `GestorOpcionesRespuesta` |
| **CU 25** | Evaluar candidatos | `GestorEvaluacion` |
| **CU 26** | Completar cuestionario | `GestorCompletarCuestionario` |
| **CU 28** | Emitir orden de mérito | `GestorReportes` |
| **CU 33** | Generar pregunta con IA *(Opcional)* | `GestorPreguntas` + `GeneradorPreguntasIA` |
