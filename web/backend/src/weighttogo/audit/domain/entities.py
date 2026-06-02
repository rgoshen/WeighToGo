"""Domain entities for the audit bounded context.

SRS §8.2.7 / ADR-0024. No framework imports.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from enum import StrEnum
from typing import Any


class AuditEventType(StrEnum):
    """Fixed event taxonomy enforced by CHECK constraint (ADR-0024)."""

    AUTH_REGISTER = "auth.register"
    AUTH_LOGIN_SUCCEEDED = "auth.login_succeeded"
    AUTH_LOGIN_FAILED = "auth.login_failed"
    AUTH_LOGOUT = "auth.logout"
    AUTH_TOKEN_REFRESHED = "auth.token_refreshed"
    AUTH_TOKEN_REUSE_DETECTED = "auth.token_reuse_detected"
    AUTH_ACCOUNT_LOCKED = "auth.account_locked"
    WEIGHT_ENTRY_CREATED = "weight_entry.created"
    WEIGHT_ENTRY_UPDATED = "weight_entry.updated"
    WEIGHT_ENTRY_DELETED = "weight_entry.deleted"
    GOAL_CREATED = "goal.created"
    GOAL_UPDATED = "goal.updated"
    GOAL_ABANDONED = "goal.abandoned"
    PREFERENCE_UPDATED = "preference.updated"


class ResourceType(StrEnum):
    """Resource types for data-mutation audit events."""

    WEIGHT_ENTRY = "weight_entry"
    GOAL = "goal"
    PREFERENCE = "preference"


@dataclass
class AuditEvent:
    """An immutable record of a security or data-mutation event.

    Attributes:
        audit_id: Surrogate PK. ``None`` for unsaved records.
        user_id: FK to users. ``None`` for unauthenticated events (e.g. failed login).
        event_type: Fixed taxonomy value.
        resource_type: The kind of resource affected (data-mutation events only).
        resource_id: PK of the affected resource (data-mutation events only).
        request_id: X-Request-ID header value for correlation (NFR-O-2).
        ip_address: Client IP as a string; String(45) covers IPv4 and IPv6.
        metadata: Arbitrary JSON payload (e.g. masked email for failed logins).
        created_at: UTC timestamp set at persistence time.
    """

    audit_id: int | None
    user_id: int | None
    event_type: AuditEventType
    resource_type: ResourceType | None
    resource_id: int | None
    request_id: str | None
    ip_address: str | None
    metadata: dict[str, Any] | None
    created_at: datetime
