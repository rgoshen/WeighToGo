"""Integration tests: auth router wires audit events correctly."""

from __future__ import annotations

from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from weighttogo.audit.infrastructure.models import AuditLogModel
from weighttogo.config import get_settings


def _audit_rows(db_session: Session) -> list[AuditLogModel]:
    return db_session.query(AuditLogModel).order_by(AuditLogModel.created_at).all()


def _assert_email_masked(row: AuditLogModel, raw_email: str = "test@example.com") -> None:
    """Assert the audit row stored a masked email, never the raw address.

    Encodes the NFR-Priv-1 front-masking contract: the local part is replaced by
    a ``***`` prefix (the precise algorithm lives in test_logging.py), so a
    regression that stored the raw address or leaked the leading local-part chars
    (e.g. ``tes***@example.com``) fails here. Shared by the failed-login and
    account-locked audit tests.
    """
    assert row.event_metadata is not None
    masked = row.event_metadata.get("email", "")
    _, domain = raw_email.split("@", 1)
    assert raw_email not in masked
    assert masked.startswith("***")
    assert masked.endswith(f"@{domain}")


def _register(
    client: TestClient, email: str = "test@example.com", password: str = "SecurePass1!"
) -> None:
    client.post(
        "/api/v1/auth/register",
        json={"email": email, "password": password, "display_name": "Test"},
    )


def test_register_writes_auth_register_event(client: TestClient, db_session: Session) -> None:
    # ACT
    _register(client)

    # ASSERT
    rows = _audit_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "auth.register"
    assert rows[0].user_id is not None
    assert rows[0].resource_type is None


def test_login_success_writes_login_succeeded_event(
    client: TestClient, db_session: Session
) -> None:
    # ARRANGE
    _register(client)
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    client.post(
        "/api/v1/auth/login",
        json={"email": "test@example.com", "password": "SecurePass1!"},
    )

    # ASSERT
    rows = _audit_rows(db_session)
    assert any(r.event_type == "auth.login_succeeded" for r in rows)
    succeeded = next(r for r in rows if r.event_type == "auth.login_succeeded")
    assert succeeded.user_id is not None


def test_login_failure_writes_login_failed_event_with_masked_email(
    client: TestClient, db_session: Session
) -> None:
    # ARRANGE
    _register(client)
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    client.post(
        "/api/v1/auth/login",
        json={"email": "test@example.com", "password": "wrongpassword"},
    )

    # ASSERT
    rows = _audit_rows(db_session)
    failed = next((r for r in rows if r.event_type == "auth.login_failed"), None)
    assert failed is not None
    assert failed.user_id is None
    _assert_email_masked(failed)


def test_account_locked_writes_account_locked_event_with_masked_email(
    client: TestClient, db_session: Session
) -> None:
    # ARRANGE
    _register(client)
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT — drive max_login_attempts failures (the last one sets the lockout),
    # then one further attempt is the first to trip AccountLockedError and audit
    # the lockout. Deriving the bound keeps the test tracking the configured
    # threshold rather than a hard-coded boundary.
    attempts = get_settings().max_login_attempts + 1
    for _ in range(attempts):
        client.post(
            "/api/v1/auth/login",
            json={"email": "test@example.com", "password": "wrongpassword"},
        )

    # ASSERT
    rows = _audit_rows(db_session)
    locked = next((r for r in rows if r.event_type == "auth.account_locked"), None)
    assert locked is not None
    assert locked.user_id is None
    _assert_email_masked(locked)


def test_logout_writes_auth_logout_event(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    _register(client)
    client.post(
        "/api/v1/auth/login", json={"email": "test@example.com", "password": "SecurePass1!"}
    )
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    client.post("/api/v1/auth/logout")

    # ASSERT
    rows = _audit_rows(db_session)
    assert any(r.event_type == "auth.logout" for r in rows)


def test_refresh_success_writes_token_refreshed_event(
    client: TestClient, db_session: Session
) -> None:
    # ARRANGE
    _register(client)
    client.post(
        "/api/v1/auth/login", json={"email": "test@example.com", "password": "SecurePass1!"}
    )
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    client.post("/api/v1/auth/refresh")

    # ASSERT
    rows = _audit_rows(db_session)
    assert any(r.event_type == "auth.token_refreshed" for r in rows)
