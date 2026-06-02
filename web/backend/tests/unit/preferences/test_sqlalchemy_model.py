"""Unit tests for UserPreferenceModel ORM definition and constraint rejection."""

from __future__ import annotations

from collections.abc import Callable

import pytest
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.preferences.infrastructure.models import UserPreferenceModel


def test_preference_model_tablename() -> None:
    assert UserPreferenceModel.__tablename__ == "user_preferences"


def test_preference_model_has_required_columns() -> None:
    cols = {c.key for c in UserPreferenceModel.__table__.columns}
    assert {"id", "user_id", "pref_key", "pref_value", "updated_at"} <= cols


def test_preference_invalid_key_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    # ARRANGE
    user_id = make_user()
    db_session.add(
        UserPreferenceModel(
            user_id=user_id,
            pref_key="unknown_key",
            pref_value="lbs",
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_preference_invalid_value_rejected(
    db_session: Session, make_user: Callable[..., int]
) -> None:
    # ARRANGE
    user_id = make_user()
    db_session.add(
        UserPreferenceModel(
            user_id=user_id,
            pref_key="weight_unit",
            pref_value="pounds",  # only 'lbs' or 'kg' are valid
        )
    )
    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()
