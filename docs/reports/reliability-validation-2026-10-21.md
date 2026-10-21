# AirHive Reliability Validation Report

**Report date:** 2026-10-21  
**Project:** AirHive airline management system  
**Repository branch:** `main`  
**Scope:** Automated tests, database resilience, API load testing, monitoring, rollback rehearsal, and resource observations.

## 1. Executive Summary

AirHive has completed substantial reliability validation across automated tests, database recovery, read-only API load testing, observability, and operational documentation.

The available evidence supports successful validation of the recorded test runs and an isolated rollback candidate startup. It does not establish production-scale capacity or a complete production rollback.

Docker resource-monitoring samples have been committed, but their overlap with a valid load-test window has not been established. One later k6 run completed with zero HTTP requests and zero checks; that run is invalid as performance evidence.

## 2. Automated Tests and Database Migrations

### Recorded results

- Backend automated test suite: **341 tests passed, zero failures, zero errors, zero skipped** in the latest full run on 2026-10-21.
- The Redis cache outage/recovery integration test passed in both the latest isolated run and the subsequent full backend suite.
- An earlier full-suite run exposed an intermittent Redis cache recovery assertion failure. The latest runs passed, but the cause of the intermittent failure has not been conclusively established.
- Flyway configuration and H2 test isolation were implemented in commit `b61e7fc`.
- A fresh-database migration validation was performed against the isolated database `airhive_flyway_test_20261019`.
- The existing database was baselined at Flyway version 1 without intentionally replacing its existing data.
- Redis outage/recovery integration coverage was added in commit `8117088`.
- Additional test coverage was recorded in commit `c145ba9`.

The latest full-suite result above was obtained on the report date. Other migration, coverage, load-test, and resilience results in this report remain historical results unless explicitly identified otherwise.

## 3. API Load Testing

All results below are from recorded local test runs. They should not be interpreted as production capacity guarantees.

### 3.1 Mixed read-only API test — earlier run

| Metric | Recorded result |
|---|---:|
| Virtual users | 50 |
| Duration | 90 seconds |
| HTTP requests | 2,700 |
| Throughput | 29.75 requests/second |
| Average latency | 7.06 ms |
| Median latency | 5.75 ms |
| p90 latency | 14.02 ms |
| p95 latency | 16.45 ms |
| Maximum latency | 45.19 ms |
| Failed HTTP requests | 0% |
| Checks | 5,400 / 5,400 passed |

### 3.2 Mixed read-only API test — later valid run

The recorded valid run exercised ten read-only API endpoints with 50 virtual users for 90 seconds.

| Metric | Recorded result |
|---|---:|
| Virtual users | 50 |
| Duration | 90 seconds |
| HTTP requests | 673,413 |
| Throughput | 7,481.86 requests/second |
| Average latency | 6.63 ms |
| Median latency | 5.57 ms |
| p90 latency | 11.11 ms |
| p95 latency | 13.92 ms |
| Maximum latency | 88.06 ms |
| Failed HTTP requests | 0% |
| Checks | 673,413 / 673,413 passed |

All ten configured endpoints returned HTTP 200 during that run.

The unusually high throughput is specific to the tested local environment and workload. Caching, dataset size, hardware, and the absence of write-heavy operations affect these results. Further testing is required before making capacity claims.

### 3.3 Invalid k6 run

A subsequent 90-second run reported:

- 50 virtual users.
- 1,993,258 iterations.
- Zero HTTP requests.
- Zero checks.
- Zero bytes sent and received.

This run is **invalid for API performance measurement**. The iteration count does not represent requests served. Its zero-valued latency and apparently passing thresholds must not be used as evidence of performance.

A separate short diagnostic and earlier valid runs did produce HTTP requests, but the cause of the intermittent zero-request behavior remains unresolved.

## 4. Readiness, Liveness, and Database Resilience

Recorded fault-injection results established the following behavior:

- During a PostgreSQL outage, readiness returned HTTP 503.
- Liveness continued returning HTTP 200.
- Readiness recovered after PostgreSQL became available again.
- `/api/airports` continued returning HTTP 200 during the observed outage, likely due to caching; this does not establish that every endpoint remains available during a database outage.
- Redis outage/recovery integration testing was recorded as completed.

The separation of readiness and liveness helps distinguish inability to serve normal traffic from a process that is still alive.

## 5. Observability and Alerting

Recorded observability work includes:

- OpenTelemetry Collector configuration in `observability/otel-collector.yml`.
- OTLP receiver ports 4317 and 4318.
- Metrics forwarding to Grafana Cloud Prometheus remote-write.
- Verification of alert firing and subsequent recovery.
- Operational runbooks for service outage recovery and monitoring response.

The protected `/actuator/prometheus` endpoint returning HTTP 401 is intentional under the current security configuration; this report does not recommend exposing it publicly.

Relevant runbooks:

- `docs/runbooks/service-outage-recovery.md`
- `docs/runbooks/monitoring-and-alert-response.md`
- `docs/runbooks/deployment-rollback.md`

## 6. Rollback Rehearsal

An isolated rollback-candidate rehearsal was recorded on 2026-10-20.

Recorded evidence includes:

- Candidate backend image built from source commit `027a7fa`.
- An isolated database, `airhive_rollback_test_20261020`, initialized using the candidate migration.
- Twelve database tables confirmed in that isolated database.
- Candidate startup completed and its liveness endpoint returned HTTP 200.
- Readiness and overall health were subsequently confirmed by the operator.
- The active deployment remained unaffected during the rehearsal.

This was an isolated startup and health rehearsal, not proof of a complete production rollback.

Still unverified:

- Compatibility with a production database schema and its data.
- Restoration of representative authenticated business workflows.
- End-to-end rollback timing and recovery objectives.
- Compatibility of older application images with every newer schema state.

The isolated test database was retained; this report does not call for deleting it.

## 7. Docker Resource Utilization

The following resource-monitoring artifact was committed:

`reports/resource-tests/docker-stats-load-20261021-093703.csv`

Commit: `2e66e09`  
Commit message: `test: record Docker resource utilization samples`

The CSV contains Docker resource samples. However, the monitoring interval has not been correlated with a confirmed successful k6 load-test interval. Therefore:

- Its samples are retained as observational evidence.
- They must not be presented as verified resource usage under load.
- No load-attributed CPU or memory averages or peaks are claimed here.
- A future measurement should capture the k6 start/end times and Docker statistics in a synchronized window.

Other resource CSVs and a k6 summary JSON remain separate, untracked artifacts and are not claimed as part of this report's evidence.

## 8. Evidence Classification

| Area | Status | Qualification |
|---|---|---|
| Recorded backend test suite | Verified historical result | 341 passed in the recorded run |
| Fresh-database migration | Completed | Isolated database validation |
| PostgreSQL readiness/recovery | Verified in recorded test | Readiness degraded and recovered |
| Redis recovery | Completed | Integration test recorded |
| Mixed read-only API load | Valid recorded runs | Local workload only |
| Monitoring and alert recovery | Verified by operator | Alert firing and recovery observed |
| Rollback candidate | Partially verified | Isolated startup and health only |
| Docker resource samples | Captured | Load-window correlation not verified |
| Zero-request k6 run | Invalid | Excluded from performance conclusions |
| Production capacity | Not established | Requires representative workload and environment |
| Full production rollback | Not established | Schema and business workflow compatibility remain unverified |

## 9. Remaining Work

1. Diagnose and prevent intermittent zero-request k6 runs.
2. Repeat resource monitoring with a confirmed successful load-test window if load-attributed resource figures are required.
3. Validate rollback against representative database states and authenticated workflows in an isolated environment.
4. Record architecture decisions and operational trade-offs.
5. Perform a final readiness review and confirm repository, deployment, monitoring, and rollback documentation are synchronized.

## 10. Conclusion

AirHive has meaningful recorded evidence of automated test success, migration validation, database resilience, local read-only API performance, monitoring, and isolated rollback-candidate health.

The evidence supports progress toward deployment readiness, but it does not prove production capacity or complete production rollback safety. Resource utilization under load remains unverified, and the zero-request k6 run is explicitly excluded from performance claims.
