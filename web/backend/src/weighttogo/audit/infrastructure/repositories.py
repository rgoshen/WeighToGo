"""SQLAlchemy implementation of the IAuditRepository port (ADR-0024)."""

from __future__ import annotations

from sqlalchemy.orm import Session

from weighttogo.audit.domain.entities import AuditEvent
from weighttogo.audit.infrastructure.models import AuditLogModel


class SqlAlchemyAuditRepository:
    """Append-only audit event store backed by SQLAlchemy.

    Args:
        session: The active request-scoped SQLAlchemy session.
    """

    def __init__(self, session: Session) -> None:
        """Initialise the repository with the request-scoped session."""
        self._session = session

    def add(self, event: AuditEvent) -> None:
        """Stage a new audit row on the session. Does not flush or commit.

        Args:
            event: The ``AuditEvent`` to persist.
        """
        row = AuditLogModel(
            user_id=event.user_id,
            event_type=str(event.event_type),
            resource_type=str(event.resource_type) if event.resource_type else None,
            resource_id=event.resource_id,
            request_id=event.request_id[:64] if event.request_id else None,
            ip_address=event.ip_address[:45] if event.ip_address else None,
            event_metadata=event.metadata,
            created_at=event.created_at,
        )
        self._session.add(row)
