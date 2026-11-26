# Capit@l Humano — Gestión de Evaluaciones de Perfiles (Tornidoscopio)

Trabajo Práctico Integrador de las cátedras **Desarrollo de Software** y **Diseño de Sistemas de Información** (UTN FRSF - 2026).

---

## 1. Requisitos del Entorno

Antes de comenzar a desarrollar o ejecutar el sistema, asegurate de tener instalado:
- **Java 21 (JDK 21)** *(Importante: el proyecto está configurado para Java 21 y Spring Boot 3.4.3).*
- **Maven** (opcional, se puede ejecutar desde IntelliJ IDEA o usar el wrapper `./mvnw` incluido en `/api`).
- **PostgreSQL 16** (o conexión a la base de datos compartida en Neon.tech).
- **Node.js 20+** (para la ejecución del frontend en Next.js dentro de `/frontend`).

---

## 2. Estructura del Repositorio

Siguiendo los requisitos de entrega de la cátedra, el repositorio se divide en dos carpetas principales:

```text
Tornidoscopio-main/
├── api/                        # Backend en Spring Boot (Java 21)
│   ├── src/main/java/com/tornidoscopio/tp/
│   │   ├── config/             # Configuración de Spring Security y CORS
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

## 3. Casos de Uso del Sistema

De acuerdo con el enunciado de Desarrollo de Software, el sistema abarca los siguientes Casos de Uso:

| Código | Caso de Uso | Gestor / Clase Responsable (`servicio`) | Estado |
| :--- | :--- | :--- | :--- |
| **CU 01** | Autenticar usuario (Consultor y Candidato) | `GestorAutenticacion` | Próxima Entrega |
| **CU 06** | Importar candidatos (CSV) | `ImportadorCandidatos` | Próxima Entrega |
| **CU 09** | Dar de alta puesto | `GestorPuestos` | Próxima Entrega |
| **CU 13** | Dar de alta competencia | `GestorCompetencias` | Próxima Entrega |
| **CU 17** | Dar de alta factor | `GestorFactores` | Próxima Entrega |
| **CU 20** | Gestionar preguntas | `GestorPreguntas` | **Implementado (1° Entrega)** |
| **CU 21** | Dar de alta pregunta | `GestorPreguntas` | **Implementado (1° Entrega)** |
| **CU 22** | Modificar pregunta | `GestorPreguntas` | Próxima Entrega |
| **CU 23** | Dar de baja pregunta | `GestorPreguntas` | Próxima Entrega |
| **CU 24** | Gestionar opciones de respuestas | `GestorOpcionesRespuesta` | Próxima Entrega |
| **CU 29** | Dar de alta opción de respuesta | `GestorOpcionesRespuesta` | Próxima Entrega |
| **CU 25** | Evaluar candidatos | `GestorEvaluacion` + `GestorPuestos` | **Implementado (1° Entrega)** |
| **CU 26** | Completar cuestionario | `GestorCompletarCuestionario` | **Implementado (1° Entrega)** |
| **CU 28** | Emitir orden de mérito | `GestorReportes` + `GestorPuestos` | **Implementado (1° Entrega)** |
| **CU 33** | Generar pregunta con IA *(Opcional)* | `GestorPreguntas` + `GeneradorPreguntasIA` | Opcional / Próxima Entrega |

---

## 4. Guía de Ejecución: Cómo levantar el sistema

### Paso 1: Configuración de la Base de Datos (PostgreSQL)
El sistema puede conectarse tanto a una instancia local de PostgreSQL 16 como a la base de datos compartida en la nube (Neon.tech).

1. **Si se utiliza PostgreSQL local:**
   - Crear una base de datos llamada `capital_humano`.
   - Ejecutar los scripts ubicados en `api/src/main/resources/` en el siguiente orden:
     1. `esquema_capital.sql` (creación de tablas y restricciones).
     2. `seed_capital.sql` (datos iniciales de prueba: empresas, competencias, factores, opciones de respuesta, preguntas, puestos y candidatos).

2. **Variables de conexión (`api/src/main/resources/application.properties`):**
   El backend toma las credenciales desde variables de entorno (o usa por defecto `localhost:5432/capital_humano` con usuario y clave `postgres`):
   - `DB_URL`: URL JDBC de conexión (ej. `jdbc:postgresql://localhost:5432/capital_humano`)
   - `DB_USER`: Usuario de la base de datos
   - `DB_PASSWORD`: Contraseña de la base de datos

---

### Paso 2: Levantar el Backend (API REST - Spring Boot)
El backend se ejecuta en el puerto **`8080`**.

- **Desde IntelliJ IDEA:**
  1. Abrir el proyecto y configurar, si corresponde, las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD` en la configuración de ejecución (*Run Configuration*).
  2. Ejecutar la clase principal `com.tornidoscopio.tp.TpApplication`.

- **Por consola (con Maven):**
  ```bash
  cd api
  mvn spring-boot:run
  ```

- **Documentación Swagger / OpenAPI:**
  Una vez iniciado el backend, se puede consultar y probar la documentación interactiva de los endpoints en:
  `http://localhost:8080/swagger-ui/index.html`

---

### Paso 3: Levantar el Frontend (Next.js)
El frontend se ejecuta en el puerto **`3000`** y consume la API del backend en `http://localhost:8080/api`.

1. Abrir una terminal en la carpeta `frontend`:
   ```bash
   cd frontend
   ```
2. Instalar las dependencias (solo la primera vez):
   ```bash
   npm install
   ```
   *(Nota en Windows PowerShell: si la ejecución de scripts `.ps1` está restringida, usar `npm.cmd install` o habilitar con `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`).*
3. Iniciar el servidor de desarrollo:
   ```bash
   npm run dev
   ```
4. Abrir en el navegador:
   `http://localhost:3000`

---

## 5. Ejecución de Pruebas (Tests)

El backend cuenta con **pruebas unitarias** (con JUnit 5 y Mockito) y **pruebas de integración** contra PostgreSQL (anotadas con `@SpringBootTest` y `@Transactional` para realizar rollback automático al finalizar cada test):

- **Tests Unitarios** (no requieren conexión a base de datos):
  - `GestorPreguntasTest` (CU020 y CU021)
  - `GestorEvaluacionTest` y `GestorPuestosTest` (CU025)
  - `GestorCompletarCuestionarioTest`, `CuestionarioDomainTest` y `FactorTest` (CU026)
  - `GestorReportesTest` (CU028)

- **Tests de Integración** (se activan automáticamente cuando está configurada la variable de entorno `DB_URL`):
  - `CU020yCU021IntegracionTest`
  - `CU025IntegracionTest`
  - `CU026IntegracionTest`
  - `CU028IntegracionTest`
