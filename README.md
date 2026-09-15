# ✈️ AirHive

### Airline Operations & Management System

AirHive is a full-stack airline operations management system designed to provide a centralized platform for managing flights, aircraft, airports, routes, and real-time operational updates.

The project combines a modern React frontend with a Spring Boot backend, PostgreSQL for persistent storage, Redis for caching, and WebSockets for real-time flight updates.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Database Setup](#-database-setup)
- [Redis Setup](#-redis-setup)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [WebSocket Real-Time Updates](#-websocket-real-time-updates)
- [Testing](#-testing)
- [Frontend Development](#-frontend-development)
- [Backend Development](#-backend-development)
- [Git Workflow](#-git-workflow)
- [Current Project Status](#-current-project-status)
- [Roadmap](#-roadmap)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

# 🌐 Overview

AirHive is built as a modular airline management platform.

The system currently provides functionality for:

- Airport management
- Aircraft type management
- Aircraft management
- Route management
- Flight management
- Flight validation
- Aircraft scheduling conflict detection
- REST API communication
- Redis caching
- Real-time flight updates using WebSockets
- Frontend/backend integration
- Automated backend testing

The project is being developed with an emphasis on:

- Clean architecture
- Separation of concerns
- Validation
- Performance
- Real-time communication
- Maintainability
- Scalable API design
- Modern user experience

---

# ✨ Features

## 🛫 Flight Management

Manage airline flights through the backend REST API and frontend operations interface.

Supported operations include:

- Create flights
- View flights
- View individual flights
- Update flights
- Delete flights
- Flight status management
- Aircraft assignment
- Route assignment
- Departure/arrival airport assignment
- Scheduled departure and arrival times

The system also performs business validation before accepting flight operations.

---

## ✈️ Aircraft Management

AirHive maintains aircraft information including:

- Aircraft registration
- Aircraft type
- Aircraft status
- Aircraft assignments

Duplicate aircraft registration numbers are prevented through backend validation.

---

## 🛩️ Aircraft Type Management

Aircraft types can be managed independently from individual aircraft.

Examples include:

- Boeing 737-800
- Airbus A320-200

This allows aircraft to reference reusable aircraft type definitions.

---

## 🌍 Airport Management

Airport records include information such as:

- Airport name
- IATA code
- ICAO code
- Location information

The backend validates duplicate airport identifiers.

---

## 🗺️ Route Management

Routes connect departure and arrival airports and provide the foundation for flight scheduling.

Flight records can reference an existing route while maintaining their airport relationships.

---

## 🧠 Business Validation

AirHive includes backend validation for operational consistency.

Examples include:

- Duplicate flight validation
- Duplicate aircraft registration validation
- Duplicate airport identifier validation
- Aircraft existence validation
- Airport existence validation
- Route existence validation
- Aircraft scheduling conflict detection
- DTO validation

---

# 🏗️ Architecture

AirHive follows a layered backend architecture:

```text
                    ┌─────────────────────┐
                    │   React Frontend    │
                    │ React + TypeScript  │
                    └──────────┬──────────┘
                               │
                    REST API / WebSocket
                               │
             ┌─────────────────▼─────────────────┐
             │          Spring Boot              │
             │             Backend               │
             └─────────────────┬─────────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
        Controllers        Services       WebSocket
              │                │                │
              ▼                ▼                ▼
         DTO / Mapper     Repositories     STOMP Broker
              │                │
              └────────┬───────┘
                       ▼
                PostgreSQL
                       │
                       ▲
                       │
                 Redis Cache
```

### Backend Flow

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
     │
     ├── Validation
     ├── Business Rules
     ├── Cache
     └── WebSocket Events
     │
     ▼
Repository
     │
     ▼
PostgreSQL
```

---

# 🧰 Technology Stack

## Frontend

| Technology | Purpose |
|---|---|
| React | UI framework |
| TypeScript | Type-safe development |
| Vite | Development/build tooling |
| Tailwind CSS | Styling |
| Framer Motion | UI animations |
| Lucide | Icons |
| STOMP.js | WebSocket client |

## Backend

| Technology | Purpose |
|---|---|
| Java 26 | Backend language |
| Spring Boot 4.1.0 | Application framework |
| Spring Web | REST APIs |
| Spring Data JPA | Database access |
| Spring WebSocket | Real-time communication |
| Spring Cache | Application caching |
| Maven | Dependency/build management |

## Infrastructure

| Technology | Purpose |
|---|---|
| PostgreSQL | Primary relational database |
| Redis | Caching |
| Git | Version control |
| GitHub | Source control and collaboration |

---

# 📁 Project Structure

```text
AirHive/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/airhive/backend/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entity/
│   │   │   │       ├── exception/
│   │   │   │       ├── mapper/
│   │   │   │       ├── repository/
│   │   │   │       └── service/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       └── java/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── src/
│   ├── components/
│   ├── lib/
│   │   ├── api/
│   │   └── ams/
│   ├── routes/
│   └── ...
│
├── public/
├── package.json
├── package-lock.json
├── vite.config.ts
├── tailwind.config.ts
└── README.md
```

---

# ⚙️ Prerequisites

Before running AirHive, install:

- Java 26
- Maven
- Node.js
- npm
- PostgreSQL
- Redis
- Git

Verify installations:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
redis-server --version
```

---

# 🚀 Getting Started

## 1. Clone the Repository

```bash
git clone https://github.com/edtheninja/AirHive.git
cd AirHive
```

---

# 🗄️ Database Setup

AirHive uses PostgreSQL as its primary database.

Create the database:

```sql
CREATE DATABASE airhive;
```

Create the application user:

```sql
CREATE USER airhive_user WITH PASSWORD 'your_password';
```

Grant access:

```sql
GRANT ALL PRIVILEGES ON DATABASE airhive TO airhive_user;
```

Connect:

```bash
psql -U airhive_user -d airhive
```

## Database Configuration

Configure:

```text
backend/src/main/resources/application.yml
```

Example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/airhive
    username: airhive_user
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update
```

> Never commit real database credentials to Git.

---

# 🔴 Redis Setup

AirHive uses Redis for caching frequently accessed backend data.

Install on macOS with Homebrew:

```bash
brew install redis
```

Start Redis:

```bash
brew services start redis
```

Verify:

```bash
redis-cli ping
```

Expected:

```text
PONG
```

## Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

The default cache TTL is:

```text
10 minutes
```

---

# ▶️ Running the Application

AirHive requires the frontend and backend to run separately during development.

## Start the Backend

Open Terminal 1:

```bash
cd AirHive/backend
./mvnw clean install
./mvnw spring-boot:run
```

Backend:

```text
http://localhost:8080
```

## Start the Frontend

Open Terminal 2:

```bash
cd AirHive
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

# 🔌 API Documentation

Base URL:

```text
http://localhost:8080/api
```

## Airports

```http
GET    /api/airports
GET    /api/airports/{id}
POST   /api/airports
PUT    /api/airports/{id}
DELETE /api/airports/{id}
```

## Aircraft Types

```http
GET    /api/aircraft-types
GET    /api/aircraft-types/{id}
POST   /api/aircraft-types
PUT    /api/aircraft-types/{id}
DELETE /api/aircraft-types/{id}
```

## Aircraft

```http
GET    /api/aircraft
GET    /api/aircraft/{id}
POST   /api/aircraft
PUT    /api/aircraft/{id}
DELETE /api/aircraft/{id}
```

## Routes

```http
GET    /api/routes
GET    /api/routes/{id}
POST   /api/routes
PUT    /api/routes/{id}
DELETE /api/routes/{id}
```

## Flights

```http
GET    /api/flights
GET    /api/flights/{id}
POST   /api/flights
PUT    /api/flights/{id}
DELETE /api/flights/{id}
```

---

# 🛰️ WebSocket Real-Time Updates

AirHive supports real-time flight updates using:

- Spring WebSocket
- STOMP
- STOMP.js

## WebSocket Endpoint

```text
ws://localhost:8080/ws
```

## Flight Topic

Clients subscribe to:

```text
/topic/flights
```

Events currently supported:

```text
FLIGHT_CREATED
FLIGHT_UPDATED
FLIGHT_DELETED
```

Example:

```json
{
  "eventType": "FLIGHT_UPDATED",
  "flight": {
    "id": 1,
    "flightNumber": "AH101",
    "aircraftId": 2,
    "aircraftRegistration": "VT-AIR01",
    "departureAirportId": 2,
    "departureAirportCode": "DEL",
    "arrivalAirportId": 3,
    "arrivalAirportCode": "BOM",
    "routeId": 2,
    "scheduledDeparture": "2026-08-17T09:00:00",
    "scheduledArrival": "2026-08-17T11:15:00",
    "status": "SCHEDULED"
  }
}
```

The frontend receives these events and updates the live operations dashboard without requiring a page refresh.

---

# ⚡ Caching

Redis caching is implemented for:

- Airports
- Aircraft Types
- Aircraft
- Flights

Cache flow:

```text
Request
   │
   ▼
Redis Cache
   │
   ├── HIT ──► Return cached data
   │
   └── MISS
         │
         ▼
     PostgreSQL
         │
         ▼
     Store in Redis
         │
         ▼
     Return response
```

Mutation operations invalidate relevant cache entries to prevent stale data.

---

# 🧪 Testing

The backend contains tests covering:

- Controllers
- Services
- Repositories
- Validation
- Exception handling
- Redis behavior
- WebSocket publishing
- Flight business rules

Run all backend tests:

```bash
cd backend
./mvnw test
```

At the current development checkpoint:

```text
Tests: 172
Failures: 0
Errors: 0
Skipped: 0
```

> The test count may increase as development continues.

## Frontend Type Checking

```bash
npx tsc --noEmit
```

A successful run should return without TypeScript errors.

---

# 🛠️ Frontend Development

Frontend API communication is separated from UI components.

Important areas:

```text
src/
├── components/
├── lib/
│   ├── api/
│   │   ├── aircraft.ts
│   │   ├── flights.ts
│   │   ├── websocket.ts
│   │   └── ...
│   └── ams/
│       └── live-ops.tsx
└── routes/
    ├── aircraft.tsx
    ├── flights.tsx
    ├── routes.tsx
    └── ...
```

Useful commands:

```bash
npm install
npm run dev
npm run build
npm run preview
npx tsc --noEmit
```

---

# ☕ Backend Development

The backend follows:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

DTOs and mappers prevent direct exposure of persistence entities through API responses.

```text
Entity
  ↓
Mapper
  ↓
Response DTO
  ↓
REST API
```

---

# 🔐 Security

Spring Security infrastructure is present in the backend.

The current configuration permits the existing development API endpoints for development and testing.

Authentication and role-based authorization are planned for a later phase.

> Production deployment must not use the current permissive development security configuration.

---

# 🧑‍💻 Git Workflow

Pull the latest changes:

```bash
git pull origin main
```

Run validation:

```bash
npx tsc --noEmit
```

Run backend tests:

```bash
cd backend
./mvnw test
```

Check formatting:

```bash
git diff --check
```

Check Git status:

```bash
git status
```

Commit changes:

```bash
git add .
git commit -m "type: description"
```

Push:

```bash
git push origin main
```

## Commit Convention

| Prefix | Usage |
|---|---|
| `feat:` | New feature |
| `fix:` | Bug fix |
| `test:` | Tests |
| `refactor:` | Code restructuring |
| `style:` | Formatting/UI-only changes |
| `docs:` | Documentation |
| `chore:` | Maintenance |

Examples:

```text
feat: integrate flight websocket updates
test: improve flight service validation coverage
docs: update project README
```

# 📊 Current Project Status

| Area                            | Status                  |
| ------------------------------- | ----------------------- |
| Core Architecture               | ✅ Complete              |
| PostgreSQL Database             | ✅ Complete              |
| Airport APIs                    | ✅ Complete              |
| Aircraft Type APIs              | ✅ Complete              |
| Aircraft APIs                   | ✅ Complete              |
| Route APIs                      | ✅ Complete              |
| Flight APIs                     | ✅ Complete              |
| Business Validation             | ✅ Complete              |
| Frontend API Integration        | ✅ Complete              |
| Backend Testing                 | ✅ Complete              |
| Redis Integration               | ✅ Complete              |
| Airport Caching                 | ✅ Complete              |
| Aircraft Type Caching           | ✅ Complete              |
| Aircraft Caching                | ✅ Complete              |
| Flight DTO Caching              | ⚠️ Temporarily Disabled |
| Cache Invalidation              | ✅ Complete              |
| TTL Verification                | ✅ Complete              |
| Flight WebSockets               | ✅ Complete              |
| Frontend Live Flight Updates    | ✅ Complete              |
| Authentication                  | ✅ Complete              |
| JWT Security                    | ✅ Complete              |
| Role-Based Access Control       | ✅ Complete              |
| Protected API Endpoints         | ✅ Complete              |
| Security Hardening              | ✅ Complete              |
| Notification System             | ✅ Complete              |
| Real-Time Notification Delivery | ✅ Complete              |
| Notification Read/Unread UX     | ✅ Complete              |
| Notification Clearing           | ✅ Complete              |
| Flight Notification Navigation  | ✅ Complete              |
| Flight Detail View              | ✅ Complete              |
| UI/Design System Polish         | 🔄 In Progress          |
| Advanced Flight Operations      | 🔄 In Progress          |
| Analytics Dashboard             | 🔜 Planned              |
| AI Features                     | 🔜 Planned              |
| Docker                          | 🔜 Planned              |
| CI/CD                           | 🔜 Planned              |
| Production Deployment           | 🔜 Planned              |
| Final QA & Documentation        | 🔜 Planned              |

---

# 🗺️ Roadmap

## Phase 1 — Core Architecture

* [x] Project structure
* [x] Frontend architecture
* [x] Backend architecture
* [x] Database model
* [x] API client structure
* [x] Shared frontend components

## Phase 2 — Database

* [x] PostgreSQL integration
* [x] Airport schema
* [x] Aircraft type schema
* [x] Aircraft schema
* [x] Route schema
* [x] Flight schema
* [x] User and role schema
* [x] Notification schema

## Phase 3 — REST APIs

* [x] Airport APIs
* [x] Aircraft Type APIs
* [x] Aircraft APIs
* [x] Route APIs
* [x] Flight APIs
* [x] Authentication APIs
* [x] User management APIs
* [x] Notification APIs

## Phase 4 — Business Logic

* [x] Request validation
* [x] Duplicate detection
* [x] Resource validation
* [x] Aircraft scheduling conflict detection
* [x] Exception handling
* [x] Flight status validation
* [x] User lifecycle validation
* [x] Role and permission validation

## Phase 5 — Frontend Integration

* [x] API client layer
* [x] Flight integration
* [x] Aircraft integration
* [x] Route integration
* [x] Live operations integration
* [x] Authentication flow
* [x] Session handling
* [x] Protected frontend routes
* [x] Notification panel
* [x] Flight detail page
* [x] Flight detail navigation

## Phase 6 — Testing

* [x] Service tests
* [x] Controller tests
* [x] Repository tests
* [x] Validation tests
* [x] Authentication tests
* [x] RBAC tests
* [x] WebSocket tests
* [x] Notification tests
* [x] Regression testing

## Phase 7 — Redis and Caching

* [x] Redis integration
* [x] Airport caching
* [x] Aircraft type caching
* [x] Aircraft caching
* [x] Cache invalidation
* [x] TTL verification
* [x] Redis health verification
* [ ] Correctly typed flight DTO caching
* [ ] Flight cache serialization review

> Flight DTO caching is currently disabled because the Redis JSON serializer was returning cached values as `LinkedHashMap` instead of `FlightResponseDTO`. The flight APIs currently load flight data directly from PostgreSQL.

## Phase 8 — Real-Time Operations

* [x] Spring WebSocket
* [x] STOMP broker
* [x] Flight event DTO
* [x] Flight created events
* [x] Flight updated events
* [x] Flight deleted events
* [x] Frontend STOMP client
* [x] Live flight dashboard updates
* [x] Real-time notification delivery
* [x] Notification read/unread state
* [x] Notification clearing
* [x] Flight notification navigation

## Phase 9 — UI and Design System

* [x] macOS-inspired application shell
* [x] Shared design primitives
* [x] Glass-style cards
* [x] Status pills
* [x] Responsive layout foundation
* [x] Loading states
* [x] Empty states
* [x] Error states
* [x] Accessibility improvements
* [ ] Final design system refinement
* [ ] Advanced dashboard interactions
* [ ] Full responsive QA
* [ ] Visual consistency review

## Phase 10 — Security

* [x] Authentication
* [x] JWT/session strategy
* [x] Role-based access control
* [x] Protected endpoints
* [x] User lifecycle management
* [x] Password security
* [x] Security exception handling
* [x] WebSocket security foundation
* [x] Production security configuration foundation
* [ ] Final security audit

## Phase 11 — Advanced Flight Operations

* [x] Dynamic flight detail route
* [x] Flight detail information view
* [x] Flight schedule information
* [x] Aircraft information display
* [x] Flight status display
* [ ] Flight status update from detail page
* [ ] Flight activity timeline
* [ ] Delay and cancellation reasons
* [ ] Aircraft and route detail links
* [ ] Operational action permissions
* [ ] Advanced flight filtering
* [ ] Flight history

## Phase 12 — Analytics

* [ ] Operations dashboard analytics
* [ ] Flight status distribution
* [ ] Delay analytics
* [ ] Airport activity analytics
* [ ] Aircraft utilization analytics
* [ ] Route performance analytics
* [ ] Historical reporting
* [ ] Exportable reports

## Phase 13 — AI Features

* [ ] AI-assisted operational insights
* [ ] Delay prediction
* [ ] Flight disruption analysis
* [ ] Natural-language operations search
* [ ] AI recommendations
* [ ] AI monitoring and evaluation

## Phase 14 — Deployment and Observability

* [ ] Docker configuration
* [ ] Production environment configuration
* [ ] CI/CD pipeline
* [ ] Automated deployment
* [ ] Application monitoring
* [ ] Logging and tracing
* [ ] Production database strategy
* [ ] Backup and recovery strategy
* [ ] Redis production configuration

## Phase 15 — Finalization

* [ ] End-to-end QA
* [ ] Performance testing
* [ ] Security review
* [ ] API documentation
* [ ] Developer documentation
* [ ] Deployment documentation
* [ ] Final project report
* [ ] Final presentation


# 🧰 Useful Commands

## Frontend

```bash
npm install
npm run dev
npm run build
npm run preview
npx tsc --noEmit
```

## Backend

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
./mvnw test
./mvnw clean
```

## PostgreSQL

```bash
psql --version
psql -U airhive_user -d airhive
```

## Redis

```bash
redis-cli ping
redis-cli KEYS '*'
redis-cli FLUSHDB
```

> Use `FLUSHDB` carefully because it removes all keys from the currently selected Redis database.

---

# 🐛 Troubleshooting

<details>
<summary><strong>Backend cannot connect to PostgreSQL</strong></summary>

Check PostgreSQL:

```bash
brew services list
```

Test the database:

```bash
psql -U airhive_user -d airhive
```

Then verify the credentials and URL in:

```text
backend/src/main/resources/application.yml
```

</details>

<details>
<summary><strong>Redis connection fails</strong></summary>

Check:

```bash
redis-cli ping
```

Expected:

```text
PONG
```

Start Redis if necessary:

```bash
brew services start redis
```

</details>

<details>
<summary><strong>Port 8080 is already in use</strong></summary>

Find the process:

```bash
lsof -i :8080
```

Stop it if appropriate:

```bash
kill <PID>
```

Then restart Spring Boot.

</details>

<details>
<summary><strong>WebSocket does not connect</strong></summary>

Verify the backend is running:

```text
http://localhost:8080
```

WebSocket endpoint:

```text
ws://localhost:8080/ws
```

Flight topic:

```text
/topic/flights
```

Browser debugging:

```text
Developer Tools → Network → WS
```

</details>

<details>
<summary><strong>Redis returns unexpected cached data</strong></summary>

Clear the development Redis database:

```bash
redis-cli FLUSHDB
```

Restart the backend if necessary.

</details>

---

# 🌎 Live Application

Frontend deployment:

https://skyward-zenith-ops.lovable.app

The deployed frontend represents the current user-facing airline operations experience.

The local development environment remains the primary environment for backend, API, database, Redis, and WebSocket development.

---

# 📚 Project History

AirHive started as a frontend-focused airline operations interface and was subsequently expanded into a full-stack application.

The project evolved through:

```text
Frontend Prototype
       ↓
React AMS Interface
       ↓
Spring Boot Backend
       ↓
PostgreSQL
       ↓
REST API Integration
       ↓
Automated Testing
       ↓
Redis Caching
       ↓
WebSocket Real-Time Operations
```

The current architecture is designed to support future authentication, authorization, deployment, and advanced operational features.

---

# 🎯 Design Philosophy

### 1. Separation of Concerns

Frontend, API, business logic, persistence, caching, and real-time communication are kept as separate layers.

### 2. API-First Development

The backend exposes structured REST APIs consumed by the frontend.

### 3. Validation at the Service Layer

Business rules are enforced independently of the UI.

### 4. Performance Through Caching

Redis reduces unnecessary database reads for frequently requested resources.

### 5. Real-Time Operations

WebSockets allow operational changes to reach connected clients without requiring page refreshes.

### 6. Test-Driven Stabilization

Major backend functionality is supported by automated tests before progressing to subsequent development phases.

---

# 📄 License

This project is currently maintained as an academic/development project.

License terms can be added when the project is formally released.

---

# 👥 Development

## AirHive

**Airline Operations & Management System**

Built with:

```text
React
TypeScript
Spring Boot
Java
PostgreSQL
Redis
WebSockets
STOMP
Maven
Vite
```

---

<p align="center">
  ✈️ <strong>AirHive</strong><br>
  <em>Modern airline operations, connected in real time.</em>
</p>
