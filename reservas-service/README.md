# reservas-service (Persona A) · puerto 8083

Núcleo del sistema de reservas de canchas deportivas. Spring Boot 4.1.1 · Java 21 · arquitectura limpia
(misma estructura de paquetes que `catalogo`). Cumple `docs/CONTRATO.md` (sección 4).

## Estructura

```
domain/model            Reserva y modelos (records, sin dependencias de Spring/Lombok)
domain/model/gateway    Interfaces: ReservaGateway, UsuarioServiceGateway, CanchaServiceGateway,
                        ClimaGateway, NotificacionServiceGateway
domain/usecase          ReservaUseCase, DisponibilidadUseCase, PronosticoUseCase,
                        RecordatorioUseCase, EstadisticasUseCase (+ ValidadorAdmin, NotificadorReserva)
application/config      Beans (UseCaseConfig, AppConfig: Clock America/Bogota, RestClient con timeout 2 s)
infraestructure/entry_points         Controller, GlobalExceptionHandler (JSON 2.4), RecordatorioScheduler
infraestructure/driver_adapters      jpa_repository (PostgreSQL) y external_repository (auth, canchas,
                                     notificaciones @Async, Open-Meteo con caché Caffeine + fallback)
infraestructure/mapper
```

## Variables de entorno

| Variable | Default local | Notas |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/reservas_db` | |
| `DB_USER` / `DB_PASSWORD` | `postgres` / *(vacío)* | **nunca** en el repo |
| `AUTH_URL` | `http://localhost:8081` | sin barra final ni base path |
| `CANCHAS_URL` | `http://localhost:8082` | idem |
| `NOTIFICACIONES_URL` | `http://localhost:8084` | idem |
| `OPENMETEO_URL` | `https://api.open-meteo.com/v1` | |
| `PORT` | `8083` | lo inyecta el hosting |
| `LLUVIA_UMBRAL` | `60` | % de probabilidad para `riesgoLluvia` |
| `RECORDATORIOS_SCHEDULER` | `false` | `true` activa el recordatorio diario (9:00 Bogotá) |

## Ejecutar y probar

```bash
./mvnw spring-boot:run          # requiere PostgreSQL con la base reservas_db creada
./mvnw clean verify             # tests + JaCoCo: FALLA si las líneas cubiertas bajan de 90 %
# reporte: target/site/jacoco/index.html
```

Swagger: `http://localhost:8083/swagger-ui.html` · Salud: `/actuator/health`

Los tests **no necesitan base de datos ni internet** (Mockito, MockMvc standalone y `MockRestServiceServer`).
JaCoCo excluye solo `ReservasApplication` y `application/config/**` (decláralo en el artículo).

## Endpoints (ver contrato §4.2)

CRUD: `POST/GET /api/reservas`, `GET/DELETE /api/reservas/{id}`.
No CRUD: `GET /disponibilidad` · `PATCH /{id}/cancelar` · `PATCH /{id}/reprogramar` · `GET /{id}/pronostico` ·
`POST /alertas/lluvia` (ADMIN) · `POST /recordatorios` (ADMIN) · `PATCH /{id}/asistencia` (ADMIN) ·
`GET /usuario/{usuarioId}` · `GET /cancha/{canchaId}/agenda` · `GET /estadisticas` (ADMIN).
Los (ADMIN) exigen el header `X-Usuario-Id`.

## Despliegue (Docker)

```bash
docker build -t reservas .
docker run -p 8083:8083 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... -e AUTH_URL=... \
  -e CANCHAS_URL=... -e NOTIFICACIONES_URL=... reservas
```
Health check del hosting: `/actuator/health`.

## Carga y estrés (k6, contra la URL desplegada)

```bash
curl https://TU-SERVICIO/actuator/health        # calentamiento (repite 3-4 veces)
k6 run -e BASE_URL=https://TU-SERVICIO k6/carga.js
k6 run -e BASE_URL=https://TU-SERVICIO k6/estres.js
```
Mide con `p(95)`; la latencia incluye tu red hasta el servidor (anótalo en la metodología).
