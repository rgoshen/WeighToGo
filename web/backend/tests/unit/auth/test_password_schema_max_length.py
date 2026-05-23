"""C14 regression: passwords longer than 72 bytes must be rejected at the schema layer."""

import pytest
from pydantic import ValidationError

from weighttogo.auth.interface.schemas import LoginRequest, RegisterRequest


def _pw(length: int) -> str:
    """Build a password of the given length that passes complexity rules."""
    # Pattern: Uppercase + digit + symbol + lowercase padding
    base = "Aa1!"
    return (base * ((length // len(base)) + 1))[:length]


def test_register_rejects_password_over_72_chars() -> None:
    with pytest.raises(ValidationError):
        RegisterRequest(email="a@b.com", password=_pw(73), display_name="Test User")


def test_login_rejects_password_over_72_chars() -> None:
    with pytest.raises(ValidationError):
        LoginRequest(email="a@b.com", password=_pw(73))


def test_register_accepts_password_at_72_chars() -> None:
    req = RegisterRequest(email="a@b.com", password=_pw(72), display_name="Test User")
    assert len(req.password) == 72


def test_login_accepts_password_at_72_chars() -> None:
    req = LoginRequest(email="a@b.com", password=_pw(72))
    assert len(req.password) == 72
