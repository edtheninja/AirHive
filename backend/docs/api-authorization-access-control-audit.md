# A4 — API Authorization & Access Control Audit

## 1. Audit Overview

**Project:** AirHive Airline Management System  
**Audit area:** API Authorization & Access Control  
**Audit phase:** A4  
**Audit status:** Review completed  
**Approach:** Read-only source-code and test review

### Objective

Review API authorization rules, identity validation, user governance, resource ownership enforcement, operational data access, and related security test coverage.

The audit distinguishes confirmed implementation behavior from policy concerns and hardening recommendations.

## 2. Scope

The review covered:

- Spring Security authorization configuration
- User administration and governance
- JWT identity, role, and account-status validation
- Passenger and booking ownership
- Notification ownership
- Flight, Route, Airport, Aircraft, and AircraftType operations
- Crew and Maintenance read access
- Related integration and service tests

## 3. Authorization Policy

Source reviewed: `SecurityConfig.java`

| Endpoint / Operation | Authorized roles |
|---|---|
| GET `/api/users` | ADMIN |
| POST `/api/users` | ADMIN |
| PATCH `/api/users/**` | ADMIN |
| GET `/api/bookings` | PASSENGER, VIEWER, OPERATOR, ADMIN |
| GET `/api/passengers` | PASSENGER, VIEWER, OPERATOR, ADMIN |
| General GET `/api/**` | VIEWER, OPERATOR, ADMIN |
| Operational POST endpoints | OPERATOR, ADMIN |
| Operational PUT endpoints | OPERATOR, ADMIN |
| PATCH flight status | OPERATOR, ADMIN |
| PATCH notifications | VIEWER, OPERATOR, ADMIN |
| DELETE `/api/notifications` | VIEWER, OPERATOR, ADMIN |
| General DELETE `/api/**` | ADMIN |
| Unmatched endpoints | Denied |

Public endpoints identified:

- `/actuator/health/**`
- POST `/api/auth/login`
- POST `/api/auth/signup`
- OPTIONS preflight requests
- `/ws/**` WebSocket handshake

PASSENGER is excluded from the broad staff GET rule. Passenger and booking list endpoints have a separate authorization rule.

## 4. User Governance

Sources reviewed:

- `UserManagementService`
- User management controller and DTOs
- `GovernanceLockRepository`
- JWT configuration

Controls identified:

- Administrative mutations require an enabled ADMIN actor.
- Governance mutations acquire a pessimistic write lock.
- User creation checks username uniqueness.
- Passwords are hashed before storage.
- New accounts are enabled by default.
- The last enabled administrator cannot be demoted or disabled.
- User creation and role/status changes are audited.
- User response DTOs expose ID, username, role, and enabled status.

Request validation:

- Username: nonblank, maximum 100 characters.
- Password: nonblank, 8–100 characters.
- Role: required.
- Enabled status: required for status updates.

## 5. JWT Validation

The reviewed JWT validation checks:

- Token subject maps to an existing user.
- The user is enabled.
- The token role matches the current database role.
- Token user ID matches the database user ID.

### Hardening observation

The user ID comparison uses `Number.longValue()`. Explicit validation that the claim is integral and does not lose precision would strengthen claim validation.

This was recorded as a hardening recommendation, not a confirmed privilege escalation.

## 6. Passenger and Booking Ownership

The A3 passenger ownership implementation was reviewed as part of A4.

Controls identified:

- Passenger access is separated from the broad operational GET rule.
- Passenger and booking records are intended to be filtered by authenticated account ownership.
- PASSENGER is denied broad operational GET access.
- The rollout documentation states that legacy records remain unassigned where no reliable ownership mapping exists.
- Legacy records must not be assigned using unverified passenger names, flight numbers, or client-provided PNR data.

### Audit conclusion

No confirmed BOLA/IDOR vulnerability was identified in the reviewed passenger ownership implementation.

## 7. Notification Ownership

Sources reviewed:

- `NotificationController`
- Notification service/repository operations
- Notification tests

Ownership behavior identified:

| Operation | Ownership behavior |
|---|---|
| Get notifications | Scoped by user ID |
| Get unread notifications | Scoped by user ID |
| Get unread count | Scoped by user ID |
| Mark notification read | Verifies notification owner |
| Mark all read | Queries current user's unread notifications |
| Clear notifications | Deletes by user ID |

Foreign notification access when marking a notification read is rejected.

### Observations

- JWT user-ID extraction is not consistent across all controller methods.
- DELETE notification controller test coverage should be verified for happy-path and ownership behavior.

No confirmed notification BOLA/IDOR vulnerability was identified in the reviewed implementation.

## 8. Operational API Review

Services reviewed included Flight, Route, Airport, Aircraft, and AircraftType.

Business validations identified:

- Flight state transitions and schedule conflict checks
- Aircraft, airport, and route reference validation
- Route endpoint integrity
- Route duplicate endpoint and positive distance/duration validation
- Airport IATA/ICAO uniqueness
- Aircraft registration uniqueness and status validation
- Aircraft type reference validation
- Deletion restrictions for referenced resources
- Flight activity history restrictions

No confirmed object-level authorization vulnerability was identified in the reviewed shared operational resource services.

## 9. Crew and Maintenance Access

Sources reviewed:

- `CrewMemberController`
- `CrewMemberService`
- `MaintenanceController`
- `MaintenanceService`
- Crew and Maintenance response DTOs
- `SecurityConfigIntegrationTest`

### Crew data fields

The Crew response DTO includes:

- ID
- Crew code
- Name
- Role
- Base
- Availability
- Rest hours
- Next flight
- Medical clearance information
- Initials

### Maintenance data fields

The Maintenance response DTO includes:

- ID
- Order number
- Aircraft
- Type
- Severity
- Due status
- Progress
- Engineer assignment

### Access test evidence

| Role | Crew GET | Maintenance GET |
|---|---:|---:|
| Unauthenticated | 401 | 401 |
| VIEWER | 200 | 200 |
| OPERATOR | 200 | 200 |
| ADMIN | 200 | 200 |

### Policy concerns

- Crew medical-clearance status/expiry is visible to broad staff roles.
- Maintenance order details and engineer assignments are visible to broad staff roles.

These concerns depend on the intended business access policy. They are not classified as confirmed vulnerabilities.

## 10. Findings

| ID | Finding | Classification | Priority |
|---|---|---|---|
| A4-C01 | Crew medical-clearance information is visible to broad staff roles | Data-minimization / policy concern | Review |
| A4-M01 | Maintenance operational data is visible to broad staff roles | Data-minimization / policy concern | Review |
| A4-N01 | Notification DELETE controller test coverage should be verified | Testing gap | Low |
| A4-N02 | Notification JWT user-ID extraction is inconsistent | Hardening observation | Low |
| A4-J01 | JWT numeric user-ID claim should explicitly reject fractional or malformed values | Hardening observation | Low |

No confirmed critical or high-severity authorization vulnerability was identified in the reviewed areas.

## 11. Recommendations

1. Decide whether VIEWER should access Crew medical-clearance information.
2. Decide whether VIEWER should access Maintenance engineer assignments and detailed orders.
3. Verify notification DELETE controller test coverage.
4. Standardize notification user-ID extraction.
5. Strengthen JWT user-ID claim validation.
6. Verify PASSENGER denial on Crew and Maintenance endpoints.
7. Continue end-to-end authorization testing across complete workflows.

## 12. Limitations

This report records the source-code and test evidence reviewed during A4.

It does not claim that every endpoint, dependency, deployment configuration, or runtime behavior in the complete application was exhaustively tested.

The report distinguishes implemented behavior from intended policy. Access rules that are correctly enforced may still require business-policy review.

## 13. Final Conclusion

A4 API Authorization & Access Control Audit is complete.

The reviewed implementation contains centralized role-based authorization, administrative governance protections, and ownership checks for passenger, booking, and notification data.

No confirmed BOLA/IDOR or administrative privilege escalation was identified in the reviewed areas.

Crew medical-clearance and Maintenance data visibility remain policy decisions. JWT claim validation and notification test coverage have been recorded as hardening items.

**Recommended next milestone:** A4.2 Security Hardening & Test Gaps, followed by A5 Integration & End-to-End Verification.
