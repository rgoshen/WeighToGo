"""ORM ↔ migration type-parity tests for weight_entries (subtask 14).

Mirrors auth/test_orm_migration_types.py: verifies that WeightEntryModel
column types match what the migration creates so a future refactor cannot
silently drift them apart.
"""

from __future__ import annotations

from sqlalchemy import BigInteger, Boolean, Date, DateTime, Numeric, String

from weighttogo.weight_tracking.infrastructure.models import WeightEntryModel


def test_entry_id_column_is_biginteger() -> None:
    assert isinstance(WeightEntryModel.__table__.c.entry_id.type, BigInteger)


def test_user_id_fk_column_is_biginteger() -> None:
    assert isinstance(WeightEntryModel.__table__.c.user_id.type, BigInteger)


def test_weight_value_column_is_numeric() -> None:
    col_type = WeightEntryModel.__table__.c.weight_value.type
    assert isinstance(col_type, Numeric)


def test_weight_unit_column_is_string() -> None:
    assert isinstance(WeightEntryModel.__table__.c.weight_unit.type, String)


def test_observation_date_column_is_date() -> None:
    assert isinstance(WeightEntryModel.__table__.c.observation_date.type, Date)


def test_created_at_column_is_datetime() -> None:
    assert isinstance(WeightEntryModel.__table__.c.created_at.type, DateTime)


def test_updated_at_column_is_datetime() -> None:
    assert isinstance(WeightEntryModel.__table__.c.updated_at.type, DateTime)


def test_is_deleted_column_is_boolean() -> None:
    assert isinstance(WeightEntryModel.__table__.c.is_deleted.type, Boolean)


def test_deleted_at_column_is_datetime() -> None:
    assert isinstance(WeightEntryModel.__table__.c.deleted_at.type, DateTime)
