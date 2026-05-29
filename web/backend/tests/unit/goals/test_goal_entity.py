"""Unit tests for the Goal domain entity."""

from __future__ import annotations

from datetime import UTC, datetime
from decimal import Decimal

from weighttogo.goals.domain.entities import Goal, GoalType


def _make_goal(**overrides: object) -> Goal:
    defaults: dict[str, object] = {
        "goal_id": None,
        "user_id": 1,
        "target_value": Decimal("150"),
        "target_unit": "lbs",
        "start_value": Decimal("200"),
        "goal_type": GoalType.LOSE,
        "target_date": None,
        "is_active": True,
        "is_achieved": False,
        "achieved_at": None,
        "created_at": datetime.now(UTC),
        "updated_at": datetime.now(UTC),
    }
    defaults.update(overrides)
    return Goal(**defaults)  # type: ignore[arg-type]


# ── abandon() ─────────────────────────────────────────────────────────────────


def test_goal_entity_abandon_sets_is_active_false() -> None:
    goal = _make_goal()
    goal.abandon()
    assert goal.is_active is False


def test_goal_entity_abandon_is_idempotent() -> None:
    goal = _make_goal()
    goal.abandon()
    ts1 = goal.updated_at
    goal.abandon()
    assert goal.updated_at == ts1


def test_goal_entity_abandon_does_not_set_achieved() -> None:
    goal = _make_goal()
    goal.abandon()
    assert goal.is_achieved is False
    assert goal.achieved_at is None


# ── mark_achieved() ───────────────────────────────────────────────────────────


def test_goal_entity_mark_achieved_sets_flags() -> None:
    goal = _make_goal()
    goal.mark_achieved()
    assert goal.is_achieved is True
    assert goal.is_active is False
    assert goal.achieved_at is not None


def test_goal_entity_mark_achieved_is_idempotent() -> None:
    goal = _make_goal()
    goal.mark_achieved()
    first_achieved_at = goal.achieved_at
    goal.mark_achieved()
    assert goal.achieved_at == first_achieved_at
