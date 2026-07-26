# ScheduleFlow — Public API Contracts

This document defines the service contracts exposed by `scheduleflow-timetable-service`.
These contracts specify how API Gateway, frontend applications, and other microservices will consume the Timetable Service.

---

## Base URL
- Local: `http://localhost:8080`
- Service Name: `scheduleflow-timetable-service`

---

## 1. Generation API (`GenerationController`)

### `POST /api/generate`
Triggers the DSatur graph-coloring algorithm to generate a clash-free timetable, persist the lectures in PostgreSQL, and set the timetable status to `ACTIVE` (archiving all previously active timetables).

- **Request Body:** `TimetableRequestDTO`
```json
{
  "daysPerWeek": 5,
  "periodsPerDay": 8,
  "roomAllocationStrategy": "DYNAMIC_ALLOCATION",
  "teacherMaxLectures": {
    "Dr. Smith": 15
  },
  "sections": [
    {
      "id": "SEC-A",
      "name": "Section A",
      "capacity": 60,
      "fixedRoomId": null,
      "mappings": [
        {
          "subject": "Mathematics",
          "teacher": "Dr. Smith",
          "lecturesPerWeek": 4,
          "lectureType": "THEORY",
          "projectorRequired": false,
          "preferredRoomType": "CLASSROOM",
          "movable": true
        }
      ]
    }
  ]
}
```

- **Response (200 OK):** `TimetableResponseDTO`
```json
{
  "timetableId": 1,
  "timetable": {
    "SEC-A": {
      "Monday": [
        {
          "subject": "Mathematics",
          "teacher": "Dr. Smith",
          "roomId": 101,
          "roomNumber": "C-101",
          "free": false
        }
      ]
    }
  },
  "warnings": [],
  "stats": {
    "totalSections": 1,
    "totalScheduledLectures": 4,
    "totalFreePeriods": 36,
    "warningCount": 0,
    "teacherLoadMap": {
      "Dr. Smith": 4
    }
  }
}
```

---

## 2. Timetable Versioning APIs (`TimetableController`)

### `GET /api/timetables`
Returns metadata history for all generated timetables ordered by creation date descending.
- **Response (200 OK):** `List<TimetableDTO>`

### `GET /api/timetables/active`
Returns metadata for the currently `ACTIVE` timetable version.
- **Response (200 OK):** `TimetableDTO`

### `GET /api/timetables/{id}`
Returns metadata for a specific timetable version by ID.
- **Response (200 OK):** `TimetableDTO`
- **Error (404 Not Found):** `ResourceNotFoundException`

---

## 3. Lecture Query APIs (`LectureController`)

### Versioned Queries
- `GET /api/timetables/{id}/lectures` — All lectures in timetable `{id}`
- `GET /api/timetables/{id}/lectures/section/{sectionId}` — Lectures for a section in timetable `{id}`
- `GET /api/timetables/{id}/lectures/teacher/{teacherId}` — Lectures for a teacher in timetable `{id}`
- `GET /api/timetables/{id}/lectures/room/{roomId}` — Lectures in a room for timetable `{id}`
- `GET /api/timetables/{id}/lectures/day/{day}` — Lectures on a day (e.g. `Monday`) for timetable `{id}`

### Active Timetable Queries
- `GET /api/timetables/active/lectures` — All active lectures
- `GET /api/timetables/active/lectures/section/{sectionId}` — Active section schedule
- `GET /api/timetables/active/lectures/teacher/{teacherId}` — Active teacher schedule
- `GET /api/timetables/active/lectures/room/{roomId}` — Active room schedule
- `GET /api/timetables/active/lectures/day/{day}` — Active day schedule

---

## 4. Room APIs (`RoomController`) — *Temporary Service Boundary*

> **Note:** These endpoints will move to `scheduleflow-resource-service` in Phase 4.

- `GET /api/rooms` — List all rooms (`RoomDTO`)
- `GET /api/rooms/summary` — Room summary metrics (`RoomSummaryDTO`)
- `GET /api/rooms/{id}` — Room by ID
- `GET /api/rooms/capacity/{capacity}` — Available rooms with capacity >= `{capacity}`
- `POST /api/rooms` — Create room
- `PUT /api/rooms/{id}` — Update room
- `DELETE /api/rooms/{id}` — Delete room

---

## 5. Health & Observability Endpoints

- `GET /` — Health banner ("ScheduleFlow Timetable Service is running")
- `GET /api/health` — API status ("ScheduleFlow API is running")
- `GET /actuator/health` — Spring Boot Actuator health status (`{"status":"UP"}`)
- `GET /actuator/info` — Application info metadata
