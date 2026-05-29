"""AbandonGoal use case."""

from __future__ import annotations

from dataclasses import dataclass

from weighttogo.goals.domain.exceptions import GoalNotFoundError
from weighttogo.goals.domain.ports import IGoalRepository


@dataclass(frozen=True)
class AbandonGoalCommand:
    """Input for the AbandonGoal use case."""

    user_id: int
    goal_id: int


class AbandonGoal:
    """Deactivate a goal without marking it achieved.

    Idempotent: abandoning an already-inactive goal is a no-op at the domain
    level and still returns successfully.

    Args:
        goal_repo: The goal repository port.
    """

    def __init__(self, goal_repo: IGoalRepository) -> None:
        """Initialise with the goal repository port."""
        self._repo = goal_repo

    def execute(self, command: AbandonGoalCommand) -> None:
        """Execute the use case.

        Args:
            command: The abandon command with user and goal IDs.

        Raises:
            GoalNotFoundError: When the goal does not exist or belongs to
                a different user.
        """
        goal = self._repo.get_by_id(command.goal_id, command.user_id)
        if goal is None:
            raise GoalNotFoundError()

        goal.abandon()
        self._repo.save(goal)
