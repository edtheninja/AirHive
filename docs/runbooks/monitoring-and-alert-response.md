# Monitoring and Alert Response Runbook

## 1. Purpose

This runbook defines how to investigate monitoring issues, respond to alerts, verify service health, and confirm recovery in AirHive.

## 2. Monitoring Architecture

AirHive uses the following observability components:

- **Spring Boot Actuator:** Exposes application health and diagnostic endpoints.
- **OpenTelemetry:** Collects application telemetry.
- **OpenTelemetry Collector:** Receives telemetry and forwards metrics to Grafana Cloud.
- **Grafana Cloud:** Provides metrics visualization and alerting.

The telemetry pipeline is configured through the application's management settings and `observability/otel-collector.yml`.

## 3. Initial Alert Response

When an alert fires:

1. Record the alert name, firing time, severity, and affected service.
2. Check the status of all Compose services.
3. Review backend and OpenTelemetry Collector logs.
4. Check application readiness and liveness.
5. Identify whether the issue affects application availability, telemetry collection, or both.
6. Avoid restarting or modifying services until the likely cause is understood.

## 4. Check Service Status

Run these commands from the AirHive project root:

```bash
docker compose ps
```

Review recent backend logs:

```bash
docker compose logs --tail=200 backend
```

Review OpenTelemetry Collector logs:

```bash
docker compose logs --tail=200 otel-collector
```

Check PostgreSQL and Redis logs if the symptoms indicate a dependency issue:

```bash
docker compose logs --tail=100 postgres
docker compose logs --tail=100 redis
```

Look for repeated exceptions, connection failures, authentication errors, failed health checks, and telemetry export errors.

## 5. Verify Application Health

Check the general health endpoint:

```bash
curl -i http://localhost:8080/actuator/health
```

Check readiness:

```bash
curl -i http://localhost:8080/actuator/health/readiness
```

Check liveness:

```bash
curl -i http://localhost:8080/actuator/health/liveness
```

Interpretation:

- **HTTP 200:** The endpoint reports a successful response. Inspect its response body for the actual health status.
- **HTTP 503 on readiness:** The application may be running but unable to serve traffic safely because a required dependency or readiness condition is unavailable.
- **Liveness UP:** The application process is alive; this does not guarantee every dependency is healthy.
- **Connection refused or timeout:** Check container status, port mapping, startup logs, and resource pressure.

## 6. Investigate Missing Metrics

If Grafana Cloud stops receiving metrics:

1. Check that the backend and Collector containers are running.
2. Review backend logs for telemetry initialization or export errors.
3. Review Collector logs for receiver, exporter, authentication, or connectivity errors.
4. Verify that the configured Grafana Cloud remote-write endpoint and credentials are available to the Collector without exposing secrets.
5. Confirm that the expected metrics are present in Grafana Cloud.
6. Check whether the issue affects all metrics or only a specific metric or service.

Do not publish Grafana credentials, tokens, or environment-variable values in logs, screenshots, or incident reports.

## 7. Investigate Missing Traces

If traces are missing:

1. Confirm that the backend tracing configuration is enabled.
2. Inspect backend logs for tracing initialization or export errors.
3. Check Collector logs for OTLP receiver and trace exporter activity.
4. Confirm that the configured trace destination is reachable.
5. Generate a controlled application request and check whether new spans appear at the configured destination.

The current Collector configuration includes a debug exporter for traces. Debug output can help establish whether spans reach the Collector, but it does not by itself prove that traces are available in a tracing backend.

## 8. Alert Recovery Verification

After addressing the suspected cause:

1. Recheck Compose service status.
2. Recheck application readiness and liveness.
3. Confirm that metrics are arriving in Grafana Cloud.
4. Confirm that the alert transitions from firing to its configured recovered state.
5. Verify that the recovery remains stable rather than immediately recurring.
6. Record the corrective action and evidence.

An alert should not be considered resolved solely because a container restarted.

## 9. Escalation

Escalate the incident if:

- Readiness remains unavailable after dependency recovery.
- Backend startup repeatedly fails.
- Database errors persist or data integrity may be affected.
- Telemetry export failures continue after configuration and connectivity checks.
- An alert repeatedly fires after recovery.
- A change or rollback could risk persistent database data.

Include timestamps, affected services, relevant sanitized log excerpts, health-check results, the actions taken, and the observed outcome.

## 10. Incident Closure Checklist

- [ ] The alert's cause has been identified or documented as unknown.
- [ ] The affected services are healthy.
- [ ] Readiness and liveness have been checked.
- [ ] Metrics and alert recovery have been verified where relevant.
- [ ] No persistent data was