"""SQLAlchemy ORM model for the audit_log table.

SRS §8.2.7 / ADR-0024. Backend-only — no router, no API endpoint.
The shared ``Base`` from auth infrastructure is reused so that a single
``create_all`` call in tests covers all tables (ADR-0012).
"""

from __future__ import annotations

from datetime import UTC, datetime
from typing import Any

from sqlalchemy import (
    JSON,
    BigInteger,
    CheckConstraint,
    DateTime,
    ForeignKey,
    Index,
    Integer,
    String,
    text,
)
from sqlalchemy.orm import Mapped, mapped_column

from weighttogo.audit.domain.entities import AuditEventType
from weighttogo.auth.infrastructure.models import Base

# SQLite auto-increments only INTEGER PKs (not BIGINT). Use BigInteger on
# PostgreSQL and fall back to Integer on SQLite so in-memory tests work.
_BigInt = BigInteger().with_variant(Integer(), "sqlite")

# Build the CHECK constraint from the canonical AuditEventType enum so that
# adding a new event type to the enum automatically extends the constraint.
_VALID_EVENT_TYPES = ", ".join(f"'{v.value}'" for v in AuditEventType)


class AuditLogModel(Base):
    """ORM model for the ``audit_log`` table (SRS §8.2.7, ADR-0024)."""

    __tablename__ = "audit_log"
    __table_args__ = (
        CheckConstraint(
            f"event_type IN ({_VALID_EVENT_TYPES})",
            name="audit_log_event_type_valid",
        ),
        CheckConstraint(
            "resource_id IS NULL OR resource_type IS NOT NULL",
            name="audit_log_resource_consistency",
        ),
        Index("idx_audit_log_user_created", "user_id", text("created_at DESC")),
        Index("idx_audit_log_event_type_created", "event_type", text("created_at DESC")),
    )

    audit_id: Mapped[int] = mapped_column(_BigInt, primary_key=True, autoincrement=True)
    user_id: Mapped[int | None] = mapped_column(
        _BigInt, ForeignKey("users.user_id", ondelete="SET NULL"), nullable=True
    )
    event_type: Mapped[str] = mapped_column(String(50), nullable=False)
    resource_type: Mapped[str | None] = mapped_column(String(30), nullable=True)
    resource_id: Mapped[int | None] = mapped_column(_BigInt, nullable=True)
    request_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    ip_address: Mapped[str | None] = mapped_column(String(45), nullable=True)
    # "metadata" is a reserved SQLAlchemy class attribute on Base; use
    # event_metadata as the Python name and map it to the "metadata" column.
    event_metadata: Mapped[dict[str, Any] | None] = mapped_column("metadata", JSON(), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=lambda: datetime.now(UTC)
    )
