# ScheduleFlow — Timetable Service Architecture

## 1. Executive Summary

`scheduleflow-timetable-service` is an independently deployable, production-ready Spring Boot microservice responsible for timetable generation, versioning, lecture persistence, and timetable queries.

In Phase 1 of the microservice migration, the application was decoupled from direct infrastructure dependencies via dependency inversion patterns, while preserving 100% of the existing graph-coloring scheduling engine and business logic.

---

## 2. Service Boundary & Owned Domain

The Timetable Service strictly owns the following domain entities and capabilities:

| Domain Area | Description |
|---|---|
| **Timetable** | Timetable metadata, status (GENERATING, ACTIVE, ARCHIVED), semester, academic year |
| **Lecture** | Scheduled lecture entries (subject, section, teacher, room, day, slot, lecture type) |
| **Scheduler Engine** | DSatur graph-coloring algorithm, day-spread slot ordering, multi-day P1 compaction |
| **Versioning & History** | Atomic transactional generation with automatic archive-on-success semantics |
| **Teacher Timetable** | Queries for teacher-specific schedules |
| **Section Timetable** | Queries for section-specific schedules |

### Temporary Responsibilities
- **Room Data:** Room entity and queries currently reside in this service. In Phase 4 (Resource Service), Room data will be extracted. Access to Room data is isolated via `RoomProvider`.
- **Authentication & User Data:** User entity, registration, login, and JWT filter are currently hosted here. In Phase 3/4, authentication will be handled by the API Gateway and Auth Service.

---

## 3. Layered Architecture & Dependency Flow

The service follows strict clean layering:

```
[ HTTP Controller Layer ]
       │
       ▼
[ Service Layer ]  ────────► [ Provider Extension Interfaces ]
       │                                   │
       ▼                                   ▼
[ Domain Layer / Entities ]      [ Local Implementations ]
       │                                   │
       ▼                                   ▼
[ JPA Repositories ] ◄─────────────────────┘
       │
       ▼
[ PostgreSQL / Neon DB ]
```

### Flow Rules
1. **Controllers** call **Services** only. Controllers never access Repositories or Providers directly.
2. **Services** execute domain logic and call **Providers** or **Repositories**.
3. **Scheduler Engine** depends exclusively on the `RoomProvider` interface. It has zero knowledge of JPA, Hibernate, or PostgreSQL.
4. **All dependencies** are injected via **constructor injection**. Field injection (`@Autowired`) is strictly eliminated.

---

## 4. Key Abstractions Introduced in Phase 1

### 4.1 RoomProvider (`com.scheduleflow.scheduler.RoomProvider`)
- **Interface:** Abstracts room lookup and availability queries.
- **Current Implementation:** `LocalRoomProvider` — delegates to `RoomRepository`.
- **Future Implementation (Phase 6):** `ResourceServiceRoomProvider` — calls Resource Service via OpenFeign.
- **Impact:** Zero lines of code in `SchedulerService` will change when moving to Resource Service.

### 4.2 NotificationProvider (`com.scheduleflow.provider.NotificationProvider`)
- **Interface:** Abstracts outbound email and notification dispatch.
- **Current Implementation:** `NoOpNotificationProvider` — logs intent at DEBUG level.
- **Future Implementation:** `SmtpNotificationProvider` or Kafka event to Notification Service.

### 4.3 EventPublisher (`com.scheduleflow.provider.EventPublisher`)
- **Interface:** Abstracts domain event publication (`TIMETABLE_GENERATED`, `TIMETABLE_ARCHIVED`).
- **Current Implementation:** `NoOpEventPublisher` — logs event at INFO level.
- **Future Implementation (Phase 5):** Kafka / RabbitMQ producer publishing to Event Service.

### 4.4 AuditPublisher (`com.scheduleflow.provider.AuditPublisher`)
- **Interface:** Abstracts system audit logging.
- **Current Implementation:** `LocalAuditPublisher` — logs structured audit lines via SLF4J.
- **Future Implementation:** Writes to audit table or central log stream.

---

## 5. External Configuration & Production Readiness

All configuration parameters are externalized using Spring Boot environment variable syntax:

| Property | Environment Variable | Default Value |
|---|---|---|
| `server.port` | `PORT` | `8080` |
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/scheduleflow` |
| `spring.datasource.username` | `DB_USER` | `postgres` |
| `spring.datasource.password` | `DB_PASS` | `""` |
| `jwt.secret` | `JWT_SECRET` | *(configured fallback)* |
| `app.cors.allowed-origins` | `ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` |

### Health & Observability
- Spring Boot Actuator endpoints enabled: `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
- Health readiness/liveness probes ready for Kubernetes or Docker deployment.
