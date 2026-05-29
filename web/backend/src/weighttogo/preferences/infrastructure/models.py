"""SQLAlchemy ORM model for the preferences bounded context (EAV table).

Implements the user_preferences table from migration 0006_user_preferences.
ADR-0020: EAV key-value option selected.
"""

from __future__ import annotations

from datetime import UTC, datetime

from sqlalchemy import BigInteger, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from weighttogo.auth.infrastructure.models import Base

_BigInt = BigInteger().with_variant(Integer(), "sqlite")


class UserPreferenceModel(Base):
    """ORM model for the ``user_preferences`` EAV table."""

    __tablename__ = "user_preferences"

    id: Mapped[int] = mapped_column(_BigInt, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        _BigInt, ForeignKey("users.user_id", ondelete="CASCADE"), nullable=False
    )
    pref_key: Mapped[str] = mapped_column(String(40), nullable=False)
    pref_value: Mapped[str] = mapped_column(String(40), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=lambda: datetime.now(UTC)
    )
