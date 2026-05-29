"""Unit tests for UpdateGoal use case (FR-G-3)."""

from __future__ import annotations

from datetime import UTC, date, datetime
from decimal import Decimal
from unittest.mock import MagicMock

import pytest

from weighttogo.goals.application.update_goal import UpdateGoal, UpdateGoalCommand
from weighttogo.goals.domain.entities import Goal, GoalType
from weighttogo.goals.domain.exceptions import GoalNotFoundError


def _make_goal(goal_id: int = 1, user_id: int = 1) -> Goal:
    now = datetime.now(UTC)
    return Goal(
        goal_id=goal_id,
        user_id=user_id,
        target_value=Decimal("150"),
        target_unit="lbs",
        start_value=Decimal("200"),
        goal_type=GoalType.LOSE,
        target_date=None,
        is_active=True,
        is_achieved=False,
        achieved_at=None,
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
