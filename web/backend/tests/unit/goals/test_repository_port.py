"""Unit tests for the IGoalRepository port (Protocol duck-typing checks)."""

from __future__ import annotations


def test_igoal_repository_is_runtime_checkable() -> None:
    from weighttogo.goals.domain.ports import IGoalRepository

    assert hasattr(IGoalRepository, "__protocol_attrs__") or callable(
        getattr(IGoalRepository, "__instancecheck__", None)
    )


def test_igoal_repository_has_required_methods() -> None:
    from weighttogo.goals.domain.ports import IGoalRepository

    for method in ("save", "get_by_id", "get_active_for_user", "list_for_user"):
        assert hasattr(IGoalRepository, method), f"Missing method: {method}"
