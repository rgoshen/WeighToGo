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
