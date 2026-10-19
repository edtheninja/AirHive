# AirHive Load-Test Baseline and Dependency-Failure Tests

## Part 1 — Load-Test Baseline

**Test date:** 2026-10-18  
**Environment:** Local Docker Compose development environment  
**Target:** `GET /api/airports`  
**Authentication:** Development JWT  
**Test tool:** k6

### Objective

Establish an initial performance and correctness baseline before controlled
dependency-failure testing.

### Results

| Metric | Baseline: 5 VUs | Staged: up to 20 VUs | Staged: up to 50 VUs |
|---|---:|---:|---:|
| Test duration | 1m | 3m15.8s | 4m30.9s |
| Peak virtual users | 5 | 20 | 50 |
| Completed requests | 295 | 2,458 | 8,352 |
| Request rate | 4.916/s | 12.552/s | 30.828/s |
| Average latency | 15.21 ms | 7.62 ms | 6.81 ms |
| Median latency | 15.20 ms | 7.79 ms | 6.21 ms |
| p95 latency | 20.69 ms | 11.53 ms | 13.58 ms |
| Maximum latency | 35.50 ms | 46.69 ms | 31.10 ms |
| Failed requests | 0 / 295 | 0 / 2,458 | 0 / 8,352 |
| Checks passed | 885 / 885 | 7,374 / 7,374 | 25,056 / 25,056 |
| Thresholds | Passed | Passed | Passed |

### Initial thresholds

- Failed-request rate below 1%.
- p95 request duration below 1,000 ms.
- Check success rate above 99%.

All three runs passed these thresholds.

### Load-test scope and limitations

- Tests covered one authenticated, read-only endpoint.
- The endpoint returned HTTP 200 and the expected list of six airports.
- The 20-user and 50-user tests used staged concurrency, not a constant
  concurrency level for the entire test.
- Results apply only to this endpoint and this local environment.
- These tests do not establish whole-system capacity or production performance.

---

## Part 2 — Redis Failure-Injection and Recovery

**Test date:** 2026-10-19  
**Environment:** Local Docker Compose development environment  
**Target:** Authenticated `GET /api/airports`  
**Dependency tested:** Redis  
**Data dependency:** PostgreSQL  
**Test type:** Manual dependency-failure injection

### Objective

Verify that the airport API continues to serve data when Redis is unavailable
and resumes normal operation after Redis recovers.

### Preconditions

- Backend was running and healthy.
- Redis was healthy before the test.
- PostgreSQL remained running.
- An existing development JWT was used for the authenticated request.

### Procedure

1. Confirmed the backend and Redis were running.
2. Stopped the Redis container.
3. Requested `GET /api/airports` with a valid development JWT.
4. Recorded the HTTP status and returned record count.
5. Restarted Redis.
6. Waited for Redis to become healthy.
7. Repeated the airport API request to verify recovery.

### Results

| Check | Observed result | Outcome |
|---|---|---|
| Backend before test | Healthy | Passed |
| Redis before test | Healthy | Passed |
| Redis during outage | Stopped intentionally | Expected |
| Airport API during outage | HTTP 200 | Passed |
| Records returned during outage | 6 | Passed |
| Redis after restart | Healthy | Passed |
| Airport API after recovery | HTTP 200 | Passed |
| Records returned after recovery | 6 | Passed |

### Interpretation

The tested airport read path remained functional while Redis was unavailable.
The cache error handler allowed the service to return its underlying result,
and the API returned all six airport records.

After Redis was restarted and returned to healthy status, the airport API
continued to return HTTP 200 with all six records.

This verifies fallback and recovery for the tested endpoint. It does not prove
that all cache-dependent operations or application workflows behave identically.

### Health-check observations

During the Redis outage, the liveness endpoint returned HTTP 200 and the
readiness endpoint also returned HTTP 200.

The current readiness check therefore does not indicate Redis unavailability.
Whether Redis should be included in readiness depends on the application's
operational policy and whether requests can safely continue without the cache.

The backend container health check uses the Actuator liveness endpoint. This
helps avoid restarting the application solely because a dependency is down,
but liveness alone does not establish that all application dependencies are
available.

### Limitations

- This was a manual test of one authenticated, read-only endpoint.
- Recovery time was not measured precisely.
- No load was applied during the Redis outage.
- Cache eviction and cache-clear failures were not exercised in this test.
- No database outage, database restore, migration rollback, or full-system
  failure scenario is claimed by this report.
- The results apply to the local development environment, not production.

### Follow-up tests

- Decide and document the intended readiness policy for Redis.
- Verify cache invalidation behavior when Redis becomes unavailable during
  a write operation.
- Exercise database failure and recovery scenarios.
- Verify migration, backup, restore, and rollback procedures.
- Repeat dependency-failure tests under controlled load and record recovery
  timing.
