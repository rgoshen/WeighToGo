"""BuildDashboardSummary use case for the dashboard read model.

Covers FR-D-1 (summary), FR-D-4 (goal progress), FR-D-3 (weekly rate of
change), and FR-D-2 (trend series).
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import date, timedelta
from decimal import Decimal

from weighttogo.goals.application.get_active_goal_with_progress import (
    GetActiveGoalWithProgress,
    GetActiveGoalWithProgressCommand,
    GoalWithProgress,
)
from weighttogo.shared.units import convert_weight
from weighttogo.weight_tracking.domain.entities import WeightEntry
from weighttogo.weight_tracking.domain.ports import IWeightEntryRepository
from weighttogo.weight_tracking.domain.rate_of_change import (
    WINDOW_DAYS,
    RateEntry,
    RateOfChange,
    weekly_rate_of_change,
)

# Canonical unit the rate and trend are normalised to before display.
CANONICAL_UNIT = "lbs"


@dataclass(frozen=True)
class TrendPoint:
    """A single point in the weight trend series (FR-D-2).

    Attributes:
        observation_date: Calendar date of the measurement.
        weight_value: Recorded weight, normalised to ``lbs``.
        weight_unit: Always ``'lbs'`` — the series is reported in one unit.
    """

    observation_date: date
    weight_value: Decimal
    weight_unit: str


@dataclass(frozen=True)
class DashboardSummary:
    """Read-model returned by BuildDashboardSummary.

    Attributes:
        latest_entry: The most recent active weight entry in its stored unit,
            or ``None``.
        total_entries: Count of non-deleted weight entries.
        active_goal: The active goal with progress. ``goal`` within it is
            ``None`` when the user has no active goal.
        rate_of_change: The weekly rate of weight change (FR-D-3) in ``lbs``;
            ``weekly_rate`` is ``None`` when there is insufficient data.
        trend: The full weight series, oldest first, normalised to ``lbs``, for
            the trend chart (FR-D-2). Empty when the user has no entries.
    """

    latest_entry: WeightEntry | None
    total_entries: int
    active_goal: GoalWithProgress
    rate_of_change: RateOfChange
    trend: list[TrendPoint]


class BuildDashboardSummary:
    """Assemble the dashboard read model for the requesting user.

    Composes the ``weight_tracking`` and ``goals`` bounded contexts. Two
    repository reads are issued: a full indexed range read (``date.min`` to
    ``date.max``) that populates ``latest_entry``, ``total_entries``, and the
    trend series; and a second bounded indexed range read over the trailing
    ``2 × WINDOW_DAYS`` days, anchored on the latest observation, whose rows
    feed the weekly rate-of-change algorithm.  Goal progress is delegated to
    ``GetActiveGoalWithProgress`` (reused, not reimplemented).

    Anchoring the bounded window on the latest observation means the rate is
    computed correctly even when the most recent entry predates today.

    Args:
        weight_repo: The weight entry repository port.
        get_active_goal_with_progress: The goals use case that returns the
            active goal and its progress.
    """

    def __init__(
        self,
        weight_repo: IWeightEntryRepository,
        get_active_goal_with_progress: GetActiveGoalWithProgress,
    ) -> None:
        """Initialise with the weight repository and the goals use case."""
        self._repo = weight_repo
        self._get_active_goal = get_active_goal_with_progress

    def execute(self, user_id: int, today: date | None = None) -> DashboardSummary:
        """Build the dashboard summary for *user_id*.

        Args:
            user_id: The requesting user's surrogate ID.
            today: Accepted for backwards compatibility; the rate now anchors on
                the latest observation in the series rather than this date.

        Returns:
            A ``DashboardSummary``; ``active_goal.goal`` is ``None`` when the
            user has no active goal, and ``trend`` is empty when the user has no
            entries.
        """
        # "All time" series: the full closed date range. The repository returns
        # entries oldest-first via the (user_id, observation_date) index.
        entries = self._repo.list_for_user_in_range(user_id, date.min, date.max)

        latest_entry = entries[-1] if entries else None
        total_entries = len(entries)

        goal_with_progress = self._get_active_goal.execute(
            GetActiveGoalWithProgressCommand(
                user_id=user_id,
                latest_weight_value=latest_entry.weight_value if latest_entry else None,
                latest_weight_unit=latest_entry.weight_unit if latest_entry else None,
                readonly=True,
            )
        )

        # Bounded read: trailing 2*WINDOW_DAYS days anchored on the latest
        # observation. One indexed range scan covers both 7-day windows; the
        # domain function partitions in memory. Skipped when no entries exist.
        if latest_entry is not None:
            anchor = latest_entry.observation_date
            rate_raw = self._repo.list_for_user_in_range(
                user_id,
                anchor - timedelta(days=2 * WINDOW_DAYS),
                anchor,
            )
        else:
            rate_raw = []

        # Normalise the rate inputs to a single canonical unit so the algorithm
        # never averages mixed units. Normalisation stays in the application
        # layer; the pure domain function operates on one-unit data.
        rate_entries = [
            RateEntry(
                observation_date=e.observation_date,
                weight_value=convert_weight(e.weight_value, e.weight_unit, CANONICAL_UNIT),
                weight_unit=CANONICAL_UNIT,
            )
            for e in rate_raw
        ]
        rate_of_change = weekly_rate_of_change(rate_entries)

        trend = [
            TrendPoint(
                observation_date=e.observation_date,
                weight_value=convert_weight(e.weight_value, e.weight_unit, CANONICAL_UNIT),
                weight_unit=CANONICAL_UNIT,
            )
            for e in entries
        ]

        return DashboardSummary(
            latest_entry=latest_entry,
            total_entries=total_entries,
            active_goal=goal_with_progress,
            rate_of_change=rate_of_change,
            trend=trend,
        )
