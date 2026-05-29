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
