"""Domain entities for the goals bounded context.

Entities have identity and encapsulate domain behaviour. They carry no
dependency on any framework or persistence technology.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import UTC, date, datetime
from decimal import Decimal
from enum import StrEnum


class GoalType(StrEnum):
    """Indicates the direction of a goal (lose or gain weight)."""

    LOSE = "lose"
    GAIN = "gain"


@dataclass
class Goal:
    """A user's weight goal.

    Attributes:
        goal_id: Surrogate primary key. ``None`` for unsaved goals.
        user_id: FK to the owning user.
        target_value: The weight value the user wants to reach.
        target_unit: Either ``'lbs'`` or ``'kg'``.
        start_value: The user's weight at the time the goal was created.
        goal_type: Whether the user wants to lose or gain weight.
        target_date: Optional date by which the user hopes to reach the goal.
        is_active: ``True`` while the goal is in progress.
        is_achieved: ``True`` once the target has been reached.
        achieved_at: UTC datetime when the goal was achieved, or ``None``.
        created_at: UTC datetime the record was first persisted.
        updated_at: UTC datetime of the most recent update.
    """

    goal_id: int | None
    user_id: int
    target_value: Decimal
    target_unit: str
    start_value: Decimal
    goal_type: GoalType
    target_date: date | None
    is_active: bool
    is_achieved: bool
    achieved_at: datetime | None
    created_at: datetime
    updated_at: datetime

    def abandon(self) -> None:
        """Deactivate this goal without marking it achieved.

        Idempotent: calling this method more than once is safe — the first
        call sets ``is_active = False``; subsequent calls are no-ops.
        """
        if not self.is_active:
            return
        self.is_active = False
        self.updated_at = datetime.now(UTC)

    def mark_achieved(self) -> None:
        """Record that the target weight has been reached.

        Idempotent: calling this method more than once leaves
        ``achieved_at`` unchanged after the first call.
        """
        if self.is_achieved:
            return
        self.is_achieved = True
        self.is_active = False
        self.achieved_at = datetime.now(UTC)
        self.updated_at = self.achieved_at
