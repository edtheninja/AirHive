# Deployment Rollback Runbook

## 1. Purpose

This runbook defines a controlled procedure for reverting an AirHive deployment when a release introduces application failures, health-check regressions, or other critical issues.

The rollback process prioritizes service recovery, reproducible application versions, and preservation of persistent data.

## 2. Scope

AirHive runs as a Docker Compose application with the following services:

- Backend: Spring Boot
- Frontend: React-based web application
- Database: PostgreSQL
- Cache: Redis
- Observability: OpenTelemetry Collector

This runbook covers application rollback. Database schema rollback is a separate operation and must not be assumed to happen automatically when an older application version is deployed.

## 3. Safety Rules

Before starting:

- Confirm that the incident warrants rollback and identify the affected release.
- Record the current Git commit and deployment state.
- Identify a known-good application version.
- Check whether the release introduced database migrations or incompatible schema changes.
- Preserve PostgreSQL and Redis data.
- Do not run `docker compose down -v`.
- Do not delete or recreate persistent volumes as a rollback shortcut.
- Do not run `git reset --hard` or discard working-tree changes as part of routine rollback.
- Do not expose `.env` credentials in terminal output, logs, or incident records.

**Important:** The current development image tags, such as `airhive-backend:dev` and `airhive-frontend:dev`, are mutable. They are not sufficient by themselves to identify a reliable previous release. Use immutable, version-specific image tags or a verified saved image.

## 4. Capture the Current State

Run these commands from the AirHive project root.

Check the Git state:

```bash
git status --short
git log -5 --oneline
git rev-parse HEAD
```

Check Compose service status:

```bash
docker compose ps
```

Record the images currently used by the services:

```bash
docker compose images
```

Review recent backend logs:

```bash
docker compose logs --tail=150 backend
```

Record the timestamp, affected service, observed symptoms, and reason for considering rollback.

Do not stage or discard unrelated working-tree files during incident response.

## 5. Identify the Known-Good Release

Before rollback, establish the exact known-good version from a verified Git commit, release tag, or previously built image.

Confirm that:

- The version was previously tested successfully.
- Its backend and frontend images are available or can be rebuilt reproducibly.
- The image tags identify the intended version.
- The version is compatible with the current database schema.

Do not assume that the previous Git commit is automatically a safe rollback target.

If no known-good image is available, build and validate the intended version separately before switching the running deployment. Avoid overwriting the current image tag until the replacement has been verified.

## 6. Assess Database Compatibility

Check whether the release changed database schemas or migrations.

AirHive uses Flyway for database migrations. Flyway migrations should be treated as forward-moving schema changes unless a specific, tested rollback procedure exists.

Before deploying an older application version:

1. Determine which migrations have already been applied.
2. Check whether the older application can operate with the current schema.
3. If the rollback involves database changes or data risk, arrange a verified logical database backup using the approved PostgreSQL backup procedure.
4. Confirm that the backup can be restored before relying on it.

Do not assume that reverting application code reverses database migrations.

Do not automatically run reverse SQL or restore an older database snapshot without assessing data loss, compatibility, and recovery requirements.

If schema compatibility is uncertain, stop and escalate before changing the running deployment.

## 7. Roll Back the Application

Use the approved deployment mechanism to switch to the verified known-good backend and frontend image tags.

Before applying any command:

- Confirm the exact service names and image tags.
- Ensure the Compose configuration references the intended images.
- Preserve the current deployment configuration and record the change.
- Ensure that the rollback does not recreate or delete persistent database or Redis volumes.

Do not blindly execute `docker compose up --build` as a rollback method. It may rebuild the current source rather than deploy the known-good version.

Do not edit the working Git branch or overwrite existing image tags merely to force a rollback.

If a tested rollback procedure is unavailable, pause and prepare a controlled deployment change instead of improvising during the incident.

## 8. Verify Recovery

After the known-good application version is deployed, verify:

### Container status

```bash
docker compose ps
```

### Backend health

```bash
curl -i http://localhost:8080/actuator/health
```

### Readiness

```bash
curl -i http://localhost:8080/actuator/health/readiness
```

### Liveness

```bash
curl -i http://localhost:8080/actuator/health/liveness
```

### Representative API

```bash
curl -i http://localhost:8080/api/airports
```

The API may require authentication or return a response dependent on the application's security configuration. Interpret the HTTP status accordingly rather than treating every non-200 response as a deployment failure.

Also verify:

- The frontend loads and can communicate with the backend.
- PostgreSQL and Redis remain healthy.
- Critical application workflows function.
- Metrics continue arriving in Grafana Cloud.
- Relevant alerts return to their configured recovered state.

A healthy container alone is not sufficient evidence that the rollback succeeded.

## 9. Rollback Failure

If the rollback does not restore service:

1. Preserve logs and the current deployment state.
2. Record the exact image tags and configuration used.
3. Check application/database compatibility.
4. Avoid repeated deployment changes without a clear hypothesis.
5. Escalate if persistent data, migration state, or service integrity is at risk.

Do not delete persistent volumes to attempt to repair a failed rollback.

## 10. Post-Rollback Actions

After recovery:

- Record the failed release and known-good version.
- Document the reason for rollback.
- Record health-check, API, frontend, and observability verification results.
- Identify the change responsible for the incident, if known.
- Create follow-up work to prevent recurrence.
- Preserve relevant logs and test evidence.
- Plan a controlled redeployment after the underlying issue is corrected.

## 11. Rollback Completion Checklist

- [ ] The incident and rollback decision are documented.
- [ ] The known-good version is identified and verified.
- [ ] Database migration compatibility has been assessed.
- [ ] Persistent volumes and application data have been preserved.
- [ ] The intended backend and frontend versions are running.
- [ ] Health, readiness, and liveness have been checked.
- [ ] A representative API request has been checked.
- [ ] Frontend and critical workflows have been verified.
- [ ] Metrics and relevant alert recovery have been confirmed.
- [ ] Follow-up actions have been recorded.

## 12. Future Improvements

The following improvements will make rollback more reproducible:

- Build immutable image tags based on Git commits or release versions.
- Record image digests alongside deployment metadata.
- Automate deployment smoke tests.
- Test rollback procedures in a non-production environment.
- Document and rehearse PostgreSQL backup and restoration.
- Define explicit compatibility rules for Flyway migrations.
- Retain known-good images and deployment configuration for recovery.
