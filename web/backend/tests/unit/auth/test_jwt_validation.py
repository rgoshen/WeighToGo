"""C13 regression: verify_access_token must check typ, aud, and iss claims."""

from datetime import timedelta

import pytest
from jose import jwt as jose_jwt

from weighttogo.auth.infrastructure.jwt_adapter import InvalidTokenError, JwtAdapter

_SECRET = "test-secret-key-that-is-at-least-32-bytes-long!"
_ALG = "HS256"


def _mint(extra: dict[str, object], secret: str = _SECRET) -> str:
    """Mint a raw JWT with custom claims, bypassing JwtAdapter."""
    from datetime import UTC, datetime

    payload = {
        "sub": "1",
        "iat": datetime.now(UTC),
        "exp": datetime.now(UTC) + timedelta(minutes=15),
        "jti": "test-jti",
    }
    payload.update(extra)
    return jose_jwt.encode(payload, secret, algorithm=_ALG)


def test_verify_rejects_wrong_typ() -> None:
    """A token with typ=refresh must be rejected by verify_access_token."""
    token = _mint({"typ": "refresh", "iss": "weighttogo-api", "aud": "weighttogo-clients"})
    adapter = JwtAdapter(secret_key=_SECRET)
    with pytest.raises(InvalidTokenError):
        adapter.verify_access_token(token)


def test_verify_rejects_missing_iss() -> None:
    """A token without iss must be rejected."""
    token = _mint({"typ": "access", "aud": "weighttogo-clients"})
    adapter = JwtAdapter(secret_key=_SECRET)
    with pytest.raises(InvalidTokenError):
        adapter.verify_access_token(token)


def test_verify_rejects_missing_aud() -> None:
    """A token without aud must be rejected."""
    token = _mint({"typ": "access", "iss": "weighttogo-api"})
    adapter = JwtAdapter(secret_key=_SECRET)
    with pytest.raises(InvalidTokenError):
        adapter.verify_access_token(token)


def test_verify_accepts_valid_token() -> None:
    """A properly-minted token must still verify correctly."""
    adapter = JwtAdapter(secret_key=_SECRET)
    token = adapter.issue_access_token(user_id=7, ttl=timedelta(minutes=15))
    assert adapter.verify_access_token(token) == 7
