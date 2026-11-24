# canchas-service

Servicio de canchas, horarios, bloqueos por mantenimiento y tarifas del sistema de reservas (actor B).
Base path: `/api/canchas` · Puerto local: `8082` · Contrato: `docs/CONTRATO.md`.

## Requisitos
- Java 21 y Maven (o el Maven de IntelliJ)
- PostgreSQL (Supabase). Una vez, desde DBeaver: `CREATE SCHEMA IF NOT EXISTS canchas_service;`
- El servicio `auth` corriendo (lo usan los endpoints ADMIN)

## Variables de entorno
| Variable | Ejemplo / descripción |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>:5432/postgres?sslmode=require&currentSchema=canchas_service` |
| `DB_USER` / `DB_PASSWORD` | credenciales de la base (nunca en el repo) |
| `AUTH_URL` | URL base de auth, sin barra final (por defecto `http://localhost:8081`) |
| `NAGER_URL` | por defecto `https://date.nager.at/api/v3` |
| `PORT` | opcional, por defecto 8082 (el hosting la inyecta) |
| `DB_POOL_SIZE` | opcional, por defecto 3 |
| `HTTP_TIMEOUT_MS` | opcional, timeout de las llamadas salientes (por defecto 2000) |
| `TARIFA_FACTOR_RECARGO` | opcional, factor para festivos y fines de semana (por defecto 1.20) |

## Comandos
```bash
mvn clean verify          # pruebas + reporte JaCoCo + validación de cobertura >= 90 %
mvn spring-boot:run       # arranca el servicio (con las variables definidas)
```
Reporte de cobertura: `target/site/jacoco/index.html`.
Swagger: `http://localhost:8082/swagger-ui/index.html`.

## Datos semilla (solo si la tabla está vacía)
| id | nombre | deporte | techada | horario | precio/hora |
|---|---|---|---|---|---|
| 1 | Sintética 1 | FUTBOL_5 | no | 06:00 a 22:00 | 50000 |
| 2 | Coliseo | BALONCESTO | sí | 08:00 a 21:00 | 40000 |
| 3 | Pádel 1 | PADEL | no | 07:00 a 22:00 | 60000 |

## Endpoints
`POST /` (ADMIN) · `GET /` · `GET /{id}` · `PUT /{id}` (ADMIN) · `DELETE /{id}` (ADMIN) ·
`GET /{id}/horario?fecha=` · `GET /{id}/tarifa?fecha=` · `POST /{id}/bloqueos` (ADMIN) ·
`DELETE /{id}/bloqueos/{bloqueoId}` (ADMIN) · `GET /festivos/{anio}` · `GET /buscar?deporte&techada&precioMax`

Los endpoints ADMIN exigen el header `X-Usuario-Id` (contrato 2.5): sin header 401; usuario inexistente,
no ADMIN o inactivo 403; `auth` caído 502 `SERVICIO_NO_DISPONIBLE`.

## Decisiones de implementación
- Los bloqueos deben empezar y terminar en hora en punto (misma regla de franjas del contrato).
- En `/horario`, un bloqueo que sigue después de medianoche se recorta con `horaFin = 23:59`.
- Si Nager.Date falla, `/tarifa` trata la fecha como no festiva y `/festivos` devuelve `[]` (no lanza error);
  el fallo no se guarda en caché.
- Crear un bloqueo no cancela reservas existentes (limitación conocida del contrato).
