"""C4 regression: bcrypt verify must run before lockout check."""

from datetime import UTC, datetime, timedelta
from unittest.mock import MagicMock

import pytest

from weighttogo.auth.application.authenticate_user import AuthenticateUser, AuthenticateUserCommand
from weighttogo.auth.domain.entities import User
from weighttogo.auth.domain.exceptions import AccountLockedError


def _locked_user() -> User:
    return User(
        user_id=1,
        email="locked@example.com",
        hashed_password="$2b$12$fakehash",
        display_name="Locked",
        is_active=True,
        failed_login_count=5,
        locked_until=datetime.now(UTC) + timedelta(minutes=10),
    )


def test_verify_called_before_account_locked_error() -> None:
    """password_adapter.verify must be called even for locked accounts (timing guard)."""
    user = _locked_user()
    repo = MagicMock()
    repo.get_by_email.return_value = user
    repo.save.side_effect = lambda u: u

    pw = MagicMock()
    pw.verify.return_value = True  # correct password — irrelevant; lockout wins

    use_case = AuthenticateUser(user_repo=repo, password_adapter=pw, max_attempts=5)

    with pytest.raises(AccountLockedError):
        use_case.execute(AuthenticateUserCommand(email="locked@example.com", password="any"))

    pw.verify.assert_called_once()
