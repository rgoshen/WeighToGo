"""Unit tests for the GetRateOfChange use case (FR-D-3).

Uses an in-memory fake repository so the use case is verified without any
database dependency.
"""

from __future__ import annotations

from datetime import UTC, date, datetime, timedelta
from decimal import Decimal

from weighttogo.weight_tracking.application.get_rate_of_change import GetRateOfChange
from weighttogo.weight_tracking.domain.entities import WeightEntry


class _FakeRepo:
    """Minimal fake recording the range query and returning canned entries."""

    def __init__(self, entries: list[WeightEntry]) -> None:
        self._entries = entries
        self.last_range: tuple[int, date, date] | None = None

    def list_for_user_in_range(self, user_id: int, start: date, end: date) -> list[WeightEntry]:
        self.last_range = (user_id, start, end)
        return [
            e for e in self._entries if e.user_id == user_id and start <= e.observation_date <= end
        ]


def _entry(observation_date: date, value: str) -> WeightEntry:
    now = datetime.now(UTC)
    return WeightEntry(
        entry_id=None,
        user_id=1,
        weight_value=Decimal(value),
        weight_unit="lbs",
        observation_date=observation_date,
        notes=None,
        created_at=now,
        updated_at=now,
    )


def test_returns_none_rate_when_no_entries() -> None:
    # ARRANGE
    repo = _FakeRepo([])
    use_case = GetRateOfChange(weight_repo=repo)

    # ACT
    result = use_case.execute(user_id=1, today=date(2026, 5, 29))

    # ASSERT
    assert result.weekly_rate is None
    assert result.reason == "insufficient_data"


def test_computes_rate_from_repo_entries() -> None:
    # ARRANGE — falling trend across the two 7-day windows
    anchor = date(2026, 5, 29)
    repo = _FakeRepo(
        [
            _entry(anchor - timedelta(days=10), "190.0"),
            _entry(anchor - timedelta(days=2), "182.0"),
        ]
    )
    use_case = GetRateOfChange(weight_repo=repo)

    # ACT
    result = use_case.execute(user_id=1, today=anchor)

    # ASSERT
    assert result.weekly_rate == Decimal("-8")
    assert result.unit == "lbs"


def test_uses_14_day_range_read_ending_today() -> None:
    # ARRANGE
    repo = _FakeRepo([])
    use_case = GetRateOfChange(weight_repo=repo)
    today = date(2026, 5, 29)

    # ACT
    use_case.execute(user_id=7, today=today)

    # ASSERT — the use case must query exactly the trailing 14-day window
    assert repo.last_range == (7, today - timedelta(days=14), today)
