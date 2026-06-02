"""Pydantic response schemas for the achievements API endpoints (SRS §9.7)."""

from __future__ import annotations

from datetime import datetime
from decimal import Decimal

from pydantic import BaseModel


class AchievementResponse(BaseModel):
    """Response body for a single achievement.

    Attributes:
        achievement_id: The surrogate primary key.
        goal_id: The goal this achievement belongs to.
        achievement_type: ``'goal_reached'``, ``'milestone'``, or
            ``'streak'``.
        threshold: The lb threshold crossed for milestones or the day count
            for streaks; ``None`` for ``goal_reached`` achievements.
        earned_at: UTC timestamp when the achievement was recorded.
    """

    achievement_id: int
    goal_id: int
    achievement_type: str
    threshold: Decimal | None
    earned_at: datetime

    model_config = {"from_attributes": True}


class AchievementListResponse(BaseModel):
    """Response envelope for the list achievements endpoint (SRS §9.7)."""

    items: list[AchievementResponse]
