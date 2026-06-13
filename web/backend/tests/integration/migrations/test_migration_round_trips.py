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


def test_full_chain_round_trip(pg_migration_engine: Engine) -> None:
    """The full migration chain round-trips with data in every M4 table.

    Deliberately a single linear test: with no sibling tests there is no collection-order
    dependency to enforce, so pytest-randomly is a non-issue.  This replaces three previously
    order-dependent tests that relied on unenforced file-definition order (finding 8).

    1. a fresh upgrade head (applied by the module fixture) lands at revision 0010,
    2. seed representative rows so every M4 table is data-bearing (an audit_log row, a
       'milestone' achievement with threshold > 0, and an epoch-boundary goal),
    3. assert those seeded rows exist, so a silently-failed or removed INSERT fails the test
       instead of letting an empty-schema rollback pass green,
    4. downgrade base removes all seven SRS domain tables with the data present (migration
       0008's downgrade must DELETE the 'streak' row before re-narrowing its CHECK), and
    5. upgrade head restores the full schema at revision 0010.
    """
    engine = pg_migration_engine
    cfg = Config("alembic.ini")

    # 1. fresh apply reached head
    with engine.connect() as conn:
        version = conn.execute(text("SELECT version_num FROM alembic_version")).scalar_one()
    assert version == "0010"  # update when migration 0011 is added

    # 2. seed data-bearing rows in every M4 table
    _seed_representative_data(engine)

    # 3. the seeded M4 rows must exist before the downgrade, so "data-bearing" is an enforced
    #    precondition rather than an implicit hope
    with engine.connect() as conn:
        audit_count = conn.execute(text("SELECT count(*) FROM audit_log")).scalar_one()
        achievement_count = conn.execute(text("SELECT count(*) FROM achievements")).scalar_one()
        threshold_count = conn.execute(
            text("SELECT count(*) FROM achievements WHERE threshold > 0")
        ).scalar_one()
        epoch_goal_count = conn.execute(
            text("SELECT count(*) FROM goals WHERE target_date = '2020-01-01'")
        ).scalar_one()
    assert audit_count == 1
    assert achievement_count == 2  # one 'streak' + one 'milestone'
    assert threshold_count == 1
    assert epoch_goal_count == 1

    # 4. downgrade base clears every domain table with data present
    command.downgrade(cfg, "base")
    after_downgrade = set(inspect(engine).get_table_names())
    assert after_downgrade.isdisjoint(_DOMAIN_TABLES), (
        f"Expected all domain tables gone after downgrade base; "
        f"still present: {after_downgrade & _DOMAIN_TABLES}"
    )

    # 5. upgrade head restores the full schema
    command.upgrade(cfg, "head")
    after_upgrade = set(inspect(engine).get_table_names())
    assert after_upgrade >= _DOMAIN_TABLES, (
        f"Expected all domain tables present after upgrade head; "
        f"missing: {_DOMAIN_TABLES - after_upgrade}"
    )
    with engine.connect() as conn:
        version = conn.execute(text("SELECT version_num FROM alembic_version")).scalar_one()
    assert version == "0010"  # update when migration 0011 is added
