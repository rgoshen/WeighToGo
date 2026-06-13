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


def _seed_representative_data(engine: Engine) -> None:
    """Insert rows that exercise widened constraints to prove data-bearing rollback is safe.

    The critical row is achievement_type='streak', which exercises the migration 0008
    widened CHECK on downgrade.  When downgrade base runs, migration 0008's downgrade
    must DELETE streak rows before recreating the narrow CHECK; without that cleanup,
    PostgreSQL raises a constraint violation on any existing streak row.

    Every M4 table is data-bearing so the full downgrade chain is exercised against real
    data, not just an empty schema (finding 8):
      - audit_log: one row (table introduced by migration 0009),
      - achievements: a 'milestone' row with threshold > 0 (the 0010
        achievements_threshold_positive column) alongside the 'streak' row,
      - goals: target_date = '2020-01-01', the inclusive lower bound of the 0010
        goals_target_date_epoch CHECK.

    All other rows anchor the representative-data set and confirm the full downgrade
    chain handles real data without errors.
    """
    with engine.begin() as conn:
        user_id = conn.execute(
            text(
                "INSERT INTO users (email, password_hash, display_name) "
                "VALUES ('rollback-test@example.com', 'x', 'Rollback Test') "
                "RETURNING user_id"
            )
        ).scalar_one()
        conn.execute(
            text(
                "INSERT INTO weight_entries "
                "(user_id, weight_value, weight_unit, observation_date, is_deleted) "
                "VALUES (:uid, 180.0, 'lbs', '2024-01-01', FALSE)"
            ),
            {"uid": user_id},
        )
        # 'lose' goal: target_value < start_value satisfies the direction invariant (0004).
        # target_date = '2020-01-01' is the inclusive lower bound of the 0010
        # goals_target_date_epoch CHECK (target_date IS NULL OR target_date >= '2020-01-01').
        goal_id = conn.execute(
            text(
                "INSERT INTO goals "
                "(user_id, target_value, target_unit, start_value, goal_type, target_date) "
                "VALUES (:uid, 160.0, 'lbs', 180.0, 'lose', '2020-01-01') "
                "RETURNING goal_id"
            ),
            {"uid": user_id},
        ).scalar_one()
        # 'streak' is the value widened by migration 0008; downgrade must delete it first
        conn.execute(
            text(
                "INSERT INTO achievements (user_id, goal_id, achievement_type, earned_at) "
                "VALUES (:uid, :gid, 'streak', NOW())"
            ),
            {"uid": user_id, "gid": goal_id},
        )
        # 'milestone' with threshold > 0 makes the 0010 achievements_threshold_positive
        # column data-bearing.  It lives in the idx_achievements_unique_milestone partition
        # (WHERE threshold IS NOT NULL), so it does not collide with the NULL-threshold streak row.
        conn.execute(
            text(
                "INSERT INTO achievements "
                "(user_id, goal_id, achievement_type, threshold, earned_at) "
                "VALUES (:uid, :gid, 'milestone', 10.00, NOW())"
            ),
            {"uid": user_id, "gid": goal_id},
        )
        conn.execute(
            text(
                "INSERT INTO user_preferences (user_id, pref_key, pref_value) "
                "VALUES (:uid, 'weight_unit', 'lbs')"
            ),
            {"uid": user_id},
        )
        # audit_log row (table introduced by migration 0009).  event_type is in the
        # audit_log_event_type_valid taxonomy; resource_type is non-null because resource_id
        # is set (audit_log_resource_consistency).  resource_id carries no FK, so :gid is for
        # realism only; created_at defaults to NOW().
        conn.execute(
            text(
                "INSERT INTO audit_log (user_id, event_type, resource_type, resource_id) "
                "VALUES (:uid, 'goal.created', 'goal', :gid)"
            ),
            {"uid": user_id, "gid": goal_id},
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
    """upgrade head from an empty DB lands at revision 0010 (AC: from-scratch apply).

    Seeds representative data after the version check so that
    test_downgrade_base_removes_all_domain_tables exercises data-bearing rollback,
    not just empty-schema rollback.
    """
    with pg_migration_engine.connect() as conn:
        row = conn.execute(text("SELECT version_num FROM alembic_version")).scalar_one()
    assert row == "0010"  # update when migration 0011 is added
    _seed_representative_data(pg_migration_engine)


def test_downgrade_base_removes_all_domain_tables(pg_migration_engine: Engine) -> None:
    """downgrade base removes all seven SRS domain tables with representative data present.

    AC: chain is reversible.

    Depends on test_from_scratch_apply_reaches_head running first (DB at head with seeded data).
    The seeded data includes achievement_type='streak'; migration 0008's downgrade deletes
    these rows before narrowing the CHECK so this test catches constraint-violation hazards
    that an empty-schema rollback would miss.
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
