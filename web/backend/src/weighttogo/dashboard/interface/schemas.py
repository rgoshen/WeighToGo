"""Pydantic response schema for the dashboard API endpoint (response-only)."""

from __future__ import annotations

from pydantic import BaseModel

from weighttogo.goals.interface.schemas import ActiveGoalResponse
from weighttogo.weight_tracking.interface.schemas import WeightEntryResponse


class DashboardSummaryResponse(BaseModel):
    """Response body for GET /api/v1/dashboard/summary (SRS §9.5, FR-D-1, FR-D-4).

    Attributes:
        latest_entry: The user's most recent active weight entry, or ``None``.
        total_entries: Count of non-deleted weight entries.
        active_goal: The active goal with progress, or ``None`` when no active
            goal exists.
    """

    latest_entry: WeightEntryResponse | None
    total_entries: int
    active_goal: ActiveGoalResponse | None = None
