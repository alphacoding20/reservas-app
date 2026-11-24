# auth-service

Servicio de usuarios y autenticación del sistema de reservas de canchas (actor B).
Base path: `/api/auth` · Puerto local: `8081` · Contrato: `docs/CONTRATO.md`.

## Requisitos
- Java 21 y Maven (o el `mvnw` del proyecto)
- PostgreSQL (Supabase). Una vez, desde DBeaver: `CREATE SCHEMA IF NOT EXISTS auth;`

## Variables de entorno
| Variable | Ejemplo / descripción |
|---|---|
| `DB_URL` | `jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require&currentSchema=auth` |
| `DB_USER` | usuario de la base |
| `DB_PASSWORD` | clave de la base (nunca en el repo) |
| `PORT` | opcional, por defecto 8081 (el hosting la inyecta) |
| `DB_POOL_SIZE` | opcional, por defecto 3 |
| `SEED_ADMIN_CLAVE` | opcional, clave del admin semilla (por defecto `Admin123*`) |
| `SEED_USUARIO_CLAVE` | opcional, clave de los usuarios semilla (por defecto `Usuario123*`) |
| `SEED_TELEGRAM_CHAT_ID` | chat_id real de Telegram para el usuario semilla 3 |

## Comandos
```bash
mvn clean verify          # pruebas + reporte JaCoCo + validación de cobertura >= 90 %
mvn spring-boot:run       # arranca el servicio (con las variables definidas)
```
Reporte de cobertura: `target/site/jacoco/index.html`.
Swagger: `http://localhost:8081/swagger-ui/index.html`.

## Docker
```bash
docker build -t auth-service .
docker run -p 8081:8081 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... auth-service
```

## Datos semilla (solo si la tabla está vacía)
| id | correo | rol | Telegram |
|---|---|---|---|
| 1 | admin@canchas.com | ADMIN | no |
| 2 | usuario@canchas.com | USUARIO | no |
| 3 | telegram@canchas.com | USUARIO | `SEED_TELEGRAM_CHAT_ID` |

## Endpoints
`POST /save` · `GET /{id}` · `PUT /update` · `DELETE /{id}` · `POST /login` · `PUT /{id}/clave` ·
`GET /correo/{correo}` · `PUT /{id}/rol` (ADMIN) · `PUT /{id}/estado` (ADMIN) · `GET /{id}/validar`

Los endpoints ADMIN exigen el header `X-Usuario-Id` (ver contrato 2.5).
