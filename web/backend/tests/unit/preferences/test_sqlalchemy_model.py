"""Unit tests for UserPreferenceModel ORM definition and constraint rejection."""

from __future__ import annotations

import pytest
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from weighttogo.auth.infrastructure.models import UserModel
from weighttogo.preferences.infrastructure.models import UserPreferenceModel


def _make_user(session: Session) -> int:
    user = UserModel(
        email="p@example.com",
        password_hash="x",
        display_name="P",
        is_active=True,
        failed_login_count=0,
    )
    session.add(user)
    session.flush()
    return int(user.user_id)


def test_preference_model_tablename() -> None:
    assert UserPreferenceModel.__tablename__ == "user_preferences"


def test_preference_model_has_required_columns() -> None:
    cols = {c.key for c in UserPreferenceModel.__table__.columns}
    assert {"id", "user_id", "pref_key", "pref_value", "updated_at"} <= cols


def test_preference_invalid_key_rejected(db_session: Session) -> None:
    user_id = _make_user(db_session)
    db_session.add(
        UserPreferenceModel(
            user_id=user_id,
            pref_key="unknown_key",
            pref_value="lbs",
        )
    )
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_preference_invalid_value_rejected(db_session: Session) -> None:
    user_id = _make_user(db_session)
    db_session.add(
        UserPreferenceModel(
            user_id=user_id,
            pref_key="weight_unit",
            pref_value="pounds",  # only 'lbs' or 'kg' are valid
        )
    )
    with pytest.raises(IntegrityError):
        db_session.flush()
