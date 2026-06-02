# ADR-0024: Audit Log Structure

**Status:** Accepted  
**Date:** 2026-06-02  
**Deciders:** Richard Goshen

---

## Context

SRS §8.2.7 deferred the `audit_log` schema to M4. The table must record authentication outcomes and data mutations for security/compliance review, retain entries past actor deletion, and store no unmasked PII or secrets (NFR-Priv-1). It is the seventh and final table in the schema.

---

## Decision

### Schema

| Column | Type | Notes |
|---|---|---|
| `audit_id` | BIGSERIAL PK | |
| `user_id` | BIGINT NULL → `users(user_id)` ON DELETE SET NULL | Retain trail when actor deleted |
| `event_type` | VARCHAR(50) NOT NULL | CHECK-constrained fixed taxonomy |
| `resource_type` | VARCHAR(30) NULL | Data-mutation events only |
| `resource_id` | BIGINT NULL | PK of affected resource |
| `request_id` | VARCHAR(64) NULL | X-Request-ID correlation header |
| `ip_address` | String(45) | INET on PostgreSQL; String(45) covers IPv4 + IPv6 |
| `metadata` | JSON | JSONB on PostgreSQL; JSON for SQLite portability |
| `created_at` | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

### Event Taxonomy (CHECK constraint `audit_log_event_type_valid`)

`auth.register`, `auth.login_succeeded`, `auth.login_failed`, `auth.logout`, `auth.token_refreshed`, `auth.token_reuse_detected`, `auth.account_locked`, `weight_entry.created`, `weight_entry.updated`, `weight_entry.deleted`, `goal.created`, `goal.updated`, `goal.abandoned`, `preference.updated`.

Administrative actions are absent: SRS §1.3 defines no admin role, so no administrative events can occur.

### Indexes

- `idx_audit_log_user_created (user_id, created_at DESC)` — user-scoped audit queries ordered newest-first.
- `idx_audit_log_event_type_created (event_type, created_at DESC)` — event-type filtering ordered newest-first.

### Failure handling asymmetry

**Data mutations** (weight entries, goals, preferences): the audit write shares the operation's request-scoped SQLAlchemy session. If the audit write fails, the whole operation rolls back (fail-closed). An unaudited mutation is a compliance failure worse than a visible error.

**Auth events** (register, login, logout, refresh): the audit write is wrapped in explicit `try/except Exception` that emits a structlog `warning` and never re-raises (fail-open). An audit hiccup must never turn a valid login into a 500.

### Append-only invariant

`SqlAlchemyAuditRepository` exposes only `add()`. No update or delete paths exist at any layer. This invariant cannot be enforced by a DB trigger without adding DDL complexity; it is instead enforced by the port contract and documented here.

### ON DELETE SET NULL vs CASCADE

ON DELETE SET NULL retains the audit trail when a user account is deleted, which is the forensically correct choice. CASCADE would silently destroy evidence of the actor's historical activity.

### VARCHAR CHECK vs Postgres ENUM for event_type

`VARCHAR(50)` with a CHECK constraint is chosen over a Postgres ENUM because:
1. CHECK constraints are portable to SQLite (the integration test harness).
2. Adding a new event type requires only a new migration adding a value to the CHECK — not an `ALTER TYPE` DDL operation which blocks writes on PostgreSQL.
3. The domain `AuditEventType` StrEnum is the canonical taxonomy; the DB constraint is a defence-in-depth guard.

### Synchronous composition-root wiring vs middleware

Audit writes are injected at the interface/composition-root layer (the same pattern as `DetectAchievements` in ADR-0019), not in middleware. Middleware fires after the response is sent and cannot share the primary operation's session, losing the atomicity guarantee for data mutations. Composition-root wiring keeps the audit calls explicit and visible, preserves session atomicity, and allows resource_id/resource_type context to flow naturally from the endpoint.

### Cross-dialect portability

`ip_address` uses `String(45)` (covers both IPv4 `nnn.nnn.nnn.nnn` and IPv6 full form). `metadata` uses `JSON` rather than `JSONB`. Both are stored as the portable type in the model; a future migration can ALTER to `INET`/`JSONB` on PostgreSQL if query operators on those types become needed.

### auth.token_reuse_detected status

This event type is reserved in the CHECK constraint taxonomy but is not currently wired at the composition root. The `RefreshSession` use case raises `InvalidCredentialsError` for both "token not found" and "replay detected" scenarios with no distinguishing exception type. The value is reserved for a future enhancement when a distinct exception is introduced.

---

## Alternatives Considered

| Option | Rejected because |
|---|---|
| ENUM for event_type | Not portable to SQLite; ALTER TYPE blocks writes on PostgreSQL |
| CASCADE on user delete | Destroys audit evidence; ON DELETE SET NULL preserves it |
| Post-response middleware for all audit writes | Loses session atomicity for data mutations; resource context not available |
| Fail-closed for auth events | Auth hiccup would block valid logins; best-effort telemetry is sufficient for auth events |

---

## Consequences

- Every in-scope operation writes exactly one audit row.
- User deletion does not destroy the audit trail.
- No unmasked PII is stored; failed logins carry a masked email in `metadata` via `mask_pii()`.
- The `audit` domain is never imported by other domain packages (enforced by import-linter, verified in `test_import_contracts.py`).
