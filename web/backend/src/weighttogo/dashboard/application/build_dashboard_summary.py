"""BuildDashboardSummary use case (FR-D-1)."""

from __future__ import annotations

from dataclasses import dataclass

from weighttogo.weight_tracking.domain.entities import WeightEntry
from weighttogo.weight_tracking.domain.ports import IWeightEntryRepository


@dataclass(frozen=True)
class DashboardSummary:
    """The read-model returned by BuildDashboardSummary.

    Attributes:
        latest_entry: The most recent active weight entry, or ``None`` when the
            user has no entries.
        total_entries: Count of non-deleted weight entries.
        active_goal: Always ``None`` in M2; goal integration is deferred to M3
            (SRS §13.1.4, §6.7).
    """

    latest_entry: WeightEntry | None
    total_entries: int
    active_goal: None = None


class BuildDashboardSummary:
    """Assemble the dashboard summary read model for the requesting user.

    This use case owns no domain entities of its own.  It composes data from
    the ``weight_tracking`` bounded context via ``IWeightEntryRepository``,
    which is the only port it depends on.

    Args:
        weight_repo: The weight entry repository port.
    """

    def __init__(self, weight_repo: IWeightEntryRepository) -> None:
        """Initialise with the weight entry repository port."""
        self._repo = weight_repo

    def execute(self, user_id: int) -> DashboardSummary:
        """Build and return the dashboard summary for *user_id*.

        Args:
            user_id: The requesting user's surrogate ID.

        Returns:
            A ``DashboardSummary`` with the latest entry, total count, and a
            null active goal (M2 placeholder per SRS §6.7).
        """
        latest_entry = self._repo.get_latest_for_user(user_id)
        total_entries = self._repo.count_for_user(user_id)
        return DashboardSummary(
            latest_entry=latest_entry,
            total_entries=total_entries,
        )
