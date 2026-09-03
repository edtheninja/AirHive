# ✈️ AirHive

> **Modern Airline Management System for real-time airline operations**

AirHive is a full-stack **Airline Management System (AMS)** designed for airline administrators, airport staff, operations managers, supervisors, and operational teams.

Unlike a passenger booking website, AirHive focuses on the **operational side of an airline** — managing flights, aircraft, routes, airports, schedules, validations, caching, and real-time operational updates.

---

## ✨ Highlights

- 🛫 Flight Management
- 🛩️ Aircraft Management
- 🏢 Airport Management
- 🗺️ Route Management
- 📋 Aircraft Type Management
- ⚙️ Business Rule Validation
- 🔴 Redis Caching
- ⚡ Real-Time WebSocket Updates
- 🔄 STOMP Messaging
- 🧪 Automated Backend Testing
- 🎨 Premium Operations Dashboard
- 📊 Operational KPIs
- 🧩 Component-Driven React Architecture
- 🌙 Dark Mode Support
- 📱 Responsive Interface

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [PostgreSQL Setup](#-postgresql-setup)
- [Redis Setup](#-redis-setup)
- [Running the Application](#-running-the-application)
- [WebSocket Real-Time Updates](#-websocket-real-time-updates)
- [API Overview](#-api-overview)
- [Testing](#-testing)
- [Development Workflow](#-development-workflow)
- [Current Status](#-current-status)
- [Roadmap](#-roadmap)
- [Design Philosophy](#-design-philosophy)
- [Live Application](#-live-application)
- [Contributing](#-contributing)
- [License](#-license)

---

# 🌐 Overview

AirHive provides a centralized platform for managing airline resources and flight operations.

The system combines a modern React frontend with a Spring Boot backend, PostgreSQL persistence, Redis caching, and WebSocket-based real-time communication.

```text
                    ┌──────────────────────────┐
                    │      React Frontend      │
                    │ React + TypeScript + Vite│
                    └────────────┬─────────────┘
                                 │
                 ┌───────────────┴────────────────┐
                 │                                │
                 │ REST API                       │ WebSocket
                 │                                │ / STOMP
                 ▼                                ▼
        ┌───────────────────┐           ┌───────────────────┐
        │   Spring Boot     │           │  Live Operations  │
        │     Backend       │           │     Updates       │
        └─────────┬─────────┘           └───────────────────┘
                  │
          ┌───────┴────────┐
          │                │
          ▼                ▼
 ┌────────────────┐  ┌────────────────┐
 │   PostgreSQL   │  │     Redis      │
 │   Persistent   │  │     Caching    │
 │     Storage    │  │                │
 └────────────────┘  └────────────────┘
 🏗️ Architecture

AirHive follows a layered backend architecture:

Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
PostgreSQL

Additional infrastructure:

Spring Boot
    │
    ├── REST APIs
    │
    ├── Validation
    │
    ├── Business Rules
    │
    ├── Redis Cache
    │
    └── WebSocket / STOMP
             │
             ▼
       React Frontend
REST Request Flow
React
  │
  │ HTTP
  ▼
REST Controller
  │
  ▼
Service Layer
  │
  ├── Validation
  ├── Business Rules
  └── Cache
  │
  ▼
Repository
  │
  ▼
PostgreSQL
Real-Time Flow
Flight Mutation
      │
      ▼
FlightService
      │
      ▼
FlightWebSocketService
      │
      ▼
STOMP Broker
      │
      ▼
/topic/flights
      │
      ▼
React WebSocket Client
      │
      ▼
Live Operations Dashboard
🧰 Technology Stack
Frontend
Technology	Purpose
React	UI framework
TypeScript	Type-safe frontend development
Vite	Frontend build tool
Tailwind CSS	Styling
Framer Motion	UI animations
shadcn/ui	Reusable UI components
Lucide React	Icons
React Router	Client-side routing
STOMP.js	WebSocket/STOMP client
Backend
Technology	Purpose
Java 26	Backend language/runtime
Spring Boot 4.1.0	Backend framework
Spring Web	REST APIs
Spring Data JPA	Database access
Hibernate	ORM
Spring Validation	Request validation
Spring WebSocket	Real-time communication
STOMP	WebSocket messaging protocol
Maven	Build and dependency management
Database & Infrastructure
Technology	Purpose
PostgreSQL	Primary relational database
Redis	Application caching
Spring Cache	Cache abstraction
Git	Version control
GitHub	Source-code hosting
Testing
Technology	Purpose
JUnit	Unit testing
Mockito	Mocking
Spring Boot Test	Application/integration testing
🚀 Features
🛫 Flight Management

AirHive currently supports:

Create flights
Retrieve flights
Retrieve individual flights
Update flights
Delete flights
Flight status management
Aircraft assignment
Route assignment
Departure/arrival airport validation
Schedule validation
Duplicate flight validation
Aircraft scheduling conflict detection

Supported statuses:

SCHEDULED
BOARDING
IN_AIR
LANDED
DELAYED
CANCELLED
🛩️ Aircraft Management

Aircraft management includes:

Aircraft registration
Aircraft type
Aircraft status
Aircraft assignment
Duplicate registration validation
Aircraft type relationships
Redis caching
🏢 Airport Management

Airport management includes:

Airport records
IATA codes
ICAO codes
Airport validation
Duplicate airport protection
Redis caching
🗺️ Route Management

Routes connect operational airports and provide:

Departure airport
Arrival airport
Route distance
Estimated flight duration
🔴 Redis Caching

AirHive uses Redis to reduce unnecessary database queries.

Caching has been implemented for:

Airports
Aircraft Types
Aircraft
Flights

Spring Cache is used with Redis as the backing cache store.

Cache Flow
API Request
    │
    ▼
Service
    │
    ▼
Redis Cache
    │
    ├── HIT ──► Return cached DTO
    │
    └── MISS
          │
          ▼
      PostgreSQL
          │
          ▼
      Store in Redis

Cached entries currently use a 10-minute TTL.

Relevant cache entries are invalidated when data is created, updated, or deleted.

⚡ WebSocket Real-Time Updates

AirHive supports real-time flight updates using:

Spring WebSocket
STOMP
STOMP.js
/ws WebSocket endpoint
/topic/flights subscription
Flight Events

The frontend currently receives:

FLIGHT_CREATED
FLIGHT_UPDATED
FLIGHT_DELETED

Example:

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
    "status": "DELAYED"
  }
}

The Operations Dashboard can process these events without requiring a page refresh.

📁 Project Structure
AirHive/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/airhive/backend/
│   │   │   │
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
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
├── vite.config.ts
├── tailwind.config.ts
├── tsconfig.json
└── README.md
💻 Prerequisites

Before installing AirHive, install:

Git
Node.js
npm
Java 26
Maven
PostgreSQL
Redis

Check the installed versions:

git --version
node --version
npm --version
java --version
mvn --version
psql --version
redis-cli --version
📥 Installation
1. Clone the Repository
git clone https://github.com/edtheninja/AirHive.git

Enter the project:

cd AirHive
2. Install Frontend Dependencies

From the project root:

npm install
3. Configure the Backend

Move into the backend:

cd backend

Open:

src/main/resources/application.yml

Configure PostgreSQL:

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/airhive
    username: airhive_user
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

Never commit real production passwords or secrets to GitHub.

🗄️ PostgreSQL Setup

AirHive uses PostgreSQL as its primary persistent database.

macOS / Homebrew

Start PostgreSQL:

brew services start postgresql

If using PostgreSQL 18:

brew services start postgresql@18

Verify:

pg_isready

Expected result should indicate that PostgreSQL is accepting connections.

Create the AirHive Database

Open PostgreSQL:

psql postgres

Create the database:

CREATE DATABASE airhive;

Create the application user:

CREATE USER airhive_user WITH PASSWORD 'your_password';

Grant database privileges:

GRANT ALL PRIVILEGES ON DATABASE airhive TO airhive_user;

Exit:

\q

Then configure the credentials in:

backend/src/main/resources/application.yml
🔴 Redis Setup

Redis is required for AirHive's caching system.

Install Redis
brew install redis

Start Redis:

brew services start redis

Verify:

redis-cli ping

Expected:

PONG

AirHive uses:

spring:
  data:
    redis:
      host: localhost
      port: 6379
▶️ Running the Application

AirHive uses multiple services during local development.

┌─────────────────────────┐
│ React / Vite            │
│ localhost:5173          │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ Spring Boot             │
│ localhost:8080          │
└───────┬─────────┬───────┘
        │         │
        ▼         ▼
┌────────────┐ ┌────────────┐
│ PostgreSQL │ │   Redis    │
│ :5432      │ │   :6379    │
└────────────┘ └────────────┘
Terminal 1 — PostgreSQL

Make sure PostgreSQL is running:

pg_isready
Terminal 2 — Redis

Verify Redis:

redis-cli ping
Terminal 3 — Backend

From the project root:

cd backend
./mvnw spring-boot:run

The backend runs on:

http://localhost:8080
Terminal 4 — Frontend

Open another terminal and return to the project root:

cd AirHive
npm run dev

The frontend normally runs on:

http://localhost:5173

Open that address in your browser.

🔌 API Overview

The backend exposes REST APIs under:

/api
Airports
GET     /api/airports
GET     /api/airports/{id}
POST    /api/airports
PUT     /api/airports/{id}
DELETE  /api/airports/{id}
Aircraft Types
GET     /api/aircraft-types
GET     /api/aircraft-types/{id}
POST    /api/aircraft-types
PUT     /api/aircraft-types/{id}
DELETE  /api/aircraft-types/{id}
Aircraft
GET     /api/aircraft
GET     /api/aircraft/{id}
POST    /api/aircraft
PUT     /api/aircraft/{id}
DELETE  /api/aircraft/{id}
Routes
GET     /api/routes
GET     /api/routes/{id}
POST    /api/routes
PUT     /api/routes/{id}
DELETE  /api/routes/{id}
Flights
GET     /api/flights
GET     /api/flights/{id}
POST    /api/flights
PUT     /api/flights/{id}
DELETE  /api/flights/{id}
📡 WebSocket Endpoint

The WebSocket endpoint is:

ws://localhost:8080/ws

Flight events are published to:

/topic/flights

The frontend STOMP client connects to:

ws://localhost:8080/ws

and subscribes to:

/topic/flights
🧪 Testing

Run the complete backend test suite:

cd backend
./mvnw test

Build the backend without tests:

./mvnw clean package -DskipTests

Run a specific test:

./mvnw test -Dtest=FlightServiceTest
Frontend Type Checking

From the project root:

npx tsc --noEmit

A successful check produces no TypeScript error output.

🔍 API Testing with cURL

Retrieve all flights:

curl http://localhost:8080/api/flights

Retrieve a specific flight:

curl http://localhost:8080/api/flights/1

Create a flight:

curl -X POST http://localhost:8080/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AH999",
    "aircraftId": 2,
    "routeId": 2,
    "departureAirportId": 2,
    "arrivalAirportId": 3,
    "scheduledDeparture": "2026-08-25T09:00:00",
    "scheduledArrival": "2026-08-25T11:15:00",
    "status": "SCHEDULED"
  }'
🧹 Development Workflow

Recommended workflow:

1. Pull latest changes
        ↓
2. Start PostgreSQL
        ↓
3. Start Redis
        ↓
4. Start Spring Boot
        ↓
5. Start Vite
        ↓
6. Develop
        ↓
7. Run TypeScript checks
        ↓
8. Run backend tests
        ↓
9. Review Git changes
        ↓
10. Commit
        ↓
11. Push
Check Git Status
git status
Review Changes
git diff
Check for Whitespace Problems
git diff --check
Frontend Type Check
npx tsc --noEmit
Backend Tests
cd backend
./mvnw test
📊 Current Status
Module	Status
Core Architecture	✅ Complete
PostgreSQL Database	✅ Complete
Airport Management	✅ Complete
Aircraft Type Management	✅ Complete
Aircraft Management	✅ Complete
Route Management	✅ Complete
Flight Management	✅ Complete
REST APIs	✅ Complete
DTO Architecture	✅ Complete
Validation	✅ Complete
Business Rules	✅ Complete
Exception Handling	✅ Complete
Automated Testing	✅ Complete
Frontend API Integration	✅ Complete
Redis Caching	✅ Complete
Flight WebSocket Updates	✅ Complete
Aircraft WebSocket Updates	⏳ Planned
Advanced Real-Time Operations	⏳ Planned
UI Design-System Polish	⏳ Planned
Authentication	⏳ Planned
RBAC	⏳ Planned
Docker	⏳ Planned
CI/CD	⏳ Planned
Production Deployment	⏳ Planned
Final QA	⏳ Planned
🗺️ Roadmap
Phase 1 — Foundation
 Project architecture
 Database
 Core entities
 REST APIs
Phase 2 — Business Logic
 DTOs
 Validation
 Duplicate-resource protection
 Scheduling conflict detection
 Exception handling
Phase 3 — Frontend Integration
 API client
 Flight integration
 Aircraft integration
 Airport integration
 Route integration
 Operations dashboard integration
Phase 4 — Performance
 Redis integration
 Cache configuration
 TTL
 Cache invalidation
 DTO-based caching
Phase 5 — Real-Time Operations
 Spring WebSocket
 STOMP
 Flight events
 Frontend STOMP client
 Live dashboard updates
 Flight created events
 Flight updated events
 Flight deleted events
 Aircraft events
 Advanced operational event streams
Phase 6 — Security
 Authentication
 JWT
 Role-based access control
 Admin roles
 Operations roles
 Airport staff roles
 Protected WebSocket channels
Phase 7 — Deployment
 Docker
 Docker Compose
 CI/CD
 Production configuration
 Cloud deployment
 Monitoring
Phase 8 — Finalization
 End-to-end QA
 Performance testing
 Security testing
 API documentation
 Developer documentation
 Deployment documentation
🎨 Design Philosophy

AirHive's frontend follows a premium airline operations aesthetic inspired by modern desktop operating systems.

Visual Principles
Frosted glass surfaces
Soft layered shadows
Rounded cards
Clean typography
Spacious layouts
Minimal visual clutter
Clear information hierarchy
Smooth animations
Responsive layouts
Color Palette
Purpose	Color
Background	#F6F7FB
Primary	#1E3A8A
Accent	#3B82F6
Success	#22C55E
Warning	#F59E0B
Danger	#EF4444
Text	#111827
Secondary Text	#6B7280
Dark Mode
Background: #0F172A
Glass: rgba(30,41,59,.65)
🖥️ Operations Dashboard

The Operations Dashboard is designed around airline operational workflows.

Current dashboard concepts include:

Total Flights
Active Flights
Delayed Flights
Revenue
Passenger Count
Fleet Availability
Live Flight Operations
Notifications
Aircraft information
Operational actions

The dashboard is designed to evolve into a complete airline operations center.

🔔 Live Operations

Real-time infrastructure allows the dashboard to react to backend events without refreshing the page.

Backend
   │
   │ Flight status changed
   ▼
Spring Boot
   │
   │ STOMP Event
   ▼
/topic/flights
   │
   ▼
React
   │
   ▼
Live Operations Table

This provides the foundation for:

Live aircraft tracking
Gate changes
Boarding updates
Delay notifications
Crew changes
Maintenance alerts
Operational announcements
🌍 Future Expansion

The architecture is designed to support additional airline-management modules:

👨‍✈️ Crew Management
👥 Passenger Management
🎫 Booking Management
🔧 Maintenance Management
🛬 Gate Management
📊 Advanced Analytics
🌍 Real-Time Aircraft Tracking
🔔 Operational Notifications
🔐 Enterprise Authentication
🏢 Multi-Airport Operations
📈 Revenue Analytics
🌐 Live Application

The current frontend application is available at:

https://skyward-zenith-ops.lovable.app

The live application represents the frontend experience. The complete local development environment includes the Spring Boot backend, PostgreSQL database, Redis cache, and WebSocket infrastructure.

🧑‍💻 Lovable

The original frontend was initially created using Lovable.

The project can still be accessed through the Lovable editor:

https://lovable.dev/projects/df8b1868-feef-4438-9a0a-d5022b91e158

The project has since evolved into a full-stack application with:

React frontend
Spring Boot backend
PostgreSQL database
Redis caching
WebSocket/STOMP infrastructure
Automated backend testing
🛠️ Troubleshooting
<details> <summary><strong>Backend does not start</strong></summary>

Check whether port 8080 is already being used:

lsof -i :8080

Stop the conflicting process if necessary and restart Spring Boot.

</details> <details> <summary><strong>PostgreSQL connection error</strong></summary>

Check PostgreSQL:

pg_isready

Check available databases:

psql -l

Verify the credentials in:

backend/src/main/resources/application.yml
</details> <details> <summary><strong>Redis connection error</strong></summary>

Check Redis:

redis-cli ping

Expected:

PONG

Start Redis if necessary:

brew services start redis
</details> <details> <summary><strong>Frontend cannot connect to backend</strong></summary>

Verify the backend:

curl http://localhost:8080/api/flights

Then start the frontend:

npm run dev
</details> <details> <summary><strong>WebSocket is not connecting</strong></summary>

Verify that Spring Boot is running on port 8080.

WebSocket endpoint:

ws://localhost:8080/ws

Flight topic:

/topic/flights

Browser Developer Tools → Network → WS can be used to inspect the connection.

</details>
🔐 Production Considerations

AirHive currently runs primarily as a development environment.

Before production deployment, the following should be implemented:

Production database credentials
Environment-specific configuration
Authentication
Authorization
JWT
HTTPS
Secure WebSocket configuration
Production Redis configuration
Database migrations
Docker
CI/CD
Monitoring
Structured logging
Rate limiting
Secret management
Production backups
🤝 Contributing
Fork the repository.
Create a feature branch:
git checkout -b feature/your-feature
Make your changes.
Run frontend checks:
npx tsc --noEmit
Run backend tests:
cd backend
./mvnw test
Check formatting:
git diff --check
Review your changes:
git diff
Commit:
git add .
git commit -m "feat: describe your change"
Push:
git push origin feature/your-feature
📄 License

This project is currently under development.

License information will be added before the first production release.

✈️ AirHive
From airline data to live operations.

Built with:

React
TypeScript
Vite
Tailwind CSS
Framer Motion
shadcn/ui
Lucide React
React Router
Spring Boot
Java 26
Spring Data JPA
Hibernate
PostgreSQL
Redis
Spring Cache
WebSocket
STOMP
Maven
JUnit
Mockito
Git
GitHub

### One important change from the old README

I intentionally removed the old **"Build with Lovable" as the main project identity**. Lovable is now documented as part of the project's history, while the README presents AirHive accurately as the **full-stack Spring Boot + React + PostgreSQL + Redis + WebSocket system** it has become.