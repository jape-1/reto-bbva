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

| Variable      | Descripción                                  |
|---------------|----------------------------------------------|
| `DB_NAME`     | Nombre de la base de datos                    |
| `DB_USER`     | Usuario de PostgreSQL                         |
| `DB_PASSWORD` | Contraseña de PostgreSQL                      |
| `DB_PORT`     | Puerto expuesto en la máquina host (ej. 5432) |

Docker Compose lee ese `.env` automáticamente por estar junto al
`docker-compose.yml`.

### 2. Levantar PostgreSQL

```bash
docker compose up -d
```

Para apagarlo (los datos se conservan en el volumen `datos-postgres`):

```bash
docker compose down
```
