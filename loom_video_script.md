# ScheduleFlow — Complete 5-Minute Loom Script
### All Services · Associate Software Engineer (Intern) — Better

---

> **Before you hit record — open all of these:**
> - Browser tab 1: App at `localhost:3000`
> - VS Code: project root of `scheduleflow_v3`
> - Browser tab 2: GitHub commit history
> - This script on a second monitor

---

## ⏱️ TIMING CHEATSHEET

| # | Segment | Time | What's on Screen |
|---|---|---|---|
| 1 | Hook | 0:00–0:30 | App home / landing |
| 2 | Live Demo | 0:30–1:20 | Browser — localhost:3000 |
| 3 | System Architecture | 1:20–2:00 | VS Code file tree + docker-compose.yml |
| 4 | Timetable Service | 2:00–2:50 | SchedulerService.java + SecurityConfig.java |
| 5 | Resource Service | 2:50–3:15 | RoomController.java |
| 6 | Event Service | 3:15–4:00 | EventServiceImpl.java + TimetableServiceClient.java |
| 7 | Code Quality | 4:00–4:35 | GitHub commits + AcademicEventServiceTest.java |
| 8 | Design Decisions | 4:35–4:55 | VS Code overview |
| 9 | Wrap-up | 4:55–5:00 | App results page |

---

## ⏱️ SEGMENT 1 — Hook & Problem (0:00 – 0:30)

**[SCREEN: Browser — ScheduleFlow app home page]**

> "Hi, I'm Kinjal. I built ScheduleFlow — a production-grade timetable scheduling system with a React frontend and a full microservices backend on Java Spring Boot.
>
> The problem: school timetabling is NP-hard. With multiple sections, 10+ teachers, and 6 periods a day, manually building a clash-free schedule is a nightmare. Teachers double-book, lectures pile up on the same day, and when a campus event hits, the whole timetable breaks.
>
> ScheduleFlow solves all of that — automatically. Let me walk you through the entire system."

---

## ⏱️ SEGMENT 2 — Live Demo (0:30 – 1:20)

**[SCREEN: Browser — localhost:3000, Setup Page]**

> "The app has four builder pages — Setup, Constraints, Review, and Results — plus a full dashboard with room management and event scheduling.
>
> I'll go through the core flow fast."

**[On Setup Page — add sections and subjects]**

> "On Setup, I add sections — A, B, C. Each section gets subjects assigned to specific teachers with a lecture count per week. Here: Maths with Mr. B, 8 lectures. English with Mr. A, 4. Science with Mr. C, 6."

**[Navigate to Constraints Page]**

> "Constraints — days per week, periods per day, and per-teacher weekly caps. If Mr. A can only teach 20 lectures across all sections, I cap it here. The backend enforces this as a hard constraint, not a soft suggestion."

**[Navigate to Review Page]**

> "Review gives me a clean summary before I commit — all sections, all mappings, total lecture counts. One click to go back and edit, or forward to generate."

**[Click Generate → Results Page]**

> "I hit Generate. One POST to the backend. The DSatur algorithm runs, assigns every lecture to a unique slot, and the conflict-free timetable renders — no teacher in two places, no section with two subjects at the same time. I can filter by section, check teacher load stats, and export as CSV."

**[Navigate to Dashboard — show Events and Manage Rooms]**

> "The dashboard also has a Manage Rooms panel — that's the Resource Service. And an Events page — that's the Event Service. I'll get to those in the architecture walk."

---

## ⏱️ SEGMENT 3 — System Architecture (1:20 – 2:00)

**[SCREEN: VS Code — open `docker-compose.yml`]**

> "Here's the full system — six containers in Docker Compose. Let me name them."

**[Scroll through docker-compose.yml slowly, pointing to each service block]**

> "**PostgreSQL** — shared database on port 5432. All three backend services use the same Postgres instance, each writing to their own tables, managed by Flyway migrations.
>
> **Eureka Server** — Spring Cloud Netflix Eureka on port 8761. Every service registers here by name on startup. This is the service registry.
>
> **Timetable Service** — the core backend on port 8080. This is the scheduling brain — DSatur algorithm, JWT auth, timetable versioning, and the impact execution engine.
>
> **Resource Service** — port 8081. A dedicated microservice for room data — CRUD for classrooms, seminar halls, auditoriums. Queried by the Event Service via Feign.
>
> **Event Service** — port 8083. Manages academic events, room reservations, and timetable synchronization. It's the orchestrator — it talks to both the Timetable Service and the Resource Service.
>
> **API Gateway** — port 8082. Spring Cloud Gateway. All client requests hit here first, and it routes them to the correct service based on the path."

**[Show `application.properties` of API Gateway — the three route blocks]**

> "The Gateway config is explicit: `/api/rooms/**` goes to the Resource Service, `/api/generate` and `/api/timetables/**` go to the Timetable Service, and `/api/events/**` goes to the Event Service. Zero ambiguity — each service owns its route namespace."

---

## ⏱️ SEGMENT 4 — Timetable Service Deep Dive (2:00 – 2:50)

**[SCREEN: VS Code — open `backend/` folder, show service layer]**

> "The Timetable Service has two core services. `TimetableService` handles versioning and persistence — it wraps every generation in a `@Transactional` block, sets a GENERATING status, runs the scheduler, then archives any previously ACTIVE timetable and marks the new one ACTIVE. That's version control built in.
>
> `SchedulerService` is the algorithm."

**[Open `SchedulerService.java` — scroll to the `generate()` method around line 50]**

> "The `generate()` method has five clearly documented steps. I'll hit the most important ones."

**[Scroll to `buildConflictEdges()` around line 147]**

> "Step 2 — Build conflict edges. Every lecture is a node. Two nodes share an edge if they can never be in the same slot: same teacher, or same section. This is the graph model."

**[Scroll to `colorGraph()` around line 165]**

> "Step 3 — DSatur coloring. I run this in two passes. Pass 1 handles the first 5 lecture occurrences of each subject — I force them onto different days using the `lectureIndex % days` offset. This guarantees variety. Pass 2 handles all remaining repeats. The result: no subject stacks all its lectures on one day."

**[Scroll to `getDaySpreadSlots()` around line 253]**

> "This slot ordering function is the core of the day-spread logic. For lecture index `i`, the preferred day is `i % days`. Days with the fewest existing section lectures are tried first. Within a day, earlier periods win. It's compact and predictable — lectures cluster, free periods go to the end."

**[Open `SecurityConfig.java` — show the `permitAll` block around line 46]**

> "Security: Spring Security with stateless JWT. The `POST /api/generate` and `GET /api/timetables/**` endpoints are public — no token needed. Everything else — rooms, lectures, auth — requires a valid JWT in the Authorization header. The `JwtAuthFilter` validates and extracts the user on every request."

---

## ⏱️ SEGMENT 5 — Resource Service (2:50 – 3:15)

**[SCREEN: VS Code — open `RoomController.java` in resource service]**

> "The Resource Service is purpose-built for one thing: room data. It exposes a clean REST API — get all rooms, get active rooms, filter by type, filter by capacity, get by ID, create, update, delete.
>
> Notice `GET /api/rooms/active` — it calls `getAvailableRoomsForCapacity(1)`, which returns all rooms with capacity at least 1. This is the endpoint the Event Service calls via Feign every time it needs to build an availability list.
>
> The room model has a `RoomType` enum — `CLASSROOM`, `SEMINAR_HALL`, `AUDITORIUM` — and capacity fields. The Event Service uses these to rank rooms by suitability when suggesting the best option for an event."

---

## ⏱️ SEGMENT 6 — Event Service (3:15 – 4:00)

**[SCREEN: VS Code — open `EventServiceImpl.java`]**

> "The Event Service is the most complex part of the system. It operates across three phases."

**[Point to the comment `Phase 7A` around line 60]**

> "**Phase 7A — General event CRUD.** Create, read, update, delete events. Events have categories: `TIMETABLE_EVENT` for academic disruptions, `ROOM_RESERVATION` for ad-hoc bookings, and types like SPORTS, EXAM, CULTURAL. Every event has a lifecycle status: `DRAFT → IMPACT_ANALYZED → READY_FOR_EXECUTION → EXECUTING → COMPLETED`."

**[Point to the comment `Phase 7B` around line 177]**

> "**Phase 7B — Room reservations.** When someone reserves a room, the service: fetches the room from the Resource Service via Feign, checks it's active, validates the period range, then does a conflict query — `existsConflictingReservation()` — against the event repository. If a non-cancelled reservation already exists for that room on that date and period range, it throws a `ReservationConflictException`. Fail-fast, meaningful error."

**[Open `TimetableServiceClient.java` — show the Feign interface]**

> "**Phase 7C — Timetable synchronization.** This is where it gets interesting. The Event Service talks to the Timetable Service via a Feign client — `TimetableServiceClient`. Look at these methods: `getActiveTimetable()`, `getOccupiedRoomIds()`, `getImpactedLectures()`, and `executeEventImpact()`. Four clean, typed Feign calls — the Event Service never calls the Timetable Service's DB directly."

**[Back to `EventServiceImpl.java` — jump to `checkAvailability()` around line 270]**

> "When checking room availability, the service combines two data sources: events in its own database for that date, AND occupied rooms from the Timetable Service for that slot. A room is unavailable if either source says so. Available rooms are then ranked by a suitability score — classrooms score higher than seminar halls, rooms between 40-100 capacity score higher. The top result is flagged as Recommended."

**[Scroll to `executeStrategy()` around line 570]**

> "The execution flow: when an event fires, the service sets status to EXECUTING, calls `generateImpactAnalysis()` via Feign to get the list of affected lecture IDs, builds a `TimetableExecutionRequest` DTO, then calls the Timetable Service's `POST /api/timetables/{id}/event-execution` endpoint. The Timetable Service reschedules or cancels those lectures in its own database. The Event Service then updates its own status to COMPLETED or FAILED. Clean separation — the Event Service orchestrates, the Timetable Service executes."

---

## ⏱️ SEGMENT 7 — Code Quality & Commits (4:00 – 4:35)

**[SCREEN: GitHub — commit history]**

> "Let me show three commits that tell you how I work."

**[Show commit `533b402` — enforce strict room ID resolution]**

> "This commit added fail-fast validation for room persistence. Previously the system silently persisted a lecture with a null room ID — corrupt data, no error. I replaced that with a hard exception. The rule: if you can't satisfy the invariant, crash loudly. Silent corruption is worse than a crash."

**[Show commit `ef0ce04` — 8-step event synchronization]**

> "This is my most complex commit. I documented the entire 8-step sync algorithm in the Javadoc before writing the implementation. The steps: resolve the active timetable, compute impact, build the execution request, call the Timetable Service, handle reschedule vs. cancel logic, update statuses, record execution metadata, return the result. Writing the spec first forced me to find edge cases before the code existed."

**[Show commit `628daff` — resolve active timetable ID when null]**

> "This commit fixed a subtle bug: when an event's `timetableId` was null at execution time, the service threw a NullPointerException instead of gracefully auto-resolving the active timetable. One Feign call to `getActiveTimetable()` and a null-check fixed it. Small change, big reliability improvement."

**[Open `AcademicEventServiceTest.java`]**

> "I wrote unit tests using JUnit 5 and Mockito. Here the `EventRepository`, `ResourceServiceClient`, and `TimetableServiceClient` are all mocked. Each test targets one behavior — impact analysis, execution strategy, room conflict detection. The test setup builds a realistic `Event` entity with all required fields, so the tests actually cover real edge cases, not happy paths only."

---

## ⏱️ SEGMENT 8 — Design Decisions (4:35 – 4:55)

**[SCREEN: VS Code — any overview, or back to app in browser]**

> "Three architectural decisions I'm proud of, and one I'd revisit:
>
> **Stateless scheduler.** `POST /api/generate` is completely stateless. No session, no cached state. Every request is independent — you can run 10 Timetable Service instances behind a load balancer and the algorithm works identically on each. That's a deliberate scalability choice.
>
> **Feign for cross-service calls.** I could have used raw `RestTemplate` or `WebClient`, but Feign gives me typed interfaces, automatic error propagation, and clean timeout configuration per client. The `TimetableServiceClient` interface is 74 lines and replaces what would be 200+ lines of boilerplate HTTP code.
>
> **Flyway for schema migrations.** Every service uses Flyway with `baseline-on-migrate` so migrations are versioned and reproducible. No `ddl-auto=create-drop` in production. Schema changes are explicit, auditable, and reversible.
>
> **What I'd change:** Add backtracking to the DSatur pass. Right now, if a lecture genuinely can't be placed, we log a warning and skip it. With backtracking, we could un-assign a lower-priority lecture and retry. I'd also add circuit breakers — if the Timetable Service is down, the Event Service should degrade gracefully instead of propagating the failure."

---

## ⏱️ SEGMENT 9 — Wrap Up (4:55 – 5:00)

**[SCREEN: Browser — Results page with a generated timetable visible]**

> "That's the full ScheduleFlow system: a DSatur graph-coloring timetable engine, timetable versioning with PostgreSQL and Flyway, a dedicated Room resource microservice, an Event Service that orchestrates real-time timetable impact and execution — all wired through a Spring Cloud Gateway and Eureka, deployable with a single `docker compose up`.
>
> Built end to end by me. I'd love to bring this kind of thinking to Better."

---

## 📋 FILES TO HAVE OPEN IN VS CODE (in order)

1. `docker-compose.yml` — for architecture overview
2. `scheduleflow-api-gateway/src/main/resources/application.properties` — gateway routes
3. `backend/src/main/java/com/scheduleflow/service/SchedulerService.java` — algorithm (lines 50, 147, 165, 253)
4. `backend/src/main/java/com/scheduleflow/security/SecurityConfig.java` — JWT setup (line 46)
5. `scheduleflow-resource-service/.../controller/RoomController.java` — resource service API
6. `scheduleflow-event-service/.../service/impl/EventServiceImpl.java` — event service (lines 60, 177, 270, 570)
7. `scheduleflow-event-service/.../client/TimetableServiceClient.java` — Feign client
8. `scheduleflow-event-service/.../service/AcademicEventServiceTest.java` — tests

---

## 🎤 Delivery Tips

- **Speak at 80% normal speed** — technical content needs air.
- **Mouse cursor = your pointer** — move it to whatever you're naming.
- **Don't explain every line** — explain the *decision*, not the syntax.
- **One dry run at 1.5x speed** to check timing, then record for real.
- **Close Slack and notifications** before starting.
- If you stumble, pause for 2 seconds and continue — it edits cleanly.
