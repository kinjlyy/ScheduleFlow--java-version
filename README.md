# ScheduleFlow — Smart Timetable Scheduler

> **React frontend + Java Spring Boot backend**
> Conflict-free timetable generation using the DSatur Graph Coloring algorithm.

---

## Project Structure

```
scheduleflow/
├── backend/                    Java Spring Boot (port 8080)
│   ├── pom.xml
│   └── src/main/java/com/scheduleflow/
│       ├── ScheduleFlowApplication.java
│       ├── controller/
│       │   └── TimetableController.java      POST /api/generate
│       ├── service/
│       │   └── SchedulerService.java         ★ Core Algorithm
│       └── dto/
│           ├── SectionDTO.java
│           ├── SubjectMappingDTO.java
│           ├── TimetableRequestDTO.java
│           └── TimetableResponseDTO.java
│
└── frontend/                   React + Vite (port 3000)
    ├── package.json
    ├── vite.config.js           Proxies /api → localhost:8080
    ├── index.html
    └── src/
        ├── main.jsx
        ├── App.jsx              Root — page routing + state
        ├── App.module.css
        ├── api/
        │   └── timetableApi.js  fetch() calls to backend
        ├── hooks/
        │   ├── useSections.js   All section CRUD state
        │   └── useConstraints.js Days, periods, teacher caps
        ├── styles/
        │   └── globals.css      CSS variables + resets
        ├── components/
        │   ├── Navbar.jsx / .module.css
        │   ├── Sidebar.jsx / .module.css
        │   ├── SectionPanel.jsx / .module.css
        │   ├── TimetableGrid.jsx / .module.css
        │   └── UI.jsx / UI.module.css        Shared primitives
        └── pages/
            ├── SetupPage.jsx / .module.css
            ├── ConstraintsPage.jsx / .module.css
            ├── ReviewPage.jsx / .module.css
            └── ResultPage.jsx / .module.css
```

---

## Algorithm — DSatur Graph Coloring

Located in `SchedulerService.java`.

### How it works

1. **Model as a graph**
   - Every lecture (subject + teacher + section × N times/week) = a **node**
   - Two nodes share an **edge** if they conflict:
     - Same **teacher** (teacher clash)
     - Same **section** (section clash — two subjects at same time)

2. **Graph coloring**
   - Each "color" = a `(day, period)` time slot
   - Goal: assign a color to every node so no two adjacent nodes share a color
   - Uses **DSatur heuristic**:
     - Always pick the uncolored node with the **highest saturation degree** (most distinctly-colored neighbors)
     - Ties broken by largest degree
     - Near-optimal without backtracking — O(N² log N)

3. **Gap minimization**
   - Slot ordering prefers days that are **already partially filled** (packing strategy)
   - Within a day, earlier periods are preferred
   - Result: lectures cluster together, free periods pushed to end of day

4. **Constraints enforced**
   - ✅ No two sections at same `(day, period)` for same teacher
   - ✅ No two subjects at same `(day, period)` within one section
   - ✅ Per-teacher weekly lecture cap (`teacherMaxLectures`)
   - ✅ Total lectures per section ≤ `daysPerWeek × periodsPerDay`
   - ⚠️ Violations produce warnings (not errors) — partial schedule returned

### API

```
POST /api/generate
Content-Type: application/json

{
  "sections": [
    {
      "id": "sec_1",
      "name": "A",
      "capacity": 78,
      "subjects": ["Maths", "English", "Science"],
      "teachers": ["Mr.A", "Mr.B", "Mr.C"],
      "mappings": [
        { "subject": "Maths",   "teacher": "Mr.B", "lecturesPerWeek": 8 },
        { "subject": "English", "teacher": "Mr.A", "lecturesPerWeek": 4 },
        { "subject": "Science", "teacher": "Mr.C", "lecturesPerWeek": 6 }
      ]
    }
  ],
  "daysPerWeek": 5,
  "periodsPerDay": 6,
  "teacherMaxLectures": {
    "Mr.A": 30,
    "Mr.B": 30,
    "Mr.C": 30
  }
}
```

Response:
```json
{
  "timetable": {
    "sec_1": {
      "Monday":    [{ "subject": "Maths", "teacher": "Mr.B", "free": false }, ...],
      "Tuesday":   [...],
      ...
    }
  },
  "warnings": [],
  "stats": {
    "totalSections": 1,
    "totalScheduledLectures": 18,
    "totalFreePeriods": 12,
    "warningCount": 0,
    "teacherLoadMap": { "Mr.A": 4, "Mr.B": 8, "Mr.C": 6 }
  }
}
```

---

## Running Locally

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 18+

### Backend

```bash
cd backend
mvn spring-boot:run
# API running at http://localhost:8080
# Health check: http://localhost:8080/api/health
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# UI running at http://localhost:3000
```

The Vite dev server proxies all `/api/*` requests to `http://localhost:8080`.

---

## Features

| Feature | Details |
|---|---|
| Multiple sections | Add A, B, C… each with their own subjects & teachers |
| Subject mapping | Assign teacher + lectures/week per subject per section |
| Global constraints | Days/week, periods/day, teacher weekly caps |
| Clash-free output | DSatur guarantees no teacher or section clashes |
| Gap minimization | Greedy slot ordering packs lectures, minimizes gaps |
| Warnings | Over-capacity or teacher-cap violations flagged in UI |
| Teacher load view | Bar chart of each teacher's weekly lecture count |
| Section filter | View timetable for one section at a time |
| CSV download | Export full timetable as `.csv` |
| Review page | Summary of all sections before generation |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite, CSS Modules |
| Backend | Java 21, Spring Boot 3.2 |
| Algorithm | DSatur Graph Coloring (custom Java implementation) |
| API | REST JSON (no DB — stateless) |
| Fonts | Syne (display) + DM Sans (body) via Google Fonts |
