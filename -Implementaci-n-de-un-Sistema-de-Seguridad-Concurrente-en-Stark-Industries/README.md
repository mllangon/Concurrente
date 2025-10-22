# Implementación de un Sistema de Seguridad Concurrente en Stark Industries

Sistema Spring Boot 3.3 (Java 21, Maven) para ingerir eventos de sensores en concurrencia, evaluar reglas de seguridad y emitir alertas en tiempo real vía WebSocket y email. Incluye seguridad con roles, métricas/Actuator, logging JSON-friendly, perfiles (H2 dev, Postgres prod) y Docker Compose con Postgres, MailHog, Prometheus y Grafana.

## Lógica de la solución
- Ingesta concurrente con `@Async` y `ThreadPoolTaskExecutor` (`sensorExecutor`), con cola interna opcional (`LinkedBlockingQueue` + `@Scheduled`).
- Reglas: movimiento (warn/critical según horario), temperatura (umbrales), acceso (critical si no autorizado).
- Alertas publicadas a `/topic/alerts` (STOMP) y enviadas por email (MailHog en dev).
- Seguridad in-memory (roles ADMIN, SECURITY_ENGINEER, OPERATOR, VIEWER). Rutas públicas: `/`, `/index.html`, `/ws/**`, `/actuator/health/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- Métricas personalizadas: `sensor.events.processed`, `sensor.events.latency`, `alerts.published`, `access.denied`.

## Estructura relevante
- `pom.xml`: dependencias, plugins (Spotless), Spring Boot plugin.
- `src/main/java/com/stark/...`: paquetes `security`, `sensors`, `alerts`, `access`, `monitoring`, `common`.
- `application.yml`: perfiles dev/prod, umbrales, mail.
- `logback-spring.xml`: consola y rolling file con patrón JSON-friendly.
- `static/index.html`: UI mínima con tabla en vivo y métricas.

## Ejecutar en local (sin Docker)
1. Java 21 y Maven instalados.
2. `mvn -q -DskipTests package` y luego `./mvnw spring-boot:run` o `mvn spring-boot:run`.
3. Abrir `http://localhost:8080/`. Credenciales: `admin/admin`, `sec/sec`, `op/op`, `view/view`.

## Docker Compose
1. `cp .env.example .env` y ajustar variables.
2. `docker compose up --build`.
3. Servicios: app:8080, Postgres:5432, MailHog:8025, Prometheus:9090, Grafana:3000.

## Endpoints clave
- POST `/api/sensors` (ADMIN/SECURITY_ENGINEER)
- GET `/api/sensors` (todos con rol)
- POST `/api/sensors/{id}/events` (ADMIN/SECURITY_ENGINEER/OPERATOR)
- GET `/api/events?type=&severity=&from=&to=` (todos con rol)
- POST `/api/access/logs` y GET `/api/access/logs`
- Health: `/actuator/health/**`, Prometheus: `/actuator/prometheus`
- Swagger UI: `/swagger-ui`

## Probar WebSocket
1. Abrir `http://localhost:8080/`.
2. Enviar un evento CRITICAL (por ejemplo, acceso no autorizado) y observar la fila en la tabla <100ms.

## MailHog y métricas
- Correos: `http://localhost:8025`.
- Prometheus: `http://localhost:9090`, Grafana: `http://localhost:3000` (dashboards importados desde `grafana/dashboards`).

## Métricas y criterios de éxito
- Latencia media ingesta (`sensor.events.latency`) < 20ms en dev.
- Throughput > 500 eps en dev.
- WebSocket CRITICAL visible < 100ms.

## Equipo y roles
- Equipo ficticio: líder técnico, backend, QA, DevOps.

## Referencias
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- Spring Framework: https://spring.io/projects/spring-framework