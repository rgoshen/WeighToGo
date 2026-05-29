"""GetRateOfChange use case (FR-D-3).

Fetches the trailing two-week window of entries via the indexed range read and
delegates the arithmetic to the pure ``weekly_rate_of_change`` domain function.
"""

from __future__ import annotations

from datetime import date, timedelta
from typing import Protocol

from weighttogo.weight_tracking.domain.entities import WeightEntry
from weighttogo.weight_tracking.domain.rate_of_change import (
    RateEntry,
    RateOfChange,
    weekly_rate_of_change,
)

WINDOW_DAYS = 14


class _RangeReader(Protocol):
    """Structural type for the one repository method this use case needs."""

    def list_for_user_in_range(self, user_id: int, start: date, end: date) -> list[WeightEntry]: ...


class GetRateOfChange:
    """Return the user's weekly rate of weight change (FR-D-3).

    Reads only the trailing 14-day window — the minimum span the two 7-day
    moving-average windows require — so the indexed range read stays small.

    Args:
        weight_repo: A repository exposing ``list_for_user_in_range``.
    """

    def __init__(self, weight_repo: _RangeReader) -> None:
        """Initialise with the weight repository range reader."""
        self._repo = weight_repo

    def execute(self, user_id: int, today: date) -> RateOfChange:
        """Compute the weekly rate of change for *user_id* as of *today*.

        Args:
            user_id: The requesting user's surrogate ID.
            today: The reference date; the read window is ``[today - 14d,
                today]``.  Passed in (not read from the clock) so the use case
                stays deterministic and testable.

        Returns:
            A :class:`RateOfChange`; ``weekly_rate`` is ``None`` with
            ``reason='insufficient_data'`` when either 7-day window is empty.
        """
        start = today - timedelta(days=WINDOW_DAYS)
        entries = self._repo.list_for_user_in_range(user_id, start, today)
        projected = [
            RateEntry(
                observation_date=e.observation_date,
                weight_value=e.weight_value,
                weight_unit=e.weight_unit,
            )
            for e in entries
        ]
        return weekly_rate_of_change(projected)
