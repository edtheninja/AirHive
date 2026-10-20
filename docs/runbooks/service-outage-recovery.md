# AirHive Service Outage and Recovery Runbook

## 1. Purpose

This runbook describes how to diagnose and recover the AirHive Docker Compose
services after an outage.

Services:
- Backend: `airhive-backend` — port 8080
- Frontend: `airhive-frontend` — port 3000
- PostgreSQL: `airhive-postgres` — port 5432
- Redis: `airhive-redis` — port 6379
- OpenTelemetry Collector: `airhive-otel-collector` — ports 4317 and 4318

## 2. Safety rules

- Run Docker Compose commands from the project root.
- Inspect service status and logs before restarting anything.
- Recover only the affected service when practical.
- Never use `docker compose down -v` as a recovery shortcut.
- Never remove `postgres_data` or `redis_data` during recovery.
- Do not print `.env` contents, JWT tokens, passwords, or Grafana tokens.
- Do not rebuild or roll back the database schema as an initial recovery step.
- Record the time, symptoms, actions, and verification results.

## 3. Check service status

Run:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps
```

Check recent logs for the affected service:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker logs \
  --tail 100 airhive-backend
```

Replace `airhive-backend` with the affected container name when appropriate.

For the Collector, inspect recent telemetry/export errors:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker logs \
  --since 15m airhive-otel-collector 2>&1
```

## 4. Check backend health

Check overall health:

```bash
/usr/bin/curl -sS -w '\nHTTP %{http_code}\n' \
  http://localhost:8080/actuator/health
```

Check readiness:

```bash
/usr/bin/curl -sS -w '\nHTTP %{http_code}\n' \
  http://localhost:8080/actuator/health/readiness
```

Check liveness:

```bash
/usr/bin/curl -sS -w '\nHTTP %{http_code}\n' \
  http://localhost:8080/actuator/health/liveness
```

Expected healthy result: HTTP 200 and `{"status":"UP"}`.

Interpretation:
- Liveness DOWN: investigate whether the backend process is functioning.
- Readiness DOWN: inspect dependencies, especially PostgreSQL, and backend logs.
- Liveness UP with readiness DOWN: the process may be alive but unable to serve
  requests reliably.
- A successful health check alone does not prove every business API works.

## 5. Recover the backend

First inspect status and logs. If the backend is stopped or unresponsive and its
dependencies are healthy, restart only the backend:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose \
  restart backend
```

Wait for it to become healthy:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps
```

Then repeat the overall health, readiness, and liveness checks.

If the backend repeatedly becomes unhealthy, do not repeatedly restart it
without diagnosis. Inspect its logs and confirm PostgreSQL, Redis, and the
Collector status.

## 6. Recover Redis

Inspect Redis status and logs:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps redis

/Applications/Docker.app/Contents/Resources/bin/docker logs \
  --tail 100 airhive-redis
```

If Redis is stopped, start only Redis:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose start redis
```

Wait for Redis to report healthy:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps
```

Then verify backend liveness, readiness, and a representative authenticated,
read-only business API request.

AirHive has previously remained live and served an airport API request during
a Redis outage. Verify current behavior rather than assuming every endpoint
will work without Redis.

Do not delete or recreate the Redis data volume as a routine recovery action.

## 7. Recover PostgreSQL

Inspect PostgreSQL status and logs:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps postgres

/Applications/Docker.app/Contents/Resources/bin/docker logs \
  --tail 100 airhive-postgres
```

If PostgreSQL is stopped, start only PostgreSQL:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose start postgres
```

Wait for its health check to pass:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps
```

Then verify backend readiness:

```bash
/usr/bin/curl -sS -w '\nHTTP %{http_code}\n' \
  http://localhost:8080/actuator/health/readiness
```

Expected result after successful recovery: HTTP 200 and `{"status":"UP"}`.

Follow this with a representative authenticated, read-only business API
request. A successful cached response is not sufficient proof that database
queries work.

If PostgreSQL does not recover, inspect its logs and available disk space.
Preserve the existing `postgres_data` volume. Do not delete the volume, run
destructive database commands, or reinitialize the database to make the health
check pass.

## 8. Recover the OpenTelemetry Collector

Inspect Collector status and logs:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps otel-collector

/Applications/Docker.app/Contents/Resources/bin/docker logs \
  --since 15m airhive-otel-collector 2>&1
```

If the Collector is stopped, start only that service:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose start otel-collector
```

Check the logs for incoming metrics/traces and remote-write export failures.
Confirm that new metrics become visible in Grafana Cloud.

Previously observed debug-exporter batches do not, by themselves, prove that
the remote-write destination accepted the data.

## 9. Recover the frontend

Inspect status and logs:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose ps frontend

/Applications/Docker.app/Contents/Resources/bin/docker logs \
  --tail 100 airhive-frontend
```

If the frontend is stopped, restart only the frontend:

```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose restart frontend
```

Verify that `http://localhost:3000` loads and that critical frontend-to-backend
flows work.

## 10. Recovery verification checklist

After recovery, verify and record:

- [ ] All expected containers are running.
- [ ] PostgreSQL reports healthy.
- [ ] Redis reports healthy.
- [ ] Backend liveness returns HTTP 200 / UP.
- [ ] Backend readiness returns HTTP 200 / UP.
- [ ] A representative authenticated, read-only API request succeeds.
- [ ] Frontend loads and can reach the backend.
- [ ] New telemetry is visible in Grafana Cloud.
- [ ] Relevant Grafana alerts return to Normal.
- [ ] Logs show no continuing startup, dependency, or export failures.

## 11. Escalation

If recovery fails:

1. Record the time and affected service.
2. Capture container status and relevant recent logs.
3. Note the last successful health check and the failing check.
4. Preserve database and Redis volumes.
5. Avoid unplanned rebuilds, schema changes, credential rotations, or destructive
   cleanup.
6. Use the deployment rollback runbook only after identifying a deployment
   regression and confirming the rollback target.

## 12. Incident record

For each incident, record:

- Incident date/time:
- Affected service:
- User-visible symptoms:
- Initial container status:
- Relevant log findings:
- Recovery action:
- Recovery verification:
- Root cause (confirmed or still unknown):
- Follow-up action:
