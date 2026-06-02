# ADR-0025: Constraint Hardening Strategy

- **Date**: 2026-06-02
- **Status**: Accepted

## Context

The Android predecessor deferred all data-integrity validation to the application layer; the web rebuild closes this gap. Migrations `0002`–`0006` added CHECK constraints and partial indexes at the DB level, but the corresponding ORM models (`WeightEntryModel`, `GoalModel`, `AchievementModel`, `UserPreferenceModel`) did not declare those constraints in `__table_args__`. This means `Base.metadata.create_all` — used by the SQLite integration suite — builds a schema without the value-domain guards, silently degrading the fidelity of unit-level constraint tests. `AuditLogModel` (migration `0009`) first established the correct dual-declaration pattern; this ADR retroactively documents that pattern and extends it.

GH-98 audited all seven tables on four axes: missing NOT NULL, value-domain CHECK gaps, column-type tightness, and read-path index coverage. Two new constraints were identified as genuine gaps: `achievements.threshold IS NULL OR threshold > 0` and `goals.target_date IS NULL OR target_date >= '2020-01-01'`. One read-path index was missing: `goals (user_id, created_at DESC)` for all-goals listing.

## Decision

### Defense-in-depth policy

Database-level constraints are the last line of defense — application-layer validation (Pydantic, domain validators) fires first. DB constraints are a backstop that prevents invalid data from persisting through a use-case bypass or a direct DB insert. This explicitly closes the Android finding where `weight_value` and `weight_unit` had no DB-level enforcement.

### Dual-declaration pattern

Every CHECK constraint lives in two places:

1. **The ORM model's `__table_args__`** — enforced by `Base.metadata.create_all` on the SQLite integration suite.
2. **The Alembic migration** — enforced on PostgreSQL/production via `op.create_check_constraint`.

Constraints present in earlier migrations (`0002`, `0004`, `0005/0008`, `0006`) but absent from the ORM models are backfilled to `__table_args__` in the same PR as migration `0010`. No new migration is needed for these — the constraints already exist in the database; the backfill closes only the SQLite test-fidelity gap.

### Audit methodology

Each table was evaluated on four axes: missing NOT NULL, value-domain CHECK gaps, column-type tightness, and read-path index coverage. Covered items are documented as "covered" — not silent omissions. Only genuine gaps received new DDL.

### Audit summary

| Table | Finding | Action |
|---|---|---|
| `users` | No value-domain CHECK gaps worth migration complexity (app layer sufficient) | No change |
| `refresh_tokens` | Token lifecycle is tightly application-controlled | No change |
| `weight_entries` | 5 CHECKs in migration `0002` absent from model | Model backfill |
| `goals` | Direction invariant in `0004` absent from model; `target_date` bound missing | Model backfill + new CHECK in `0010` |
| `achievements` | Type-valid CHECK in `0005/0008` absent from model; `threshold > 0` missing | Model backfill + new CHECK in `0010` |
| `user_preferences` | Key/value CHECKs in `0006` absent from model | Model backfill |
| `audit_log` | Both CHECKs already in model `__table_args__` (established pattern) | No change |

### New constraints in migration `0010`

- `achievements_threshold_positive`: `threshold IS NULL OR threshold > 0` — threshold is NULL for `goal_reached` and a positive decimal for `milestone`/`streak`; zero or negative threshold breaks milestone-detection math.
- `goals_target_date_epoch`: `target_date IS NULL OR target_date >= '2020-01-01'` — dialect-portable lower-bound epoch; rejects clearly impossible historical dates without restricting future target dates.

### New index in migration `0010`

- `idx_goals_user_created (user_id, created_at DESC)` — `idx_goals_one_active_per_user` is a partial unique index (WHERE is_active = TRUE) that covers only active-goal lookups. A full user-goals listing query has no index without this composite. Follows the `(user_id, <timestamp> DESC)` pattern established by `weight_entries` (migration `0007`) and `audit_log` (migration `0009`).

## Rationale

### Why a fixed lower-bound epoch for `target_date` instead of cross-column `>= created_at`

A cross-column check (`target_date >= created_at`) compares a `Date` to a timezone-aware `DateTime`. SQLite stores `DateTime` as text; the dialect-portable cast is fragile and untested in the existing harness. A fixed epoch (`>= '2020-01-01'`) is dialect-portable, safe against all existing data (the application launched in 2026), and sufficient to catch clearly bogus historical values.

### Why NOT enforce `refresh_tokens.expires_at > issued_at`

Token generation is tightly application-controlled (one code path). The application-layer invariant is tested. Adding a cross-column DATE/DATETIME CHECK in a migration requires careful portability analysis for marginal defensive value.

### Why NOT enforce additional `users` CHECKs

Email uniqueness is enforced by CITEXT unique index. `failed_login_count >= 0` is a theoretical overflow the application layer already prevents. Adding these constraints adds migration risk with negligible data-integrity improvement.

## Consequences

- **Positive**: SQLite integration suite now enforces the same value-domain rules as PostgreSQL, eliminating a silent fidelity gap. New constraints prevent two previously unguarded data-corruption classes. The goal-listing query gains an index.
- **Negative**: The backfill adds no new DB constraints so the PostgreSQL schema is unchanged for four tables; only the SQLite test harness gains new enforcement.
- **Follow-ups**: Migration-discipline review (GH-99) verifies the downgrade paths for `0010`.

## Alternatives Considered

| Option | Rejected because |
|---|---|
| Model-only backfill without migration for new constraints | New constraints would only be enforced on SQLite (via `create_all`), not on PostgreSQL/production — incomplete defense |
| Migration-only for new constraints, no model update | SQLite integration suite would not enforce the new rules; violates the dual-declaration pattern |
| Cross-column `target_date >= created_at` | SQLite Date/DateTime portability is fragile; fixed epoch achieves the same practical safety |
| `expires_at > issued_at` for refresh_tokens | Application layer already enforces this; migration risk outweighs defensive value |
