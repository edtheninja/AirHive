# ✈️ AirHive

### Airline Operations & Management System

AirHive is a full-stack airline operations management system designed to provide a centralized platform for managing flights, aircraft, airports, routes, passengers, crew, bookings, maintenance, analytics, notifications, and real-time operational updates.

The project combines a modern React frontend with a Spring Boot backend, PostgreSQL for persistent storage, Redis for caching, WebSockets for real-time communication, JWT-based authentication, role-based access control, operational analytics, Docker-based infrastructure, and application health monitoring.

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
- [Docker](#-docker)
- [API Documentation](#-api-documentation)
- [WebSocket Real-Time Updates](#-websocket-real-time-updates)
- [Caching](#-caching)
- [Analytics](#-analytics)
- [Testing](#-testing)
- [Frontend Development](#-frontend-development)
- [Backend Development](#-backend-development)
- [Environment Configuration](#-environment-configuration)
- [Application Health & Observability](#-application-health--observability)
- [Security](#-security)
- [Git Workflow](#-git-workflow)
- [Current Project Status](#-current-project-status)
- [Roadmap](#-roadmap)
- [Useful Commands](#-useful-commands)
- [Troubleshooting](#-troubleshooting)
- [Live Application](#-live-application)
- [Project History](#-project-history)
- [Design Philosophy](#-design-philosophy)
- [License](#-license)
- [Development](#-development)

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
- Passenger management
- Crew management
- Booking management
- Maintenance management
- REST API communication
- Redis caching
- Real-time flight updates using WebSockets
- Real-time notifications
- Authentication and authorization
- JWT-based security
- Role-based access control
- Advanced flight operations
- Flight history
- Operational analytics
- Historical flight analytics
- Environment-based runtime configuration
- Dockerized application infrastructure
- Application health monitoring
- Liveness and readiness probes
- Automated backend testing

The project is being developed with an emphasis on:

- Clean architecture
- Separation of concerns
- Validation
- Performance
- Real-time communication
- Maintainability
- Scalable API design
- Security
- Observability
- Modern user experience
- Production-oriented configuration
- Reproducible development environments

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
- Flight history
- Advanced filtering
- Flight detail views
- Delay and cancellation reasons
- Operational actions

The system also performs business validation before accepting flight operations.

---

## ✈️ Aircraft Management

AirHive maintains aircraft information including:

- Aircraft registration
- Aircraft type
- Aircraft status
- Aircraft assignments
- Maintenance relationships

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

## 👥 Passenger Management

Passenger management provides support for:

- Passenger records
- Passenger information
- Passenger-to-booking relationships
- Passenger manifests
- Flight passenger information

---

## 🧑‍✈️ Crew Management

Crew management provides support for:

- Crew member records
- Crew information
- Crew-to-flight relationships
- Operational crew management

---

## 🎫 Booking Management

The booking system provides:

- Booking records
- Passenger association
- Flight association
- Booking status
- Booking management workflows
- Passenger manifest integration

---

## 🔧 Maintenance Management

Aircraft maintenance functionality includes:

- Maintenance records
- Maintenance type
- Maintenance severity
- Maintenance status
- Maintenance progress
- Aircraft maintenance relationships
- Operational maintenance tracking

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
- Flight status validation
- User lifecycle validation
- Role and permission validation
- Booking validation
- Passenger validation
- Resource existence validation

---

## 🔐 Authentication & Authorization

AirHive includes backend authentication and authorization infrastructure.

Current capabilities include:

- User authentication
- JWT-based authentication
- Role-based access control
- Protected API endpoints
- Password security
- Security exception handling
- WebSocket security foundation
- Runtime security configuration

---

## 🔔 Notification System

The platform includes real-time operational notifications.

Current functionality includes:

- Notification creation
- Real-time notification delivery
- Read/unread state
- Notification clearing
- Flight-related notifications
- Navigation from notifications to flight details

---

## 📊 Operational Analytics

AirHive includes an analytics backend for operational monitoring.

Current analytics include:

- Operations overview
- Flight status distribution
- Delayed flight count
- Cancelled flight count
- Airborne flight count
- Aircraft utilization
- Aircraft status distribution
- Airport activity
- Route activity
- Historical flight activity
- Historical delayed flight counts
- Historical cancelled flight counts

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

### Docker Runtime Architecture

```text
                    ┌─────────────────────┐
                    │   AirHive Frontend  │
                    │      Port 3000      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   AirHive Backend   │
                    │      Port 8080      │
                    └──────────┬──────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
             ┌─────────────┐       ┌─────────────┐
             │  PostgreSQL  │       │    Redis    │
             │    :5432     │       │    :6379    │
             └─────────────┘       └─────────────┘
```

---

# 🧰 Technology Stack

## Frontend

| Technology | Purpose |
|---|---|
| React | UI framework |
| TypeScript | Type-safe development |
| Vite | Development/build tooling |
| TanStack Start | Application framework |
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
| Spring Security | Authentication and authorization |
| Spring WebSocket | Real-time communication |
| Spring Cache | Application caching |
| Spring Boot Actuator | Health and observability |
| Maven | Dependency/build management |

## Infrastructure

| Technology | Purpose |
|---|---|
| PostgreSQL 18 | Primary relational database |
| Redis 8 | Caching |
| Docker | Containerization |
| Docker Compose | Local multi-service orchestration |
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
│   ├── .dockerignore
│   ├── Dockerfile
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
│
├── .dockerignore
├── .env.example
├── Dockerfile
├── docker-compose.yml
├── package.json
├── package-lock.json
├── vite.config.ts
├── tailwind.config.ts
└── README.md
```

---

# ⚙️ Prerequisites

Before running AirHive locally, install:

- Java 26
- Maven
- Node.js
- npm
- PostgreSQL
- Redis
- Docker Desktop
- Git

Verify installations:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
redis-server --version
docker --version
docker compose version
```

Docker is required for the containerized AirHive environment.

---

# 🚀 Getting Started

## 1. Clone the Repository

```bash
git clone https://github.com/edtheninja/AirHive.git

cd AirHive
```

## 2. Configure Environment Variables

Create the local environment file:

```bash
cp .env.example .env
```

Configure the required values in `.env`.

Do not commit `.env`.

---

# 🗄️ Database Setup

AirHive uses PostgreSQL as its primary database.

## Manual Local PostgreSQL Setup

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

## Local Redis Installation

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

```text
AIRHIVE_REDIS_HOST=localhost
AIRHIVE_REDIS_PORT=6379
```

The default airport cache TTL is:

```text
10 minutes
```

---

# ▶️ Running the Application

AirHive supports both traditional local development and a Dockerized development environment.

## Local Development

### Start the Backend

```bash
cd AirHive/backend

./mvnw clean install

./mvnw spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Start the Frontend

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

# 🐳 Docker

AirHive is fully containerized using Docker and Docker Compose.

The Docker environment contains four services:

| Service | Image | Port |
|---|---|---:|
| Frontend | `airhive-frontend:dev` | 3000 |
| Backend | `airhive-backend:dev` | 8080 |
| PostgreSQL | `postgres:18-alpine` | 5432 |
| Redis | `redis:8-alpine` | 6379 |

## Start the Docker Environment

```bash
cp .env.example .env
```

Configure the required environment variables.

Then:

```bash
docker compose up -d --build
```

Check the services:

```bash
docker compose ps
```

Expected services:

```text
airhive-postgres
airhive-redis
airhive-backend
airhive-frontend
```

## Docker Healthchecks

Docker healthchecks are configured for:

- PostgreSQL
- Redis
- Backend

The backend waits for healthy PostgreSQL and Redis services before starting.

The frontend waits for the backend to become healthy before starting.

## Docker Logs

```bash
docker compose logs
docker compose logs -f
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
docker compose logs redis
```

## Stop Docker Services

```bash
docker compose down
```

This stops and removes the containers while preserving the named PostgreSQL and Redis volumes.

## Rebuild Docker Services

```bash
docker compose up -d --build
```

---

# 💾 Persistent Docker Storage

AirHive uses Docker named volumes:

```text
postgres_data
redis_data
```

These preserve PostgreSQL and Redis data across container recreation.

The current Docker environment has been verified to preserve application data after container restarts.

Do not remove these volumes unless intentionally resetting the development environment.

---

# ❤️ Application Health & Observability

AirHive uses Spring Boot Actuator for application health monitoring.

Exposed Actuator functionality includes:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
/actuator/info
```

Only the required Actuator endpoints are exposed.

## Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

## Liveness Probe

```bash
curl http://localhost:8080/actuator/health/liveness
```

## Readiness Probe

```bash
curl http://localhost:8080/actuator/health/readiness
```

Current verified state:

```text
Overall Health: UP
Liveness:       UP
Readiness:      UP
```

## Application Information

```text
/actuator/info
```

Current metadata:

```text
Name: AirHive Backend
Description: Airline Management System backend
Version: 1.0.0
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

```text
/topic/flights
```

Events currently supported:

```text
FLIGHT_CREATED
FLIGHT_UPDATED
FLIGHT_DELETED
```

The frontend receives these events and updates the live operations dashboard without requiring a page refresh.

---

# ⚡ Caching

Redis caching is implemented for:

- Airports
- Aircraft Types
- Aircraft
- Other frequently accessed backend resources

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

Cache mutation operations invalidate relevant entries.

Flight DTO caching is currently disabled because the Redis JSON serializer was returning cached values as `LinkedHashMap` instead of `FlightResponseDTO`.

Flight APIs therefore currently load flight data directly from PostgreSQL.

---

# 📊 Analytics

AirHive includes a backend analytics layer.

Current analytics include:

- Operations overview
- Flight status distribution
- Delayed flight count
- Cancelled flight count
- Airborne flight count
- Aircraft utilization
- Aircraft status distribution
- Airport activity
- Route activity
- Historical flight activity
- Historical delayed flight counts
- Historical cancelled flight counts

Analytics are generated through the backend service layer using existing flight, aircraft, airport, and route data.

Current analytics DTOs include:

```text
AnalyticsOverviewResponseDTO
StatusCountDTO
AircraftUtilizationDTO
ActivityCountDTO
HistoricalActivityDTO
```

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
- Authentication
- Authorization
- Notifications
- Analytics
- Regression scenarios

Run all backend tests:

```bash
cd backend
./mvnw test
```

## Current Test Checkpoint

```text
Tests:    313
Failures: 0
Errors:   0
Skipped:  0
```

Frontend type checking:

```bash
npx tsc --noEmit
```

---

# 🛠️ Frontend Development

Frontend API communication is separated from UI components.

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

Create:

```bash
cp .env.example .env
```

Actual secrets must not be committed to Git.

## Frontend Variables

| Variable | Purpose | Example |
|---|---|---|
| `VITE_API_BASE_URL` | Backend REST API URL | `http://localhost:8080/api` |
| `VITE_WS_URL` | Backend WebSocket endpoint | `ws://localhost:8080/ws` |

## Backend Variables

| Variable | Purpose |
|---|---|
| `AIRHIVE_DB_URL` | PostgreSQL JDBC connection URL |
| `AIRHIVE_DB_USERNAME` | PostgreSQL username |
| `AIRHIVE_DB_PASSWORD` | PostgreSQL password |
| `AIRHIVE_REDIS_HOST` | Redis hostname |
| `AIRHIVE_REDIS_PORT` | Redis port |
| `AIRHIVE_JWT_SECRET` | JWT signing secret |
| `AIRHIVE_CORS_ORIGINS` | Allowed REST origins |
| `AIRHIVE_WS_ALLOWED_ORIGINS` | Allowed WebSocket origins |

Production deployments should replace local development origins with the actual deployed frontend origin.

---

# 🔒 Secret Management

The actual `.env` file is intentionally excluded from Git.

Verify:

```bash
git check-ignore -v .env
```

The repository should not contain:

- Database passwords
- JWT secrets
- Production API keys
- Deployment credentials
- Other private runtime secrets

The `.env.example` file contains configuration templates only.

---

# 🛡️ Security

AirHive includes:

- Authentication
- JWT-based security
- Role-based access control
- Protected API endpoints
- User lifecycle validation
- Password security
- Security exception handling
- WebSocket security foundation
- Runtime security configuration

Production-oriented configuration includes:

- `ddl-auto: validate`
- SQL statement logging disabled
- Configurable database settings
- Configurable Redis settings
- Externally supplied JWT secret
- Configurable CORS origins
- Configurable WebSocket origins
- Limited Actuator exposure

---

# 🐳 Container Security & Runtime Hardening

The Docker environment has been reviewed for:

- Container privileges
- Runtime configuration
- Secret handling
- Port exposure
- Docker networking
- Restart policies
- Persistent storage
- Healthchecks
- Service dependency ordering

Application containers are not configured as privileged containers.

Runtime secrets are supplied through environment configuration rather than baked into Docker images.

---

# 🧑‍💻 Git Workflow

Pull:

```bash
git pull origin main
```

Frontend validation:

```bash
npx tsc --noEmit
```

Backend tests:

```bash
cd backend
./mvnw test
```

Check:

```bash
git diff --check
git status
```

Commit:

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

---

# 📊 Current Project Status

| Area | Status |
|---|---|
| Core Architecture | ✅ Complete |
| PostgreSQL Database | ✅ Complete |
| Airport APIs | ✅ Complete |
| Aircraft Type APIs | ✅ Complete |
| Aircraft APIs | ✅ Complete |
| Route APIs | ✅ Complete |
| Flight APIs | ✅ Complete |
| Passenger Management | ✅ Complete |
| Crew Management | ✅ Complete |
| Booking Management | ✅ Complete |
| Maintenance Management | ✅ Complete |
| Business Validation | ✅ Complete |
| Frontend API Integration | ✅ Complete |
| Backend Testing | ✅ Complete |
| Redis Integration | ✅ Complete |
| Redis Caching | ✅ Complete |
| Cache Invalidation | ✅ Complete |
| Flight WebSockets | ✅ Complete |
| Live Flight Updates | ✅ Complete |
| Authentication | ✅ Complete |
| JWT Security | ✅ Complete |
| Role-Based Access Control | ✅ Complete |
| Notifications | ✅ Complete |
| Advanced Flight Operations | ✅ Complete |
| Flight History | ✅ Complete |
| Analytics Backend | ✅ Complete |
| Historical Analytics | ✅ Complete |
| Environment Externalization | ✅ Complete |
| Dockerization | ✅ Complete |
| Docker Compose Environment | ✅ Complete |
| Docker Healthchecks | ✅ Complete |
| Persistent Docker Storage | ✅ Complete |
| Actuator Health | ✅ Complete |
| Liveness Probe | ✅ Complete |
| Readiness Probe | ✅ Complete |
| Deployment Hardening | ✅ Complete |
| Phase 12 Analytics | ✅ Complete |
| Phase 13 AI | 🔮 Future / Postponed |
| Phase 14 Deployment & Observability | ✅ Complete |
| Phase 15 Finalization & Release | ✅ Complete |

---

# 🗺️ Roadmap

## Phase 1 — Core Architecture

- [x] Project structure
- [x] Frontend architecture
- [x] Backend architecture
- [x] Database model
- [x] API client structure
- [x] Shared frontend components

## Phase 2 — Database

- [x] PostgreSQL integration
- [x] Airport schema
- [x] Aircraft type schema
- [x] Aircraft schema
- [x] Route schema
- [x] Flight schema
- [x] User and role schema
- [x] Notification schema
- [x] Passenger schema
- [x] Crew schema
- [x] Booking schema
- [x] Maintenance schema

## Phase 3 — REST APIs

- [x] Airport APIs
- [x] Aircraft Type APIs
- [x] Aircraft APIs
- [x] Route APIs
- [x] Flight APIs
- [x] Authentication APIs
- [x] User management APIs
- [x] Notification APIs
- [x] Passenger APIs
- [x] Crew APIs
- [x] Booking APIs
- [x] Maintenance APIs

## Phase 4 — Business Logic

- [x] Request validation
- [x] Duplicate detection
- [x] Resource validation
- [x] Aircraft scheduling conflict detection
- [x] Exception handling
- [x] Flight status validation
- [x] User lifecycle validation
- [x] Role and permission validation
- [x] Booking validation
- [x] Passenger validation

## Phase 5 — Frontend Integration

- [x] API client layer
- [x] Flight integration
- [x] Aircraft integration
- [x] Route integration
- [x] Live operations integration
- [x] Authentication flow
- [x] Session handling
- [x] Protected frontend routes
- [x] Notification panel
- [x] Flight detail page
- [x] Passenger management
- [x] Crew management
- [x] Booking management
- [x] Maintenance management

## Phase 6 — Testing

- [x] Service tests
- [x] Controller tests
- [x] Repository tests
- [x] Validation tests
- [x] Authentication tests
- [x] RBAC tests
- [x] WebSocket tests
- [x] Notification tests
- [x] Analytics tests
- [x] Regression testing
- [x] 313 backend tests passing

## Phase 7 — Redis and Caching

- [x] Redis integration
- [x] Airport caching
- [x] Aircraft type caching
- [x] Aircraft caching
- [x] Cache invalidation
- [x] TTL verification
- [x] Redis health verification
- [ ] Correctly typed flight DTO caching
- [x] Flight cache serialization review

## Phase 8 — Real-Time Operations

- [x] Spring WebSocket
- [x] STOMP broker
- [x] Flight event DTO
- [x] Flight created events
- [x] Flight updated events
- [x] Flight deleted events
- [x] Frontend STOMP client
- [x] Live flight dashboard updates
- [x] Real-time notification delivery
- [x] Notification read/unread state
- [x] Notification clearing
- [x] Flight notification navigation

## Phase 9 — UI and Design System

- [x] macOS-inspired application shell
- [x] Shared design primitives
- [x] Glass-style cards
- [x] Status pills
- [x] Responsive layout foundation
- [x] Loading states
- [x] Empty states
- [x] Error states
- [x] Accessibility improvements
- [x] Final design system refinement
- [x] Advanced dashboard interactions
- [x] Full responsive QA
- [x] Visual consistency review

## Phase 10 — Security

- [x] Authentication
- [x] JWT/session strategy
- [x] Role-based access control
- [x] Protected endpoints
- [x] User lifecycle management
- [x] Password security
- [x] Security exception handling
- [x] WebSocket security foundation
- [x] Production security configuration foundation
- [x] Runtime secret externalization
- [x] Final security review

## Phase 11 — Advanced Flight Operations

- [x] Dynamic flight detail route
- [x] Flight detail information view
- [x] Flight schedule information
- [x] Aircraft information display
- [x] Flight status display
- [x] Flight status update from detail page
- [x] Flight activity timeline
- [x] Delay and cancellation reasons
- [x] Aircraft and route detail links
- [x] Operational action permissions
- [x] Advanced flight filtering
- [x] Flight history

## Phase 12 — Analytics

**Status: ✅ Complete**

- [x] Operations dashboard analytics
- [x] Flight status distribution
- [x] Delay analytics
- [x] Airport activity analytics
- [x] Aircraft utilization analytics
- [x] Route activity analytics
- [x] Historical reporting
- [x] Analytics DTOs
- [x] Analytics service layer
- [x] Analytics regression testing

## Phase 13 — AI Features

**Status: 🔮 Future Phase / Postponed**

AI development is intentionally postponed.

Planned future capabilities:

- [ ] AI-assisted operational insights
- [ ] Delay prediction
- [ ] Flight disruption analysis
- [ ] Natural-language operations search
- [ ] AI recommendations
- [ ] AI monitoring and evaluation

Phase 13 is not part of the current release scope.

## Phase 14 — Deployment and Observability

**Status: ✅ Complete**

Completed:

- [x] Runtime configuration externalization
- [x] Database configuration externalization
- [x] Redis configuration externalization
- [x] JWT secret externalization
- [x] CORS configuration
- [x] WebSocket origin configuration
- [x] Production-oriented JPA configuration
- [x] SQL logging reduction
- [x] Dockerfiles
- [x] Docker Compose
- [x] PostgreSQL container
- [x] Redis container
- [x] Backend container
- [x] Frontend container
- [x] Docker healthchecks
- [x] Persistent volumes
- [x] Docker network verification
- [x] Actuator health
- [x] Liveness
- [x] Readiness
- [x] Application information
- [x] Secret handling audit
- [x] Container runtime audit
- [x] Final deployment verification

## Phase 15 — Finalization & Release

**Status: 🔄 Current Development Phase**

### 15.1 Final Repository & Code Audit

- [x] Working tree clean
- [x] Main branch synchronized
- [x] Docker files verified
- [x] `.env.example` verified
- [x] `.env` excluded from Git
- [x] Build/log artifacts checked
- [x] TODO/FIXME markers reviewed
- [x] Git repository integrity checked

### 15.2 README & Documentation

- [x] Project status updated
- [x] Phase 14 marked complete
- [x] Phase 15 marked current
- [x] Phase 13 marked future/postponed
- [x] Docker workflow documented
- [x] Environment variables documented
- [x] Health endpoints documented
- [x] Testing documented
- [x] Deployment configuration documented
- [x] Final documentation review

### 15.3 Environment & Setup Documentation

- [x] Verify `.env.example`
- [x] Verify fresh setup instructions
- [x] Verify Docker setup from clean environment
- [x] Verify local development instructions

### 15.4 Final Application QA

- [x] Frontend smoke test
- [x] Authentication flow
- [x] Flight management
- [x] Airport management
- [x] Aircraft management
- [x] Route management
- [x] Passenger management
- [x] Crew management
- [x] Booking management
- [x] Maintenance management
- [x] Analytics
- [x] Notifications
- [x] WebSocket functionality

### 15.5 Deployment Documentation

- [x] Final deployment instructions
- [x] Runtime configuration documentation
- [x] Environment variable reference
- [x] Healthcheck documentation
- [x] Deployment troubleshooting
- [x] Backup/recovery notes

### 15.6 Final Build & Regression Verification

- [x] Frontend production build
- [x] Backend clean build
- [x] Backend regression suite
- [x] Docker rebuild
- [x] Docker runtime verification
- [x] Final health verification

### 15.7 Git & Release Audit

- [x] Working tree clean
- [x] Branch synchronized
- [x] Final documentation commit
- [x] Final push
- [x] Final release state verification

### 15.8 Final Release Checkpoint

- [x] Release documentation complete
- [x] Application stable
- [x] Backend regression suite passing
- [x] Docker environment verified
- [x] No accidental secrets committed
- [x] Final release checkpoint

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
```

Development-only Redis reset:

```bash
redis-cli FLUSHDB
```

> ⚠️ `FLUSHDB` removes all keys from the selected Redis database. Use it only when intentionally clearing development Redis data.

## Docker

```bash
docker compose up -d --build
docker compose ps
docker compose logs
docker compose logs -f
docker compose down
docker compose config --quiet
```

## Actuator

```bash
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/health/readiness
curl -s http://localhost:8080/actuator/health/liveness
```

---

# 🔍 Useful Docker Diagnostics

```bash
docker compose ps
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
docker compose logs redis
docker compose logs -f
docker compose config
```

---

# 🐛 Troubleshooting

<details>
<summary><strong>Backend cannot connect to PostgreSQL</strong></summary>

Check PostgreSQL:

```bash
brew services list
```

Test:

```bash
psql -U airhive_user -d airhive
```

Verify:

```text
AIRHIVE_DB_URL
AIRHIVE_DB_USERNAME
AIRHIVE_DB_PASSWORD
```

For Docker, the backend should connect using:

```text
jdbc:postgresql://postgres:5432/airhive
```

</details>

<details>
<summary><strong>Redis connection fails</strong></summary>

```bash
redis-cli ping
```

Expected:

```text
PONG
```

Start Redis:

```bash
brew services start redis
```

For Docker:

```text
redis:6379
```

</details>

<details>
<summary><strong>Port 8080 is already in use</strong></summary>

```bash
lsof -i :8080
```

Stop the process if appropriate:

```bash
kill <PID>
```

</details>

<details>
<summary><strong>Port 3000 is already in use</strong></summary>

```bash
lsof -i :3000
```

Stop the process if appropriate:

```bash
kill <PID>
```

</details>

<details>
<summary><strong>JWT configuration error</strong></summary>

Verify:

```text
AIRHIVE_JWT_SECRET
```

The backend requires a runtime JWT secret.

Never commit the production JWT secret.

</details>

<details>
<summary><strong>WebSocket does not connect</strong></summary>

Verify:

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

</details>

<details>
<summary><strong>Redis returns unexpected cached data</strong></summary>

Development-only reset:

```bash
redis-cli FLUSHDB
```

> ⚠️ This removes all keys from the selected Redis database.

</details>

<details>
<summary><strong>Docker backend is unhealthy</strong></summary>

Check:

```bash
docker compose ps
docker compose logs backend
```

Then:

```bash
curl http://localhost:8080/actuator/health
```

Also verify:

```bash
docker compose ps postgres
docker compose ps redis
```

</details>

<details>
<summary><strong>Actuator health endpoint returns 401</strong></summary>

The following endpoints are configured for runtime health checks:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

Review `SecurityConfig` if health endpoints unexpectedly require authentication.

</details>

---

# 🌎 Live Application

Frontend deployment:

```text
https://skyward-zenith-ops.lovable.app
```

The deployed frontend represents the current user-facing airline operations experience.

The local development and Docker environments remain the primary environments for backend, API, database, Redis, WebSocket, analytics, and deployment configuration development.

The current Docker Compose setup is a **development/local deployment environment** and should not be interpreted as a production hosting deployment.

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
Dockerization
       ↓
Actuator Health & Readiness Monitoring
       ↓
Deployment Hardening
       ↓
Phase 15 Finalization & Release
```

Phase 12 completed the analytics layer.

Phase 13 AI capabilities were deliberately moved to a future phase.

Phase 14 completed deployment, Dockerization, observability, runtime configuration, and hardening.

The current development focus is:

**Phase 15 — Finalization & Release**

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

Environment-specific values such as database credentials, Redis settings, JWT secrets, CORS origins, and WebSocket origins are externalized.

### 8. Containerized Development

Docker Compose provides a reproducible environment containing the frontend, backend, PostgreSQL, and Redis infrastructure.

### 9. Operational Observability

Spring Boot Actuator health, liveness, readiness, and application information endpoints provide a foundation for monitoring the application during deployment and operation.

### 10. Stable Core Before AI

The AI roadmap is intentionally separated from the current release so the core airline management platform can first reach a stable, tested, documented, and deployable state.

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
Vite
TanStack Start
Spring Boot
Java 26
PostgreSQL
Redis
WebSockets
STOMP
Spring Security
Spring Boot Actuator
Maven
Docker
Docker Compose
```

---

<p align="center">

✈️ <strong>AirHive</strong><br>

<em>Modern airline operations, connected in real time.</em>

</p>
