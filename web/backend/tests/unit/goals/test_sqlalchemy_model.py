"""Unit tests for GoalModel ORM class."""

from __future__ import annotations

from collections.abc import Callable
from datetime import date
from decimal import Decimal

import pytest
from sqlalchemy import inspect, text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.goals.infrastructure.models import GoalModel


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
