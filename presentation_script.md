# ScheduleFlow — 5-Minute Project Presentation Script
> **Natural, Student-Friendly Script for Video Demos, Loom Recordings, and Project Presentations**

---

## ⏱️ Timing & Screen Guide

| Segment | Time | Screen / Action | Focus Area |
|---|---|---|---|
| **1. Hook & Project Overview** | 0:00 – 0:50 | Browser (`localhost:3000`) — Landing Page | Problem statement, DSatur graph algorithm, project goals |
| **2. Quick Access & Login** | 0:50 – 1:05 | Browser — Login Page | Brief mention of Admin Sign-In & JWT token authentication |
| **3. Main Dashboard Hub** | 1:05 – 1:45 | Browser — Dashboard Home | Navigation sidebar, action cards, central feature hub |
| **4. Timetable Builder in Action** | 1:45 – 3:00 | Browser — Constraints → Setup → Review → Results | Interactive step-by-step workflow, conflict-free grid, stats |
| **5. Microservices Architecture** | 3:00 – 4:10 | VS Code — `docker-compose.yml` & Services | 6 Docker containers, Eureka, Gateway, Timetable, Resource & Event Services |
| **6. Code Quality & Technical Highlights** | 4:10 – 4:45 | VS Code — Java files & Tests | Stateless engine, Feign clients, Flyway migrations, JUnit 5 testing |
| **7. Conclusion & Wrap-Up** | 4:45 – 5:00 | Browser — Generated Results Page | Final summary & project closing |

---

## 🎙️ Complete Presentation Script

### ⏱️ 0:00 – 0:50 | SEGMENT 1: Landing Page & Project Introduction

**[SCREEN: Open browser at `http://localhost:3000`. Show the Landing Page, scrolling past the Hero header to the Features section.]**

> **🗣️ Speaker:**
> 
> "Hey everyone! Welcome to my demo of **ScheduleFlow** — a smart, full-stack timetable scheduling and college management platform that I built using **React** for the frontend and **Java Spring Boot microservices** for the backend.
> 
> Let me start with why I built this. If you’ve ever talked to college coordinators, you know that creating a class schedule manually is a total nightmare. People spend weeks messing around with Excel spreadsheets. Teachers get double-booked, classes clash in the same room, subjects get randomly stacked on a single day, and if an event comes up, the whole schedule collapses!
> 
> **ScheduleFlow** solves all of that automatically. Right here on our landing page, you can see what the platform brings to the table: 1-click conflict-free timetables, automated constraint enforcement, and campus event management. 
> 
> Under the hood, it uses an advanced graph algorithm called **DSatur Graph Coloring** to guarantee that no teacher or class section is ever double-booked."

---

### ⏱️ 0:50 – 1:05 | SEGMENT 2: Quick Access & Authentication

**[ACTION: Click the 'Get Started' button on the Landing Page. The app transitions briefly to the Login Page.]**

**[SCREEN: Show the Login form.]**

> **🗣️ Speaker:**
> 
> "Now let's head inside the application! I'll click **Get Started**, which brings us to our login screen. 
> 
> We have secure user authentication built with **Spring Security and JWT tokens**. Users can register a new profile or sign in as an admin. I'll enter my credentials and log in. The backend validates the user, generates a JWT token, and takes us right to the main dashboard."

---

### ⏱️ 1:05 – 1:45 | SEGMENT 3: Main Dashboard Overview & How to Use It

**[ACTION: Click 'Sign In'. The screen loads the Dashboard Home.]**

**[SCREEN: Hover cursor over the welcome card, left sidebar navigation, and action cards.]**

> **🗣️ Speaker:**
> 
> "And here is our main **Dashboard** — the central hub for everything in ScheduleFlow!
> 
> Up top, you get a personalized greeting (`Welcome Admin!`). 
> 
> On the left sidebar, we have our key navigation tools:
> - **Dashboard**: Our main home view.
> - **Current Time Table**: To inspect active, published schedules.
> - **Manage Rooms**: To add and update classrooms, labs, or seminar halls.
> - **Manage Events**: To schedule college events and check their impact on classes.
> - **Find Free Room**: To quickly find empty rooms in real time.
> 
> Right in the middle, we have three primary action cards: **New Time Table**, **Manage Rooms**, and **Manage Events**. Let's jump into **New Time Table** to see how we actually build a schedule!"

---

### ⏱️ 1:45 – 3:00 | SEGMENT 4: Timetable Builder Flow (Setup → Constraints → Review → Results)

**[ACTION: Click 'New Time Table'. The builder opens on the Constraints Page.]**

**[SCREEN: Step 1 — Constraints Page]**

> **🗣️ Speaker:**
> 
> "The timetable generator takes us through four simple steps:
> 
> **Step 1: Constraints Page.** Here, we set global rules for the institution — like 5 days per week, 6 periods per day, and weekly lecture caps per teacher. For instance, if Mr. A can only teach 20 lectures a week, we cap it here, and the backend enforces it strictly."

**[ACTION: Click 'Next' to move to Setup Page.]**

**[SCREEN: Step 2 — Setup Page]**

> **🗣️ Speaker:**
> 
> "**Step 2: Setup Page.** This is where we define class sections like Section A, B, or C. For each section, we map subjects to specific teachers along with weekly period counts — like Maths with Mr. B (8 lectures), English with Mr. A (4 lectures), and Science with Mr. C (6 lectures)."

**[ACTION: Click 'Next' to move to Review Page.]**

**[SCREEN: Step 3 — Review Page]**

> **🗣️ Speaker:**
> 
> "**Step 3: Review Page.** This gives us a complete live summary before generating. It double-checks that period totals don't exceed weekly slot limits or teacher workloads."

**[ACTION: Click 'Generate Timetable'.]**

**[SCREEN: Step 4 — Results Page showing generated grid, teacher load chart, and CSV download button.]**

> **🗣️ Speaker:**
> 
> "**Step 4: Results Page.** I click **Generate**. The frontend sends a single `POST` request to our backend. The **DSatur algorithm** runs instantly, assigns every lecture to an optimal time slot, and renders this clean interactive timetable!
> 
> Zero teacher overlaps, zero section conflicts. You can filter by section, check teacher workload graphs, and export the final schedule as a CSV file with one click."

---

### ⏱️ 3:00 – 4:10 | SEGMENT 5: Backend Microservices & Architecture

**[ACTION: Switch to VS Code. Open `docker-compose.yml` and expand project folders.]**

**[SCREEN: Scroll through `docker-compose.yml`, pointing out each service block.]**

> **🗣️ Speaker:**
> 
> "Now let's take a look under the hood. The system is architected as a production-ready microservice ecosystem running across **6 Docker containers**:
> 
> 1. **PostgreSQL Database**: Shared database running on port 5432 with **Flyway schema migrations** for automated, versioned database tables.
> 2. **Eureka Server**: Spring Cloud Eureka on port 8761, acting as our service registry where every microservice registers on startup.
> 3. **Spring Cloud API Gateway**: Running on port 8082. It routes all client traffic — `/api/rooms/**` goes to the Resource Service, `/api/generate` goes to the Timetable Service, and `/api/events/**` goes to the Event Service.
> 4. **Timetable Service** (Port 8080): The core scheduling brain. It runs the DSatur algorithm, handles JWT security, and manages timetable versioning (`GENERATING → ACTIVE → ARCHIVED`).
> 5. **Resource Service** (Port 8081): Handles room data CRUD, capacities, and room types (`CLASSROOM`, `SEMINAR_HALL`, `AUDITORIUM`).
> 6. **Event Service** (Port 8083): Manages campus events and room reservations. It talks to the Timetable Service using a **Declarative OpenFeign Client** (`TimetableServiceClient`) to automatically calculate affected lectures and reschedule them."

---

### ⏱️ 4:10 – 4:45 | SEGMENT 6: Code Quality & Engineering Highlights

**[SCREEN: Open `SchedulerService.java` and `TimetableServiceClient.java` in VS Code.]**

> **🗣️ Speaker:**
> 
> "Here are three engineering highlights from the codebase that I'm really proud of:
> 
> 1. **Stateless Scheduling Engine**: The schedule generation API is completely stateless. That means we can spin up multiple instances of the Timetable Service behind a load balancer and scale horizontally effortlessly.
> 2. **Declarative Feign Clients**: Instead of writing boilerplate HTTP request code, services communicate cleanly through strongly-typed Feign interfaces.
> 3. **Fail-Fast Principles & Testing**: I implemented strict data validation and unit tests using **JUnit 5** and **Mockito**. If a room or timetable constraint cannot be satisfied, the system fails fast with meaningful exceptions rather than writing corrupt data into PostgreSQL."

---

### ⏱️ 4:45 – 5:00 | SEGMENT 7: Conclusion & Wrap-Up

**[ACTION: Switch back to the Browser showing the finished Results Page.]**

> **🗣️ Speaker:**
> 
> "To wrap things up: **ScheduleFlow** turns weeks of stressful, manual scheduling into a smooth, 1-click automated solution. 
> 
> From graph coloring algorithms and microservices down to an interactive React dashboard, the entire project is built end-to-end.
> 
> Thank you so much for your time! I’d love to answer any questions or walk you through any part of the code."

---

### 📁 Quick Reference of Open Files in VS Code

1. [docker-compose.yml](file:///c:/Users/kinjal/Downloads/scheduleflow_v3/docker-compose.yml) — Docker container overview
2. [application.properties (Gateway)](file:///c:/Users/kinjal/Downloads/scheduleflow_v3/scheduleflow-api-gateway/src/main/resources/application.properties) — Microservice routing
3. `backend/src/main/java/com/scheduleflow/service/SchedulerService.java` — Core DSatur algorithm
4. `scheduleflow-event-service/.../client/TimetableServiceClient.java` — Feign client interface
5. [LandingPage.jsx](file:///c:/Users/kinjal/Downloads/scheduleflow_v3/frontend/src/pages/public/LandingPage.jsx) — Landing Page component
6. [DashboardHome.jsx](file:///c:/Users/kinjal/Downloads/scheduleflow_v3/frontend/src/pages/public/DashboardHome.jsx) — Dashboard component
