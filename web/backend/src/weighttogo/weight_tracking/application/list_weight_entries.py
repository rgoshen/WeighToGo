"""ListWeightEntries use case (FR-W-2).

The cursor encodes the full sort key ``(observation_date, entry_id)`` per
ADR-0015. The use case stays in domain terms; the router is responsible
for encoding/decoding the opaque wire format.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import date

from weighttogo.weight_tracking.domain.entities import WeightEntry
from weighttogo.weight_tracking.domain.ports import IWeightEntryRepository


@dataclass(frozen=True)
class ListWeightEntriesCommand:
    """Input for the ListWeightEntries use case."""

    user_id: int
    limit: int
    cursor: tuple[date, int] | None


@dataclass(frozen=True)
class WeightEntryPage:
    """Output envelope for a paginated list of weight entries.

    ``next_cursor`` is the compound sort key ``(observation_date, entry_id)``
    of the **last returned entry**, or ``None`` when no further pages exist.
    The router base64-encodes this tuple before returning it on the wire.
    """

    items: list[WeightEntry]
    next_cursor: tuple[date, int] | None


class ListWeightEntries:
    """Return a paginated page of active weight entries for a user.

    Args:
        weight_repo: The weight entry repository port.
    """

    def __init__(self, weight_repo: IWeightEntryRepository) -> None:
        """Initialise with the weight entry repository port."""
        self._repo = weight_repo

    def execute(self, command: ListWeightEntriesCommand) -> WeightEntryPage:
        """Execute the use case.

        Fetches ``limit + 1`` rows from the repository to detect whether a
        next page exists, trims to *limit*, and derives ``next_cursor`` from
        the **last returned** entry (keyset pagination — ADR-0015). Using
        the last returned row instead of the peeked-but-trimmed row is what
        prevents the page-boundary skip the original implementation had.

        Args:
            command: The list command with pagination parameters.

        Returns:
            A ``WeightEntryPage`` with ``items`` and ``next_cursor``.
        """
        rows = self._repo.list_for_user(
            user_id=command.user_id,
            limit=command.limit + 1,
            before=command.cursor,
        )

        if len(rows) > command.limit:
            items = rows[: command.limit]
            last = items[-1]
            # Repo returns persisted rows; entry_id is never None on this path.
            assert last.entry_id is not None
            next_cursor: tuple[date, int] | None = (last.observation_date, last.entry_id)
        else:
            items = rows
            next_cursor = None

        return WeightEntryPage(items=items, next_cursor=next_cursor)
