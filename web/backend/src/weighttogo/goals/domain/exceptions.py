"""Domain exceptions for the goals bounded context.

Raised by use cases; caught and translated to HTTP responses by the
interface layer.  No framework dependencies.
"""

from __future__ import annotations


class GoalNotFoundError(Exception):
    """Raised when a goal does not exist or belongs to another user.

    Maps to HTTP 404.
    """


class ActiveGoalAlreadyExistsError(Exception):
    """Raised when creating a goal while one is already active for the user.

    Maps to HTTP 409.
    """


class GoalNotActiveError(Exception):
    """Raised when attempting to mutate a goal that is no longer active.

    Maps to HTTP 409.
    """


class InvalidGoalTargetError(Exception):
    """Raised when a target value contradicts the goal's direction invariant.

    For LOSE goals the target must be strictly below start_value; for GAIN
    goals the target must be strictly above start_value.

    Maps to HTTP 422.
    """
