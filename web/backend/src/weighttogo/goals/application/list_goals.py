"""ListGoals use case (FR-G-5 list endpoint)."""

from __future__ import annotations

from dataclasses import dataclass

from weighttogo.goals.domain.entities import Goal
from weighttogo.goals.domain.ports import IGoalRepository


@dataclass(frozen=True)
class ListGoalsCommand:
    """Input for the ListGoals use case."""

    user_id: int


class ListGoals:
    """Return all goals (active and historical) for a user.

    Args:
        goal_repo: The goal repository port.
    """

    def __init__(self, goal_repo: IGoalRepository) -> None:
        """Initialise with the goal repository port."""
        self._repo = goal_repo

    def execute(self, command: ListGoalsCommand) -> list[Goal]:
        """Execute the use case.

        Args:
            command: The command carrying the user ID.

        Returns:
            All goals for the user, newest first.
        """
        return self._repo.list_for_user(command.user_id)
