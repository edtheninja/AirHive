# AirHive Observability and Alerting Runbook

## 1. Purpose

This runbook helps operators diagnose AirHive backend health, JVM heap
pressure, missing JVM metrics, and notification delivery problems.

It covers the Docker Compose deployment and Grafana Cloud metrics and
alerting configuration.

## 2. Current Observability Architecture

- Spring Boot backend exposes application metrics through Micrometer.
- The backend sends OTLP metrics to the OpenTelemetry Collector over HTTP.
- The Collector receives OTLP metrics on port 4318 and exports them to
  Grafana Cloud Prometheus Remote Write.
- The Collector also has a debug exporter for local diagnostic output.
- PostgreSQL stores application data.
- Redis provides caching.
- Grafana Cloud evaluates alert rules and delivers notifications through
  configured contact points.

Compose services currently include:

- backend
- frontend
- postgres
- redis
- otel-collector

Grafana Cloud is the remote metrics and alerting platform; it is not a
local Grafana Compose service.

## 3. Current Alert Rules

### AirHive-JVM-Heap-High

Purpose: Detect sustained high JVM heap usage.

Expected configuration:
- PromQL calculates heap usage as a fraction of the maximum heap.
- Threshold: greater than 0.85 (85%).
- Evaluation interval: 1 minute.
- Pending period: 5 minutes.

Interpretation:
- Normal: the alert condition is not currently sustained.
- Pending: the condition has been met but the pending period has not elapsed.
- Firing: the condition has remained true for the pending period.
- Error: inspect the query and rule evaluation details.
- No Data: investigate telemetry availability and the rule's no-data policy.

A Normal state does not guarantee the application is free of memory issues.

### AirHive-JVM-Metrics-Missing

Purpose: Detect the absence of the expected backend JVM heap metric series.

PromQL:

    absent(jvm_memory_used_bytes{area="heap", job="airhive-backend"})

Expected configuration:
- Condition: above 0.
- Evaluation interval: 1 minute.
- Pending period: 5 minutes.
- Notification destination: the configured AirHive contact point.

A Normal state indicates that this alert is not currently detecting missing
matching series. It does not prove that every application metric or every
part of the telemetry pipeline is healthy.

## 4. First Response: Check Docker Services

Run from the repository root:

    docker compose ps

Inspect recent service logs:

    docker compose logs --since=15m backend
    docker compose logs --since=15m postgres
    docker compose logs --since=15m redis
    docker compose logs --since=15m otel-collector

Look for repeated restarts, connection failures, database errors, failed
health checks, OTLP export errors, authentication errors, and timeouts.

Do not paste environment files, access tokens, passwords, or unredacted
connection strings into incident reports.

## 5. High JVM Heap Usage

When AirHive-JVM-Heap-High fires:

1. Record the alert start time, current value, and affected labels.
2. Check backend container status:

       docker compose ps backend

3. Inspect recent backend logs:

       docker compose logs --since=15m backend

4. Inspect JVM memory metrics in Grafana Cloud Explore:

       jvm_memory_used_bytes{area="heap", job="airhive-backend"}

       jvm_memory_max_bytes{area="heap", job="airhive-backend"}

5. Check whether usage is sustained or a short-lived spike.
6. Correlate with request volume, application errors, latency, and recent
   deployments if those measurements are available.
7. If usage continues rising or the service becomes unhealthy, follow the
   team's incident escalation procedure before restarting anything.

Recovery:
- Confirm heap usage falls below the alert threshold.
- Confirm the alert returns to Normal.
- Verify the backend is healthy and critical application workflows work.
- Record the cause, corrective action, and recovery time.

Do not increase heap limits or restart the service without checking available
container memory and the likely cause.

## 6. Missing JVM Metrics

When AirHive-JVM-Metrics-Missing fires:

1. Check backend status:

       docker compose ps backend

2. Inspect backend telemetry configuration and logs:

       docker compose logs --since=15m backend

3. Inspect Collector logs:

       docker compose logs --since=15m otel-collector

4. In Grafana Cloud Explore, query:

       jvm_memory_used_bytes{area="heap", job="airhive-backend"}

5. Check whether other backend metrics are still arriving.
6. Look for OTLP connection errors, Collector restarts, remote-write
   authentication failures, and exporter errors.
7. Restore the failed component or configuration, then verify that fresh
   metrics resume.

Recovery:
- Confirm the query returns current data.
- Confirm AirHive-JVM-Metrics-Missing returns to Normal.
- Confirm AirHive-JVM-Heap-High evaluates without errors.
- Record the outage duration and root cause if known.

The `absent()` rule detects missing matching series. It does not independently
detect stale values that remain queryable or every possible telemetry failure.

## 7. Notification Delivery Failure

1. Open Grafana Cloud Alerting and inspect the alert rule state and history.
2. Open the configured contact point and run its test action if available.
3. Check the destination for delivery and inspect any Grafana delivery error.
4. Confirm the alert rule or notification policy routes to the intended
   contact point.
5. Verify notification-policy matching, grouping, silences, and mute timings.
6. Correct the destination or routing configuration and retest.

Recovery:
- A contact-point test is delivered successfully.
- The intended alert rule routes to that contact point.
- A real rule notification is confirmed when a safe, controlled test is
  available.

A successful contact-point test alone does not prove every alert rule is
routed correctly.

## 8. PostgreSQL or Redis Problems

Inspect service status:

    docker compose ps postgres redis

Inspect logs:

    docker compose logs --since=15m postgres
    docker compose logs --since=15m redis

Check for failed health checks, connection refusal, authentication errors,
repeated restarts, and storage or memory pressure.

Before recovery actions:
- Determine whether the failure is isolated or affects the backend.
- Preserve relevant logs and incident timestamps.
- Do not delete volumes or run destructive database commands as a first
  response.
- Follow the database backup and recovery procedure before modifying
  persistent data.

After recovery, confirm the backend reconnects and the affected application
workflow succeeds.

## 9. OpenTelemetry Collector Problems

Inspect:

    docker compose ps otel-collector
    docker compose logs --since=15m otel-collector

Verify:
- The Collector container remains running.
- OTLP receiver endpoints are configured as expected.
- The backend can reach the Collector using the Compose service name.
- Grafana Cloud Remote Write credentials and endpoint configuration are valid.
- Exporter logs do not show persistent authentication, timeout, or retry
  errors.
- Metrics continue appearing in Grafana Cloud Explore.

Do not print or share secret environment variable values while debugging.

## 10. Incident Record Template

For each incident, record:

- Incident ID:
- Date and time detected (include timezone):
- Alert or symptom:
- Affected services:
- Impact:
- Relevant metric values and alert labels:
- Diagnostic commands and sanitized findings:
- Root cause (or unknown):
- Corrective action:
- Recovery verification:
- Time service recovered:
- Follow-up action and owner:

## 11. Verification Status and Limitations

Verified during the current observability work:
- Docker Compose backend, PostgreSQL, Redis, and Collector were running;
  backend, PostgreSQL, and Redis health were confirmed at that time.
- Backend JVM metrics were queryable in Grafana Cloud.
- The synthetic alert was observed firing and recovering.
- The Grafana contact-point test notification was delivered.
- AirHive-JVM-Heap-High was created and reported Normal.
- AirHive-JVM-Metrics-Missing was created and reported Normal.

Not yet established solely by these checks:
- Real JVM heap alert firing and recovery under a controlled heap condition.
- End-to-end notification delivery from each real alert rule.
- Detection of all telemetry outages or stale metrics.
- Automated database migrations, rollback, and restore procedures.
- Load and failure-injection behavior.
- Persistent trace storage and a complete distributed tracing workflow.

Update this section only when new verification evidence is available.
