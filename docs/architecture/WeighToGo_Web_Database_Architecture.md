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
| `email` | CITEXT | NOT NULL | — | Unique; case-insensitive (ADR-0009) |
| `password_hash` | TEXT | NOT NULL | — | bcrypt hash; raw value never stored |
| `display_name` | VARCHAR(50) | NOT NULL | — | |
| `is_active` | BOOLEAN | NOT NULL | TRUE | Soft-disable without deletion |
| `failed_login_count` | INTEGER | NOT NULL | 0 | Lockout counter |
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
