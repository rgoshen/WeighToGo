# M4 Web App Quality Review

**Date:** 2026-06-02
**Scope:** Web application only (`web/backend`, `web/frontend`, web CI/docs), focused on the Milestone Four database work: the `audit_log` bounded context, migrations `0009`/`0010`, the backup/restore scripts and runbook, and the M4 documentation reconciliation. Android implementation code is excluded.
**Checklist:** `docs/standards/cs499_code_review_checklist.md`
**Review posture:** Checklist-based self-review with explicit assumption challenges against the SRS, the M4 ADRs (0024/0025), and the delivered M4 behavior.
**Overall assessment:** M4 is both feature-complete and quality-complete. The full quality suite is green, coverage is above threshold, and the architectural boundaries hold. The few caveats are documented, intentional engineering trade-offs (fail-open auth-event auditing; append-only enforced by absence of a mutation path) rather than defects.

## Documentation Reviewed

- `docs/standards/cs499_code_review_checklist.md`
- `docs/standards/M2_WEB_APP_QUALITY.md`, `docs/standards/M3_WEB_APP_QUALITY.md`
- `docs/specs/WeighToGo_Web_SRS_v2.md` (§7.8, §8, §13.3, §14)
- `docs/plans/milestone-four-plan.md`
- `docs/adr/0024-audit-log-structure.md`, `docs/adr/0025-constraint-hardening-strategy.md`
- `docs/adr/0011-pii-masking-strategy-in-logs.md`, `docs/adr/0019-milestone-detection-algorithm.md`, `docs/adr/0021-composite-index-strategy.md`
- `docs/architecture/WeighToGo_Web_Database_Architecture.md`
- `docs/runbooks/backup-restore.md`

## Verification Performed

Commands run from the local checkout on branch `feature/m4-backup-restore-closeout`:

| Area | Command | Result |
| --- | --- | --- |
| Backend lint | `uv run ruff check .` (web/backend) | Passed — all checks passed |
| Backend format | `uv run ruff format --check .` (web/backend) | Passed — 257 files already formatted |
| Backend types | `uv run mypy` (web/backend) | Passed — no issues in 246 source files |
| Backend tests | `uv run pytest` (web/backend) | 672 passed, 11 skipped; 98% total coverage |
| Frontend lint | `npm run lint` (web/frontend) | Passed (no React Compiler warning — M3-remediation `useWatch` refactor) |
| Frontend types | `npm run typecheck` (web/frontend) | Passed |
| Frontend tests | `npm run test:ci` (web/frontend) | 388 passed (68 files); 94.28% statements, 96.33% lines |
| Frontend format | `npm run format:check` (web/frontend) | Passed |
| Frontend build | `npm run build` (web/frontend) | Passed; route-split chunks, no oversized-chunk warning |
| Scripts tests | `bats web/backend/scripts/tests` | 10 passed |
| Scripts lint | `shellcheck web/backend/scripts/*.sh` | Passed — no findings |

The 11 skipped backend tests are the `@pytest.mark.postgres` index-plan tests (weight-history and `audit_log`); they require `WEIGHTTOGO_TEST_POSTGRES_DSN` and run in CI's `perf-postgres` job against `postgres:16`. E2E specs (19) were reviewed statically, not executed (they require the full stack and are validated in CI).

## Strengths

- **The `audit` context is backend-only and import-isolated.** No domain package imports `audit`; the write paths are wired at the interface/composition-root layer (mirroring the `DetectAchievements` pattern, ADR-0019), and the import-linter architecture tests still pass.
- **Append-only is a structural invariant.** No UPDATE/DELETE path is exposed for `audit_log`; the invariant is documented in ADR-0024 rather than relying on a runtime guard.
- **Constraint hardening closes the Android "validation-in-app-only" class of flaw.** CHECK/FK/unique constraints across all seven tables are enforced at the database; migration `0010` adds the genuine gaps (`achievements_threshold_positive`, `goals_target_date_epoch`, the goals-listing index).
- **Cross-dialect portability is handled deliberately.** `INET`→`String(45)` and `JSONB`→`JSON` variants let the SQLite integration suite enforce the same CHECK constraints the production Postgres schema uses, via model `__table_args__` + `create_all`.
- **Migration discipline is proven, not asserted.** Round-trip (upgrade→downgrade→upgrade) tests and a from-scratch CI apply (`migration-ci.yml`) cover the full `0001`–`0010` chain.
- **PII discipline holds in the new surface.** Audit rows store `user_id` and, for failed logins, a `mask_pii()`-masked email — never raw email, password, or token material (ADR-0011, ADR-0024).
- **The backup/restore scripts are tested and linted.** Thin Bash wrappers with `bats` guard coverage (env validation, dump-file existence, usage, and an underlying-tool-failure guard) and a clean `shellcheck`.

## Findings

No Blocking or High-severity findings. The items below are Low-severity awareness notes; each is an intentional, documented trade-off.

### 1. Auth-event audit writes are fail-open

**Severity:** Low (intentional) · **Checklist areas:** Defensive Programming, Loops and Branches
**Reference:** ADR-0024; `auth` interface/composition-root wiring.

Authentication-event audit writes are best-effort: an explicit `try/except` logs via structlog and continues, so an audit hiccup never turns an auth response into a 500. The trade-off is that an audit row can be silently dropped under failure. This is the correct availability choice for auth (a security feature should not deny service), and it is **not** a silent swallow — the failure is logged. Data-mutation audit writes, by contrast, share the operation's transaction (fail-closed). The asymmetry is deliberate and documented.

### 2. Append-only is enforced by absence, not by a database mechanism

**Severity:** Low (intentional) · **Checklist areas:** Defensive Programming, Structure
**Reference:** ADR-0024.

There is no UPDATE/DELETE code path for `audit_log`, but there is no database trigger or role-permission that would block a future caller from adding one. ADR-0024 records this as a deliberate scope choice (a trigger/permission scheme is out of capstone scope). Acceptable; flagged so a future contributor knows the invariant is convention-plus-tests, not DB-enforced.

### 3. Index-plan proof is Postgres-only and skipped locally

**Severity:** Low · **Checklist areas:** Defensive Programming, Documentation
**Reference:** web/CLAUDE.md testing notes; `backend-ci.yml` `perf-postgres`.

The `audit_log` index-usage assertions are `@pytest.mark.postgres` and skip locally without a DSN. Strong unit/integration evidence exists locally; the production-planner proof runs in CI. This mirrors the NFR-P-3 pattern and is the accepted approach, but the local suite alone does not prove index usage.

## Assumptions Challenged

1. **"All quality is green, so M4 is done."** Substantially true here — but "green locally" excludes the Postgres index-plan tests (finding 3). The CI `perf-postgres` job is the real proof; this checkout shows the rest.
2. **"The audit trail captures everything."** It captures the fixed 14-event taxonomy. There is no admin-action category because there is no admin role (§1.3, ADR-0024) — the SRS's original "administrative actions" wording maps to nothing, now reconciled in §8.2.7.
3. **"A backup is safe to keep around."** No — a dump is raw PII and credential material (finding documented in the runbook security note); the new `.gitignore` entry prevents accidental commit, but operational handling (encryption, least privilege) is the operator's responsibility.
4. **"Constraint hardening means every invariant is at the DB."** Most value-domain invariants are, but some (e.g., cross-row business rules) remain application-enforced by design; ADR-0025 records which invariants live where and why.

## Checklist Review

### Structure
**Status:** Pass. The `audit` context has one clear responsibility, is import-isolated, and follows the established domain/application/infrastructure/interface layout. Migrations are single-purpose. No dead code or leftover stubs (the test stubs live under `scripts/tests/helpers`, not production paths).

### Documentation
**Status:** Pass. ADR-0024/0025 document the decisions; the runbook, the architecture doc, and the SRS reconciliation are consistent (this review verified the migration-label fix that brought the arch doc and SRS into agreement). Comments explain intent (e.g., the DSN-strip rationale in the scripts).

### Variables
**Status:** Pass. Domain-driven names throughout; `mypy --strict` and `tsc` are clean. No `Any` in the audit code.

### Arithmetic Operations
**Status:** N/A. The M4 surface (audit logging, constraints, shell wrappers, docs) performs no numeric computation. Existing `Decimal`-based numeric code is unchanged.

### Loops and Branches
**Status:** Pass. The scripts' guard chains cover `--help`, missing env, arg count, and file existence, each with a defined exit code and a default/else path; the bats suite asserts each branch. The audit recorder has no complex branching.

### Defensive Programming
**Status:** Pass with documented trade-offs. Inputs are validated (env, args, file existence); files are checked before access; `set -euo pipefail` leaves the scripts in a correct state on tool failure (proven by a bats test). The fail-open auth-audit path (finding 1) is the one deliberate exception, logged rather than swallowed.

## Recommended Remediation Order

No remediation is required for M4 to be quality-complete. For future hardening (out of capstone scope), in priority order:

1. If an audit-viewing surface is ever added, revisit append-only enforcement at the DB layer (trigger or role permission) — finding 2.
2. Consider a periodic CI assertion that the auth fail-open path's dropped-write rate is observable (a metric/log alert) — finding 1.
3. Keep the Postgres index-plan job as the authoritative NFR proof; do not treat a local green run as index-usage evidence — finding 3.

## Review Conclusion

Milestone Four delivers the database-engineering competencies it set out to: a security/compliance audit trail, database-level constraint hardening, a verified migration chain, the final architecture document, and a documented backup/restore procedure. Unlike the M3 review — which surfaced real gaps that became the M3 remediation effort — this review finds no blocking or high-severity issues. The remaining notes are intentional, documented trade-offs. I consider the web app **M4 quality-complete**.
