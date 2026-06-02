"""PostgreSQL index-plan assertions for audit_log indexes (NFR-P-3 / ADR-0024).

These tests are skipped unless WEIGHTTOGO_TEST_POSTGRES_DSN is set.
They are run automatically by the CI perf-postgres job.

To run locally:
    docker compose up -d postgres
    export WEIGHTTOGO_TEST_POSTGRES_DSN=postgresql+psycopg://weighttogo:weighttogo@127.0.0.1:5432/weighttogo_test
    uv run pytest -m postgres tests/integration/audit/test_index_usage_audit_postgres.py -v
"""

from __future__ import annotations

import os
from collections.abc import Generator
from datetime import UTC, datetime

import pytest
from sqlalchemy import create_engine, text
from sqlalchemy.orm import Session, sessionmaker

from weighttogo.audit.infrastructure.models import AuditLogModel
from weighttogo.auth.infrastructure.models import Base

_DSN = os.getenv("WEIGHTTOGO_TEST_POSTGRES_DSN", "")
pytestmark = pytest.mark.postgres


@pytest.fixture(scope="module")
def pg_session() -> Generator[Session, None, None]:
    if not _DSN:
        pytest.skip("WEIGHTTOGO_TEST_POSTGRES_DSN not set; skipping")
    engine = create_engine(_DSN)
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)
    factory = sessionmaker(bind=engine, autocommit=False, autoflush=False)
    session = factory()

    # Seed one row so EXPLAIN has statistics to work with
    session.add(
        AuditLogModel(
            user_id=None,
            event_type="auth.login_failed",
            resource_type=None,
            resource_id=None,
            request_id=None,
            ip_address=None,
            event_metadata=None,
            created_at=datetime.now(UTC),
        )
    )
    session.commit()
    yield session
    session.close()
    Base.metadata.drop_all(engine)
    engine.dispose()


def _explain(session: Session, query: str) -> str:
    result = session.execute(text(f"EXPLAIN {query}"))
    return "\n".join(row[0] for row in result)


def test_user_created_index_present(pg_session: Session) -> None:
    """idx_audit_log_user_created exists on the audit_log table."""
    result = pg_session.execute(
        text(
            "SELECT indexname FROM pg_indexes "
            "WHERE tablename = 'audit_log' AND indexname = 'idx_audit_log_user_created'"
        )
    )
    assert result.fetchone() is not None, "idx_audit_log_user_created not found"


def test_event_type_created_index_present(pg_session: Session) -> None:
    """idx_audit_log_event_type_created exists on the audit_log table."""
    result = pg_session.execute(
        text(
            "SELECT indexname FROM pg_indexes "
            "WHERE tablename = 'audit_log' AND indexname = 'idx_audit_log_event_type_created'"
        )
    )
    assert result.fetchone() is not None, "idx_audit_log_event_type_created not found"


def test_user_query_uses_index_not_seqscan(pg_session: Session) -> None:
    """Filtering by user_id ordered by created_at uses idx_audit_log_user_created."""
    plan = _explain(
        pg_session,
        "SELECT * FROM audit_log WHERE user_id = 1 ORDER BY created_at DESC LIMIT 50",
    )
    assert "Seq Scan" not in plan, f"Expected Index Scan, got:\n{plan}"


def test_event_type_query_uses_index_not_seqscan(pg_session: Session) -> None:
    """Filtering by event_type ordered by created_at uses idx_audit_log_event_type_created."""
    sql = (
        "SELECT * FROM audit_log"
        " WHERE event_type = 'auth.login_failed'"
        " ORDER BY created_at DESC LIMIT 50"
    )
    plan = _explain(pg_session, sql)
    assert "Seq Scan" not in plan, f"Expected Index Scan, got:\n{plan}"
