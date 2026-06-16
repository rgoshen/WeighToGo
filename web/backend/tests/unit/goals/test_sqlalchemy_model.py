"""Unit tests for GoalModel ORM class."""

from __future__ import annotations

import re
from collections.abc import Callable
from datetime import date
from decimal import Decimal
from pathlib import Path
from typing import cast

import pytest
from sqlalchemy import CheckConstraint, Table, inspect, text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.goals.infrastructure.models import GoalModel

_EPOCH_RE = re.compile(r"\d{4}-\d{2}-\d{2}")


def _check_constraint_sqltext(table: Table, name: str) -> str:
    """Return the SQL text of the named CHECK constraint declared on ``table``."""
    for constraint in table.constraints:
        if isinstance(constraint, CheckConstraint) and constraint.name == name:
            return str(constraint.sqltext)
    raise AssertionError(f"CheckConstraint {name!r} not found on {table.name}")


def _migration_check_constraint_sql(source: str, name: str) -> str:
    """Return the ``sa.text`` SQL for the named ``create_check_constraint`` call.

    Anchored on the ``create_check_constraint(<name>, ...)`` call so the
    constraint's own docstring mention of the same SQL is never matched.
    """
    pattern = re.compile(
        r'create_check_constraint\(\s*"'
        + re.escape(name)
        + r'",\s*"[^"]+",\s*sa\.text\(\s*"([^"]+)"',
        re.DOTALL,
    )
    match = pattern.search(source)
    assert match is not None, f"create_check_constraint for {name!r} not found in migration"
    return match.group(1)


def _extract_epoch(constraint_sql: str) -> str:
    """Extract the YYYY-MM-DD epoch literal from a constraint's SQL text."""
    match = _EPOCH_RE.search(constraint_sql)
    assert match is not None, f"no epoch date found in {constraint_sql!r}"
    return match.group(0)


def test_goal_model_tablename() -> None:
    assert GoalModel.__tablename__ == "goals"


def test_goal_model_declares_user_created_listing_index(db_session: Session) -> None:
    """The goal-listing index is declared on the model, so ``create_all`` builds it.

    ``idx_goals_user_created (user_id, created_at DESC)`` backs the all-goals
    history read path (``SqlAlchemyGoalRepository.list_for_user``). Migration 0010
    creates it in production; declaring it on the model gives the SQLite
    integration schema the same index, following the codebase precedent that read
    indexes are declared in both the model and the migration (e.g. the
    also-dual-declared ``idx_achievements_user_earned``) rather than leaving this
    index migration-only (issue #135, M4 review finding 6).

    Asserts the columns *and* the descending order. SQLite reflection
    (``get_indexes``) drops the sort direction, so the ``created_at DESC`` shape is
    checked against the raw ``sqlite_master`` DDL. Together these fail if the
    declaration drifts to the wrong columns or to ASC.
    """
    # ARRANGE — the db_session fixture ran Base.metadata.create_all on SQLite.
    engine = db_session.get_bind()
    # ACT
    indexes = {ix["name"]: ix for ix in inspect(engine).get_indexes("goals")}
    ddl = db_session.execute(
        text(
            "SELECT sql FROM sqlite_master WHERE type = 'index' AND name = 'idx_goals_user_created'"
        )
    ).scalar()
    # ASSERT
    assert "idx_goals_user_created" in indexes
    assert indexes["idx_goals_user_created"]["column_names"] == ["user_id", "created_at"]
    assert ddl is not None and "created_at DESC" in ddl


def test_goal_model_has_required_columns() -> None:
    mapper = GoalModel.__mapper__
    col_names = {c.key for c in mapper.columns}
    expected = {
        "goal_id",
        "user_id",
        "target_value",
        "target_unit",
        "start_value",
        "goal_type",
        "target_date",
        "is_active",
        "is_achieved",
        "achieved_at",
        "created_at",
        "updated_at",
    }
    assert expected <= col_names


def test_goal_direction_invariant_violated_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    # ARRANGE — a lose goal where target_value > start_value violates the invariant.
    user_id = make_user()
    db_session.add(
        GoalModel(
            user_id=user_id,
            target_value=Decimal("200.0"),  # higher than start — wrong for lose
            target_unit="lbs",
            start_value=Decimal("150.0"),
            goal_type="lose",
            is_active=True,
            is_achieved=False,
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_goal_target_date_before_epoch_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    # ARRANGE
    user_id = make_user()
    db_session.add(
        GoalModel(
            user_id=user_id,
            target_value=Decimal("150.0"),
            target_unit="lbs",
            start_value=Decimal("200.0"),
            goal_type="lose",
            is_active=False,  # avoid conflict with active-goal unique index
            is_achieved=False,
            target_date=date(2019, 12, 31),  # before epoch — invalid
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_goal_target_date_epoch_matches_migration() -> None:
    """The ``goals_target_date_epoch`` literal must not drift between declarations.

    The constraint is dual-declared: the model's ``CheckConstraint`` (so the
    SQLite ``create_all`` suite enforces it) and migration 0010's
    ``create_check_constraint`` (so production enforces it) each carry the
    ``'2020-01-01'`` epoch inline. A migration is a point-in-time snapshot and
    must not import a live application constant, so the literal is intentionally
    written in both places; this test is the drift guard the acceptance criteria
    require — it fails if the two epochs ever diverge (M4 review finding 7).
    """
    # ARRANGE — the epoch the model enforces.
    model_sql = _check_constraint_sqltext(
        cast(Table, GoalModel.__table__), "goals_target_date_epoch"
    )
    model_epoch = _extract_epoch(model_sql)
    # ARRANGE — the epoch migration 0010 writes for the same constraint.
    migration_path = (
        Path(__file__).resolve().parents[3] / "alembic/versions/0010_constraint_hardening.py"
    )
    migration_sql = _migration_check_constraint_sql(
        migration_path.read_text(encoding="utf-8"), "goals_target_date_epoch"
    )
    migration_epoch = _extract_epoch(migration_sql)
    # ASSERT
    assert model_epoch == migration_epoch


def test_goal_target_date_at_epoch_accepted(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    """The epoch boundary date itself is accepted (the constraint is inclusive).

    ``goals_target_date_epoch`` is ``target_date >= '2020-01-01'``, so the exact
    boundary must be admitted. Complements the ``2019-12-31`` reject case, pinning
    the inclusive side of the boundary that was previously unverified (M4 review
    finding 7).
    """
    # ARRANGE
    user_id = make_user()
    goal = GoalModel(
        user_id=user_id,
        target_value=Decimal("150.0"),
        target_unit="lbs",
        start_value=Decimal("200.0"),
        goal_type="lose",
        is_active=False,  # avoid conflict with the active-goal unique index
        is_achieved=False,
        target_date=date(2020, 1, 1),  # exactly the inclusive epoch boundary
    )
    db_session.add(goal)
    # ACT
    db_session.flush()
    # ASSERT
    assert goal.goal_id is not None


def test_goal_invalid_type_rejected(db_session: Session, make_user: Callable[..., int]) -> None:
    # ARRANGE
    user_id = make_user()
    db_session.add(
        GoalModel(
            user_id=user_id,
            target_value=Decimal("150.0"),
            target_unit="lbs",
            start_value=Decimal("200.0"),
            goal_type="invalid_type",
            is_active=True,
            is_achieved=False,
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()
