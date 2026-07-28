# ScheduleFlow — Timetable Service Architecture

## 1. Executive Summary

`scheduleflow-timetable-service` is an independently deployable, production-ready Spring Boot microservice responsible for timetable generation, versioning, lecture persistence, and timetable queries.

Following Phase 6 of the microservice migration, legacy local Room infrastructure (JPA entity, repository, service, controller) has been completely removed. `TIMETABLE-SERVICE` communicates with `RESOURCE-SERVICE` via declarative OpenFeign inter-service calls for active room data, while preserving 100% of the existing graph-coloring scheduling engine and business logic.

---

## 2. Final Domain Ownership

The microservice architecture strictly demarcates domain ownership between services:

### RESOURCE-SERVICE owns:
- **Room Entity**: Persistence model and table (`rooms`) in PostgreSQL
- **Room Repository**: JPA persistence layer for Room resources
- **Room Service**: Business logic for room CRUD and availability queries
- **Room Controller**: REST API endpoints (`/api/rooms/**`) for room management
- **Room Database**: Complete ownership of Room database storage

### TIMETABLE-SERVICE owns:
- **Timetable & Lecture**: Timetable metadata, lifecycle status (GENERATING, ACTIVE, ARCHIVED), and generated lecture schedule records
- **Scheduler Engine**: Graph-coloring DSatur algorithm, day-spread slot ordering, multi-day P1 compaction
- **RoomProvider**: High-level domain abstraction interface for fetching active room resources
- **FeignRoomProvider**: Production OpenFeign client wrapper translating transport objects into domain models
- **ResourceClient**: Declarative OpenFeign HTTP client communicating with `RESOURCE-SERVICE`
- **RoomMapper**: DTO-to-domain transformation mapper
- **Room Domain Model**: Pure in-memory POJO with zero persistence responsibility

> [!NOTE]
> `TIMETABLE-SERVICE` contains no room persistence configuration and does not manage any `rooms` database table.

---

## 3. Room Retrieval Execution Path

Room retrieval for scheduling in `TIMETABLE-SERVICE` follows a strict, single execution path:

```
SchedulerService
       │
       ▼
  RoomProvider (Abstraction Interface)
       │
       ▼
FeignRoomProvider (@Primary Implementation)
       │
       ▼
 ResourceClient (Declarative OpenFeign Client)
       │
       ▼
RESOURCE-SERVICE (via Eureka Service Registry)
```

---

## 4. Lecture Room Snapshot Strategy (Option A)

For historical lecture persistence, `TIMETABLE-SERVICE` uses **Option A — Immutable Historical Snapshot Strategy**:
- `Lecture` entities persist both `roomId` (`Long`) and `roomNumber` (`String`) directly as scalar columns in the `lectures` database table.
- **Rationale**: Historical timetables must remain immutable records. If a room is later renamed or modified in `RESOURCE-SERVICE`, past generated timetables retain the exact room details assigned at generation time.

---

## 5. Layered Architecture & Dependency Flow

The service follows strict clean layering:

```
[ HTTP Controller Layer ]
       │
       ▼
[ Service Layer ]  ────────► [ Provider Extension Interfaces ]
       │                                   │
       ▼                                   ▼
[ Domain Layer / Entities ]      [ Feign Implementations ]
       │                                   │
       ▼                                   ▼
[ JPA Repositories ]             [ OpenFeign Clients ]
       │                                   │
       ▼                                   ▼
[ PostgreSQL DB ]                [ RESOURCE-SERVICE ]
```

### Flow Rules
1. **Controllers** call **Services** only.
2. **Services** execute domain logic and call **Providers** or **Repositories**.
3. **Scheduler Engine** depends exclusively on the `RoomProvider` domain interface. It has zero knowledge of JPA, Hibernate, or OpenFeign HTTP details.
4. **All dependencies** are injected via **constructor injection**.

---

## 6. External Configuration & Production Readiness

All configuration parameters are externalized using Spring Boot environment variable syntax:

| Property | Environment Variable | Default Value |
|---|---|---|
| `server.port` | `PORT` | `8080` |
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/scheduleflow` |
| `spring.datasource.username` | `DB_USER` | `postgres` |
| `spring.datasource.password` | `DB_PASS` | `""` |
| `jwt.secret` | `JWT_SECRET` | *(configured fallback)* |
| `app.cors.allowed-origins` | `ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` |
| `eureka.client.service-url.defaultZone` | `EUREKA_URI` | `http://localhost:8761/eureka` |

### Health & Observability
- Spring Boot Actuator endpoints enabled: `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
- Health readiness/liveness probes ready for Kubernetes or Docker deployment.
