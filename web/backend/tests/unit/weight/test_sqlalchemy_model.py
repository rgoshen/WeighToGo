"""Unit tests for WeightEntryModel ORM model (subtask 13)."""

from __future__ import annotations

from collections.abc import Callable
from datetime import UTC, date, datetime
from decimal import Decimal

import pytest
from sqlalchemy import BigInteger, Boolean, Date, DateTime, Numeric, String
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.weight_tracking.infrastructure.models import WeightEntryModel


def test_weight_entry_model_table_name() -> None:
    assert WeightEntryModel.__tablename__ == "weight_entries"


def test_entry_id_is_biginteger() -> None:
    assert isinstance(WeightEntryModel.__table__.c.entry_id.type, BigInteger)


def test_user_id_is_biginteger() -> None:
    assert isinstance(WeightEntryModel.__table__.c.user_id.type, BigInteger)


def test_weight_value_is_numeric_6_2() -> None:
    col_type = WeightEntryModel.__table__.c.weight_value.type
    assert isinstance(col_type, Numeric)
    assert col_type.precision == 6
    assert col_type.scale == 2


def test_weight_unit_is_string() -> None:
    assert isinstance(WeightEntryModel.__table__.c.weight_unit.type, String)


def test_observation_date_is_date() -> None:
    assert isinstance(WeightEntryModel.__table__.c.observation_date.type, Date)


def test_created_at_is_timezone_aware_datetime() -> None:
    col = WeightEntryModel.__table__.c.created_at
    assert isinstance(col.type, DateTime)
    assert col.type.timezone is True


def test_is_deleted_is_boolean() -> None:
    assert isinstance(WeightEntryModel.__table__.c.is_deleted.type, Boolean)


def test_model_accepts_decimal_weight_value() -> None:
    """WeightEntryModel constructor accepts a Decimal without truncation."""
    entry = WeightEntryModel(
        user_id=1,
        weight_value=Decimal("175.50"),
        weight_unit="lbs",
        observation_date=date(2026, 5, 20),
        notes=None,
        created_at=datetime(2026, 5, 20, tzinfo=UTC),
        updated_at=datetime(2026, 5, 20, tzinfo=UTC),
        is_deleted=False,
        deleted_at=None,
    )
    assert entry.weight_value == Decimal("175.50")


def test_weight_entry_negative_value_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    # ARRANGE
    user_id = make_user()
    db_session.add(
        WeightEntryModel(
            user_id=user_id,
            weight_value=Decimal("-1.0"),
            weight_unit="lbs",
            observation_date=date(2026, 1, 1),
            is_deleted=False,
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_weight_entry_invalid_unit_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    # ARRANGE
    user_id = make_user()
    db_session.add(
        WeightEntryModel(
            user_id=user_id,
            weight_value=Decimal("150.0"),
            weight_unit="pounds",
            observation_date=date(2026, 1, 1),
            is_deleted=False,
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()
