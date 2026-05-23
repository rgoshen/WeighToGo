"""Unit tests verifying SQLAlchemy ORM models are importable and well-formed."""

from weighttogo.auth.infrastructure.models import RefreshTokenModel, UserModel


def test_user_model_has_required_columns() -> None:
    required = {
        "user_id",
        "email",
        "password_hash",
        "display_name",
        "is_active",
        "failed_login_count",
        "locked_until",
        "created_at",
        "updated_at",
        "last_login_at",
    }
    columns = {c.name for c in UserModel.__table__.columns}
    assert required <= columns


def test_refresh_token_model_has_required_columns() -> None:
    required = {
        "token_id",
        "user_id",
        "token_hash",
        "family_id",
        "issued_at",
        "expires_at",
        "revoked_at",
        "replaced_by",
    }
    columns = {c.name for c in RefreshTokenModel.__table__.columns}
    assert required <= columns


def test_user_model_tablename() -> None:
    assert UserModel.__tablename__ == "users"


def test_refresh_token_model_tablename() -> None:
    assert RefreshTokenModel.__tablename__ == "refresh_tokens"
