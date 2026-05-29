"""Unit tests for goals domain exceptions."""

from __future__ import annotations

import pytest


def test_goal_not_found_error_is_exception() -> None:
    from weighttogo.goals.domain.exceptions import GoalNotFoundError

    with pytest.raises(GoalNotFoundError):
        raise GoalNotFoundError()


def test_active_goal_already_exists_error_is_exception() -> None:
    from weighttogo.goals.domain.exceptions import ActiveGoalAlreadyExistsError

    with pytest.raises(ActiveGoalAlreadyExistsError):
        raise ActiveGoalAlreadyExistsError()
