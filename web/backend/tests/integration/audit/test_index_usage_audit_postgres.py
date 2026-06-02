"""PostgreSQL index-plan assertions for audit_log indexes (NFR-P-3 / ADR-0024).

Runs only when ``WEIGHTTOGO_TEST_POSTGRES_DSN`` is set. CI provides a Postgres
service; locally run ``docker compose up -d postgres`` and export the DSN, then:

    uv run pytest -m postgres tests/integration/audit/test_index_usage_audit_postgres.py -v
"""

from __future__ import annotations

import contextlib
import os
from collections.abc import Iterator
from datetime import UTC, datetime, timedelta

import pytest
from sqlalchemy import Engine, create_engine, text

pytestmark = pytest.mark.postgres

_DSN = os.environ.get("WEIGHTTOGO_TEST_POSTGRES_DSN")
_SEED_ROWS = 150  # enough for the planner to prefer index scans


@pytest.fixture(scope="module")
def pg_engine() -> Iterator[Engine]:
    if not _DSN:
        pytest.skip("WEIGHTTOGO_TEST_POSTGRES_DSN not set; skipping")

    from alembic import command
    from alembic.config import Config

    from weighttogo.config import get_settings

    prior_db_url = os.environ.get("DATABASE_URL")
    os.environ["DATABASE_URL"] = _DSN
    get_settings.cache_clear()

    try:
        cfg = Config("alembic.ini")
        command.upgrade(cfg, "head")
        engine = create_engine(_DSN)
        try:
            yield engine
        finally:
            engine.dispose()
            with contextlib.suppress(Exception):
                command.downgrade(cfg, "base")
    finally:
        if prior_db_url is None:
            os.environ.pop("DATABASE_URL", None)
        else:
            os.environ["DATABASE_URL"] = prior_db_url
        get_settings.cache_clear()


def _seed(engine: Engine, n: int) -> int:
    """Insert a user and n audit_log rows; return the user_id.

    Half the rows are keyed to the user (login_succeeded), half have
    user_id=NULL (login_failed). Timestamps spread across n seconds so the
    created_at index has meaningful cardinality. ANALYZE is called at the end
    so the planner statistics are up-to-date before the EXPLAIN tests run.
    """
    now = datetime.now(UTC)
    with engine.begin() as conn:
        user_id: int = conn.execute(
            text(
                "INSERT INTO users (email, password_hash, display_name) "
                "VALUES ('audit.perf@example.com', 'x', 'AuditPerf') RETURNING user_id"
            )
        ).scalar_one()

        # Half the rows tied to the user
        succeeded_rows = [{"uid": user_id, "ts": now - timedelta(seconds=i)} for i in range(n // 2)]
        conn.execute(
            text(
                "INSERT INTO audit_log (user_id, event_type, created_at) "
                "VALUES (:uid, 'auth.login_succeeded', :ts)"
            ),
            succeeded_rows,
        )

        # Half the rows with no user (failed logins)
        failed_rows = [{"ts": now - timedelta(seconds=i)} for i in range(n // 2)]
        conn.execute(
            text(
                "INSERT INTO audit_log (user_id, event_type, created_at) "
                "VALUES (NULL, 'auth.login_failed', :ts)"
            ),
            failed_rows,
        )

        conn.execute(text("ANALYZE audit_log"))
    return int(user_id)


@pytest.fixture(scope="module")
def seeded_engine(pg_engine: Engine) -> Iterator[tuple[Engine, int]]:
    """Seed the database once per module; yield (engine, user_id)."""
    user_id = _seed(pg_engine, n=_SEED_ROWS)
    yield pg_engine, user_id


def test_user_created_index_present(pg_engine: Engine) -> None:
    """idx_audit_log_user_created exists on the audit_log table."""
    with pg_engine.connect() as conn:
        result = conn.execute(
            text(
                "SELECT indexname FROM pg_indexes "
                "WHERE tablename = 'audit_log' AND indexname = 'idx_audit_log_user_created'"
            )
        )
        assert result.fetchone() is not None, "idx_audit_log_user_created not found"


def test_event_type_created_index_present(pg_engine: Engine) -> None:
    """idx_audit_log_event_type_created exists on the audit_log table."""
    with pg_engine.connect() as conn:
        result = conn.execute(
            text(
                "SELECT indexname FROM pg_indexes "
                "WHERE tablename = 'audit_log' AND indexname = 'idx_audit_log_event_type_created'"
            )
        )
        assert result.fetchone() is not None, "idx_audit_log_event_type_created not found"


def test_user_query_uses_index_not_seqscan(seeded_engine: tuple[Engine, int]) -> None:
    """Filtering by user_id ordered by created_at uses idx_audit_log_user_created."""
    engine, user_id = seeded_engine
    sql = "EXPLAIN SELECT * FROM audit_log WHERE user_id = :uid ORDER BY created_at DESC LIMIT 50"
    with engine.connect() as conn:
        conn.execute(text("SET enable_seqscan = off"))
        plan = "\n".join(row[0] for row in conn.execute(text(sql), {"uid": user_id}))
    assert "Seq Scan" not in plan, f"Expected Index Scan, got:\n{plan}"
    assert "idx_audit_log_user_created" in plan, f"Expected index name in plan, got:\n{plan}"


def test_event_type_query_uses_index_not_seqscan(seeded_engine: tuple[Engine, int]) -> None:
    """Filtering by event_type ordered by created_at uses idx_audit_log_event_type_created."""
    engine, _ = seeded_engine
    sql = (
        "EXPLAIN SELECT * FROM audit_log "
        "WHERE event_type = 'auth.login_failed' ORDER BY created_at DESC LIMIT 50"
    )
    with engine.connect() as conn:
        conn.execute(text("SET enable_seqscan = off"))
        plan = "\n".join(row[0] for row in conn.execute(text(sql)))
    assert "Seq Scan" not in plan, f"Expected Index Scan, got:\n{plan}"
    assert "idx_audit_log_event_type_created" in plan, f"Expected index name in plan, got:\n{plan}"
