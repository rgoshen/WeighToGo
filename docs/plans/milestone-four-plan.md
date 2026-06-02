# Milestone Four Implementation Brief

| Field | Value |
| --- | --- |
| Course | CS 499, Computer Science Capstone (SNHU) |
| Milestone | Four |
| Enhancement Category | Three: Databases |
| Status | Active |
| Authoritative Spec | `/docs/specs/WeighToGo_Web_SRS_v2.md` |
| Predecessor Brief | `/docs/plans/milestone-three-plan.md` |
| Tag Target | `v0.3.0` |
| Last Updated | 2026-05-31 |

---

## 1. Purpose and Scope

This brief is the entry point for Milestone Four work on the Weigh to Go! capstone artifact. The milestone covers Enhancement Three: Databases. The work builds on the Milestone Three web application (goals, achievements, trends, preferences, tagged `v0.2.0`) and enhances the persistence layer with the integrity and operational concerns that demonstrate database-engineering competency: a security/compliance audit trail, database-level constraint hardening, an index-coverage audit, a migration-discipline review, the final database-architecture document, and a backup/restore runbook.

The headline net-new feature is the `audit_log` — the seventh and final table in the SRS schema (§8.2.7), with its write paths wired through a dedicated bounded context. Much of what the SRS lists for M4 (CHECK constraints, composite/partial indexes) was already delivered incrementally during M2 and M3; M4 audits that work, hardens the genuine gaps, and documents it.

Deliverables at the end of M4:

- Technical artifact (zip of all code, original Android plus enhanced web)
- Narrative document (Word format) addressing the four reflection prompts in the CS 499 Milestone Four rubric

The narrative is drafted in parallel with the code work and finalized once implementation is complete.

### 1.1 Scope Tiers

**Core (committed):**

- `audit_log` table + write paths for authentication/security events and data mutations (create/update/delete on weight entries, goals, preferences) — SRS §13.3.1 #1, §8.2.7
- Constraint & index audit across all seven tables + a hardening migration — SRS §13.3.1 #2, #3
- Migration-discipline review: rollback round-trips verified, from-scratch apply in CI — SRS §13.3.1 #4
- Web database-architecture document reflecting the final schema with per-constraint and per-index rationale — SRS §13.3.1 #5
- Backup and restore runbook (documented; thin scripts, not automated/scheduled) — SRS §13.3.1 #6
- `M4_WEB_APP_QUALITY.md` quality review at closeout (matching the M2/M3 pattern)

**No stretch tier:** unlike M3, every M4 deliverable is committed; there are no optional stretch slices.

**Already delivered in Milestone 2/3 (cite in the narrative, do not rebuild):**

- All CHECK constraints in SRS §8.2 — `users`, `refresh_tokens`, `weight_entries`, `goals`, `achievements`, `user_preferences` (migrations `0001`–`0006`, plus the goal direction invariant in `0004`)
- NFR-P-3 composite indexes `(user_id, observation_date)` and `(user_id, created_at)` (migrations `0002`/`0007`, [ADR-0021](../adr/0021-composite-index-strategy.md))
- Cursor pagination (ADR-0015), TTL caching (ADR-0023), EAV preferences (ADR-0020), CITEXT email, partial unique indexes, parameterized queries throughout, and user-scoped / IDOR-safe access (404-on-cross-user)

### 1.2 Reconciling Spec Drift Discovered During Planning

SRS v2 was authored before the M3 migration/ADR numbering settled. The plan accounts for the following, and Step 5 reconciles the documents:

1. **Migration numbering.** SRS §8.3 predicts `0007_audit_log` and `0008_constraint_hardening`. Those numbers were consumed by M3 (`0007_performance_indexes`, `0008_streak_achievement_type`). M4's migrations are therefore **`0009_audit_log`** and **`0010_constraint_hardening`**; the §8.3 table is corrected in Step 5.
2. **`audit_log` schema.** SRS §8.2.7 leaves the schema "deferred to Milestone 4 documentation." M4 defines it (ADR-0024) and backfills §8.2.7 with the final DDL.
3. **ADR numbering.** On-disk ADRs run through 0023. M4 ADRs are **0024 and 0025** (next-available at authorship). The separate M3 remediation effort (`milestone-three-plan.md` §9) draws from the same sequence; numbering is coordinated to avoid collisions.

---

## 2. Authoritative References

Read these before generating detailed task lists. The SRS is the source of truth when references conflict.

| Document | Location | Key Sections for M4 |
| --- | --- | --- |
| **Software Requirements Specification** | `/docs/specs/WeighToGo_Web_SRS_v2.md` | §7.8 Privacy, §8 Data Architecture (esp. §8.2.7 audit_log, §8.3 migrations, §8.4 connection policy), §13.3 Milestone 4 Roadmap, §14 Acceptance Criteria |
| **CS 499 Milestone Four Guidelines and Rubric** | `/docs/plans/CS 499 Milestone Four Guidelines and Rubric.md` | Possible Indicators of Success; the four narrative prompts; pass/fail criteria |
| **CS 499 Code Review Checklist** | `/docs/standards/cs499_code_review_checklist.md` | Program-standard self-review gate, applied in Step 5 before tagging `v0.3.0` |
| **Existing ADRs** | `/docs/adr/0001-*.md` through `/docs/adr/0023-*.md` | Context on prior decisions. ADR-0011 (PII masking) constrains audit field handling; ADR-0019 (synchronous on-write detection) is the pattern audit writes mirror; ADR-0021 (composite indexes) is the indexing-strategy ADR M4 builds on. |
| **Android Database Architecture** | `/docs/architecture/WeighToGo_Database_Architecture.md` | Original schema. Historical predecessor; superseded for the web side by the new web DB-architecture document this milestone produces. |

---

## 3. Implementation Sequence

Five high-level steps, ordered by data dependency (Approach A: vertical slices). Each step is a single vertical slice — migration -> domain (framework-free) -> application -> interface/composition-root -> tests — and each ends with a green CI run and all coverage thresholds met before moving to the next. Each step gets its own feature branch per project convention (`feature/m4-short-title`; the audit log is NFR/infrastructure-driven, not an FR).

**Layout patterns to mirror (established in M2/M3):**

- Backend domain: `web/backend/src/weighttogo/<domain>/{domain/entities.py, domain/ports.py, application/<use_case>.py, infrastructure/models.py, infrastructure/repositories.py, interface/router.py, interface/schemas.py}` (the `audit` domain has no `interface/router.py` — it is backend-only)
- Backend tests: `web/backend/tests/unit/<domain>/test_*.py`, `web/backend/tests/integration/<domain>/test_*.py`, plus `web/backend/tests/architecture/test_import_contracts.py` (the import-boundary linter)
- Migrations: `web/backend/alembic/versions/000N_*.py`; the integration suite builds schema with `Base.metadata.create_all` (SQLite), so model `__table_args__` is the source of truth for constraints
- No frontend work this milestone — the `audit_log` is backend-only and no audit-viewing surface is in scope

### Step 1: Audit Log Vertical Slice

Implements the `audit_log` table and its write paths — the headline net-new feature: a security/compliance event trail covering authentication/security events and data mutations (create/update/delete on weight entries, goals, preferences), wired through a dedicated `audit` bounded context.

**ADR (write before any code in this step):** ADR-0024 Audit Log Structure.

**Migration:** `web/backend/alembic/versions/0009_audit_log.py` — create the `audit_log` table per SRS §8.2.7 (backfilled into the SRS in Step 5):

- Columns: `audit_id` BIGSERIAL PK; `user_id` BIGINT NULL REFERENCES `users(user_id)` **ON DELETE SET NULL** (retain the trail when an actor is removed); `event_type` VARCHAR(50) NOT NULL; `resource_type` VARCHAR(30) NULL; `resource_id` BIGINT NULL; `request_id` VARCHAR(64) NULL (correlates with NFR-O-2 `X-Request-ID`); `ip_address` INET NULL; `metadata` JSONB NULL; `created_at` TIMESTAMPTZ NOT NULL DEFAULT NOW().
- CHECK `audit_log_event_type_valid` — `event_type IN (...)` over the fixed taxonomy: `auth.register`, `auth.login_succeeded`, `auth.login_failed`, `auth.logout`, `auth.token_refreshed`, `auth.token_reuse_detected`, `auth.account_locked`, `weight_entry.created|updated|deleted`, `goal.created|updated|abandoned`, `preference.updated`. (SRS §8.2.7's third category — administrative actions — maps to nothing: there is no admin role per SRS §1.3. Recorded in ADR-0024.)
- CHECK `audit_log_resource_consistency` — `resource_id IS NULL OR resource_type IS NOT NULL`.
- Indexes: `idx_audit_log_user_created (user_id, created_at DESC)`; `idx_audit_log_event_type_created (event_type, created_at DESC)`.
- Both CHECK constraints are declared in the model `__table_args__` (not only the migration), so `create_all` enforces them in the SQLite integration suite. `ip_address` uses an `INET` -> `String(45)` SQLAlchemy variant and `metadata` a `JSONB` -> `JSON` variant for cross-dialect portability. The CI `perf-postgres` job already exists (`.github/workflows/backend-ci.yml`, `pytest -m postgres` against `postgres:16`); the `audit_log` index-plan assertions are added as `@pytest.mark.postgres` tests the existing job runs automatically — no new harness.

**Backend (create):**

- `audit/domain/entities.py` — `AuditEvent` entity; `AuditEventType` enum; `ResourceType` enum
- `audit/domain/ports.py` — `AuditRepository` port (append-only `add`)
- `audit/application/record_audit_event.py` — `RecordAuditEvent` use case + command (returns `None`; the audit row is a side-effect with no caller-facing value)
- `audit/infrastructure/models.py` (`AuditLogModel`), `repositories.py` (`SqlAlchemyAuditRepository`)
- No `interface/router.py` — the audit context is backend-only (no UI, no API endpoint this milestone)

**Cross-domain wiring (respect NFR-M-3):** the `auth`, `weight_tracking`, `goals`, and `preferences` routers (interface/composition-root layer) call `RecordAuditEvent` after a successful operation — and, for auth, on failures and lockouts. This mirrors the way the weight-entries router already calls `DetectAchievements` (ADR-0019), so the architectural import test (`tests/architecture/test_import_contracts.py`) must still pass; no domain package imports the `audit` package. The recorder takes the FastAPI `Request`; `request_id` is read with `request.headers.get("x-request-id")` (the pattern at `preferences/interface/router.py`) and `ip_address` from `request.client`. Data-mutation audit writes share the operation's request-scoped session (atomic, fail-closed); auth-event audit writes are best-effort (fail-open via an explicit `try/except` that logs via structlog — never a silent swallow) so an audit hiccup never converts an auth response into a 500. Step 1 first verifies the session commit boundary (per-request vs per-use-case commit) so "atomic with the operation" is real. PII (NFR-Priv-1, ADR-0011): store `user_id`, never email/password/token; failed logins (no user) carry a masked email in `metadata` via `mask_pii()` in `shared/logging.py`. Both trade-offs are recorded in ADR-0024.

**Testing strategy:** unit tests for `RecordAuditEvent` with an in-memory fake repository and for entity invariants; integration tests proving each audited operation writes exactly one row with the correct `event_type`/`resource_*`, that a failed login writes a row with NULL `user_id` and a masked-email metadata, and that the JSON/INET variants round-trip on SQLite; a `test_migration_0009` upgrade/downgrade test; `@pytest.mark.postgres` index-plan assertions for the two indexes.

**Risks:** a missed call site (an audited operation with no row). Mitigation: integration tests assert the row for every audited path. The append-only invariant is enforced by exposing no update/delete path (documented in ADR-0024), not a DB trigger.

### Step 2: Constraint & Index Audit and Hardening

Audits constraints and indexes across all seven tables, then adds a hardening migration for the genuine gaps. The schema is expected to be mostly solid (M2/M3 were thorough); the audit is itself a deliverable even where it confirms correctness.

**ADR (write before any code in this step):** ADR-0025 Constraint Hardening Strategy.

**Audit:** review each table for missing NOT NULL, value-domain CHECKs, column-type tightness, and read-path index coverage. Candidate gaps to evaluate test-first: `achievements.threshold > 0`, a `goals.target_date` sanity bound, per-key `user_preferences` value domains, and index coverage for the goal-listing and achievement-listing read paths. Findings feed the web DB-architecture document (Step 4).

**Migration:** `web/backend/alembic/versions/0010_constraint_hardening.py` — add the constraints/type-tightenings the audit surfaces and any missing read-path indexes. Follow the established pattern (migrations `0004`/`0005`/`0008`): declare each new CHECK in the owning model's `__table_args__` (so `create_all` enforces it in the SQLite suite) and emit it in the migration via `op.create_check_constraint` (the PostgreSQL/production path). The integration suite builds schema with `create_all`, not by running migrations, so no `batch_alter_table` is needed. Any partial index added here supplies both `postgresql_where` and `sqlite_where`.

**Testing strategy:** a rejection test per new CHECK (bad data raises `IntegrityError`) against the SQLite `create_all` schema — viable because the constraint lives in the model; a `test_migration_0010` test that asserts the migration's text/structure (the `0004`/`0005`/`0008` precedent — constraint-adding migrations are not executed on SQLite); confirmation the existing suite still passes against the tightened schema.

**Risks:** a new constraint an existing row would violate. Mitigation: the audit checks current data assumptions before adding each constraint; downgrade paths are verified. Depends on Step 1 (migration `0009` precedes `0010`).

### Step 3: Migration-Discipline Review

Verification-and-rationale work; no new schema.

- Verify every migration `0001`–`0010` has a correct, tested `downgrade`; run upgrade -> downgrade -> upgrade round-trips.
- Confirm a from-scratch apply against a fresh database in CI (the SRS §8.3 expectation).
- Produce a rationalized migration table (each migration's purpose + milestone) feeding the web DB-architecture document (Step 4).

**Testing strategy:** a round-trip test per migration (or a parametrized sweep); a CI check asserting a clean from-scratch `upgrade head`.

**Risks:** depends on Steps 1–2 (migrations `0009`/`0010` must exist).

### Step 4: Database Documentation

Create `docs/architecture/WeighToGo_Web_Database_Architecture.md` (parallels the SRS v1->v2 pattern; the Android database doc stays as historical). Contents: the final seven-table schema; every constraint and index with its rationale; the `audit_log` design and retention choice; a mermaid ERD; the connection/pooling policy (SRS §8.4); and the rationalized migration list from Step 3. Add a "superseded for the web side" pointer from the Android document.

**Risks:** depends on Steps 1–3 (schema and migration table finalized). Independent of Step 5's runbook and parallelizable with it.

### Step 5: Backup/Restore Runbook and Closeout

Verification, documentation, and reconciliation. ADRs are authored during their steps, not here.

**Backup/restore (SRS §13.3.1 #6):** `docs/runbooks/backup-restore.md` — `pg_dump`/`pg_restore` procedure, what to capture, restore + verification steps, and a scope note (documented, not automated/scheduled). Thin `web/backend/scripts/backup.sh` and `restore.sh` wrappers.

**Closeout:**

- Verify ADR-0024 and ADR-0025 are committed and listed in `docs/adr/README.md` with correct numbers and status.
- Reconcile SRS v2 drift: backfill §8.2.7 with the final `audit_log` DDL; correct the §8.3 migration table (`0009`/`0010`); update Appendix A §17.2 with ADR-0024/0025; mark §13.3.1 deliverables complete.
- Update the root `README.md` with the M4 feature set (audit trail, hardened constraints, final schema doc, backup runbook).
- M4 adds no API endpoints, so the OpenAPI snapshot is unchanged (the achievement-schema `streak` description fix is owned by the separate M3 remediation effort, `milestone-three-plan.md` §9).
- Produce `docs/standards/M4_WEB_APP_QUALITY.md` — a checklist-based review in the M2/M3 format (verification commands run, strengths, findings by severity, assumptions challenged, section-by-section checklist, remediation order, conclusion).
- Update the project `CLAUDE.md` CURRENT ASSIGNMENT block from Milestone Two to Milestone Four.
- Self-review all M4 code against `/docs/standards/cs499_code_review_checklist.md`; record findings as PR comments and resolve before merge.
- Draft the M4 narrative addressing the four rubric prompts, emphasizing the database-security indicator (the audit trail and constraint hardening) and the honest framing that late-stage database work is verification and operational rigor; acknowledge AI-tool usage per the rubric's AI Usage section and the Shapiro Library citation guide.
- Tag the repository `v0.3.0`.

**Note on ADR timing:** records must be written at the time of the decision — on the feature branch, before the first line of implementation code that depends on the decision. Step 5 is a verification step, not the authorship step. A record committed after the code it documents is a retrospective note, not a decision document.

---

## 4. New ADRs Required

Two new ADRs for M4, numbered from the next available slot (on-disk ADRs run through 0023). Each documents an engineering decision with viable alternatives considered. None reference course requirements as rationale. The "When to write" column is the gate. A third ADR — the achievement write-flow contract — is authored by the separate M3 remediation effort (`milestone-three-plan.md` §9), not here; numbering is coordinated across both efforts.

| ID | Title | Decision Captured | When to Write |
| --- | --- | --- | --- |
| ADR-0024 | Audit Log Structure | Schema and column choices; `ON DELETE SET NULL` retention vs. cascade; the event taxonomy as a CHECK-constrained `VARCHAR` vs. a Postgres ENUM; synchronous write at the composition root (mirrors ADR-0019) vs. middleware/event-driven; fail-open for auth events vs. fail-closed for data mutations; PII handling (user_id + masked email, no secrets); cross-dialect INET/JSONB variants; the append-only invariant; that "administrative actions" maps to no event (no admin role). | Before Step 1 — before writing `0009` and the audit domain |
| ADR-0025 | Constraint Hardening Strategy | Which invariants are enforced at the database (CHECK/FK/unique) vs. the application layer, and why defense-in-depth at the DB closes the Android "validation-in-app-only" gap; the audit methodology; the constraint-in-model + `op.create_check_constraint` + text-test pattern (`0004`/`0005`/`0008`) for SQLite-portable migrations. | Before Step 2 — before writing `0010` |

The indexing-strategy ADR that SRS §13.3.1 #7 asks for is already satisfied by [ADR-0021](../adr/0021-composite-index-strategy.md) (Composite Index Strategy); M4 references it rather than duplicating it. SRS Appendix A §17.2 is updated in Step 5 to reflect this numbering.

---

## 5. New DDRs Required

**None.** Milestone Four makes no UI changes — the `audit_log` is backend-only and no audit-viewing surface is in scope. The project rule that any UI change gets a Design Decision Record is satisfied vacuously and recorded here so its absence is deliberate, not an oversight.

---

## 6. M4-Specific Constraints

Project-wide constraints — TDD discipline, security baseline, strict typing, import linters, branching strategy, lint/test gates, and commit conventions — are specified in the SRS (§7 Non-Functional Requirements and §12 DevOps and Tooling) and the repository's contribution guidelines. Refer to them for execution rules.

M4-specific additions:

- The `audit_log` is **append-only**: no update or delete paths are exposed, and the invariant is documented in ADR-0024.
- Every audit write is wired at the interface/composition-root layer; the `audit` domain package is never imported by another domain package (verified by `tests/architecture/test_import_contracts.py`).
- **Security thread (rubric indicator).** The audit trail is the milestone's security showcase: it records authentication outcomes and data mutations for review, retains entries past actor deletion (`ON DELETE SET NULL`), and stores no unmasked PII or secrets. Constraint hardening closes the structural-integrity class of flaw (bad data can never persist). Both are given explicit narrative treatment.
- Constraint-adding migrations follow the `0004`/`0005`/`0008` pattern: the CHECK lives in the model `__table_args__` (enforced on SQLite via `create_all`) and in the migration via `op.create_check_constraint` (PostgreSQL path); the migration test asserts text. No `batch_alter_table`.
- Both ADRs are written before the code they govern, not after, and before the `v0.3.0` tag is applied.
- The narrative must acknowledge AI-tool usage per the rubric's AI Usage section.

---

## 7. Definition of Done

Adapted from SRS §14 for Milestone Four:

- [ ] `audit_log` table + write paths implemented for all in-scope events, with passing tests written test-first; append-only; no unmasked PII/secrets, verified by tests
- [ ] Constraint & index audit documented; hardening migration `0010` applied with rejection tests
- [ ] Migration-discipline review complete: every migration's downgrade verified; from-scratch apply green in CI
- [ ] Web database-architecture document written, with per-constraint/per-index rationale and an ERD
- [ ] Backup/restore runbook (+ thin scripts) committed
- [ ] Coverage thresholds met per SRS §11.5 (backend domain 95%/90%, application 90%/85%)
- [ ] CI is green on every relevant workflow
- [ ] ADR-0024 and ADR-0025 written, committed, and indexed in `docs/adr/README.md`
- [ ] No DDRs required (no UI changes) — recorded explicitly
- [ ] SRS v2 drift reconciled (§8.2.7 schema, §8.3 migration table, Appendix A numbering, §13.3.1 status)
- [ ] `M4_WEB_APP_QUALITY.md` produced against the code-review checklist
- [ ] Root `README.md` updated with the M4 feature set; project `CLAUDE.md` CURRENT ASSIGNMENT updated to M4
- [ ] All existing M2/M3 tests still pass against the tightened schema
- [ ] The M4 narrative document is drafted and reviewed against the rubric, with the database-security indicator emphasized and AI usage acknowledged
- [ ] The repository is tagged `v0.3.0`

---

## 8. Out of Scope

The following are explicitly NOT in M4. They are deferred to the M3 remediation effort, later milestones, or out of capstone entirely.

| Item | Deferred To |
| --- | --- |
| Rate-of-change indexed-window fix (R1); PostgreSQL index-plan verification (R2 — run/confirm the existing `perf-postgres` CI job, not build it); achievement write-flow contract (R3) | M3 remediation effort (`milestone-three-plan.md` §9) |
| Frontend M3 findings: route code-splitting, preferred-unit formatting gaps, placeholder cleanup, React Compiler warning | M3 remediation effort (§9) |
| OpenAPI `streak` description + SRS route-table naming fix | M3 remediation effort (§9) |
| Achievement recompute-on-update/delete (implementation) | M3 remediation effort (§9), pending the ADR decision |
| Audit-log viewing UI ("Account Activity" page) | Out of M4 (possible Final stretch) |
| Automated/scheduled backups, point-in-time recovery, continuous archiving | Out of capstone |
| Audit-log retention/rotation automation | Out of capstone (manual procedure only) |
| Account deactivation/deletion and audit-row cascade (NFR-Priv-4) | Final |
| Cloud deployment (AWS, GCP, Azure); infrastructure-as-code tooling | Out of capstone |

---

**End of Brief**
