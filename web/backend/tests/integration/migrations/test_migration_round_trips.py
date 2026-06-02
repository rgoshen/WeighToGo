"""Full-chain migration round-trip tests (PostgreSQL required).

Verifies that the complete migration chain (0001–0010) applies cleanly
from scratch, reverses fully to base, and re-applies correctly.

Must run with CWD = web/backend so Config("alembic.ini") resolves correctly
(CI sets working-directory: web/backend).  Locally:
    cd web/backend && uv run pytest -m postgres tests/integration/migrations/ -v

Requires WEIGHTTOGO_TEST_POSTGRES_DSN pointing to a throwaway test database.
Update the two "0010" revision assertions when migration 0011 is added.
"""

from __future__ import annotations

import contextlib
import os
from collections.abc import Iterator

import pytest
from alembic import command
from alembic.config import Config
from sqlalchemy import Engine, create_engine, inspect, text

pytestmark = pytest.mark.postgres

_DSN = os.environ.get("WEIGHTTOGO_TEST_POSTGRES_DSN")

_DOMAIN_TABLES: frozenset[str] = frozenset(
    {
        "users",
        "refresh_tokens",
        "weight_entries",
        "goals",
        "achievements",
        "user_preferences",
        "audit_log",
    }
)


@pytest.fixture(scope="module")
def pg_migration_engine() -> Iterator[Engine]:
    """Apply all migrations to a fresh Postgres DB, yield the engine, then tear down."""
    if not _DSN:
        pytest.skip("WEIGHTTOGO_TEST_POSTGRES_DSN not set; skipping migration round-trip tests")

    from weighttogo.config import get_settings

    prior = os.environ.get("DATABASE_URL")
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
        if prior is None:
            os.environ.pop("DATABASE_URL", None)
        else:
            os.environ["DATABASE_URL"] = prior
        get_settings.cache_clear()


def test_from_scratch_apply_reaches_head(pg_migration_engine: Engine) -> None:
    """upgrade head from an empty DB lands at revision 0010 (AC: from-scratch apply)."""
    with pg_migration_engine.connect() as conn:
        row = conn.execute(text("SELECT version_num FROM alembic_version")).scalar_one()
    assert row == "0010"  # update when migration 0011 is added


def test_downgrade_base_removes_all_domain_tables(pg_migration_engine: Engine) -> None:
    """downgrade base removes all seven SRS domain tables (AC: chain is reversible).

    Depends on test_from_scratch_apply_reaches_head running first (DB must be at head).
    Tests run in file-definition order by default; do not add pytest-randomly without
    adding explicit ordering enforcement.
    """
    cfg = Config("alembic.ini")
    command.downgrade(cfg, "base")
    table_names = set(inspect(pg_migration_engine).get_table_names())
    assert table_names.isdisjoint(_DOMAIN_TABLES), (
        f"Expected all domain tables gone after downgrade base; "
        f"still present: {table_names & _DOMAIN_TABLES}"
    )


def test_upgrade_head_after_downgrade_restores_schema(pg_migration_engine: Engine) -> None:
    """upgrade head after downgrade base restores all domain tables (AC: chain round-trips).

    Depends on test_downgrade_base_removes_all_domain_tables running first (DB must be at base).
    Tests run in file-definition order by default; do not add pytest-randomly without
    adding explicit ordering enforcement.
    """
    cfg = Config("alembic.ini")
    command.upgrade(cfg, "head")
    table_names = set(inspect(pg_migration_engine).get_table_names())
    assert table_names >= _DOMAIN_TABLES, (
        f"Expected all domain tables present after upgrade head; "
        f"missing: {_DOMAIN_TABLES - table_names}"
    )
    with pg_migration_engine.connect() as conn:
        row = conn.execute(text("SELECT version_num FROM alembic_version")).scalar_one()
    assert row == "0010"  # update when migration 0011 is added
