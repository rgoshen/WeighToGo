"""Unit tests for UpdateGoal use case (FR-G-3)."""

from __future__ import annotations

from datetime import UTC, date, datetime
from decimal import Decimal
from unittest.mock import MagicMock

import pytest

from weighttogo.goals.application.update_goal import UpdateGoal, UpdateGoalCommand
from weighttogo.goals.domain.entities import Goal, GoalType
from weighttogo.goals.domain.exceptions import (
    GoalNotActiveError,
    GoalNotFoundError,
    InvalidGoalTargetError,
)


def _make_goal(
    goal_id: int = 1,
    user_id: int = 1,
    *,
    is_active: bool = True,
    is_achieved: bool = False,
    goal_type: GoalType = GoalType.LOSE,
    start_value: Decimal = Decimal("200"),
    target_value: Decimal = Decimal("150"),
) -> Goal:
    now = datetime.now(UTC)
    return Goal(
        goal_id=goal_id,
        user_id=user_id,
        target_value=target_value,
        target_unit="lbs",
        start_value=start_value,
        goal_type=goal_type,
        target_date=None,
        is_active=is_active,
        is_achieved=is_achieved,
        achieved_at=now if is_achieved else None,
        created_at=now,
        updated_at=now,
    )


def _make_repo(goal: Goal | None = None) -> MagicMock:
    repo = MagicMock()
    repo.get_by_id.return_value = goal
    repo.save.side_effect = lambda g: g
    return repo


def _run(repo: MagicMock, **overrides: object) -> Goal:
    defaults: dict[str, object] = {
        "user_id": 1,
        "goal_id": 1,
        "target_value": Decimal("145"),
        "target_date": None,
    }
    defaults.update(overrides)
    uc = UpdateGoal(goal_repo=repo)
    return uc.execute(UpdateGoalCommand(**defaults))  # type: ignore[arg-type]


# ── happy path ────────────────────────────────────────────────────────────────


def test_update_goal_happy_path_calls_save() -> None:
    repo = _make_repo(goal=_make_goal())
    _run(repo)
    repo.save.assert_called_once()


def test_update_goal_changes_target_value() -> None:
    repo = _make_repo(goal=_make_goal())
    result = _run(repo, target_value=Decimal("145"))
    assert result.target_value == Decimal("145")


def test_update_goal_sets_target_date() -> None:
    repo = _make_repo(goal=_make_goal())
    result = _run(repo, target_date=date(2026, 12, 31))
    assert result.target_date == date(2026, 12, 31)


# ── not found ─────────────────────────────────────────────────────────────────


def test_update_goal_raises_not_found_when_goal_missing() -> None:
    repo = _make_repo(goal=None)
    with pytest.raises(GoalNotFoundError):
        _run(repo)


def test_update_goal_does_not_save_on_not_found() -> None:
    repo = _make_repo(goal=None)
    with pytest.raises(GoalNotFoundError):
        _run(repo)
    repo.save.assert_not_called()


# ── inactive / achieved goal guard (Issue A) ──────────────────────────────────


def test_update_goal_raises_not_active_when_goal_is_abandoned() -> None:
    repo = _make_repo(goal=_make_goal(is_active=False, is_achieved=False))
    with pytest.raises(GoalNotActiveError):
        _run(repo)


def test_update_goal_raises_not_active_when_goal_is_achieved() -> None:
    repo = _make_repo(goal=_make_goal(is_active=False, is_achieved=True))
    with pytest.raises(GoalNotActiveError):
        _run(repo)


def test_update_goal_does_not_save_when_inactive() -> None:
    repo = _make_repo(goal=_make_goal(is_active=False))
    with pytest.raises(GoalNotActiveError):
        _run(repo)
    repo.save.assert_not_called()


# ── direction invariant guard (Issue B) ───────────────────────────────────────


def test_update_goal_raises_invalid_target_when_lose_goal_target_equals_start() -> None:
    goal = _make_goal(
        goal_type=GoalType.LOSE, start_value=Decimal("200"), target_value=Decimal("150")
    )
    repo = _make_repo(goal=goal)
    with pytest.raises(InvalidGoalTargetError):
        _run(repo, target_value=Decimal("200"))


def test_update_goal_raises_invalid_target_when_lose_goal_target_exceeds_start() -> None:
    goal = _make_goal(
        goal_type=GoalType.LOSE, start_value=Decimal("200"), target_value=Decimal("150")
    )
    repo = _make_repo(goal=goal)
    with pytest.raises(InvalidGoalTargetError):
        _run(repo, target_value=Decimal("210"))


def test_update_goal_raises_invalid_target_when_gain_goal_target_equals_start() -> None:
    goal = _make_goal(
        goal_type=GoalType.GAIN, start_value=Decimal("150"), target_value=Decimal("180")
    )
    repo = _make_repo(goal=goal)
    with pytest.raises(InvalidGoalTargetError):
        _run(repo, target_value=Decimal("150"))


def test_update_goal_raises_invalid_target_when_gain_goal_target_is_below_start() -> None:
    goal = _make_goal(
        goal_type=GoalType.GAIN, start_value=Decimal("150"), target_value=Decimal("180")
    )
    repo = _make_repo(goal=goal)
    with pytest.raises(InvalidGoalTargetError):
        _run(repo, target_value=Decimal("140"))


def test_update_goal_does_not_save_when_direction_invalid() -> None:
    goal = _make_goal(
        goal_type=GoalType.LOSE, start_value=Decimal("200"), target_value=Decimal("150")
    )
    repo = _make_repo(goal=goal)
    with pytest.raises(InvalidGoalTargetError):
        _run(repo, target_value=Decimal("220"))
    repo.save.assert_not_called()
