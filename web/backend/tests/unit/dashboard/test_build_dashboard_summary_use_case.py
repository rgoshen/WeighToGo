"""Unit tests for BuildDashboardSummary use case (subtask 20)."""

from __future__ import annotations

from datetime import UTC, date, datetime
from decimal import Decimal
from unittest.mock import MagicMock

from weighttogo.weight_tracking.domain.entities import WeightEntry


def _make_entry(entry_id: int = 1) -> WeightEntry:
    return WeightEntry(
        entry_id=entry_id,
        user_id=1,
        weight_value=Decimal("175.50"),
        weight_unit="lbs",
        observation_date=date(2026, 5, 20),
        notes=None,
        created_at=datetime(2026, 5, 20, tzinfo=UTC),
        updated_at=datetime(2026, 5, 20, tzinfo=UTC),
    )


def _run(repo: MagicMock, user_id: int = 1) -> object:
    from weighttogo.dashboard.application.build_dashboard_summary import (
        BuildDashboardSummary,
    )

    uc = BuildDashboardSummary(weight_repo=repo)
    return uc.execute(user_id=user_id)


def test_empty_user_returns_null_latest_entry() -> None:
    repo = MagicMock()
    repo.get_latest_for_user.return_value = None
    repo.count_for_user.return_value = 0
    result = _run(repo)
    assert result.latest_entry is None  # type: ignore[attr-defined]


def test_empty_user_returns_zero_total_entries() -> None:
    repo = MagicMock()
    repo.get_latest_for_user.return_value = None
    repo.count_for_user.return_value = 0
    result = _run(repo)
    assert result.total_entries == 0  # type: ignore[attr-defined]


def test_empty_user_returns_null_active_goal() -> None:
    repo = MagicMock()
    repo.get_latest_for_user.return_value = None
    repo.count_for_user.return_value = 0
    result = _run(repo)
    assert result.active_goal is None  # type: ignore[attr-defined]


def test_populated_user_returns_latest_entry() -> None:
    repo = MagicMock()
    entry = _make_entry()
    repo.get_latest_for_user.return_value = entry
    repo.count_for_user.return_value = 5
    result = _run(repo)
    assert result.latest_entry == entry  # type: ignore[attr-defined]


def test_populated_user_returns_correct_count() -> None:
    repo = MagicMock()
    repo.get_latest_for_user.return_value = _make_entry()
    repo.count_for_user.return_value = 5
    result = _run(repo)
    assert result.total_entries == 5  # type: ignore[attr-defined]


def test_soft_deleted_entries_excluded_from_count() -> None:
    """count_for_user excludes soft-deleted entries (repository responsibility)."""
    repo = MagicMock()
    repo.get_latest_for_user.return_value = _make_entry()
    # repo.count_for_user already filters is_deleted=False by contract
    repo.count_for_user.return_value = 3
    result = _run(repo)
    assert result.total_entries == 3  # type: ignore[attr-defined]
