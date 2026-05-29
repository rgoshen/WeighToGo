"""Goal progress calculation (FR-G-2).

Pure domain function — no framework or persistence dependencies.
"""

from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal


@dataclass(frozen=True)
class GoalProgress:
    """Value object representing progress toward a goal.

    Attributes:
        percent: Progress as a percentage in [0.0, 100.0].
    """

    percent: float


def calculate_progress(
    start: Decimal,
    current: Decimal,
    target: Decimal,
) -> GoalProgress:
    """Calculate goal progress as a percentage.

    Formula: ``(start - current) / (start - target) × 100``, clamped to
    ``[0.0, 100.0]``.  The formula is direction-agnostic: it works for
    both ``lose`` (target < start) and ``gain`` (target > start) goals
    because the signs in numerator and denominator cancel correctly.

    Complexity: O(1) time, O(1) space.

    Args:
        start: The starting weight captured when the goal was created.
        current: The user's most recent weight (converted to the goal's
            unit by the caller before this function is invoked).
        target: The target weight the user wants to reach.

    Returns:
        A :class:`GoalProgress` with ``percent`` in ``[0.0, 100.0]``.
    """
    denominator = start - target
    if denominator == Decimal("0"):
        return GoalProgress(percent=0.0)

    raw = float((start - current) / denominator) * 100.0
    return GoalProgress(percent=max(0.0, min(100.0, raw)))
