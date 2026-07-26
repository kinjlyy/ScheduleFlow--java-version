# ScheduleFlow — Microservice Migration Plan

This document outlines the multi-phase roadmap for evolving ScheduleFlow from a monolith into a fully distributed microservice architecture.

---

## Migration Roadmap

```
[ Phase 1: Completed ]
Timetable Service Standalone + Dependency Inversion
                     │
                     ▼
[ Phase 2: Discovery ] ──► Eureka Naming Server
                     │
                     ▼
[ Phase 3: Gateway ]   ──► Spring Cloud API Gateway (CORS, Auth Routing)
                     │
                     ▼
[ Phase 4: Resource ]  ──► Extract Resource Service (Rooms, Labs, Buildings)
                     │
                     ▼
[ Phase 5: Event ]     ──► Event & Notification Service (Kafka / RabbitMQ)
                     │
                     ▼
[ Phase 6: Feign ]     ──► OpenFeign Inter-Service Communication
```

---

## Phase Breakdown

### Phase 1: Timetable Service Baseline (COMPLETED)
- **Goal:** Establish `scheduleflow-timetable-service` as an independent service.
- **Key Deliverables:**
  - Artifact renamed to `scheduleflow-timetable-service`.
  - Introduced `RoomProvider` abstraction (`LocalRoomProvider` delegating to JPA).
  - Introduced `NotificationProvider`, `EventPublisher`, `AuditPublisher` extension interfaces.
  - Externalized all configuration via environment variables.
  - Added Spring Boot Actuator health endpoints.
  - Cleaned controller layering (separated Generation, Timetable, Lecture controllers).

---

### Phase 2: Service Discovery (Eureka Server)
- **Goal:** Register services with a central service registry so services locate each other dynamically without hardcoded URLs.
- **Actions:**
  1. Create `scheduleflow-eureka-server` Spring Boot project with `@EnableEurekaServer`.
  2. In `scheduleflow-timetable-service`:
     - Uncomment `spring-cloud-starter-netflix-eureka-client` dependency in `pom.xml`.
     - Uncomment `@EnableDiscoveryClient` in `ScheduleFlowApplication.java`.
     - Set `eureka.client.service-url.defaultZone=${EUREKA_URI}` in `application.properties`.

---

### Phase 3: API Gateway & Centralized Auth
- **Goal:** Provide a single entry point for clients, routing, CORS management, and centralized authentication.
- **Actions:**
  1. Create `scheduleflow-api-gateway` using Spring Cloud Gateway.
  2. Route `/api/timetables/**`, `/api/generate`, `/api/lectures/**` to `scheduleflow-timetable-service`.
  3. Move Auth logic (JWT validation, login, registration) to Gateway / Auth Service.
  4. Simplify `SecurityConfig` in Timetable Service to accept pre-validated gateway headers (`X-User-Id`, `X-User-Roles`).

---

### Phase 4: Resource Service Extraction
- **Goal:** Separate physical resource management (Rooms, Labs, Equipment, Buildings) into `scheduleflow-resource-service`.
- **Actions:**
  1. Move `Room` entity, `RoomRepository`, `RoomService`, and `RoomController` to `scheduleflow-resource-service`.
  2. Resource Service owns the `rooms` database table.
  3. Timetable Service removes `Room` database table mapping.

---

### Phase 5: Event & Notification Service
- **Goal:** Asynchronous, event-driven architecture using Kafka or RabbitMQ.
- **Actions:**
  1. Deploy message broker (Apache Kafka or RabbitMQ).
  2. Implement `KafkaEventPublisher` implementing `EventPublisher` in Timetable Service.
  3. Publish `TIMETABLE_GENERATED` event when generation completes.
  4. Create `scheduleflow-notification-service` to consume `TIMETABLE_GENERATED` and dispatch emails to teachers/students.

---

### Phase 6: OpenFeign Inter-Service Communication
- **Goal:** Wire `scheduleflow-timetable-service` to `scheduleflow-resource-service` dynamically.
- **Actions:**
  1. Uncomment `spring-cloud-starter-openfeign` in `pom.xml`.
  2. Create `ResourceServiceRoomProvider` implementing `RoomProvider`:
     ```java
     @FeignClient(name = "scheduleflow-resource-service")
     public interface ResourceServiceRoomProvider extends RoomProvider {
         @GetMapping("/api/rooms/active")
         List<Room> findAllActiveRooms();

         @GetMapping("/api/rooms/{id}")
         Optional<Room> findRoomById(@PathVariable("id") Long id);
     }
     ```
  3. Annotate `ResourceServiceRoomProvider` with `@Primary`.
  4. **Scheduler Engine behavior remains 100% untouched.**
