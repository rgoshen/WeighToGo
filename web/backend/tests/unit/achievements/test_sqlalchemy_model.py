"""Unit tests for AchievementModel ORM definition."""

from __future__ import annotations

from datetime import UTC, datetime
from decimal import Decimal

import pytest
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.auth.infrastructure.models import UserModel
from weighttogo.goals.infrastructure.models import GoalModel


def _make_user(session: Session) -> int:
    user = UserModel(
        email="a@example.com",
        password_hash="x",
        display_name="A",
        is_active=True,
        failed_login_count=0,
    )
    session.add(user)
    session.flush()
    return int(user.user_id)


def _make_goal(session: Session, user_id: int) -> int:
    goal = GoalModel(
        user_id=user_id,
        target_value=Decimal("150.0"),
        target_unit="lbs",
        start_value=Decimal("200.0"),
        goal_type="lose",
        is_active=True,
        is_achieved=False,
    )
    session.add(goal)
    session.flush()
    return int(goal.goal_id)


def test_achievement_model_tablename() -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    assert AchievementModel.__tablename__ == "achievements"


def test_achievement_model_has_required_columns() -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    cols = {c.key for c in AchievementModel.__table__.columns}
    assert {"id", "user_id", "goal_id", "achievement_type", "threshold", "earned_at"} <= cols


def test_achievement_invalid_type_rejected(db_session: Session) -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    user_id = _make_user(db_session)
    goal_id = _make_goal(db_session, user_id)
    db_session.add(
        AchievementModel(
            user_id=user_id,
            goal_id=goal_id,
            achievement_type="invalid_type",
            threshold=None,
            earned_at=datetime.now(UTC),
        )
    )
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_achievement_threshold_zero_rejected(db_session: Session) -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    user_id = _make_user(db_session)
    goal_id = _make_goal(db_session, user_id)
    db_session.add(
        AchievementModel(
            user_id=user_id,
            goal_id=goal_id,
            achievement_type="milestone",
            threshold=Decimal("0"),  # zero is invalid for milestone
            earned_at=datetime.now(UTC),
        )
    )
    with pytest.raises(IntegrityError):
        db_session.flush()
