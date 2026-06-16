# M4 Web App Quality Review

**Date:** 2026-06-03  
**Scope:** Web application only (`web/backend`, `web/frontend`, web CI/docs) at the Milestone Four completion state on `main` (tag `v0.3.0`). Android implementation code is excluded.  
**Checklist:** `docs/standards/cs499_code_review_checklist.md`  
**Review posture:** Checklist-based code review with explicit assumption challenges against the SRS, the M4 brief, ADRs, and the delivered M4 behavior, plus a live render of the running application.  
**Overall assessment:** Strong, honest Databases enhancement — the audit trail, constraint hardening, migration discipline, and operational runbook are real and well-tested. Not fully quality-complete: a pre-existing desktop layout defect in the application shell hides content under the sidebar, and the milestone's flagship database-architecture document has two completeness gaps against the actual schema.

## Documentation Reviewed

- `docs/standards/cs499_code_review_checklist.md`
- `docs/standards/M2_WEB_APP_QUALITY.md` and `docs/standards/M3_WEB_APP_QUALITY.md` (format and predecessor findings)
- `docs/specs/WeighToGo_Web_SRS_v2.md` (§7.8 Privacy, §8 Data Architecture — esp. §8.2.7 audit_log, §8.3 migrations, §8.4 connection policy — §13.3 Milestone 4 roadmap, §14 acceptance, Appendix A §17.2)
- `docs/plans/milestone-four-plan.md` (M4 scope and definition of done)
- `docs/architecture/WeighToGo_Web_Database_Architecture.md` (the M4 database-architecture deliverable)
- `docs/runbooks/backup-restore.md` and `web/backend/scripts/{backup.sh,restore.sh}`
- `docs/adr/0024-audit-log-structure.md`, `docs/adr/0025-constraint-hardening-strategy.md`, and `docs/adr/0026-achievement-write-flow-contract.md` (predecessor remediation context), plus `docs/adr/README.md`
- `web/backend/alembic/versions/0001`–`0010`, the per-context `infrastructure/models.py`, and the architecture import contracts in `web/backend/pyproject.toml`

## Verification Performed

Commands run from the local checkout against the merged M4 state. A local PostgreSQL 16 instance (`docker compose up -d postgres`) was provisioned so the runtime, the migrations, and the PostgreSQL-only tests could be exercised on the production engine rather than the SQLite proxy.

| Area | Command | Result |
| --- | --- | --- |
| Backend lint | `uv run ruff check .` | Passed |
| Backend format | `uv run ruff format --check .` | Passed, 259 files |
| Backend types | `uv run mypy` | Passed, 248 source files |
| Backend tests (SQLite) | `uv run pytest` | 682 passed, 11 skipped, 2 warnings; 98% total coverage |
| Migrations (PostgreSQL) | `uv run alembic upgrade head` | Clean apply `0008 → 0009 → 0010` on `postgres:16` |
| Backend tests (PostgreSQL) | `uv run pytest -m postgres` (with `WEIGHTTOGO_TEST_POSTGRES_DSN`) | 11 passed |
| Frontend lint | `npm run lint` | Passed, 0 warnings |
| Frontend format | `npm run format:check` | Passed |
| Frontend types | `npm run typecheck` | Passed |
| Frontend tests | `npm run test:ci` | 68 files / 388 tests passed; statements 94.28%, branches 91.87%, functions 91.48%, lines 96.33% |
| Frontend build | `npm run build` | Passed; route-level code-split chunks; largest lazy chunk 92.27 kB gzip (under the documented 110 kB budget) |
| End-to-end | `npm run test:e2e` | 47 passed |
| Live render | Running app at 1366×768 and 390×844 | Desktop shell defect reproduced; mobile shell correct (see Finding 1) |

The 11 backend tests skipped under SQLite are the `@pytest.mark.postgres` index-plan assertions (NFR-P-3 weight-read indexes and the two new `audit_log` indexes). Unlike the M3 pass, they were executed here against a real PostgreSQL instance and pass, so the index-usage behavior is proven on the production engine rather than assumed.

## Strengths

- **The audit-log slice is genuinely append-only.** The port exposes only `add()` (`web/backend/src/weighttogo/audit/domain/ports.py:17-19`), the repository implements only `add()` (`web/backend/src/weighttogo/audit/infrastructure/repositories.py:22-38`), and no router or endpoint exposes audit read/update/delete. The invariant is structural, not a runtime guard.
- **The write-failure asymmetry is implemented exactly as designed.** Data-mutation audits share the request-scoped session and therefore fail closed — a failed audit insert aborts the whole unit of work (`web/backend/src/weighttogo/weight_tracking/interface/router.py:91-114`, `goals/interface/router.py:63-86`, `preferences/interface/router.py:43-66`, committed by `shared/db.py:62-72`). Auth-event audits fail open inside a `try/except` that logs via structlog and is isolated in a `begin_nested()` SAVEPOINT, so an audit hiccup can neither convert an auth response into a 500 nor poison the surrounding transaction (`auth/interface/router.py:217-230`).
- **PII handling is sound.** The schema stores `user_id` and never email/password/token (`web/backend/src/weighttogo/audit/infrastructure/models.py:56-73`); failed logins carry only a masked email via `mask_pii()` (`auth/interface/router.py:367,379`; `shared/logging.py:46-62`), asserted by `tests/integration/auth/test_auth_audit.py:77-79`. `ON DELETE SET NULL` preserves the trail past actor deletion, proven by `tests/integration/audit/test_audit_repository.py:137-171`.
- **Migration discipline is strong.** The `0001`–`0010` revision chain is linear with no branches or gaps; every migration has a correct, non-trivial `downgrade`; the `0008` data-bearing downgrade repair (delete `streak` rows before narrowing the CHECK) is present and tested (`web/backend/alembic/versions/0008_streak_achievement_type.py:41-51`; `tests/unit/achievements/test_migration_0008.py:48-53`). A full-chain upgrade→downgrade→upgrade round-trip runs on real PostgreSQL, and two independent CI jobs (`migration-ci.yml`, `e2e.yml`) each apply `upgrade head` from scratch against a fresh `postgres:16`.
- **Constraint hardening follows a disciplined dual-declaration pattern.** Each new CHECK is declared in the owning model's `__table_args__` (so `create_all` enforces it under SQLite) and emitted in the migration via `op.create_check_constraint` (the PostgreSQL path), with a text/structure migration test; no `batch_alter_table` is used. Every model-declared partial index supplies both `postgresql_where` and `sqlite_where`, avoiding the silent full-index degradation hazard.
- **The audit event taxonomy has a single canonical source.** The model builds its event-type CHECK from the `AuditEventType` enum (`audit/infrastructure/models.py:34-47`), so the constraint cannot drift from the enum within a release.
- **The backup/restore scripts are well-engineered.** Both use `set -euo pipefail`, quote every expansion, take credentials only from `DATABASE_URL` (no literals), and are shellcheck-clean and wired into both pre-commit and CI (`.pre-commit-config.yaml:35-40`, `.github/workflows/backend-ci.yml:52-53`). `restore.sh` validates argument count, environment, and dump-file existence before invoking `pg_restore`.
- **Predecessor remediations are present and verified.** Route-level code splitting now ships (the production build emits separate lazy chunks for Dashboard, Goals, Login, etc., with a size-limit budget), the retired M2 placeholder pages/tests are removed, the React Compiler warning on `WeightEntryForm` is resolved via `useWatch`, and the `as any` form-resolver cast is gone. The M2/M3 security baseline — security headers with CSP/HSTS, CSRF Origin/Referer middleware, refresh-token coalescing, and generic auth messages — remains intact (`web/backend/src/weighttogo/main.py:85-102`, `interface/middleware/csrf.py`, `web/frontend/src/lib/api-client.ts:79,100-152`, `web/frontend/src/features/auth/messages.ts`).

## Blocking and High-Priority Findings

### 1. The permanent sidebar overlays the main content on desktop, hiding the left edge of every protected page

**Severity:** High  
**Checklist areas:** Structure (completely and correctly implements the design), Defensive Programming (layout invariant not held)  
**Files:** `web/frontend/src/components/AppLayout.tsx:106-125`

The desktop `Drawer variant="permanent"` applies its 240px width only to the `.MuiDrawer-paper` slot, never to the Drawer root, and sets no `flexShrink: 0`. A permanent drawer's paper is `position: fixed` (removed from flow); the docked root is what reserves horizontal space in the flex row. With no width on the root, the root collapses and the main region — which carries `flexGrow: 1` — starts at the viewport's left edge and spans the full width, so the fixed drawer paints on top of its left 240px.

This was reproduced on the running application at 1366×768 and measured in the browser:

- Drawer paper: `x=0, width=240`; both Drawer roots: `width=0`.
- Main content region: `x=0, width=1366` (should be `x=240, width=1126`).
- The page `<h1>` "Dashboard" sits at `x=24` — entirely under the 240px drawer — and is present in the accessibility tree but not visible in the render.

The defect is horizontal width reservation on the `md` breakpoint and up (≥900px). The same render at 390×844 is correct: MUI switches to a `temporary` overlay drawer and content is full-width and unoccluded. The empty-state and card surfaces only *appear* acceptable because their content is centered within the full-width region, landing clear of the sidebar; any left-aligned element (page heading, table column, the left edge of a card grid) is occluded. M4 introduced no frontend changes, so this is a standing defect in the application shell rather than an M4 regression, but it is the most user-visible issue in the whole-app scope and it evades the existing unit tests and the axe accessibility specs (overlap of content under a fixed element is not an axe-critical violation), which is why the green test and a11y suites do not catch it.

**Recommended fix:** Add `width: DRAWER_WIDTH, flexShrink: 0` to the permanent Drawer root `sx` (the block at `AppLayout.tsx:107-112`), matching the MUI responsive-drawer pattern. With the root reserving 240px, the main region resolves correctly; keep either its `flexGrow: 1` or its `calc(100% - 240px)` width, not both. Add a render assertion (component-level or Playwright) that the main region's left edge is at or beyond the drawer width on a desktop viewport, since neither the unit tests nor axe currently detect the overlap.

### 2. The database-architecture document's constraint and index catalogues claim completeness but omit the auth tables

**Severity:** High  
**Checklist areas:** Documentation (docs consistent with code), Structure  
**Files:** `docs/architecture/WeighToGo_Web_Database_Architecture.md:303-374` versus `web/backend/alembic/versions/0001_initial_users_and_auth.py:61-127` and `0006_user_preferences.py:73-77`

The database-architecture document is the milestone's flagship deliverable, and §4 bills itself as "all named CHECK and UNIQUE constraints across all tables" while §5 bills itself as "all named indexes across all tables." Both claims are literally false against the schema:

- §4 omits four real CHECK constraints that live in migration `0001`: `users_email_format`, `users_display_name_length`, `users_failed_login_nonneg`, and `refresh_tokens_expiry_after_issuance`. As a side effect, the `users.display_name` and `failed_login_count` rows in §3 show no rule even though both are DB-enforced.
- §5 omits six indexes: `uq_users_email`, `idx_users_email_active`, `uq_refresh_tokens_hash`, `idx_refresh_tokens_user_active`, and `idx_refresh_tokens_family` (migration `0001`), plus `idx_user_preferences_user` (migration `0006`).

A related traceability error appears at `:372`: `idx_achievements_user_earned` is attributed to ADR-0026, but ADR-0026 is the achievement write-flow contract and makes no indexing decision; the index was created in migration `0005` (Milestone Three) with no governing ADR, and the convention already used for `idx_goals_one_active_per_user` at `:368` ("migration 0003 (no ADR)") is the correct form.

The schema itself is correct — every omission is documentation drift, with the code as the accurate source of truth. But for a Databases milestone whose graded artifact is precisely this catalogue, an "all tables" claim that silently excludes the entire authentication subsystem undermines the document's credibility.

**Recommended fix:** Add a `users` (three constraints) and `refresh_tokens` (one constraint) subsection to §4 and the six missing index rows to §5, each citing migration `0001`/`0006` as provenance; or narrow both scope sentences to state precisely what is in scope (e.g., "value-domain integrity constraints introduced across the data tables; structural auth constraints and indexes are defined in migration `0001`") and cross-reference. Correct the ADR column for `idx_achievements_user_earned` to "migration 0005 (no ADR)".

## Medium-Priority Findings

### 3. The audit context-isolation invariant is documented as import-linter-enforced but is not

**Severity:** Medium  
**Checklist areas:** Documentation (comments consistent with code), Structure (module coupling)  
**Files:** `docs/adr/0024-audit-log-structure.md:77`; `web/backend/pyproject.toml:215-228`; `web/backend/tests/architecture/test_import_contracts.py`

ADR-0024 states that "the `audit` domain is never imported by other domain packages (enforced by import-linter, verified in `test_import_contracts.py`)." The only two import-linter contracts touching audit enforce layer ordering *within* `weighttogo.audit` and framework independence; there is no `independence` or `forbidden` contract that would fail CI if, say, `weighttogo.goals.application` imported `weighttogo.audit`. The invariant currently holds by convention — a grep confirms only the four interface routers import audit — but the documented mechanical guarantee does not exist. This is the same class of issue M3 surfaced repeatedly: a documented claim stronger than the guard that backs it.

**Recommended fix:** Add a `forbidden` contract (source = the other six contexts' `domain`/`application` subpackages, forbidden = `weighttogo.audit`) or an `independence` contract across the bounded contexts; this is cheap and matches the ADR's stated intent. Alternatively, soften the ADR wording to "enforced by the composition-root wiring pattern and verified by review" if mechanical enforcement is deliberately out of scope.

### 4. Restore is not atomic, and the runbook does not name the partial-restore risk

**Severity:** Medium  
**Checklist areas:** Defensive Programming (leave the resource in a correct state on failure), Documentation  
**Files:** `web/backend/scripts/restore.sh:40-41`; `docs/runbooks/backup-restore.md:71-93`

`pg_restore --clean --if-exists` runs DROP/CREATE/COPY as independent statements with no `--single-transaction`. Under `set -e`, a mid-restore failure surfaces a non-zero exit only after the database has been partially clobbered — some objects dropped, some recreated, some data loaded — leaving it in an indeterminate state. The only mitigation is the runbook's procedural "restore into a scratch database first," which limits blast radius but does not make the operation atomic, and the script does not enforce it.

**Recommended fix:** Add `--single-transaction` to the `pg_restore` invocation so the restore commits or rolls back atomically (this also implies `--exit-on-error`, making `set -e` predictable). If atomicity is intentionally declined, state explicitly in the runbook that a failed restore can leave a partially-restored database and that the remedy is to drop and recreate the target before retrying.

## Low-Priority Findings

### 5. Two audited events lack coverage, and no test guards the taxonomy against future drift

**Severity:** Low  
**Checklist areas:** Structure (leftover/unexercised cases), Loops and Branches (all cases covered), Defensive Programming  
**Files:** `web/backend/src/weighttogo/auth/interface/router.py:362-368`; `web/backend/src/weighttogo/audit/domain/entities.py:22-23`; `web/backend/alembic/versions/0009_audit_log.py:28-46`

`auth.account_locked` is wired but, unlike every other audited path, has no integration test asserting the row is written with a null `user_id` and a masked email. `auth.token_reuse_detected` is in the taxonomy but never recorded — this is documented as reserved-for-future in ADR-0024, so it is an accepted gap, not a defect. Separately, the model derives its event-type CHECK from the enum while the migration hard-codes the 14-value list; the two match today, but nothing asserts that a future enum addition stays consistent with a new migration's CHECK, which is the cross-dialect divergence class the slice otherwise guards against.

**Recommended fix:** Add an integration test that drives login past `max_login_attempts` and asserts the `auth.account_locked` row. Add a unit test asserting the latest audit CHECK migration's value set equals `{e.value for e in AuditEventType}`.

### 6. The new goal-listing index is migration-only and is proven only by a substring assertion

**Severity:** Low  
**Checklist areas:** Defensive Programming, Documentation, Structure (read-path coverage)  
**Files:** `web/backend/alembic/versions/0010_constraint_hardening.py:53-57`; `web/backend/src/weighttogo/goals/infrastructure/models.py:43-77`; `web/backend/tests/integration/goals/test_migration_0010.py:43-44`

`idx_goals_user_created (user_id, created_at DESC)` is created by migration `0010` to support the all-goals listing path but is not declared in `GoalModel.__table_args__`, so the SQLite `create_all` schema never builds it and its presence is asserted only by a substring match on the migration source. There is no `@pytest.mark.postgres` index-usage test proving the goal-listing query plans against it, unlike the weight-read path. This is internally inconsistent with `idx_achievements_user_earned`, which *is* dual-declared. A sibling index-hygiene case is already tracked: issue #104 covers the orphaned `idx_weight_entries_user_created_at`, which ADR-0021 explicitly deferred to the M4 migration-discipline review and which remains undisposed after the milestone closed — so the discipline review tracked the decision as a follow-up rather than making it. This finding (the goal-listing index) is a distinct but related instance of the same index-declaration and justification discipline.

**Recommended fix:** Either declare `idx_goals_user_created` in the model for parity and to give `create_all` schemas the index, or add a `@pytest.mark.postgres` index-usage test for the goal-listing query mirroring `test_index_usage_postgres.py`. At minimum, document why this index is migration-only while the achievement read index is dual-declared.

### 7. New CHECK boundaries are tested on the reject side only, and the epoch literal is duplicated

**Severity:** Low  
**Checklist areas:** Arithmetic Operations (inclusive boundary), Variables (symbolic constant vs literal), Defensive Programming  
**Files:** `web/backend/tests/unit/achievements/test_sqlalchemy_model.py:66-85`; `web/backend/tests/unit/goals/test_sqlalchemy_model.py:61-80`; `web/backend/src/weighttogo/goals/infrastructure/models.py:73-76`; `web/backend/alembic/versions/0010_constraint_hardening.py:48-52`

`achievements_threshold_positive` (`threshold IS NULL OR threshold > 0`) is tested at `threshold = 0` but not at a negative value, and NULL-acceptance is only incidental. `goals_target_date_epoch` (`target_date >= '2020-01-01'`) is tested at `2019-12-31` (reject) but has no acceptance case at or above the inclusive boundary, so the exact boundary is unverified. The `'2020-01-01'` epoch is an inline literal duplicated in both the model and the migration rather than a shared named constant, so the dual-declaration pattern leaves it drift-prone.

**Recommended fix:** Add a negative-threshold reject case and at-boundary accept cases (`threshold = 0.01`, `target_date = 2020-01-01`). Optionally hoist the epoch to a shared constant or add a test asserting the same literal appears in both the model and the migration.

### 8. The migration round-trip seed does not exercise the new M4 tables' downgrade paths

**Severity:** Low  
**Checklist areas:** Defensive Programming (correct state on termination), Structure (test completeness)  
**Files:** `web/backend/tests/integration/migrations/test_migration_round_trips.py:42-93,126-176`

The round-trip seed inserts into `users`, `weight_entries`, `goals`, `achievements`, and `user_preferences`, but no `audit_log` row, no achievement with a non-null `threshold`, and no `goals.target_date` near the epoch boundary — so the round-trip proves structural reversal of `0009`/`0010` but not data-bearing behavior, even though the suite's docstring frames the seed as proving the full downgrade chain handles real data. The impact is genuinely low because `0009`'s downgrade is a pure `drop_table` and `0010`'s only drops a constraint and index (neither validates rows on downgrade, unlike `0008`). Separately, the three round-trip tests are mutually order-dependent and rely on unenforced pytest file-definition ordering (safe today, since `pytest-randomly` is not a dependency).

**Recommended fix:** Extend the seed with an `audit_log` row, a milestone achievement with `threshold > 0`, and a goal `target_date >= 2020-01-01` so every M4 table is data-bearing across the downgrade; and either consolidate the three round-trip tests into one ordered scenario or make the ordering explicit.

### 9. Backup-side and dump-handling hardening gaps

**Severity:** Low  
**Checklist areas:** Defensive Programming (validate outputs), Security by Default  
**Files:** `web/backend/scripts/backup.sh:34-39`; `docs/runbooks/backup-restore.md:73-76,124-134`

`backup.sh` announces success immediately after `pg_dump` with no post-write integrity check, so a corrupt-but-zero-exit archive is reported as a good backup. The dump is created under the process umask (commonly world-readable) while the runbook only advises a manual `chmod 600` after the fact, leaving a window where PII- and credential-bearing data is world-readable on a multi-user host. The runbook also frames restore as "drops and recreates the objects" without noting that `--clean` drops only objects present in the dump, so restoring over a non-empty database yields a superset rather than an exact copy.

**Recommended fix:** Add a read-only integrity probe after the dump (`pg_restore --list "${output}" >/dev/null` and a non-empty check), set `umask 077` at the top of `backup.sh` so the dump and directory are created `0600`/`0700`, and add one sentence to the runbook noting the `--clean` superset behavior.

### 10. The Weight unit control hides the selected option's radio button under the active fill

**Severity:** Low  
**Checklist areas:** Structure (correctly implements the design), Documentation (DDR-0008 conformance)  
**Files:** `web/frontend/src/features/settings/components/UnitPreferenceControl.tsx:34-43`

Each option is a `FormControlLabel` whose entire box receives `bgcolor: 'primary.main'` when selected, with the radio tinted `primary.contrastText`. Rendered on the running application, the selected option becomes a solid green pill in which the radio indicator is no longer discernible, while the unselected option shows a normal radio circle. Toggling confirms the behavior follows the selection: selecting `lbs` hides its radio, and selecting `kg` moves the hidden-radio state to `kg` and restores `lbs`'s radio. The result is a control that reads as half radio, half toggle button — the selected radio's affordance is lost under the fill, which does not match the "segmented radio control" intent documented in the component header (DDR-0008). The selection still functions and the accessibility tree reports the `checked` state correctly, so this is a visual/affordance defect rather than a functional or hard accessibility failure.

**Recommended fix:** Commit to one visual language. Either render a true segmented control (e.g., MUI `ToggleButtonGroup`) and drop the radio entirely, or keep the `RadioGroup` and convey selection without occluding the indicator — a border or a subtle background tint that leaves the radio visible, rather than a full `primary.main` fill over the radio. Confirm the chosen treatment against DDR-0008.

## Assumptions Challenged

1. **"The green test and accessibility suites prove the application shell is correct."** Not for layout geometry. The sidebar overlap (Finding 1) passes the unit tests, the type gates, and the axe specs, yet a live render and a bounding-box measurement show the page heading rendered under the drawer. Automated suites assert that elements exist and are labeled, not where they paint.
2. **"The database-architecture document is a complete catalogue."** The schema is complete; the document's §4/§5 "all tables" claims are not (Finding 2). Completeness was asserted rather than reconciled against migration `0001`.
3. **"Audit context isolation is enforced by import-linter."** It holds by convention and composition-root wiring; no contract would fail CI on a violation (Finding 3).
4. **"Constraint hardening means a large body of new constraints in `0010`."** Honestly, no — and the M4 brief says as much. Migration `0010` adds only two CHECKs and one index; most of the "hardening" was an audit that confirmed prior M2/M3 constraints and backfilled them into the models. The milestone's framing as verification-and-documentation work is accurate, and the review confirms it.
5. **"Backup and restore are operationally safe to run as-is."** Restore is non-atomic and can leave a partial database on failure; the only guard is a documented, unenforced scratch-database procedure (Finding 4).

## Checklist Review

### Structure

**Status:** Mostly pass with frontend-shell defects.

Backend structure is the strongest evidence: clean bounded contexts, import-linter-enforced layering, an append-only audit context that is never imported by another domain, and migrations that follow the established single-purpose pattern. The structural defects are both in the frontend shell: the AppLayout permanent-drawer width (Finding 1) and the Weight unit control whose selected-state fill occludes its radio (Finding 10). Minor inconsistencies: the goal-listing index is migration-only while the achievement read index is dual-declared (Finding 6), and the three data-router audit helpers are intentional per-context triplication consistent with the codebase's strict context isolation.

### Documentation

**Status:** Pass with drift.

The ADR set is thorough and the per-table column schema, FK delete policies, audit taxonomy, connection policy, ERD, and migration history in the database-architecture document are all accurate. The drift is concentrated in the constraint/index catalogues' completeness claims (Finding 2), the ADR-0024 import-linter statement (Finding 3), and a minor ADR-0024 omission of the SAVEPOINT mechanism it actually uses.

### Variables

**Status:** Pass.

Naming is domain-driven and consistent across both stacks; `mypy` (248 source files) and `tsc` pass. The `event_metadata` Python name mapped to the `metadata` column is correct and commented. A few `as WeightUnit` narrowings on API data remain on the frontend, all benign and none `as any`; the repeated `formatWeightInPreferredUnit(… as WeightUnit …)` pattern across four display sites is already tracked for extraction by issue #110.

### Arithmetic Operations

**Status:** Pass.

Backend numeric work uses `Decimal`/`Numeric(6,2)`, so the `threshold > 0` constraint is exact rather than floating-point; the frontend avoids float equality with epsilon thresholds, uses the exact NIST conversion constant, and does date math in UTC to avoid day-roll. The only caveat is that the two new CHECKs' inclusive boundaries are not asserted on the accept side (Finding 7).

### Loops and Branches

**Status:** Pass.

Migrations carry minimal control flow (the one `for` is a comprehension); the pure domain algorithms are simple and tested. Audit branches for null `user_id` (failed login) and null `resource_id` (preferences) are covered. The branch gaps are the untested `account_locked` audit path (Finding 5) and the round-trip tests' reliance on file-definition ordering (Finding 8).

### Defensive Programming

**Status:** Mostly pass.

Strong: Pydantic/Zod input validation, DB-level CHECK defense-in-depth, the append-only audit invariant, the correct fail-closed/fail-open split with no silent swallow, length-bounded and null-guarded audit fields, and the intact security baseline (headers, CSP/HSTS, CSRF, refresh coalescing). Gaps: restore atomicity / correct-state-on-failure (Finding 4), backup self-verification and dump permissions (Finding 9), and the import-boundary invariant that is asserted but not mechanically enforced (Finding 3).

## Recommended Remediation Order

1. Fix the permanent-drawer width so the main content clears the sidebar on desktop, and add a layout-geometry regression assertion (Finding 1).
2. Complete the database-architecture document's constraint/index catalogues or narrow their completeness claims, and correct the `idx_achievements_user_earned` ADR attribution (Finding 2).
3. Add an import-linter contract for audit context isolation, or align ADR-0024 with the actual guard (Finding 3).
4. Make restore atomic with `--single-transaction`, or document the partial-restore risk (Finding 4).
5. Close the audit-coverage and taxonomy-parity gaps (Finding 5).
6. Resolve the goal-listing index parity / index-usage proof (Finding 6) and the CHECK boundary acceptance tests (Finding 7).
7. Strengthen the migration round-trip seed and ordering (Finding 8) and the backup-side hardening (Finding 9).
8. Align the Weight unit control's selected-state styling with its DDR-0008 intent so the radio is not occluded (Finding 10).

## Review Conclusion

Milestone Four is a serious and honest Databases enhancement. The audit trail is implemented with real engineering judgment — append-only by construction, fail-closed for data mutations and fail-open for auth events without ever silently swallowing, and free of unmasked PII — and the constraint hardening, migration discipline, and operational runbook are well-tested and well-documented. The verification evidence is strong across both stacks, and the PostgreSQL-only index-plan tests that M3 could only assume now pass on the production engine.

Two issues hold the web app back from M4 quality-complete. The first is a pre-existing functional defect in the application shell: on desktop viewports the permanent sidebar overlays the left edge of the main content, hiding the page heading and any left-aligned content — exactly the kind of geometry problem that a green unit and accessibility suite does not catch, and the most user-visible issue in the whole-app scope. The second is that the milestone's flagship database-architecture document claims complete constraint and index catalogues while omitting the authentication tables entirely. Neither reflects a defect in the database schema, which is correct and well-enforced; both are about making the delivered surface — the shell and the signature document — match the quality of the work underneath. With the drawer fix and the catalogue reconciliation, the remaining items are test-completeness, operational-hardening, and minor UI polish (including the Weight unit control's occluded radio in Finding 10), and the web app is M4 quality-complete.
