"""C6 regression: RevokeSession must use IJwtAdapter.hash_token and revoke the full family."""

import uuid
from datetime import UTC, datetime, timedelta
from unittest.mock import MagicMock

from weighttogo.auth.application.revoke_session import RevokeSession, RevokeSessionCommand
from weighttogo.auth.domain.entities import RefreshToken


def _make_token(family_id: uuid.UUID | None = None) -> RefreshToken:
    return RefreshToken(
        token_id=1,
        user_id=42,
        token_hash="tagged_hash",
        family_id=family_id or uuid.uuid4(),
        expires_at=datetime.now(UTC) + timedelta(days=7),
    )


def _make_jwt_adapter(hashed: str = "tagged_hash") -> MagicMock:
    adapter = MagicMock()
    adapter.hash_token.return_value = hashed
    return adapter


def _make_token_repo(token: RefreshToken | None) -> MagicMock:
    repo = MagicMock()
    repo.get_by_hash.return_value = token
    return repo


def test_revoke_session_uses_injected_jwt_adapter_for_hashing() -> None:
    """hash_token must be called on the injected IJwtAdapter, not via hashlib directly."""
    token = _make_token()
    jwt = _make_jwt_adapter("tagged_hash")
    repo = _make_token_repo(token)

    use_case = RevokeSession(token_repo=repo, jwt_adapter=jwt)
    use_case.execute(RevokeSessionCommand(raw_refresh_token="raw"))

    jwt.hash_token.assert_called_once_with("raw")


def test_revoke_session_calls_revoke_family_not_single_save() -> None:
    """On logout, revoke_family must be called; individual token save must NOT be called."""
    fid = uuid.uuid4()
    token = _make_token(family_id=fid)
    jwt = _make_jwt_adapter()
    repo = _make_token_repo(token)

    use_case = RevokeSession(token_repo=repo, jwt_adapter=jwt)
    use_case.execute(RevokeSessionCommand(raw_refresh_token="raw"))

    repo.revoke_family.assert_called_once_with(fid)
    repo.save.assert_not_called()


def test_revoke_session_noop_when_token_not_found() -> None:
    """Missing token must still be silently ignored (idempotent)."""
    jwt = _make_jwt_adapter()
    repo = _make_token_repo(None)

    use_case = RevokeSession(token_repo=repo, jwt_adapter=jwt)
    use_case.execute(RevokeSessionCommand(raw_refresh_token="raw"))

    repo.revoke_family.assert_not_called()
    repo.save.assert_not_called()
