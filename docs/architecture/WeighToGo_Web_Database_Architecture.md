# Weigh to Go! — Web Database Architecture

> Authoritative schema reference for the PostgreSQL web rebuild (CS 499, Milestone 4).
> For the original Android SQLite schema see
> [WeighToGo_Database_Architecture.md](./WeighToGo_Database_Architecture.md).

---

## Table of Contents

1. [Overview](#1-overview)
2. [Entity Relationship Diagram](#2-entity-relationship-diagram)
3. [Schema](#3-schema)
   - 3.1 [users](#31-users)
   - 3.2 [refresh_tokens](#32-refresh_tokens)
   - 3.3 [weight_entries](#33-weight_entries)
   - 3.4 [goals](#34-goals)
   - 3.5 [achievements](#35-achievements)
   - 3.6 [user_preferences](#36-user_preferences)
   - 3.7 [audit_log](#37-audit_log)
4. [Constraints Catalogue](#4-constraints-catalogue)
5. [Index Catalogue](#5-index-catalogue)
6. [Audit Log Design](#6-audit-log-design)
7. [Migration History](#7-migration-history)
8. [Connection and Pooling Policy](#8-connection-and-pooling-policy)
9. [Historical Note](#9-historical-note)

---

## 1. Overview

| Property | Value |
|----------|-------|
| Database | PostgreSQL (psycopg driver) |
| ORM | SQLAlchemy 2 (`mapped_column` / `DeclarativeBase`) |
| Migration tool | Alembic |
| Tables | 7 |
| Migration chain | `0001` – `0010` (fully round-trip verified in CI) |
| SRS reference | §8.2, §8.3, §8.4, §13.3.1 #5 |

| Table | Purpose |
|-------|---------|
| `users` | Account management; CITEXT email; lockout tracking |
| `refresh_tokens` | JWT refresh token store; rotation + family revocation |
| `weight_entries` | Daily weight log; soft-delete; domain-validated |
| `goals` | Active weight goal per user; direction-invariant constraints |
| `achievements` | Milestones, streaks, goal completions; idempotency indexes |
| `user_preferences` | EAV key-value; four preference keys |
| `audit_log` | Security + data-mutation trail; append-only; 14-event taxonomy |

---

## 2. Entity Relationship Diagram

```mermaid
erDiagram
    users ||--o{ refresh_tokens : "has"
    users ||--o{ weight_entries : "logs"
    users ||--o{ goals : "sets"
    users ||--o{ achievements : "earns"
    users ||--o{ user_preferences : "configures"
    users ||--o{ audit_log : "generates"
    goals ||--o{ achievements : "triggers"

    users {
        bigint user_id PK
        citext email
        text password_hash
        varchar_50 display_name
        boolean is_active
        integer failed_login_count
        timestamptz locked_until
        timestamptz created_at
        timestamptz updated_at
        timestamptz last_login_at
    }

    refresh_tokens {
        bigint token_id PK
        bigint user_id FK
        text token_hash
        uuid family_id
        timestamptz issued_at
        timestamptz expires_at
        timestamptz revoked_at
        bigint replaced_by FK
    }

    weight_entries {
        bigint entry_id PK
        bigint user_id FK
        numeric_6_2 weight_value
        varchar_3 weight_unit
        date observation_date
        varchar_500 notes
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
        timestamptz deleted_at
    }

    goals {
        bigint goal_id PK
        bigint user_id FK
        numeric_6_2 target_value
        varchar_3 target_unit
        numeric_6_2 start_value
        varchar_10 goal_type
        date target_date
        boolean is_active
        boolean is_achieved
        timestamptz achieved_at
        timestamptz created_at
        timestamptz updated_at
    }

    achievements {
        bigint id PK
        bigint user_id FK
        bigint goal_id FK
        varchar_20 achievement_type
        numeric_6_2 threshold
        timestamptz earned_at
    }

    user_preferences {
        bigint id PK
        bigint user_id FK
        varchar_40 pref_key
        varchar_40 pref_value
        timestamptz updated_at
    }

    audit_log {
        bigint audit_id PK
        bigint user_id FK
        varchar_50 event_type
        varchar_30 resource_type
        bigint resource_id
        varchar_64 request_id
        varchar_45 ip_address
        json metadata
        timestamptz created_at
    }
```

---

## 3. Schema

Each subsection lists columns with type, nullability, default, and notes. Foreign key
ON DELETE policies are stated explicitly. SRS references are per §8.2.

### 3.1 `users`

**SRS reference:** §8.2.1

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `user_id` | BIGINT | NOT NULL | autoincrement | PK |
| `email` | CITEXT | NOT NULL | — | Unique; case-insensitive (ADR-0009); format CHECK `users_email_format` |
| `password_hash` | TEXT | NOT NULL | — | bcrypt hash; raw value never stored |
| `display_name` | VARCHAR(50) | NOT NULL | — | Trimmed length 2–50 (`users_display_name_length`) |
| `is_active` | BOOLEAN | NOT NULL | TRUE | Soft-disable without deletion |
| `failed_login_count` | INTEGER | NOT NULL | 0 | Lockout counter; `>= 0` (`users_failed_login_nonneg`) |
| `locked_until` | TIMESTAMPTZ | NULL | — | NULL = not locked |
| `created_at` | TIMESTAMPTZ | NOT NULL | `now()` | |
| `updated_at` | TIMESTAMPTZ | NOT NULL | `now()` | |
| `last_login_at` | TIMESTAMPTZ | NULL | — | |

No outbound foreign keys. Root of all CASCADE chains.

---

### 3.2 `refresh_tokens`

**SRS reference:** §8.2.2

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `token_id` | BIGINT | NOT NULL | autoincrement | PK |
| `user_id` | BIGINT | NOT NULL | — | FK → `users`; CASCADE delete |
| `token_hash` | TEXT | NOT NULL | — | SHA-256 hex of raw token; UNIQUE |
| `family_id` | UUID | NOT NULL | — | Family-level revocation (ADR-0013) |
| `issued_at` | TIMESTAMPTZ | NOT NULL | `now()` | |
| `expires_at` | TIMESTAMPTZ | NOT NULL | — | |
| `revoked_at` | TIMESTAMPTZ | NULL | — | NULL = still valid |
| `replaced_by` | BIGINT | NULL | — | Self-ref FK; rotation chain |

**Foreign keys:**
- `user_id` → `users(user_id)` ON DELETE CASCADE
- `replaced_by` → `refresh_tokens(token_id)` (nullable self-reference)

---

### 3.3 `weight_entries`

**SRS reference:** §8.2.3

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `entry_id` | BIGINT | NOT NULL | autoincrement | PK |
| `user_id` | BIGINT | NOT NULL | — | FK → `users`; CASCADE |
| `weight_value` | NUMERIC(6,2) | NOT NULL | — | Precision preserves two decimals (SRS §3.2) |
| `weight_unit` | VARCHAR(3) | NOT NULL | — | `'lbs'` or `'kg'` |
| `observation_date` | DATE | NOT NULL | — | No time component |
| `notes` | VARCHAR(500) | NULL | — | |
| `created_at` | TIMESTAMPTZ | NOT NULL | `now()` | |
| `updated_at` | TIMESTAMPTZ | NOT NULL | `now()` | |
| `is_deleted` | BOOLEAN | NOT NULL | FALSE | Soft-delete flag |
| `deleted_at` | TIMESTAMPTZ | NULL | — | NULL when `is_deleted = FALSE` |

**Foreign keys:**
- `user_id` → `users(user_id)` ON DELETE CASCADE

---

### 3.4 `goals`

**SRS reference:** §8.2.4

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `goal_id` | BIGINT | NOT NULL | autoincrement | PK |
| `user_id` | BIGINT | NOT NULL | — | FK → `users`; CASCADE |
| `target_value` | NUMERIC(6,2) | NOT NULL | — | |
| `target_unit` | VARCHAR(3) | NOT NULL | — | `'lbs'` or `'kg'` |
| `start_value` | NUMERIC(6,2) | NOT NULL | — | Baseline at goal creation |
| `goal_type` | VARCHAR(10) | NOT NULL | — | `'lose'` or `'gain'` |
| `target_date` | DATE | NULL | — | Optional deadline |
| `is_active` | BOOLEAN | NOT NULL | TRUE | |
| `is_achieved` | BOOLEAN | NOT NULL | FALSE | |
| `achieved_at` | TIMESTAMPTZ | NULL | — | NULL until achievement recorded |
| `created_at` | TIMESTAMPTZ | NOT NULL | `now()` | |
| `updated_at` | TIMESTAMPTZ | NOT NULL | `now()` | |

**Foreign keys:**
- `user_id` → `users(user_id)` ON DELETE CASCADE

---

### 3.5 `achievements`

**SRS reference:** §8.2.5

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `id` | BIGINT | NOT NULL | autoincrement | PK |
| `user_id` | BIGINT | NOT NULL | — | FK → `users`; CASCADE |
| `goal_id` | BIGINT | NOT NULL | — | FK → `goals`; CASCADE |
| `achievement_type` | VARCHAR(20) | NOT NULL | — | `'goal_reached'`, `'milestone'`, or `'streak'` |
| `threshold` | NUMERIC(6,2) | NULL | — | NULL for `goal_reached`; positive value for `milestone`/`streak` |
| `earned_at` | TIMESTAMPTZ | NOT NULL | `now()` | |

**Foreign keys:**
- `user_id` → `users(user_id)` ON DELETE CASCADE
- `goal_id` → `goals(goal_id)` ON DELETE CASCADE

---

### 3.6 `user_preferences`

**SRS reference:** §8.2.6

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `id` | BIGINT | NOT NULL | autoincrement | PK |
| `user_id` | BIGINT | NOT NULL | — | FK → `users`; CASCADE |
| `pref_key` | VARCHAR(40) | NOT NULL | — | One of four valid keys |
| `pref_value` | VARCHAR(40) | NOT NULL | — | Validated per key (see constraints) |
| `updated_at` | TIMESTAMPTZ | NOT NULL | `now()` | |

**Foreign keys:**
- `user_id` → `users(user_id)` ON DELETE CASCADE

**Valid `pref_key` values:** `weight_unit`, `notify_achievement`, `notify_milestone`, `notify_streak`

**Valid `pref_value` values:** `'lbs'`/`'kg'` for `weight_unit`; `'true'`/`'false'` for `notify_*` keys

---

### 3.7 `audit_log`

**SRS reference:** §8.2.7

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `audit_id` | BIGINT | NOT NULL | autoincrement | PK |
| `user_id` | BIGINT | NULL | — | FK → `users`; SET NULL (retains trail on user deletion) |
| `event_type` | VARCHAR(50) | NOT NULL | — | 14-value CHECK taxonomy |
| `resource_type` | VARCHAR(30) | NULL | — | Data-mutation events only |
| `resource_id` | BIGINT | NULL | — | PK of affected resource |
| `request_id` | VARCHAR(64) | NULL | — | X-Request-ID for correlation (NFR-O-2) |
| `ip_address` | VARCHAR(45) | NULL | — | Covers IPv4 and IPv6 |
| `metadata` | JSON | NULL | — | Arbitrary payload (e.g. masked email for failed logins) |
| `created_at` | TIMESTAMPTZ | NOT NULL | `now()` | Server default; no ORM override |

**Foreign keys:**
- `user_id` → `users(user_id)` ON DELETE SET NULL — critical: preserves the audit trail when an actor's account is deleted

---

## 4. Constraints Catalogue

All named CHECK and UNIQUE constraints across all tables. Format: constraint name |
table | rule | rationale | ADR. Adding a new constraint requires a migration; adding
a new value to a closed enum also requires a migration.

### `users` (4 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `users_email_format` | `email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'` | Rejects malformed addresses at the DB level beneath application validation | migration 0001 (no ADR) |
| `users_display_name_length` | `length(trim(display_name)) BETWEEN 2 AND 50` | Requires a non-trivial display name after trimming whitespace; rejects blank or single-character names | migration 0001 (no ADR) |
| `users_failed_login_nonneg` | `failed_login_count >= 0` | The lockout counter can never go negative | migration 0001 (no ADR) |
| `uq_users_email` | UNIQUE (`email`) | One account per email; the unique, case-insensitive (CITEXT) email is the sole login identifier | ADR-0009 |

### `refresh_tokens` (2 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `refresh_tokens_expiry_after_issuance` | `expires_at > issued_at` | A token cannot expire before it is issued | migration 0001 (no ADR) |
| `uq_refresh_tokens_hash` | UNIQUE (`token_hash`) | One row per stored token hash; the SHA-256 hash is the lookup key for the opaque token presented on refresh | migration 0001 (no ADR) |

### `weight_entries` (5 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `weight_entries_value_positive` | `weight_value > 0` | Prevents physically impossible zero or negative weights | ADR-0025 |
| `weight_entries_value_max` | `weight_value <= 1500` | Upper-bounds to realistic human range in lbs | ADR-0025 |
| `weight_entries_unit_valid` | `weight_unit IN ('lbs', 'kg')` | Closes the unit enum at the DB level; rejects any unknown unit string | ADR-0025 |
| `weight_entries_observation_not_future` | `observation_date <= CURRENT_DATE` | No future-dated entries; a weight must have been observed before it is recorded | ADR-0025 |
| `weight_entries_deletion_consistency` | `(is_deleted = FALSE AND deleted_at IS NULL) OR (is_deleted = TRUE AND deleted_at IS NOT NULL)` | Keeps the soft-delete pair in sync; prevents half-deleted rows | ADR-0025 |

### `goals` (9 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `goals_goal_type_valid` | `goal_type IN ('lose', 'gain')` | Closes the goal-type enum at the DB level | ADR-0025 |
| `goals_target_value_positive` | `target_value > 0` | Target weight must be positive | ADR-0025 |
| `goals_target_value_max` | `target_value <= 1500` | Upper-bounds target to realistic human range | ADR-0025 |
| `goals_start_value_positive` | `start_value > 0` | Baseline weight must be positive | ADR-0025 |
| `goals_start_value_max` | `start_value <= 1500` | Upper-bounds baseline to realistic range | ADR-0025 |
| `goals_target_unit_valid` | `target_unit IN ('lbs', 'kg')` | Prevents unknown unit strings entering goal rows | ADR-0025 |
| `goals_achieved_consistency` | `(is_achieved = FALSE AND achieved_at IS NULL) OR (is_achieved = TRUE AND achieved_at IS NOT NULL)` | Keeps the achievement pair in sync; parallels the `deletion_consistency` pattern | ADR-0025 |
| `goals_direction_invariant` | `(goal_type = 'lose' AND target_value < start_value) OR (goal_type = 'gain' AND target_value > start_value)` | Makes the direction of a goal self-proving; rejects contradictory configurations at the DB level (migration 0004) | ADR-0025 |
| `goals_target_date_epoch` | `target_date IS NULL OR target_date >= '2020-01-01'` | Rejects clearly impossible historical deadlines using a dialect-portable epoch; cross-column comparison rejected for SQLite portability (migration 0010) | ADR-0025 |

### `achievements` (2 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `achievements_type_valid` | `achievement_type IN ('goal_reached', 'milestone', 'streak')` | Closes the type enum; widened from 2 → 3 values in migration 0008 to add `streak` | ADR-0025 |
| `achievements_threshold_positive` | `threshold IS NULL OR threshold > 0` | NULL is reserved for `goal_reached` rows; milestone and streak thresholds must be positive (migration 0010) | ADR-0025 |

### `user_preferences` (3 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `user_preferences_unique_key` | UNIQUE (`user_id`, `pref_key`) | One row per preference per user; this is the upsert conflict target for preference writes | ADR-0020 |
| `user_preferences_key_valid` | `pref_key IN ('weight_unit', 'notify_achievement', 'notify_milestone', 'notify_streak')` | Closes the preference key set at the DB level; adding a new key requires a migration | ADR-0020 |
| `user_preferences_value_valid` | `(pref_key = 'weight_unit' AND pref_value IN ('lbs', 'kg')) OR (pref_key LIKE 'notify_%' AND pref_value IN ('true', 'false'))` | Validates value domain per key type; prevents invalid key/value combinations | ADR-0020 |

### `audit_log` (2 constraints)

| Constraint | Rule | Rationale | ADR |
|------------|------|-----------|-----|
| `audit_log_event_type_valid` | `event_type IN ('auth.register', 'auth.login_succeeded', 'auth.login_failed', 'auth.logout', 'auth.token_refreshed', 'auth.token_reuse_detected', 'auth.account_locked', 'weight_entry.created', 'weight_entry.updated', 'weight_entry.deleted', 'goal.created', 'goal.updated', 'goal.abandoned', 'preference.updated')` | Closes the 14-value event taxonomy derived from `AuditEventType(StrEnum)`; adding a new event type requires a migration | ADR-0024 |
| `audit_log_resource_consistency` | `resource_id IS NULL OR resource_type IS NOT NULL` | Prevents orphaned `resource_id` without a `resource_type`; the nullable FK requires explicit pairing | ADR-0024 |

---

## 5. Index Catalogue

All named indexes across all tables. Format: index name | table | columns | type |
query served | ADR. "Partial" means a `WHERE` predicate restricts the indexed rows.
"Partial UNIQUE" enforces uniqueness only within that predicate.

| Index | Table | Columns | Type | Query served | ADR |
|-------|-------|---------|------|--------------|-----|
| `idx_users_email_active` | `users` | `(email)` WHERE `is_active = TRUE` | Partial | Active-account lookup by email during login; excludes soft-disabled accounts | migration 0001 (no ADR) |
| `idx_refresh_tokens_user_active` | `refresh_tokens` | `(user_id)` WHERE `revoked_at IS NULL` | Partial | Lists a user's currently-valid refresh tokens; excludes revoked rows | migration 0001 (no ADR) |
| `idx_refresh_tokens_family` | `refresh_tokens` | `(family_id)` | Single-column | Family-level revocation: revoke every token in a family when token reuse is detected | migration 0001 (no ADR) |
| `idx_weight_entries_user_date_active` | `weight_entries` | `(user_id, observation_date)` WHERE `is_deleted = FALSE` | Partial UNIQUE | Prevents duplicate entries per (user, date); drives upsert conflict target | ADR-0021 |
| `idx_weight_entries_user_observation_desc` | `weight_entries` | `(user_id, observation_date DESC)` WHERE `is_deleted = FALSE` | Partial composite | Paginated entry list ordered most-recent-first for the authenticated user | ADR-0021 |
| `idx_weight_entries_user_created_at` | `weight_entries` | `(user_id, created_at DESC)` WHERE `is_deleted = FALSE` | Partial composite | NFR-P-3: dashboard load must retrieve 30 entries in < 100 ms; partial predicate excludes soft-delete tombstones (migration 0007) | ADR-0021 |
| `idx_goals_one_active_per_user` | `goals` | `(user_id)` WHERE `is_active = TRUE` | Partial UNIQUE | Enforces at most one active goal per user; drives the active-goal lookup | migration 0003 (no ADR) |
| `idx_goals_user_created` | `goals` | `(user_id, created_at DESC)` | Composite | Full goal history listing; `idx_goals_one_active_per_user` is partial and misses inactive goals (migration 0010) | ADR-0025 |
| `idx_achievements_unique_milestone` | `achievements` | `(goal_id, achievement_type, threshold)` WHERE `threshold IS NOT NULL` | Partial UNIQUE | Idempotency: at most one milestone/streak row per (goal, type, threshold) tuple | ADR-0019 |
| `idx_achievements_unique_goal_reached` | `achievements` | `(goal_id, achievement_type)` WHERE `threshold IS NULL` | Partial UNIQUE | Idempotency: at most one `goal_reached` row per goal | ADR-0019 |
| `idx_achievements_user_earned` | `achievements` | `(user_id, earned_at)` | Composite | Achievement history listing per user ordered by `earned_at` | migration 0005 (no ADR) |
| `idx_user_preferences_user` | `user_preferences` | `(user_id)` | Single-column | Loads all preference rows for a user in one indexed scan (EAV read path) | migration 0006 (no ADR) |
| `idx_audit_log_user_created` | `audit_log` | `(user_id, created_at DESC)` | Composite | Per-user audit trail lookup for security review queries | ADR-0024 |
| `idx_audit_log_event_type_created` | `audit_log` | `(event_type, created_at DESC)` | Composite | Per-event-type audit queries (e.g. all failed logins within a time window) | ADR-0024 |

---

## 6. Audit Log Design

### Event taxonomy

The `event_type` column accepts exactly 14 values, defined by `AuditEventType(StrEnum)`
in `web/backend/src/weighttogo/audit/domain/entities.py`. The CHECK constraint is built
from the enum at model-definition time, so adding a new event type to the enum
automatically extends the constraint on the next migration.

| Event type | Category | Triggered by |
|------------|----------|--------------|
| `auth.register` | Auth | New user registration |
| `auth.login_succeeded` | Auth | Successful login |
| `auth.login_failed` | Auth | Failed login attempt |
| `auth.logout` | Auth | Explicit logout |
| `auth.token_refreshed` | Auth | Access token refresh |
| `auth.token_reuse_detected` | Auth | Reuse of a revoked refresh token (ADR-0013) |
| `auth.account_locked` | Auth | Lockout after `failed_login_count` threshold |
| `weight_entry.created` | Data | Weight entry creation |
| `weight_entry.updated` | Data | Weight entry update |
| `weight_entry.deleted` | Data | Soft-delete of a weight entry |
| `goal.created` | Data | Goal creation |
| `goal.updated` | Data | Goal update |
| `goal.abandoned` | Data | Goal deactivation |
| `preference.updated` | Data | User preference change |

### ON DELETE SET NULL rationale

`user_id` is nullable with ON DELETE SET NULL. This is the forensically correct
choice: CASCADE would silently destroy evidence of the actor's historical activity.
A row with `user_id = NULL` retains the `event_type`, timestamp, IP address, and
`request_id` even after the actor's account is deleted.

### Append-only invariant

`SqlAlchemyAuditRepository` exposes only `add()`. No update or delete paths exist at
any layer. The invariant is enforced by the port contract (ADR-0024) rather than a
database trigger. `metadata` uses `JSON` (not `JSONB`) and `ip_address` uses
`VARCHAR(45)` (not `INET`) for SQLite portability in the integration suite.

### Retention policy

No automated purge mechanism is implemented within this milestone scope. Rows
accumulate indefinitely. A production retention strategy (e.g. time-based
partitioning or scheduled DELETE) is out of capstone scope and intentionally
left for future work.

---

## 7. Migration History

All ten Alembic migrations in the chain, their purpose, the milestone that introduced
them, and the key schema objects they create or modify.

Migrations `0001`–`0008` were authored during Milestones 2 and 3. Milestone 4
(GH-97, GH-98, GH-99) added migrations `0009`–`0010` and audited the full chain
for reversibility. A full upgrade → downgrade → upgrade round-trip is verified in CI
via `.github/workflows/migration-ci.yml`.

| Migration | Purpose | Milestone | Key objects |
|-----------|---------|-----------|-------------|
| `0001` | Initial users + auth (CITEXT email, refresh tokens) | M2 | `users`, `refresh_tokens` |
| `0002` | Weight entries + composite performance indexes | M2 | `weight_entries`, `idx_weight_entries_user_date_active`, `idx_weight_entries_user_observation_desc` |
| `0003` | Goals table | M3 | `goals` |
| `0004` | Goals direction-invariant CHECK constraint | M3 | `goals_direction_invariant` |
| `0005` | Achievements table + indexes | M3 | `achievements` |
| `0006` | User preferences table (EAV storage, ADR-0020) | M3 | `user_preferences` |
| `0007` | Composite `created_at` index for NFR-P-3 (ADR-0021) | M3 | `idx_weight_entries_user_created_at` |
| `0008` | Widen `achievement_type` CHECK to include `'streak'` | M3 | `achievements_type_valid` |
| `0009` | Audit log table + indexes (ADR-0024) | M4 | `audit_log`, `idx_audit_log_user_created`, `idx_audit_log_event_type_created` |
| `0010` | Constraint hardening + goals listing index (ADR-0025) | M4 | `achievements_threshold_positive`, `goals_target_date_epoch`, `idx_goals_user_created` |

---

## 8. Connection and Pooling Policy

Source: SRS §8.4.

- **SSL:** Required in all environments. `sslmode=require` must appear in `DATABASE_URL`.
- **Driver:** `psycopg` (psycopg3; async-compatible).
- **Pool:** SQLAlchemy `QueuePool` with `pool_size=5` and `max_overflow=10` (development defaults).
- **Configuration:** The connection string is loaded exclusively from the `DATABASE_URL` environment variable; it is never hard-coded.
- **Production tuning:** Pool sizing for production load is out of scope for this milestone. The parameters are environment-variable overrideable for future tuning.

Example `DATABASE_URL`:
```
postgresql+psycopg://weighttogo:weighttogo@localhost:5432/weighttogo_dev?sslmode=require
```

---

## 9. Historical Note

This document describes the web rebuild schema (PostgreSQL). The original Android
application used a different SQLite schema documented in
[WeighToGo_Database_Architecture.md](./WeighToGo_Database_Architecture.md).
That document is retained as historical reference.

---

## ADR Cross-Reference

| ADR | Relevance to this document |
|-----|---------------------------|
| ADR-0009 | CITEXT email in `users` table |
| ADR-0012 | Shared `DeclarativeBase` across all bounded contexts |
| ADR-0013 | `refresh_tokens`: `family_id` + `replaced_by` for rotation and family revocation |
| ADR-0019 | Milestone detection algorithm; primary source for `idx_achievements_unique_*` indexes |
| ADR-0020 | `user_preferences` EAV design + constraint rationale |
| ADR-0021 | Composite/partial index strategy for `weight_entries` |
| ADR-0024 | `audit_log` structure, event taxonomy, append-only invariant, retention choice |
| ADR-0025 | Constraint hardening strategy (migrations 0002–0010 backfill + new CHECKs in 0010) |
| ADR-0026 | Achievement write-flow contract (create-only, permanent; references ADR-0019 for indexes) |
