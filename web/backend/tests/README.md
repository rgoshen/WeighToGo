# Backend Tests

The suite runs on two engines by design. This explains the `N skipped` line in a local run.

## Two-tier strategy

- **Default — SQLite, in-memory.** `uv run pytest` runs the whole suite against in-memory
  SQLite for a fast, dependency-free inner loop. SQLite is a correctness _proxy_: it differs
  from production PostgreSQL in type strictness and query planning (ADR-0012, SRS §11).
- **PostgreSQL fidelity tests — `@pytest.mark.postgres`.** A small set is only meaningful on
  the production engine and **skips** under the SQLite run:
  - index-usage tests assert the query _planner_ selects a specific index via `EXPLAIN`;
    SQLite's planner differs and partial indexes (`postgresql_where`) don't materialize there;
  - the migration round-trip test exercises PostgreSQL-specific DDL on downgrade.

The `N skipped` line you see locally is **by design, not a coverage gap.** These tests run —
and must pass — against a real `postgres:16` service in CI (`backend-ci.yml` runs
`pytest -m postgres`; `migration-ci.yml` runs the round-trip subset). Expect the count to
_grow_ as more fidelity tests are added (e.g. a goal-listing index-usage test), not shrink.

## Running the PostgreSQL tests locally

```bash
docker compose up -d postgres
export WEIGHTTOGO_TEST_POSTGRES_DSN=postgresql+psycopg://weighttogo:weighttogo@127.0.0.1:5432/weighttogo_test
uv run pytest -m postgres   # the fidelity tests only
uv run pytest               # whole suite; 0 skipped once the DSN is set
```

Each `@pytest.mark.postgres` test skips itself when `WEIGHTTOGO_TEST_POSTGRES_DSN` is unset;
everything else always runs.
