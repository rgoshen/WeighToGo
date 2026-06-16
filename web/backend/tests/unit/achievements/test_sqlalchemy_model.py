"""Unit tests for AchievementModel ORM definition."""

from __future__ import annotations

from collections.abc import Callable
from datetime import UTC, datetime
from decimal import Decimal

import pytest
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.goals.infrastructure.models import GoalModel


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


def test_achievement_invalid_type_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    # ARRANGE
    user_id = make_user()
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
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_achievement_threshold_zero_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    # ARRANGE
    user_id = make_user()
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
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_achievement_negative_threshold_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    """A negative milestone threshold violates achievements_threshold_positive.

    Complements the zero-boundary reject case: ``threshold > 0`` is strict, so
    values on the negative side of the boundary must also be rejected, not just
    the boundary itself (M4 review finding 7).
    """
    from weighttogo.achievements.infrastructure.models import AchievementModel

    # ARRANGE
    user_id = make_user()
    goal_id = _make_goal(db_session, user_id)
    db_session.add(
        AchievementModel(
            user_id=user_id,
            goal_id=goal_id,
            achievement_type="milestone",
            threshold=Decimal("-1"),  # negative is invalid for milestone
            earned_at=datetime.now(UTC),
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_achievement_positive_threshold_accepted(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    """The smallest representable positive threshold satisfies the constraint.

    ``threshold > 0`` is strict, so the accept side of the boundary is the
    smallest value the ``Numeric(6, 2)`` column can hold above zero, i.e.
    ``0.01``. Proves the constraint admits valid positive thresholds rather than
    rejecting everything (M4 review finding 7).
    """
    from weighttogo.achievements.infrastructure.models import AchievementModel

    # ARRANGE
    user_id = make_user()
    goal_id = _make_goal(db_session, user_id)
    achievement = AchievementModel(
        user_id=user_id,
        goal_id=goal_id,
        achievement_type="milestone",
        threshold=Decimal("0.01"),  # smallest positive the column can represent
        earned_at=datetime.now(UTC),
    )
    db_session.add(achievement)
    # ACT
    db_session.flush()
    # ASSERT
    assert achievement.id is not None


def test_achievement_null_threshold_accepted(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    """A NULL threshold is accepted, reserved for ``goal_reached`` rows (ADR-0025).

    The constraint is ``threshold IS NULL OR threshold > 0``; the NULL branch was
    previously only exercised incidentally. This asserts it explicitly so a future
    tightening that drops the NULL allowance is caught (M4 review finding 7).
    """
    from weighttogo.achievements.infrastructure.models import AchievementModel

    # ARRANGE
    user_id = make_user()
    goal_id = _make_goal(db_session, user_id)
    achievement = AchievementModel(
        user_id=user_id,
        goal_id=goal_id,
        achievement_type="goal_reached",  # NULL threshold is valid for this type
        threshold=None,
        earned_at=datetime.now(UTC),
    )
    db_session.add(achievement)
    # ACT
    db_session.flush()
    # ASSERT
    assert achievement.id is not None


def test_duplicate_milestone_achievement_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    from weighttogo.achievements.infrastructure.models import AchievementModel

    # ARRANGE — two milestone rows with identical (goal_id, type, threshold)
    user_id = make_user()
    goal_id = _make_goal(db_session, user_id)
    now = datetime.now(UTC)
    db_session.add(
        AchievementModel(
            user_id=user_id,
            goal_id=goal_id,
            achievement_type="milestone",
            threshold=Decimal("25.0"),
            earned_at=now,
        )
    )
    db_session.flush()
    db_session.add(
        AchievementModel(
            user_id=user_id,
            goal_id=goal_id,
            achievement_type="milestone",
            threshold=Decimal("25.0"),
            earned_at=now,
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()
