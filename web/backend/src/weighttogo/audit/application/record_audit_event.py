"""RecordAuditEvent use case (SRS §8.2.7 / ADR-0024)."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import UTC, datetime
from typing import Any

from weighttogo.audit.domain.entities import AuditEvent, AuditEventType, ResourceType
from weighttogo.audit.domain.ports import IAuditRepository


@dataclass(frozen=True)
class RecordAuditEventCommand:
    """Input for the RecordAuditEvent use case.

    Attributes:
        event_type: The taxonomy value for this event.
        user_id: Authenticated user's ID. ``None`` for unauthenticated events.
        resource_type: Kind of resource affected (data-mutation events only).
        resource_id: PK of the affected resource (data-mutation events only).
        request_id: X-Request-ID header value for log correlation (NFR-O-2).
        ip_address: Client IP address string.
        metadata: Arbitrary JSON payload (e.g. masked email for failed logins).
    """

    event_type: AuditEventType
    user_id: int | None = None
    resource_type: ResourceType | None = None
    resource_id: int | None = None
    request_id: str | None = None
    ip_address: str | None = None
    metadata: dict[str, Any] | None = None


class RecordAuditEvent:
    """Create and persist an audit event.

    Returns ``None`` — the audit row is a side-effect with no caller-facing value.

    Args:
        audit_repo: The append-only audit repository port.
    """

    def __init__(self, audit_repo: IAuditRepository) -> None:
        """Initialise the use case with the audit repository port."""
        self._repo = audit_repo

    def execute(self, cmd: RecordAuditEventCommand) -> None:
        """Build an ``AuditEvent`` from the command and persist it.

        Args:
            cmd: Command carrying the event type and context fields.
        """
        event = AuditEvent(
            audit_id=None,
            user_id=cmd.user_id,
            event_type=cmd.event_type,
            resource_type=cmd.resource_type,
            resource_id=cmd.resource_id,
            request_id=cmd.request_id,
            ip_address=cmd.ip_address,
            metadata=cmd.metadata,
            created_at=datetime.now(UTC),
        )
        self._repo.add(event)
