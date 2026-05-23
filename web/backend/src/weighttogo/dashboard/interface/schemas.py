"""Pydantic response schemas for the dashboard API endpoint.

Schemas are response-only — no request body for GET /dashboard/summary.
"""

from __future__ import annotations

from pydantic import BaseModel

from weighttogo.weight_tracking.interface.schemas import WeightEntryResponse


class DashboardSummaryResponse(BaseModel):
    """Response body for GET /api/v1/dashboard/summary (SRS §9.5, FR-D-1).

    Attributes:
        latest_entry: The user's most recent active weight entry, or ``None``
            when no entries exist.
        total_entries: Count of non-deleted weight entries.
        active_goal: Always ``None`` in M2 (SRS §6.7, §13.1.4 deferred to M3).
    """

    latest_entry: WeightEntryResponse | None
    total_entries: int
    active_goal: None = None
