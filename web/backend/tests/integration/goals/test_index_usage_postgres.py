"""NFR-P: goal-history reads use the composite index on PostgreSQL.

Runs only when ``WEIGHTTOGO_TEST_POSTGRES_DSN`` is set. CI provides a Postgres
service; locally run ``docker compose up -d postgres`` and export the DSN. This
is the production-engine proof for ``idx_goals_user_created (user_id,
created_at DESC)`` — the listing index migration 0010 created to back the
all-goals history read path (``SqlAlchemyGoalRepository.list_for_user``).

Mirrors ``tests/integration/weight/test_index_usage_postgres.py``: the weight
read path already had this proof, the goal read path did not (M4 review finding
6, issue #135). The index exists in production via the migration, so these
tests pass on a migrated schema — their value is proving the query plans
against the index and guarding it against a future regression (e.g. an
accidental drop) the SQLite suite cannot catch.
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
_NFR_P_THRESHOLD_ROWS = 150  # must exceed the 100-row SRS §7.2 threshold


@pytest.fixture(scope="module")
def pg_engine() -> Iterator[Engine]:
    # [G6] Must run with CWD = web/backend so Config("alembic.ini") and its
    # relative script_location resolve correctly (CI sets working-directory).
    if not _DSN:
        pytest.skip("WEIGHTTOGO_TEST_POSTGRES_DSN not set; skipping Postgres index test")

    from alembic import command
    from alembic.config import Config

    from weighttogo.config import get_settings

    # [G2] Save/restore DATABASE_URL so running the full suite with a DSN set
    # cannot leak the Postgres URL into subsequent SQLite tests.
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
                command.downgrade(cfg, "base")  # [G1] DSN MUST be a throwaway test DB; best-effort
    finally:
        if prior_db_url is None:
            os.environ.pop("DATABASE_URL", None)
        else:
            os.environ["DATABASE_URL"] = prior_db_url
        get_settings.cache_clear()


def _seed(engine: Engine, n: int) -> int:
    """Insert one user with ``n`` goals — exactly one active, the rest inactive —
    and return that user's ``user_id``.

    The partial unique index ``idx_goals_one_active_per_user`` (WHERE is_active =
    TRUE) permits a single active goal, so one row is active and ``n - 1`` are
    inactive. That makes the ``is_active = FALSE`` filter in
    ``test_history_query_uses_index_not_seqscan`` exclude a real row instead of
    being always-true against an all-inactive seed. A single user suffices:
    ``enable_seqscan = off`` (not cross-user selectivity) is what forces the
    index, and no test asserts cross-user exclusion. Each goal satisfies the table
    CHECKs — a ``lose`` goal needs ``target_value < start_value`` (the direction
    invariant).

    The ``created_at`` values are intentionally distinct (``base + i days``): this
    test asserts the query *plan*, not row order, and distinct timestamps keep the
    chosen plan deterministic (no ties to sort). The ``created_at DESC, goal_id
    DESC`` tie-break that ``list_for_user`` applies for same-instant inserts is a
    row-ordering correctness concern covered by the repository tests, not
    exercised here.
    """
    with engine.begin() as conn:
        target = conn.execute(
            text(
                "INSERT INTO users (email, password_hash, display_name) "
                "VALUES ('goal-perf@example.com', 'x', 'Perf') RETURNING user_id"
            )
        ).scalar_one()
        base = datetime(2020, 1, 1, tzinfo=UTC)
        rows = [{"u": target, "ts": base + timedelta(days=i), "active": i == 0} for i in range(n)]
        conn.execute(
            text(
                "INSERT INTO goals "
                "(user_id, target_value, target_unit, start_value, goal_type, "
                " is_active, is_achieved, created_at, updated_at) "
                "VALUES (:u, 150.0, 'lbs', 200.0, 'lose', :active, FALSE, :ts, :ts)"
            ),
            rows,
        )
        conn.execute(text("ANALYZE goals"))
    return int(target)


@pytest.fixture(scope="module")
def seeded_uid(pg_engine: Engine) -> int:
    """Seed the database once per module; return the target user_id.

    The module-scoped ``pg_engine`` keeps the schema alive between tests, so a
    second ``_seed`` call would violate the unique email constraint.
    """
    return _seed(pg_engine, n=_NFR_P_THRESHOLD_ROWS)


def test_listing_query_uses_index_not_seqscan(pg_engine: Engine, seeded_uid: int) -> None:
    """The (user_id, created_at DESC) index serves the all-goals listing read.

    Mirrors ``SqlAlchemyGoalRepository.list_for_user`` with ``include_active=True``:
    a ``user_id`` equality ordered newest-first. This is the read the index was
    created for — ``idx_goals_one_active_per_user`` is partial and covers only the
    active-goal lookup, so a full listing has no other composite to use.

    ``enable_seqscan = off`` makes a sequential scan prohibitively expensive, so
    this proves the index is *usable* for the read path and guards against it
    being dropped or made unusable — not that the planner would prefer it
    organically at production row counts.
    """
    sql = (
        "EXPLAIN SELECT * FROM goals "
        "WHERE user_id = :u "
        "ORDER BY created_at DESC, goal_id DESC LIMIT 20"
    )
    with pg_engine.connect() as conn:
        conn.execute(text("SET enable_seqscan = off"))
        plan = "\n".join(row[0] for row in conn.execute(text(sql), {"u": seeded_uid}))
    assert "idx_goals_user_created" in plan, plan
    assert "Seq Scan on goals" not in plan, plan


def test_history_query_uses_index_not_seqscan(pg_engine: Engine, seeded_uid: int) -> None:
    """The past-goals history read (FR-G-5) also plans against the index.

    Mirrors ``list_for_user`` with ``include_active=False``: the same
    ``user_id`` + ``created_at DESC`` access with an added ``is_active = FALSE``
    filter that excludes the one seeded active goal. The partial active index
    cannot serve ``is_active = FALSE`` rows, so only ``idx_goals_user_created`` is
    usable for this access path.

    As above, ``enable_seqscan = off`` proves the index is usable for this read
    and guards against its removal — not an organic production-plan preference.
    """
    sql = (
        "EXPLAIN SELECT * FROM goals "
        "WHERE user_id = :u AND is_active = FALSE "
        "ORDER BY created_at DESC, goal_id DESC LIMIT 20"
    )
    with pg_engine.connect() as conn:
        conn.execute(text("SET enable_seqscan = off"))
        plan = "\n".join(row[0] for row in conn.execute(text(sql), {"u": seeded_uid}))
    assert "idx_goals_user_created" in plan, plan
    assert "Seq Scan on goals" not in plan, plan


def test_goals_user_created_index_present(pg_engine: Engine) -> None:
    """Planner-independent proof that migration 0010 created the index in Postgres.

    Distinct from the EXPLAIN tests (which depend on ``enable_seqscan = off``) and
    from the model-declaration unit test (which checks the SQLite ``create_all``
    schema): this asserts the *migration-built* index exists on the production
    engine regardless of planner choices.
    """
    with pg_engine.connect() as conn:
        names = {
            r[0]
            for r in conn.execute(
                text("SELECT indexname FROM pg_indexes WHERE tablename = 'goals'")
            )
        }
    assert "idx_goals_user_created" in names, names
