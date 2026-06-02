"""Unit tests for GoalModel ORM class."""

from __future__ import annotations

from datetime import date
from decimal import Decimal

import pytest
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.auth.infrastructure.models import UserModel
from weighttogo.goals.infrastructure.models import GoalModel


def _make_user(session: Session) -> int:
    user = UserModel(
        email="g@example.com",
        password_hash="x",
        display_name="G",
        is_active=True,
        failed_login_count=0,
    )
    session.add(user)
    session.flush()
    return int(user.user_id)


def test_goal_model_tablename() -> None:
    assert GoalModel.__tablename__ == "goals"


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


def test_goal_direction_invariant_violated_rejected(db_session: Session) -> None:
    # A lose goal where target_value > start_value violates the invariant.
    user_id = _make_user(db_session)
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
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_goal_target_date_before_epoch_rejected(db_session: Session) -> None:
    user_id = _make_user(db_session)
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
    with pytest.raises(IntegrityError):
        db_session.flush()
