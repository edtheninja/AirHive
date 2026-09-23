# ✈️ AirHive

### Airline Operations & Management System

AirHive is a full-stack airline operations management system designed to provide a centralized platform for managing flights, aircraft, airports, routes, analytics, notifications, and real-time operational updates.

The project combines a modern React frontend with a Spring Boot backend, PostgreSQL for persistent storage, Redis for caching, WebSockets for real-time communication, JWT-based authentication, role-based access control, and operational analytics.

---

## 📌 Table of Contents

* [Overview](#-overview)
* [Features](#-features)
* [Architecture](#-architecture)
* [Technology Stack](#-technology-stack)
* [Project Structure](#-project-structure)
* [Prerequisites](#-prerequisites)
* [Getting Started](#-getting-started)
* [Database Setup](#-database-setup)
* [Redis Setup](#-redis-setup)
* [Running the Application](#-running-the-application)
* [API Documentation](#-api-documentation)
* [WebSocket Real-Time Updates](#-websocket-real-time-updates)
* [Caching](#-caching)
* [Analytics](#-analytics)
* [Testing](#-testing)
* [Frontend Development](#-frontend-development)
* [Backend Development](#-backend-development)
* [Environment Configuration](#-environment-configuration)
* [Security](#-security)
* [Git Workflow](#-git-workflow)
* [Current Project Status](#-current-project-status)
* [Roadmap](#-roadmap)
* [Useful Commands](#-useful-commands)
* [Troubleshooting](#-troubleshooting)
* [Live Application](#-live-application)
* [Project History](#-project-history)
* [Design Philosophy](#-design-philosophy)
* [License](#-license)
* [Development](#-development)

---

# 🌐 Overview

AirHive is built as a modular airline management platform.

The system currently provides functionality for:

* Airport management
* Aircraft type management
* Aircraft management
* Route management
* Flight management
* Flight validation
* Aircraft scheduling conflict detection
* REST API communication
* Redis caching
* Real-time flight updates using WebSockets
* Real-time notifications
* Authentication and authorization
* JWT-based security
* Role-based access control
* Advanced flight operations
* Flight history
* Operational analytics
* Historical flight analytics
* Environment-based runtime configuration
* Application health monitoring
* Liveness and readiness probes
* Automated backend testing

The project is being developed with an emphasis on:

* Clean architecture
* Separation of concerns
* Validation
* Performance
* Real-time communication
* Maintainability
* Scalable API design
* Security
* Observability
* Modern user experience
* Production-oriented configuration

---

# ✨ Features

## 🛫 Flight Management

Manage airline flights through the backend REST API and frontend operations interface.

Supported operations include:

* Create flights
* View flights
* View individual flights
* Update flights
* Delete flights
* Flight status management
* Aircraft assignment
* Route assignment
* Departure/arrival airport assignment
* Scheduled departure and arrival times
* Flight history
* Advanced filtering
* Flight detail views
* Delay and cancellation reasons
* Operational actions

The system also performs business validation before accepting flight operations.

---

## ✈️ Aircraft Management

AirHive maintains aircraft information including:

* Aircraft registration
* Aircraft type
* Aircraft status
* Aircraft assignments

Duplicate aircraft registration numbers are prevented through backend validation.

---

## 🛩️ Aircraft Type Management

Aircraft types can be managed independently from individual aircraft.

Examples include:

* Boeing 737-800
* Airbus A320-200

This allows aircraft to reference reusable aircraft type definitions.

---

## 🌍 Airport Management

Airport records include information such as:

* Airport name
* IATA code
* ICAO code
* Location information

The backend validates duplicate airport identifiers.

---

## 🗺️ Route Management

Routes connect departure and arrival airports and provide the foundation for flight scheduling.

Flight records can reference an existing route while maintaining their airport relationships.

---

## 🧠 Business Validation

AirHive includes backend validation for operational consistency.

Examples include:

* Duplicate flight validation
* Duplicate aircraft registration validation
* Duplicate airport identifier validation
* Aircraft existence validation
* Airport existence validation
* Route existence validation
* Aircraft scheduling conflict detection
* DTO validation
* Flight status validation
* User lifecycle validation
* Role and permission validation

---

## 🔐 Authentication & Authorization

AirHive includes backend authentication and authorization infrastructure.

Current capabilities include:

* User authentication
* JWT-based authentication
* Role-based access control
* Protected API endpoints
* Password security
* Security exception handling
* User lifecycle validation
* WebSocket security foundation

---

## 🔔 Notification System

The platform includes real-time operational notifications.

Current functionality includes:

* Notification creation
* Real-time notification delivery
* Read/unread state
* Notification clearing
* Flight-related notifications
* Navigation from notifications to flight details

---

## 📊 Operational Analytics

AirHive includes an analytics backend for operational monitoring.

Current analytics include:

* Operations overview
* Flight status distribution
* Delayed flight count
* Cancelled flight count
* Airborne flight count
* Aircraft utilization
* Aircraft status distribution
* Airport activity
* Route activity
* Historical flight activity
* Historical delayed flight counts
* Historical cancelled flight counts

Analytics are exposed through dedicated DTOs and service-layer processing.

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
              │                │                │
              └────────┬───────┘                │
                       ▼                        │
                PostgreSQL                     │
                       ▲                        │
                       │                        │
                 Redis Cache             Real-Time Events
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
     ├── Security
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

| Technology    | Purpose                   |
| ------------- | ------------------------- |
| React         | UI framework              |
| TypeScript    | Type-safe development     |
| Vite          | Development/build tooling |
| Tailwind CSS  | Styling                   |
| Framer Motion | UI animations             |
| Lucide        | Icons                     |
| STOMP.js      | WebSocket client          |

## Backend

| Technology           | Purpose                          |
| -------------------- | -------------------------------- |
| Java 26              | Backend language                 |
| Spring Boot 4.1.0    | Application framework            |
| Spring Web           | REST APIs                        |
| Spring Data JPA      | Database access                  |
| Spring Security      | Authentication and authorization |
| Spring WebSocket     | Real-time communication          |
| Spring Cache         | Application caching              |
| Spring Boot Actuator | Health and observability         |
| Maven                | Dependency/build management      |

## Infrastructure

| Technology | Purpose                          |
| ---------- | -------------------------------- |
| PostgreSQL | Primary relational database      |
| Redis      | Caching                          |
| Git        | Version control                  |
| GitHub     | Source control and collaboration |

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
├── .env.example
└── README.md
```

---

# ⚙️ Prerequisites

Before running AirHive, install:

* Java 26
* Maven
* Node.js
* npm
* PostgreSQL
* Redis
* Git

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

Database configuration is supplied through environment variables.

Example:

```text
AIRHIVE_DB_URL=jdbc:postgresql://localhost:5432/airhive
AIRHIVE_DB_USERNAME=airhive_user
AIRHIVE_DB_PASSWORD=your_password
```

The backend uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

`ddl-auto: validate` ensures that Hibernate validates the existing database schema without automatically modifying it.

> Never commit real database credentials or production secrets to Git.

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

Runtime configuration:

```text
AIRHIVE_REDIS_HOST=localhost
AIRHIVE_REDIS_PORT=6379
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

* Spring WebSocket
* STOMP
* STOMP.js

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

* Airports
* Aircraft Types
* Aircraft
* Other frequently accessed backend resources

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

### Flight DTO Cache

Flight DTO caching is currently disabled because the Redis JSON serializer was returning cached values as `LinkedHashMap` instead of `FlightResponseDTO`.

The flight APIs therefore currently load flight data directly from PostgreSQL.

Future work may revisit typed flight cache serialization.

---

# 📊 Analytics

AirHive now includes a backend analytics layer.

The analytics overview provides:

* Total flights
* Delayed flights
* Cancelled flights
* Airborne flights
* Total aircraft
* Active aircraft
* Inactive aircraft
* Maintenance aircraft
* Flight status distribution
* Aircraft utilization
* Airport activity
* Route activity
* Historical activity

Analytics are generated through the backend service layer using existing flight, aircraft, airport, and route data.

The analytics architecture uses dedicated DTOs including:

```text
AnalyticsOverviewResponseDTO
StatusCountDTO
AircraftUtilizationDTO
ActivityCountDTO
HistoricalActivityDTO
```

Analytics were integrated into the backend and verified through the automated test suite.

---

# 🧪 Testing

The backend contains tests covering:

* Controllers
* Services
* Repositories
* Validation
* Exception handling
* Redis behavior
* WebSocket publishing
* Flight business rules
* Authentication
* Authorization
* Notifications
* Analytics
* Regression scenarios

Run all backend tests:

```bash
cd backend

./mvnw test
```

### Current Test Checkpoint

```text
Tests: 313
Failures: 0
Errors: 0
Skipped: 0
```

This represents the current verified backend regression checkpoint.

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
│   │
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

Business rules are implemented primarily within the service layer.

---

# 🔐 Environment Configuration

AirHive separates source-code configuration from environment-specific runtime configuration.

The root `.env.example` documents the variables required by both the frontend and backend.

Actual secrets and deployment-specific values must be provided by the runtime environment and must not be committed to Git.

## Frontend Variables

| Variable            | Purpose                    | Example                     |
| ------------------- | -------------------------- | --------------------------- |
| `VITE_API_BASE_URL` | Backend REST API URL       | `http://localhost:8080/api` |
| `VITE_WS_URL`       | Backend WebSocket endpoint | `ws://localhost:8080/ws`    |

## Backend Variables

### Database

| Variable              | Purpose                        |
| --------------------- | ------------------------------ |
| `AIRHIVE_DB_URL`      | PostgreSQL JDBC connection URL |
| `AIRHIVE_DB_USERNAME` | PostgreSQL username            |
| `AIRHIVE_DB_PASSWORD` | PostgreSQL password            |

### Redis

| Variable             | Purpose        |
| -------------------- | -------------- |
| `AIRHIVE_REDIS_HOST` | Redis hostname |
| `AIRHIVE_REDIS_PORT` | Redis port     |

### Security

| Variable             | Purpose                            |
| -------------------- | ---------------------------------- |
| `AIRHIVE_JWT_SECRET` | Secret used for JWT authentication |

`AIRHIVE_JWT_SECRET` must be supplied by the runtime environment.

A secure production secret must be used in deployed environments.

### Cross-Origin Configuration

| Variable                     | Purpose                                        |
| ---------------------------- | ---------------------------------------------- |
| `AIRHIVE_CORS_ORIGINS`       | Allowed frontend origins for REST API requests |
| `AIRHIVE_WS_ALLOWED_ORIGINS` | Allowed origins for WebSocket connections      |

Production deployments should replace local development origins with the actual deployed frontend origin.

## Production Configuration

Production environments should provide backend runtime variables through the deployment platform's environment/secrets configuration rather than committing secrets to the repository.

The backend currently uses:

* `ddl-auto: validate`
* SQL statement logging disabled
* Configurable PostgreSQL connection settings
* Configurable Redis connection settings
* Externally supplied JWT secret
* Configurable REST API CORS origins
* Configurable WebSocket origins
* Configurable WebSocket runtime settings

---

# ❤️ Application Health & Observability

AirHive now includes Spring Boot Actuator health monitoring as part of the deployment and observability work.

## Health Endpoint

```text
/actuator/health
```

## Liveness Probe

```text
/actuator/health/liveness
```

## Readiness Probe

```text
/actuator/health/readiness
```

The application health configuration exposes liveness and readiness probes for deployment and runtime monitoring.

The health endpoints have been verified successfully with:

```text
Status: UP
```

for:

* Overall application health
* Liveness
* Readiness

## Application Information

Application information is exposed through the configured Actuator information endpoint.

The current application metadata includes:

```text
Name: AirHive Backend
Description: Airline Management System backend
Version: 1.0.0
```

These capabilities provide the foundation for future deployment monitoring, container orchestration, and production observability.

---

# 🔐 Security

AirHive includes a production-oriented Spring Security foundation.

Current security capabilities include:

* Authentication
* JWT-based security
* Role-based access control
* Protected API endpoints
* User lifecycle validation
* Password security
* Security exception handling
* WebSocket security foundation
* Configurable security-related runtime settings

Authentication and authorization are integrated into the backend and frontend rather than being treated as a future-only feature.

A final production security review remains part of the finalization process.

---

# 🧑‍💻 Git Workflow

Pull the latest changes:

```bash
git pull origin main
```

Run frontend validation:

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

| Prefix      | Usage                      |
| ----------- | -------------------------- |
| `feat:`     | New feature                |
| `fix:`      | Bug fix                    |
| `test:`     | Tests                      |
| `refactor:` | Code restructuring         |
| `style:`    | Formatting/UI-only changes |
| `docs:`     | Documentation              |
| `chore:`    | Maintenance                |

Examples:

```text
feat: integrate flight websocket updates

test: improve flight service validation coverage

docs: update project README
```

---

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
| Security Hardening Foundation   | ✅ Complete              |
| Notification System             | ✅ Complete              |
| Real-Time Notification Delivery | ✅ Complete              |
| Notification Read/Unread UX     | ✅ Complete              |
| Notification Clearing           | ✅ Complete              |
| Flight Notification Navigation  | ✅ Complete              |
| Flight Detail View              | ✅ Complete              |
| Advanced Flight Operations      | ✅ Complete              |
| Flight History                  | ✅ Complete              |
| Analytics Backend               | ✅ Complete              |
| Analytics Overview              | ✅ Complete              |
| Flight Status Analytics         | ✅ Complete              |
| Airport Activity Analytics      | ✅ Complete              |
| Aircraft Utilization Analytics  | ✅ Complete              |
| Route Activity Analytics        | ✅ Complete              |
| Historical Flight Analytics     | ✅ Complete              |
| Backend Regression Suite        | ✅ 313 Tests Passing     |
| Environment Externalization     | ✅ Complete              |
| Database Runtime Configuration  | ✅ Complete              |
| Redis Runtime Configuration     | ✅ Complete              |
| JWT Runtime Configuration       | ✅ Complete              |
| CORS Configuration              | ✅ Complete              |
| WebSocket Origin Configuration  | ✅ Complete              |
| SQL Logging Reduction           | ✅ Complete              |
| Actuator Health Endpoint        | ✅ Complete              |
| Liveness Probe                  | ✅ Complete              |
| Readiness Probe                 | ✅ Complete              |
| Application Info Endpoint       | ✅ Complete              |
| UI/Design System Polish         | 🔄 In Progress          |
| Deployment Configuration        | 🔄 In Progress          |
| Production Runtime Verification | 🔄 In Progress          |
| Docker                          | 🔜 Planned              |
| CI/CD                           | 🔜 Planned              |
| Application Monitoring          | 🔜 Planned              |
| Logging & Tracing               | 🔜 Planned              |
| Production Database Strategy    | 🔜 Planned              |
| Backup & Recovery               | 🔜 Planned              |
| Redis Production Configuration  | 🔜 Planned              |
| AI Features                     | 🔮 Future Phase         |
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

---

## Phase 2 — Database

* [x] PostgreSQL integration
* [x] Airport schema
* [x] Aircraft type schema
* [x] Aircraft schema
* [x] Route schema
* [x] Flight schema
* [x] User and role schema
* [x] Notification schema

---

## Phase 3 — REST APIs

* [x] Airport APIs
* [x] Aircraft Type APIs
* [x] Aircraft APIs
* [x] Route APIs
* [x] Flight APIs
* [x] Authentication APIs
* [x] User management APIs
* [x] Notification APIs

---

## Phase 4 — Business Logic

* [x] Request validation
* [x] Duplicate detection
* [x] Resource validation
* [x] Aircraft scheduling conflict detection
* [x] Exception handling
* [x] Flight status validation
* [x] User lifecycle validation
* [x] Role and permission validation

---

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

---

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

---

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

> Flight DTO caching is currently disabled because the Redis JSON serializer was returning cached values as `LinkedHashMap` instead of `FlightResponseDTO`. Flight APIs currently load flight data directly from PostgreSQL.

---

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

---

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

---

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

---

## Phase 11 — Advanced Flight Operations

* [x] Dynamic flight detail route
* [x] Flight detail information view
* [x] Flight schedule information
* [x] Aircraft information display
* [x] Flight status display
* [x] Flight status update from detail page
* [x] Flight activity timeline
* [x] Delay and cancellation reasons
* [x] Aircraft and route detail links
* [x] Operational action permissions
* [x] Advanced flight filtering
* [x] Flight history

---

## Phase 12 — Analytics

**Status: ✅ Complete**

* [x] Operations dashboard analytics
* [x] Flight status distribution
* [x] Delay analytics
* [x] Airport activity analytics
* [x] Aircraft utilization analytics
* [x] Route activity analytics
* [x] Historical reporting
* [x] Analytics backend integration
* [x] Analytics DTOs
* [x] Analytics service layer
* [x] Analytics regression testing

The analytics backend was integrated into the existing AirHive architecture without introducing a separate analytics data store.

Current analytics are derived from the existing flight, aircraft, airport, and route data.

---

## Phase 13 — AI Features

**Status: 🔮 Future Phase / Postponed**

AI development is intentionally postponed.

The project will first complete deployment, observability, production configuration, and final stabilization.

Planned future capabilities:

* [ ] AI-assisted operational insights
* [ ] Delay prediction
* [ ] Flight disruption analysis
* [ ] Natural-language operations search
* [ ] AI recommendations
* [ ] AI monitoring and evaluation

---

## Phase 14 — Deployment and Observability

**Status: 🔄 Current Development Phase**

### Runtime Configuration

* [x] Externalize database configuration
* [x] Externalize Redis configuration
* [x] Externalize JWT secret
* [x] Externalize REST API CORS configuration
* [x] Externalize WebSocket origin configuration
* [x] Externalize frontend API URL
* [x] Externalize frontend WebSocket URL

### Production Configuration

* [x] Disable automatic Hibernate schema modification
* [x] Configure `ddl-auto: validate`
* [x] Disable SQL statement logging
* [x] Reduce unnecessary security logging
* [x] Make WebSocket configuration configurable
* [x] Make CORS configuration configurable
* [x] Document runtime environment variables

### Application Health

* [x] Spring Boot Actuator integration
* [x] Health endpoint
* [x] Liveness probe
* [x] Readiness probe
* [x] Health group verification
* [x] Application information endpoint

Verified endpoints:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

Current health verification:

```text
Overall Health: UP
Liveness:       UP
Readiness:      UP
```

### Remaining Deployment Work

* [ ] Docker configuration
* [ ] Production environment configuration
* [ ] CI/CD pipeline
* [ ] Automated deployment
* [ ] Application monitoring
* [ ] Logging and tracing
* [ ] Production database strategy
* [ ] Backup and recovery strategy
* [ ] Redis production configuration
* [ ] Production runtime verification

---

## Phase 15 — Finalization

* [ ] End-to-end QA
* [ ] Performance testing
* [ ] Final security review
* [ ] API documentation
* [ ] Developer documentation
* [ ] Deployment documentation
* [ ] Final project report
* [ ] Final presentation

---

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

## Actuator Health

```bash
curl -s http://localhost:8080/actuator/health | jq

curl -s http://localhost:8080/actuator/health/readiness | jq

curl -s http://localhost:8080/actuator/health/liveness | jq
```

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

Then verify the environment variables:

```text
AIRHIVE_DB_URL
AIRHIVE_DB_USERNAME
AIRHIVE_DB_PASSWORD
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

Also verify:

```text
AIRHIVE_REDIS_HOST
AIRHIVE_REDIS_PORT
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

<summary><strong>JWT configuration error</strong></summary>

Verify that the JWT secret is supplied:

```text
AIRHIVE_JWT_SECRET
```

The backend requires a runtime JWT secret.

Never commit the production JWT secret to Git.

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

Also verify:

```text
AIRHIVE_WS_ALLOWED_ORIGINS
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

<details>

<summary><strong>Actuator health endpoint returns 401</strong></summary>

Actuator endpoints may be protected by the application's security configuration.

When authentication is required, provide a valid bearer token when querying the endpoint.

Example:

```bash
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/actuator/health
```

</details>

---

# 🌎 Live Application

Frontend deployment:

https://skyward-zenith-ops.lovable.app

The deployed frontend represents the current user-facing airline operations experience.

The local development environment remains the primary environment for backend, API, database, Redis, WebSocket, analytics, and deployment configuration development.

---

# 📚 Project History

AirHive started as a frontend-focused airline operations interface and was progressively expanded into a full-stack airline management system.

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
Business Validation
       ↓
Automated Backend Testing
       ↓
Authentication & RBAC
       ↓
Redis Caching
       ↓
WebSocket Real-Time Operations
       ↓
Real-Time Notifications
       ↓
Advanced Flight Operations
       ↓
Analytics Backend
       ↓
Environment Externalization
       ↓
Production Configuration Hardening
       ↓
Actuator Health & Readiness Monitoring
       ↓
Deployment & Observability
```

The current development focus is **Phase 14 — Deployment and Observability**.

AI capabilities have deliberately been moved to a future phase so that the core airline management platform can first reach a stable, deployable, and observable state.

---

# 🎯 Design Philosophy

### 1. Separation of Concerns

Frontend, API, business logic, persistence, caching, security, and real-time communication are kept as separate layers.

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

### 7. Production-Oriented Configuration

Environment-specific values such as database credentials, Redis settings, JWT secrets, CORS origins, and WebSocket origins are externalized rather than hard-coded into the application.

### 8. Operational Observability

Spring Boot Actuator health, liveness, readiness, and application information endpoints provide a foundation for monitoring the application during deployment and production operation.

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
Spring Security
Spring Boot Actuator
Maven
Vite
```

---

<p align="center">

✈️ <strong>AirHive</strong><br>

<em>Modern airline operations, connected in real time.</em>

</p>
