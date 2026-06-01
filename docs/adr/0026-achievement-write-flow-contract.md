# ADR-0026: Achievement Write-Flow Contract

- **Date**: 2026-05-31
- **Status**: Accepted

## Context

Achievement detection today runs on **one write path only**: `POST /weight-entries`.
Detection runs synchronously inside the handler via `DetectAchievements.execute()` inside
a `session.begin_nested()` savepoint (ADR-0019).

The other two weight-entry mutations — `PUT /weight-entries/{id}` and
`DELETE /weight-entries/{id}` — only invalidate the dashboard cache.  They do **not**
run achievement detection.

No revoke or delete path exists in `IAchievementRepository`.  The interface exposes:
`save`, `get_recorded_thresholds`, `get_recorded_streak_thresholds`, `get_by_id`,
`has_goal_reached_been_recorded`, and `list_for_user` — no `delete` or `revoke` method.

Detection is already idempotent at three layers:
1. In-memory `frozenset` guard (loaded once per request from the DB).
2. `begin_nested()` savepoint catching `IntegrityError` on a duplicate insert.
3. DB partial unique indexes (per ADR-0019): one for milestone rows, one for
   `goal_reached` rows.

Weight entries are soft-deleted (no hard DELETE reaches the DB); the `achievements`
table has no FK pointing at `weight_entries`, so no cascade can fire.  Achievements are
scoped to `goal_id`, not to a specific entry.

SRS FR-G-4 and FR-Ach-1 used broad "causes a goal to be reached" phrasing without
specifying create-only.  ADR-0019 established synchronous on-create detection but did
not formalise the whole write-flow contract — leaving the behaviour of update/delete
paths implicit.  This ADR makes the contract explicit.

## Decision

**Create-only & permanent.**  Achievements are earned at the moment a weight entry is
**created** and are a permanent record; they are never revoked or recomputed on update or
delete.

This matches current implementation behaviour exactly.  The resolution is
documentation + contract-locking tests, with **no production logic change**.

## Rationale

- **Gamification badges are conventionally permanent.**  Revoking earned recognition is
  poor UX; users expect that a badge they earned stays earned even if they later edit the
  entry that triggered it.
- **Option B requires net-new revoke machinery** (see Alternatives) for behaviour that
  is arguably undesirable in the first place.  KISS/YAGNI resolution.
- **ADR-0019 already established synchronous on-create detection.**  This ADR extends
  that decision to an explicit whole-write-flow contract, closing the gap left by
  FR-G-4 / FR-Ach-1's create-or-update ambiguity.
- The three-layer idempotency already in place (frozenset, savepoint, DB indexes) means
  create-only is safe even under concurrent requests — no additional guard is needed.

## Consequences

- **Positive:**
  - No net-new production code required; the contract matches the current
    implementation.
  - Simplest possible answer for downstream maintainers — no revoke machinery to
    reason about.
  - Achievement rows remain stable for display and analytics.
- **Negative (accepted, not bugs):**
  1. Editing an existing entry **upward** across a 5/10/25/50 lb threshold awards
     no new milestone achievement — only `POST` (create) awards milestones.
  2. Deleting or editing **downward** the entry that originally triggered the goal
     leaves `Goal.is_achieved = True` and the achievement rows in place — they are
     not revoked.
- **Follow-ups:** Contract-locking tests for `PUT` and `DELETE` paths should be added
  in the same PR to prove no silent achievement side-effects occur on update or delete.

## Scope Boundary

This ADR bounds itself to the **weight-entry write surface**.  For completeness, the
permanence guarantee also holds across the goal lifecycle:

- **Goal abandon / new active goal:** Goals are soft-abandoned (`is_active = False`),
  never hard-deleted (FR-G-5 retains history).  The `achievements` FK `ON DELETE
  CASCADE` never fires in practice; prior-goal achievements persist, scoped to their
  original `goal_id`.
- **Goal target/date edit:** `UpdateGoal` changes only mutable fields and does **not**
  recompute or revoke achievements; existing rows stand.

These are explicit in-scope decisions (permanence holds across the goal lifecycle), not
silent omissions.

## Alternatives Considered

- **Option B — Full recompute on update/delete:** Revoke achievement rows, reset
  `Goal.is_achieved`, rerun streak teardown on every mutating write.  Rejected: no
  `IAchievementRepository.delete()` exists (net-new revoke machinery required);
  revoking earned recognition is poor UX; streak teardown is complex and fragile;
  rejected by YAGNI.
- **Option C — Hybrid create+update:** Run detection on create AND update (not delete).
  Rejected: still requires goal achieved-state reconciliation on weight-down edits;
  partial coverage with the same UX confusion as Option B; adds complexity without a
  clear product benefit.

## Relationship to ADR-0019

This ADR extends ADR-0019's on-create-detection decision to an explicit whole-write-flow
contract, covering what happens across the full weight-entry write surface (create,
update, delete).
