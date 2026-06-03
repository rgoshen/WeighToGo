# Milestone Four Narrative — Databases

**Course:** CS 499, Computer Science Capstone — Southern New Hampshire University
**Milestone:** Four (Enhancement Category Three — Databases)
**Artifact:** Weigh to Go! — repository <https://github.com/rgoshen-snhu/WeighToGo>
**Tag:** `v0.3.0` (this milestone) · prior milestones `v0.2.0`, `v0.1.0` · Android baseline `v1.0.0-android`
**Author:** Rick Goshen

> Render to Word for submission:
>
> ```bash
> cd docs/narratives
> pandoc milestone-four-narrative.md -o milestone-four-narrative.docx \
>     --reference-doc=reference.docx   # optional — for SNHU style
> ```
>
> The `.docx` output is git-ignored (see `.gitignore`); the markdown is the
> single source of truth.

---

## 1. Briefly describe the artifact. What is it? When was it created?

The artifact is **Weigh to Go!**, a weight-tracking application I originally
built as a native Android app for **CS 360 (Mobile Architecture and
Programming)** in 2025, and which I am rebuilding as a full-stack web application
for the CS 499 capstone. The web rebuild uses React 19 with TypeScript on the
frontend and FastAPI on Python 3.12 with SQLAlchemy 2.0 and PostgreSQL on the
backend, in a polyglot monorepo that preserves the original Java code under
`android/` and hosts the rebuild under `web/`.

This narrative covers **Milestone Four (Databases)**, which hardens and
operationalizes the persistence layer the earlier milestones built on. Where
Milestone Two delivered the architecture and a CRUD vertical slice (`v0.1.0`)
and Milestone Three filled it with algorithms and data structures (`v0.2.0`),
Milestone Four turns the database from "a place the application stores rows"
into a layer that enforces its own integrity and can be operated: a
security/compliance audit trail, database-level constraint hardening across all
seven tables, a migration-discipline review of the full `0001`–`0010` chain, the
final web database-architecture document, and a documented backup/restore
procedure. Milestone Four is tagged `v0.3.0`.

---

## 2. Justify the inclusion of the artifact in your ePortfolio

### Why this artifact

The category for this milestone is databases, and the most honest way to
demonstrate database competency on this artifact was not to add another feature
but to make the existing schema *trustworthy*. The Android original carried a
specific, documented weakness: value validation lived only in the application,
so the database itself would accept contradictory or impossible rows. Milestone
Four is the direct answer to that weakness — it pushes integrity down to the
database where it cannot be bypassed, and it adds the security and operational
concerns (an audit trail, a restore procedure) that distinguish a database that
merely stores data from one that can be run in production. The work was named in
advance in the SRS (§13.3.1) and the Milestone Four brief, and every piece of it
is traceable to a migration, an ADR, or a test.

### Components that showcase database skills

- **A security/compliance audit trail (ADR-0024).** The seventh and final table,
  `audit_log`, is an append-only record of authentication outcomes and data
  mutations. Two design choices carry the database-security argument. First, the
  event vocabulary is a *closed set* enforced by the database, not a free-text
  column — an unknown event type cannot be written:

  ```sql
  CONSTRAINT audit_log_event_type_valid CHECK (event_type IN (
      'auth.register', 'auth.login_succeeded', 'auth.login_failed',
      'auth.logout', 'auth.token_refreshed', 'auth.token_reuse_detected',
      'auth.account_locked', 'weight_entry.created', 'weight_entry.updated',
      'weight_entry.deleted', 'goal.created', 'goal.updated',
      'goal.abandoned', 'preference.updated'
  ))
  ```

  Second, the trail must outlive the actor it describes — deleting a user must
  not erase the security history of what that user did — so the foreign key
  releases rather than cascades:

  ```sql
  user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL
  ```

  The table stores no unmasked PII or secrets: `user_id` plus, for failed logins
  where there is no user, a masked email in `metadata` (ADR-0011).

- **Constraint hardening (ADR-0025).** Migration `0010` adds the value-domain
  checks the audit surfaced. For example, an achievement threshold is `NULL`
  only for the `goal_reached` type and otherwise must be positive — a rule the
  application assumed but the database did not enforce:

  ```sql
  CONSTRAINT achievements_threshold_positive
      CHECK (threshold IS NULL OR threshold > 0)
  ```

  Each new constraint lives in the SQLAlchemy model's `__table_args__` (so the
  SQLite integration suite enforces it through `create_all`) *and* in the
  migration via `op.create_check_constraint` (the production Postgres path), with
  a rejection test proving bad data raises `IntegrityError`.

- **Migration discipline (the `0001`–`0010` chain).** Every migration has a
  tested `downgrade`; upgrade→downgrade→upgrade round-trips and a from-scratch
  apply against a clean database run in CI (`migration-ci.yml`). A schema is only
  as trustworthy as its ability to be rebuilt and rolled back, and this proves
  it.

- **The final database-architecture document and the backup/restore runbook.**
  The architecture document records every constraint and index with its
  rationale and an ERD; the runbook gives a `pg_dump`/`pg_restore` procedure with
  verification steps and an explicit security note that a dump is raw PII.

### How the artifact was improved

The schema went from one that *assumed* its data was valid to one that
*guarantees* it: contradictory rows can no longer be written, the security
history is durable and tamper-resistant by construction, and the operational
gap (how do you recover this database?) is now documented and scripted. The
backend carries 672 tests at 98% coverage; the new shell scripts are themselves
covered by `bats` and linted with `shellcheck`. None of this is visible on the
screen — and that is the point of the database category.

---

## 3. Did you meet the course outcomes you planned to meet in Module One?

**Outcome 5 (security mindset that anticipates adversarial exploits and ensures
privacy of data) — Met (lead outcome).** This milestone is where the artifact's
security story moves from the application into the data layer. The audit trail
records the authentication events a reviewer would need after an incident,
retains them past actor deletion, and stores no secrets or unmasked PII;
constraint hardening removes a whole class of "bad data can persist" flaws; and
the restore runbook treats a dump as the sensitive artifact it is. Each is a
deliberate response to how the data could be misused, not an afterthought.

**Outcome 4 (well-founded, innovative techniques and tools — software
engineering / database) — Met.** The work uses the database's own integrity
mechanisms (CHECK, foreign-key actions, partial unique indexes) rather than
re-implementing them in Python; the migrations use the established
constraint-in-model-plus-migration pattern so one definition serves both the
SQLite test engine and production Postgres; the audit recorder is wired at the
composition root and kept import-isolated so no domain depends on it.

**Outcome 3 (design and evaluate computing solutions, managing trade-offs) —
Met.** ADR-0024 and ADR-0025 each name the alternatives and justify the choice
against explicit trade-offs: `ON DELETE SET NULL` versus cascade for audit
retention; a CHECK-constrained `VARCHAR` versus a Postgres `ENUM` for the event
taxonomy; fail-open auth-event auditing (availability) versus fail-closed
data-mutation auditing (integrity); and which invariants belong at the database
versus the application.

**Outcome 2 (professional-quality communication) — Met.** Two new ADRs
(0024–0025), the final database-architecture document, the backup/restore
runbook, the reconciled SRS, and this narrative communicate the milestone to
instructors, maintainers, and portfolio readers.

**Outcome 1 (collaborative environments) — Partial.** The capstone is solo, but
the decision records, the per-slice reviewable pull requests, the
conventional-commit history, and the closeout's deliberate reconciliation of
drift all keep the work in a form that supports collaboration.

I have no updates to the Module One outcome-coverage plan. The milestone schedule
(M2 = software engineering, M3 = algorithms and data structures, M4 = databases)
maps cleanly to the program outcomes, and M4 is the database-integrity and
security work the earlier narratives pointed toward.

---

## 4. Reflect on the process of enhancing and modifying the artifact

**What I learned.**

The clearest lesson was that late-stage database work is mostly *verification and
rigor*, not new construction — and that this is a real skill rather than a
consolation prize. Much of what the SRS listed for this milestone (CHECK
constraints, composite indexes) had been built incrementally during the earlier
milestones; the milestone's job was to audit that work, harden the genuine gaps,
and prove the whole chain rolls back and rebuilds. Writing a constraint is easy;
proving that adding it won't reject an existing row, that its downgrade is
correct, and that it is enforced identically on both the SQLite test engine and
production Postgres is the actual work. I came to see "documented, not automated"
(the backup procedure) as an honest engineering decision about scope, not a
shortcut.

A second lesson was about the audit trail as a security primitive. The
interesting design questions were not "what columns" but "what survives." An
audit row has to outlive the user it describes (hence `ON DELETE SET NULL`), it
must never become a new place that leaks PII (hence `user_id` and a masked email,
never the raw address), and it must not be able to turn a logging hiccup into a
denied login (hence fail-open writes for auth events, fail-closed for data
mutations). Each of those is a small decision with a security consequence, and
writing them into ADR-0024 forced me to defend them rather than assume them.

**Challenges I faced.**

The most instructive challenge appeared during closeout, not implementation. The
milestone's own architecture document — freshly written and reviewed — labeled
two migrations under the wrong milestone. The fact was checkable: the release
tags fix the boundaries (`v0.1.0` on 2026-05-23, `v0.2.0` on 2026-05-29), and the
migrations in question were authored between them, which makes them Milestone
Three. The lesson was to verify documentation claims against ground truth (the
tags, the files on disk) rather than against another document, because two
documents can be confidently consistent and both wrong. The reconciliation step
exists to catch exactly that, and running it as a deliberate pass turned
"the docs are probably right" into "the docs match the tags."

**How this work prepares me for the rest of the capstone.**

Milestone Four closes the three enhancement categories (software engineering,
algorithms and data structures, databases) and leaves the artifact in a state I
can defend section by section: every table has documented constraints, every
migration is round-trip tested, the security-sensitive surfaces are audited, and
the operational gap is documented. The final-project work is now polish and the
ePortfolio rather than new capability — and the per-slice review discipline,
which here caught a documentation error a reader would otherwise have trusted, is
the habit I most want to carry into that final pass.

---

## 5. AI tool usage acknowledgment

> **Author note:** review and reword this section in your own voice before
> submission — it is your academic-integrity disclosure and should describe your
> actual process. Drafted here per the Milestone Four rubric's *AI Usage* section
> and the Shapiro Library citation guidance.

In producing this milestone I used a generative-AI coding assistant to support —
not replace — my own engineering judgment. I used it for drafting and reviewing
code and documentation, for surfacing edge cases in the test design, and for
auditing my own plans and documents for gaps and drift before implementation.
Every design decision, constraint, and trade-off recorded in the ADRs and this
narrative reflects choices I made and can defend; the AI assistance accelerated
the mechanical work and acted as a second reviewer. I followed the SNHU
generative-AI guidelines and the Shapiro Library guidance on acknowledging AI
tools in coursework.

---

## Appendix A: Verifiable references

- Repository: <https://github.com/rgoshen-snhu/WeighToGo>
- Milestone Four tag: `v0.3.0`
- Prior milestone tags: `v0.2.0`, `v0.1.0` · Android baseline: `v1.0.0-android`
- Software Requirements Specification: [`/docs/specs/WeighToGo_Web_SRS_v2.md`](../specs/WeighToGo_Web_SRS_v2.md)
- ADR index (M4 records 0024–0025): [`/docs/adr/README.md`](../adr/README.md)
- Audit log structure: [`/docs/adr/0024-audit-log-structure.md`](../adr/0024-audit-log-structure.md)
- Constraint hardening strategy: [`/docs/adr/0025-constraint-hardening-strategy.md`](../adr/0025-constraint-hardening-strategy.md)
- PII masking strategy: [`/docs/adr/0011-pii-masking-strategy-in-logs.md`](../adr/0011-pii-masking-strategy-in-logs.md)
- Composite index strategy (referenced for the indexing decision): [`/docs/adr/0021-composite-index-strategy.md`](../adr/0021-composite-index-strategy.md)
- Web database-architecture document: [`/docs/architecture/WeighToGo_Web_Database_Architecture.md`](../architecture/WeighToGo_Web_Database_Architecture.md)
- Backup/restore runbook: [`/docs/runbooks/backup-restore.md`](../runbooks/backup-restore.md)
- M4 web app quality review: [`/docs/standards/M4_WEB_APP_QUALITY.md`](../standards/M4_WEB_APP_QUALITY.md)
- Migrations (`0001`–`0010`): [`/web/backend/alembic/versions/`](../../web/backend/alembic/versions/)
- Engineering log: [`/SUMMARY.md`](../../SUMMARY.md)
- Milestone Four implementation brief: [`/docs/plans/milestone-four-plan.md`](../plans/milestone-four-plan.md)
- CS 499 code review checklist: [`/docs/standards/cs499_code_review_checklist.md`](../standards/cs499_code_review_checklist.md)
