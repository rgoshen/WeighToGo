"""SQLAlchemy ORM models for the weight_tracking bounded context.

These models map Python objects to the ``weight_entries`` table defined in
SRS §8.2.3.  They exist only in the infrastructure layer and are never
imported by the domain or application layers (import-linter enforces this).

The shared ``Base`` from the auth infrastructure is reused so that a single
``create_all`` call in tests covers all tables (ADR-0012 permits sharing the
declarative base across slices).
"""

from __future__ import annotations

from datetime import UTC, datetime
from datetime import date as date_type
from decimal import Decimal

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    Date,
    DateTime,
    ForeignKey,
    Integer,
    Numeric,
    String,
)
from sqlalchemy.orm import Mapped, mapped_column

from weighttogo.auth.infrastructure.models import Base

# SQLite auto-increments only INTEGER PKs (not BIGINT). Use BigInteger on
# PostgreSQL and fall back to Integer on SQLite so in-memory tests work.
_BigInt = BigInteger().with_variant(Integer(), "sqlite")


class WeightEntryModel(Base):
    """ORM model for the ``weight_entries`` table (SRS §8.2.3).

    Column notes:
        weight_value uses ``Numeric(6, 2)`` to preserve two decimal places of
        precision through the ORM layer (SRS §3.2 micro-decision 1).
        observation_date is stored as a ``Date`` — no time component.
        created_at / updated_at / deleted_at carry timezone information.
    """

    __tablename__ = "weight_entries"
    __table_args__ = (
        CheckConstraint("weight_value > 0", name="weight_entries_value_positive"),
        CheckConstraint("weight_value <= 1500", name="weight_entries_value_max"),
        CheckConstraint("weight_unit IN ('lbs', 'kg')", name="weight_entries_unit_valid"),
        CheckConstraint(
            "observation_date <= CURRENT_DATE",
            name="weight_entries_observation_not_future",
        ),
        CheckConstraint(
            "(is_deleted = FALSE AND deleted_at IS NULL)"
            " OR (is_deleted = TRUE AND deleted_at IS NOT NULL)",
            name="weight_entries_deletion_consistency",
        ),
    )

    entry_id: Mapped[int] = mapped_column(_BigInt, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        _BigInt, ForeignKey("users.user_id", ondelete="CASCADE"), nullable=False
    )
    weight_value: Mapped[Decimal] = mapped_column(Numeric(precision=6, scale=2), nullable=False)
    weight_unit: Mapped[str] = mapped_column(String(3), nullable=False)
    observation_date: Mapped[date_type] = mapped_column(Date(), nullable=False)
    notes: Mapped[str | None] = mapped_column(String(500), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=lambda: datetime.now(UTC)
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=lambda: datetime.now(UTC)
    )
    is_deleted: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
