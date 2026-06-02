"""Integration tests for SqlAlchemyAuditRepository against the SQLite harness."""

from __future__ import annotations

from datetime import UTC, datetime

import pytest
from sqlalchemy.orm import Session

from weighttogo.audit.application.record_audit_event import (
    RecordAuditEvent,
    RecordAuditEventCommand,
)
from weighttogo.audit.domain.entities import AuditEventType, ResourceType
from weighttogo.audit.infrastructure.models import AuditLogModel
from weighttogo.audit.infrastructure.repositories import SqlAlchemyAuditRepository


def test_add_persists_audit_row_with_all_fields(db_session: Session) -> None:
    # ARRANGE
    repo = SqlAlchemyAuditRepository(db_session)
    uc = RecordAuditEvent(audit_repo=repo)
    cmd = RecordAuditEventCommand(
        event_type=AuditEventType.WEIGHT_ENTRY_CREATED,
        user_id=None,  # no FK needed in this test
        resource_type=ResourceType.WEIGHT_ENTRY,
        resource_id=99,
        request_id="req-xyz",
        ip_address="10.0.0.1",
        metadata={"note": "test"},
    )

    # ACT
    uc.execute(cmd)
    db_session.flush()

    # ASSERT
    row = db_session.query(AuditLogModel).one()
    assert row.event_type == "weight_entry.created"
    assert row.resource_type == "weight_entry"
    assert row.resource_id == 99
    assert row.request_id == "req-xyz"
    assert row.ip_address == "10.0.0.1"
    assert row.event_metadata == {"note": "test"}
    assert row.user_id is None
    assert row.audit_id is not None


def test_add_with_null_optional_fields(db_session: Session) -> None:
    # ARRANGE
    repo = SqlAlchemyAuditRepository(db_session)
    uc = RecordAuditEvent(audit_repo=repo)
    cmd = RecordAuditEventCommand(
        event_type=AuditEventType.AUTH_LOGIN_FAILED,
        user_id=None,
        metadata={"email": "***er@example.com"},
    )

    # ACT
    uc.execute(cmd)
    db_session.flush()

    # ASSERT
    row = db_session.query(AuditLogModel).one()
    assert row.event_type == "auth.login_failed"
    assert row.resource_type is None
    assert row.resource_id is None
    assert row.request_id is None
    assert row.ip_address is None
    assert row.event_metadata == {"email": "***er@example.com"}


def test_check_constraint_rejects_invalid_event_type(db_session: Session) -> None:
    # ARRANGE — attempt to bypass the enum and insert an invalid value directly
    from sqlalchemy.exc import IntegrityError

    row = AuditLogModel(
        user_id=None,
        event_type="bad.event",  # not in the CHECK taxonomy
        resource_type=None,
        resource_id=None,
        request_id=None,
        ip_address=None,
        event_metadata=None,
        created_at=datetime.now(UTC),
    )
    db_session.add(row)

    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_request_id_truncated_to_64_chars(db_session: Session) -> None:
    # ARRANGE
    repo = SqlAlchemyAuditRepository(db_session)
    uc = RecordAuditEvent(audit_repo=repo)
    long_id = "x" * 100  # 100 chars > VARCHAR(64)
    cmd = RecordAuditEventCommand(
        event_type=AuditEventType.AUTH_LOGOUT,
        user_id=None,
        request_id=long_id,
    )

    # ACT
    uc.execute(cmd)
    db_session.flush()

    # ASSERT
    row = db_session.query(AuditLogModel).one()
    assert row.request_id is not None
    assert len(row.request_id) == 64
    assert row.request_id == "x" * 64


def test_check_constraint_rejects_resource_id_without_resource_type(db_session: Session) -> None:
    # ARRANGE
    from sqlalchemy.exc import IntegrityError

    row = AuditLogModel(
        user_id=None,
        event_type="goal.created",
        resource_type=None,  # violates: resource_id set but resource_type is NULL
        resource_id=5,
        request_id=None,
        ip_address=None,
        event_metadata=None,
        created_at=datetime.now(UTC),
    )
    db_session.add(row)

    # ACT / ASSERT
    with pytest.raises(IntegrityError):
        db_session.flush()


def test_on_delete_set_null_when_user_deleted(db_session: Session) -> None:
    """Audit rows survive user deletion with user_id set to NULL (ADR-0024)."""
    from weighttogo.auth.infrastructure.models import UserModel

    # ARRANGE — create a user, then an audit row referencing them
    user_row = UserModel(
        email="audit-fk-test@example.com",
        password_hash="hashed",
        display_name="FK Tester",
        is_active=True,
    )
    db_session.add(user_row)
    db_session.flush()  # populate user_id

    audit_row = AuditLogModel(
        user_id=user_row.user_id,
        event_type="auth.login_succeeded",
        resource_type=None,
        resource_id=None,
        request_id=None,
        ip_address=None,
        event_metadata=None,
        created_at=datetime.now(UTC),
    )
    db_session.add(audit_row)
    db_session.flush()

    # ACT — delete the user
    db_session.delete(user_row)
    db_session.flush()

    # ASSERT — audit row still exists, user_id is now NULL
    db_session.expire_all()
    surviving_row = db_session.query(AuditLogModel).filter_by(audit_id=audit_row.audit_id).one()
    assert surviving_row.user_id is None
