# Reto Técnico BBVA — Gestión de Clientes Bancarios

API REST para la gestión de clientes bancarios, construida como **monolito modular**: tres
módulos con responsabilidades separadas que se comunican por eventos de dominio, no por
llamadas directas entre servicios.

## Stack

Java 17 · Spring Boot 3.3 · Spring Data JPA · Flyway · PostgreSQL 15 · Maven · Docker ·
GitHub Actions · AWS EC2 + RDS

---

## Diseño de componentes


| Módulo         | Responsabilidad                                            |
|----------------|------------------------------------------------------------|
| `cliente`      | Módulo core. El CRUD y la única fuente de eventos.          |
| `auditoria`    | Registra la traza de negocio (qué cambió y cuándo).         |
| `notificacion` | Simula el aviso de bienvenida al alta de un cliente.        |

La dirección de las flechas es lo importante: **`cliente` no conoce a los otros dos
módulos**. Publica un evento y termina. Agregar `notificacion` no requirió tocar una sola
línea de `ClienteService`.

### Flujo de eventos

| Evento               | Lo consume                  | Efecto                                          |
|----------------------|-----------------------------|-------------------------------------------------|
| `ClienteCreado`      | `auditoria`, `notificacion` | Fila `CREADO` + log `Bienvenida enviada a ...`  |
| `ClienteActualizado` | `auditoria`                 | Fila `ACTUALIZADO`                              |
| `ClienteEliminado`   | `auditoria`                 | Fila `ELIMINADO`                                |

Los listeners son `@TransactionalEventListener(phase = AFTER_COMMIT)`: reaccionan **solo si
la transacción del cliente se confirmó**, así que nunca queda traza de una operación que se
revirtió. El de `auditoria` añade `@Transactional(REQUIRES_NEW)` porque en la fase
`AFTER_COMMIT` la transacción original ya está cerrándose y, sin una nueva, el `save` se
ejecutaría sin llegar a persistir.

---

## Diseño funcional

### Endpoints

| Método   | Ruta                     | Éxito                     | Descripción                               |
|----------|--------------------------|---------------------------|-------------------------------------------|
| `POST`   | `/clientes`              | `201` + header `Location` | Crea un cliente                           |
| `GET`    | `/clientes`              | `200`                     | Lista todos los clientes                  |
| `GET`    | `/clientes/{id}`         | `200`                     | Obtiene un cliente                        |
| `PUT`    | `/clientes/{id}`         | `200`                     | Actualiza todos los campos                |
| `DELETE` | `/clientes/{id}`         | `204`                     | Baja lógica: el estado pasa a `ELIMINADO` |
| `GET`    | `/auditoria/{clienteId}` | `200`                     | Traza de auditoría del cliente            |

### Modelo

`Cliente`: `id`, `nombres`, `apellidos`, `numeroDocumento`, `email`, `telefono`, `estado`,
`fechaCreacion`, `fechaActualizacion`.

`estado` es el enum `EstadoCliente`: `ACTIVO`, `INACTIVO`, `BLOQUEADO`, `ELIMINADO`.

`fechaCreacion` y `fechaActualizacion` las escribe Spring Data JPA Auditing
(`@CreatedDate` / `@LastModifiedDate`), nunca el service a mano.

### Ejemplo

```bash
{
  "nombres": "Ana",
  "apellidos": "Torres",
  "numeroDocumento": "12345678",
  "email": "ana.torres@banco.pe",
  "telefono": "987654321",
  "estado": "ACTIVO"
}
```

```json
{
  "id": 1,
  "nombres": "Ana",
  "apellidos": "Torres",
  "numeroDocumento": "12345678",
  "email": "ana.torres@banco.pe",
  "telefono": "987654321",
  "estado": "ACTIVO",
  "fechaCreacion": "2026-08-24T15:10:22.118",
  "fechaActualizacion": "2026-08-24T15:10:22.118"
}
```

Tras crear, actualizar y dar de baja al cliente, `GET /auditoria/1` devuelve las tres
trazas en orden cronológico — sirve para demostrar en vivo que los eventos realmente
dispararon y persistieron.

### Validación

Bean Validation sobre el DTO de entrada, no sobre la entidad. Obligatorios: `nombres`,
`apellidos`, `numeroDocumento` (8–20 dígitos), `email` (formato válido) y `estado`.
`telefono` es opcional; si viene, 7–20 dígitos.

### Errores

Formato uniforme para los tres casos:

```json
{ "mensaje": "No existe el cliente con id 99", "status": 404, "timestamp": "2026-08-24T15:12:03.512" }
```

| Código | Cuándo                                                                    |
|--------|---------------------------------------------------------------------------|
| `400`  | Falla `@Valid` en el DTO. El mensaje concatena todos los campos inválidos. |
| `404`  | `RecursoNoEncontradoException`: el id no existe.                           |
| `500`  | Cualquier otra excepción. El detalle va al log, nunca a la respuesta.      |

---

## Decisiones de arquitectura

**Por qué monolito modular y no microservicios.** Con un despliegue en un solo EC2,
microservicios añadirían service discovery, más contenedores y más red sin aportar nada a
un CRUD de este tamaño. La separación por módulos demuestra el mismo criterio de
responsabilidades sin pagar ese costo operativo, y deja el camino abierto: cada módulo ya
tiene su frontera definida.

**Por qué eventos y no llamadas directas.** Si `ClienteService` invocara a un
`AuditoriaService` y a un `NotificacionService`, el módulo core quedaría acoplado a cada
funcionalidad nueva que se le cuelgue. Con eventos, el core solo anuncia lo que pasó. Es el
mismo principio de una arquitectura basada en eventos, simplificado a nivel de proceso:
sin broker ni colas, que sí serían sobre-ingeniería aquí.

**Por qué los paquetes son por módulo y no por capa.** No hay un `controllers/` global con
todos los controllers dentro. Cada módulo agrupa su entidad, su repositorio, su servicio y
sus DTOs, lo que hace visible la frontera y permite razonar sobre un módulo sin leer el
resto.

**Por qué Flyway con `ddl-auto=validate`.** El esquema se versiona en migraciones (`V1`,
`V2`, `V3`) y Hibernate solo comprueba que la entidad coincide con la tabla. Con `update`,
el esquema dependería de en qué orden arrancó la aplicación y sería irreproducible. Una
migración ya aplicada nunca se edita: por eso el estado `ELIMINADO` llegó en una `V3` nueva
y no modificando la `V1`.

**Por qué el `DELETE` es baja lógica.** Un banco no borra clientes. `DELETE` deja el estado
en `ELIMINADO`, un valor propio distinto de `INACTIVO` para no mezclar la baja con un
estado de negocio. La operación es idempotente: repetirla no genera una segunda traza. Aun
así, `audit_log` no tiene FK contra `cliente`, de modo que la traza sobreviviría incluso a
un borrado físico.

**Dos cosas distintas que se llaman "auditoría".** `@CreatedDate` / `@LastModifiedDate` son
metadata técnica de la fila. El módulo `auditoria` es una funcionalidad de negocio: el
registro de qué operaciones ocurrieron. Comparten nombre y no son lo mismo.

**Manejo de errores: tres casos, no una jerarquía.** Un solo `@RestControllerAdvice` con
400, 404 y 500, y una única excepción propia. Es el punto medio entre no manejar nada y
diseñar códigos de error por módulo, que a este alcance sería estructura sin uso.

### Fuera de alcance, a propósito

Sin seguridad ni JWT, sin arquitectura hexagonal completa, sin paginación, caché ni rate
limiting, y sin tests de integración con Testcontainers. No es desconocimiento: son
decisiones de alcance para un reto acotado. El siguiente paso natural sería autenticación y
un CD que despliegue por SSH.

---

## Entorno local

La base de datos de desarrollo corre en Docker. En EC2 no se usa `docker-compose`:
allí la aplicación se conecta directamente a RDS.

### 1. Crear el archivo `.env`

Copiar la plantilla y completar los valores. El `.env` real está git-ignorado y
nunca se commitea:

```bash
cp .env.example .env
```

| Variable            | Descripción                               |
|---------------------|-------------------------------------------|
| `RETO_DB_NAME`      | Nombre de la base de datos                |
| `RETO_DB_USER`      | Usuario de PostgreSQL                     |
| `RETO_DB_PASSWORD`  | Contraseña de PostgreSQL                  |
| `RETO_DB_PORT`      | Puerto expuesto en la máquina host (5438) |

Se usan puertos no estándar a propósito (`5438` en vez de `5432`, `8088` en vez
de `8080`) para no chocar con un PostgreSQL o un servidor ya instalados en la
máquina.

El prefijo `RETO_` responde al mismo criterio: Docker Compose da prioridad a las
variables del sistema por encima del `.env`, así que un `DB_USER` cualquiera
definido en la máquina sobrescribiría el del archivo sin avisar.

Docker Compose lee ese `.env` automáticamente por estar junto al
`docker-compose.yml`.

### 2. Levantar el entorno

```bash
docker compose up -d
```

Levanta PostgreSQL y la aplicación. La app espera al `healthcheck` de la base antes de
arrancar, y Flyway aplica las migraciones en el primer arranque. Queda escuchando en
`http://localhost:8088`.

Si cambias el usuario o la contraseña en el `.env` después del primer arranque,
hay que recrear el volumen: PostgreSQL solo aplica `POSTGRES_USER` y
`POSTGRES_PASSWORD` al inicializar el directorio de datos, y los ignora si ya
existe.

```bash
docker compose down -v
```

Para apagarlo conservando los datos del volumen `datos-postgres`:

```bash
docker compose down
```

### 3. Arrancar solo la aplicación

Spring no lee el `.env` — eso lo hace Docker Compose. Al arrancar desde el IDE o con Maven
hay que pasarle las variables al proceso (`RETO_DB_NAME`, `RETO_DB_USER`,
`RETO_DB_PASSWORD`, `RETO_DB_PORT`), con la base ya levantada:

```bash
mvn spring-boot:run
```

---

## Tests

```bash
mvn test
```

Tests de servicio con JUnit 5 y Mockito sobre `ClienteService`, cubriendo las tres
decisiones de diseño: que se publica el evento, que un id inexistente lanza la excepción de
404, y que el `DELETE` es baja lógica y nunca llama a `repository.delete()`.

No usan base de datos: el repositorio está mockeado. Por eso el pipeline de CI no necesita
ningún secreto de conexión.

## Integración continua

`.github/workflows/ci.yml` se dispara en cada push a `main`: checkout → Java 17 →
`mvn test` → empaquetado del jar → build de la imagen Docker.

