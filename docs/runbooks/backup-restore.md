# Backup and Restore Runbook (Weigh to Go! web database)

> **Scope: documented, not automated.** This runbook describes a manual
> `pg_dump`/`pg_restore` procedure for the Weigh to Go! web database. Scheduled
> or rotated backups, point-in-time recovery (PITR), and continuous WAL
> archiving are **out of capstone scope** (SRS §13.3.1 #6; Milestone Four brief
> §8 "Out of Scope"). The thin wrapper scripts under `web/backend/scripts/`
> exist to make the documented procedure repeatable, not to schedule it.

---

## 1. Prerequisites

| Requirement | Notes |
| --- | --- |
| `pg_dump` / `pg_restore` | From `postgresql-client` 16 (matches the production engine, SRS §4.3). On macOS: `brew install libpq` then add it to `PATH`; on Debian/Ubuntu: `apt-get install postgresql-client-16`. |
| `DATABASE_URL` | Exported in the environment, or sourced from `web/backend/.env`. The scripts accept both the SQLAlchemy form (`postgresql+psycopg://…`) and a plain `postgresql://…` DSN — they strip the `+psycopg` driver suffix automatically. |
| A reachable database | For a local run: `docker compose up -d postgres` from `web/backend`. |

All commands below are run from `web/backend` unless noted.

---

## 2. What is captured

The procedure takes a **logical, custom-format dump of a single application
database** — the one named in `DATABASE_URL` (for local development,
`weighttogo_dev`). The dump includes all seven tables, their data, constraints,
and indexes.

It does **not** capture:

- PostgreSQL server **roles/users** (these are environment configuration, not
  application data — recreate them from your provisioning, not from a dump);
- **other databases** on the same server;
- anything outside the target database (tablespaces, server settings).

This is the appropriate scope for the capstone: the application's data and
schema are portable; the surrounding server is environment-specific.

---

## 3. Backup procedure

```bash
# from web/backend
./scripts/backup.sh backups/weighttogo-$(date -u +%Y%m%dT%H%M%SZ).dump
```

With no argument, `backup.sh` writes to a timestamped default
(`backups/weighttogo-<UTC timestamp>.dump`). The equivalent raw command, for
transparency:

```bash
pg_dump --format=custom --no-owner --no-privileges \
  --dbname="$DSN" --file="<output-file>"
```

where `$DSN` is `DATABASE_URL` with any `postgresql+psycopg://` prefix rewritten
to `postgresql://` (the script does this for you). The custom format
(`--format=custom`) is compressed and is what `pg_restore` consumes;
`--no-owner --no-privileges` keeps the dump portable across environments with
different role names.

> **Note:** `backup.sh` overwrites the output file if it already exists (the
> caller chooses the path). The timestamped default avoids collisions, so prefer
> it for routine backups.

---

## 4. Restore procedure

> ⚠️ **DESTRUCTIVE.** Restore runs with `--clean --if-exists`, which **drops and
> recreates** the objects in the target database before loading. Restore into a
> **scratch database first** (not production) and verify before pointing the
> application at it.

```bash
# from web/backend — restore into the database named in DATABASE_URL
./scripts/restore.sh backups/weighttogo-20260602T120000Z.dump
```

The equivalent raw command:

```bash
pg_restore --clean --if-exists --no-owner --no-privileges \
  --dbname="$DSN" "<dump-file>"
```

`restore.sh` refuses to run if `DATABASE_URL` is unset, if no dump file is
given, or if the named dump file does not exist.

---

## 5. Verification (after a restore)

1. **Migration head** — the restored schema should be at the latest revision:

   ```bash
   uv run alembic current   # expect: 0010 (head)
   ```

2. **Row spot-checks** — confirm core tables carried their data:

   ```sql
   SELECT count(*) FROM users;
   SELECT count(*) FROM weight_entries;
   SELECT count(*) FROM audit_log;
   ```

3. **Application health** — start the API and confirm it serves:

   ```bash
   uv run uvicorn weighttogo.main:app --reload
   # in another shell:
   curl -fsS http://localhost:8000/health
   ```

If all three pass, the restore is good.

---

## 6. Security note

A dump contains **PII and credential material**: user emails, bcrypt password
hashes, refresh-token hashes, and `audit_log` rows (which may carry masked
emails and IP addresses). Treat a dump as sensitive:

- store it **encrypted** at rest and restrict file permissions (`chmod 600`);
- apply **least privilege** — only operators who need it should have access;
- **never commit** a dump to the repository (the `backups/` directory is for
  local use and should be git-ignored);
- note that the PII-masking applied to **logs** (ADR-0011) does **not** apply to
  a database dump — a dump is the raw data by design.

See SRS §7.8 (Privacy) for the broader data-handling requirements.

---

## 7. References

- SRS §13.3.1 #6 — Backup and restore procedure (documented, scope-appropriate).
- SRS §8.4 — Database connection policy.
- [`docs/architecture/WeighToGo_Web_Database_Architecture.md`](../architecture/WeighToGo_Web_Database_Architecture.md) — the final schema and the migration chain (`0001`–`0010`).
- [ADR-0024](../adr/0024-audit-log-structure.md) — audit-log structure (why dumps carry audit rows).
- Scripts: [`web/backend/scripts/backup.sh`](../../web/backend/scripts/backup.sh), [`web/backend/scripts/restore.sh`](../../web/backend/scripts/restore.sh).
