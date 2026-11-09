# Passenger role and record ownership rollout

## Behavior introduced by V2

- Public signup creates `PASSENGER` accounts. It never accepts a client-selected role.
- Existing `VIEWER` accounts are reclassified to `PASSENGER` because the previous public signup flow created `VIEWER` accounts.
- `ADMIN`, `OPERATOR`, and `VIEWER` retain read access to operational booking/passenger lists. Existing write permissions remain unchanged.
- A `PASSENGER` can read only `bookings` and `passengers` rows whose `user_id` references that authenticated account.
- Passenger accounts are denied the broad operational GET API.
- JWT validation checks the account's enabled state, current role, and database user ID on each token validation. Tokens issued with the old `VIEWER` role stop working after the migration; the user must sign in again.
- An administrator can change a user's role with `PATCH /api/users/{id}/role`, for example `{"role":"VIEWER"}` after verifying that the account belongs to read-only staff.

## Data preservation and legacy records

V2 adds nullable `user_id` columns and foreign keys. It does not delete bookings, passenger records, users, or their business fields. Existing bookings and passenger rows remain unassigned (`user_id IS NULL`) because the previous schema has no reliable ownership link. Those rows remain visible to operational staff, but are not exposed to any passenger.

Do **not** automatically assign records by matching passenger names, flight numbers, PNRs supplied by a client, or other unverified input. Before enabling passenger self-service against legacy data, an authorized reconciliation process must verify each ownership assignment and set `bookings.user_id` / `passengers.user_id` to the correct `app_users.id`.

The current booking and passenger controllers are read-only. This patch establishes the ownership model and secure reads; it does not add booking creation, booking modification, or an ownership-claim endpoint.

## Verification included

- Migration test: V1-to-V2 reclassifies a legacy `VIEWER` account while preserving an existing booking and passenger row unassigned.
- Booking/passenger integration tests: a passenger sees only rows linked to their own account.
- Security tests: a passenger can access only the booking/passenger endpoints, not broad operational read endpoints.
- JWT tests: a token with an old role or mismatched user ID is rejected.
- Signup test: public signup assigns `PASSENGER`.
- User controller tests: only admins can restore an account to `VIEWER`.

Run the backend tests with the repository's configured Java 26 toolchain and Docker available for Testcontainers.
