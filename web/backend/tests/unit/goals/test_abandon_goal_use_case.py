"""Unit tests for AbandonGoal use case."""

from __future__ import annotations

from datetime import UTC, datetime
from decimal import Decimal
from unittest.mock import MagicMock

import pytest

from weighttogo.goals.application.abandon_goal import AbandonGoal, AbandonGoalCommand
from weighttogo.goals.domain.entities import Goal, GoalType
from weighttogo.goals.domain.exceptions import GoalNotFoundError


def _make_goal(goal_id: int = 1, user_id: int = 1, is_active: bool = True) -> Goal:
    now = datetime.now(UTC)
    return Goal(
        goal_id=goal_id,
        user_id=user_id,
        target_value=Decimal("150"),
        target_unit="lbs",
        start_value=Decimal("200"),
        goal_type=GoalType.LOSE,
        target_date=None,
        is_active=is_active,
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


def _run(repo: MagicMock, goal_id: int = 1, user_id: int = 1) -> None:
    uc = AbandonGoal(goal_repo=repo)
    uc.execute(AbandonGoalCommand(user_id=user_id, goal_id=goal_id))


# ── happy path ────────────────────────────────────────────────────────────────


def test_abandon_goal_calls_save() -> None:
    repo = _make_repo(goal=_make_goal())
    _run(repo)
    repo.save.assert_called_once()


def test_abandon_goal_sets_is_active_false() -> None:
    goal = _make_goal()
    repo = _make_repo(goal=goal)
    _run(repo)
    assert goal.is_active is False


# ── idempotent (already abandoned) ────────────────────────────────────────────


def test_abandon_goal_already_inactive_still_saves() -> None:
    goal = _make_goal(is_active=False)
    repo = _make_repo(goal=goal)
    _run(repo)
    repo.save.assert_called_once()


# ── not found ─────────────────────────────────────────────────────────────────


def test_abandon_goal_raises_when_not_found() -> None:
    repo = _make_repo(goal=None)
    with pytest.raises(GoalNotFoundError):
        _run(repo)
