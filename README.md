# Reto Técnico BBVA — Gestión de Clientes Bancarios

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

### 2. Levantar PostgreSQL

```bash
docker compose up -d
```

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
