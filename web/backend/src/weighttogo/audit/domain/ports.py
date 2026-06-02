"""Port (interface) for the audit repository.

ADR-0024: append-only — no update or delete paths are exposed.
"""

from __future__ import annotations

from typing import Protocol, runtime_checkable

from weighttogo.audit.domain.entities import AuditEvent


@runtime_checkable
class IAuditRepository(Protocol):
    """Append-only audit event store."""

    def add(self, event: AuditEvent) -> None:
        """Persist a new audit event. Never updates or deletes."""
        ...
