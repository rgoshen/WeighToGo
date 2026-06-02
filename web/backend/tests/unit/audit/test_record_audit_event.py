"""Unit tests for the RecordAuditEvent use case."""

from __future__ import annotations

from datetime import UTC, datetime

from weighttogo.audit.application.record_audit_event import (
    RecordAuditEvent,
    RecordAuditEventCommand,
)
from weighttogo.audit.domain.entities import AuditEvent, AuditEventType, ResourceType


class FakeAuditRepository:
    """In-memory audit repository for unit testing."""

    def __init__(self) -> None:
        self.recorded: list[AuditEvent] = []

    def add(self, event: AuditEvent) -> None:
        self.recorded.append(event)


def test_record_audit_event_persists_one_row() -> None:
    # ARRANGE
    repo = FakeAuditRepository()
    uc = RecordAuditEvent(audit_repo=repo)
    cmd = RecordAuditEventCommand(
        event_type=AuditEventType.WEIGHT_ENTRY_CREATED,
        user_id=10,
        resource_type=ResourceType.WEIGHT_ENTRY,
        resource_id=5,
        request_id="req-abc",
        ip_address="127.0.0.1",
        metadata=None,
    )

    # ACT
    uc.execute(cmd)

    # ASSERT
    assert len(repo.recorded) == 1
    saved = repo.recorded[0]
    assert saved.audit_id is None
    assert saved.user_id == 10
    assert saved.event_type == AuditEventType.WEIGHT_ENTRY_CREATED
    assert saved.resource_type == ResourceType.WEIGHT_ENTRY
    assert saved.resource_id == 5
    assert saved.request_id == "req-abc"
    assert saved.ip_address == "127.0.0.1"
    assert saved.metadata is None


def test_record_audit_event_stores_created_at_in_utc() -> None:
    # ARRANGE
    repo = FakeAuditRepository()
    uc = RecordAuditEvent(audit_repo=repo)
    before = datetime.now(UTC)
    cmd = RecordAuditEventCommand(event_type=AuditEventType.AUTH_LOGIN_SUCCEEDED, user_id=1)

    # ACT
    uc.execute(cmd)
    after = datetime.now(UTC)

    # ASSERT
    saved = repo.recorded[0]
    assert before <= saved.created_at <= after
    assert saved.created_at.tzinfo is not None


def test_record_audit_event_with_null_user_for_failed_login() -> None:
    # ARRANGE
    repo = FakeAuditRepository()
    uc = RecordAuditEvent(audit_repo=repo)
    cmd = RecordAuditEventCommand(
        event_type=AuditEventType.AUTH_LOGIN_FAILED,
        user_id=None,
        metadata={"email": "***er@example.com"},
    )

    # ACT
    uc.execute(cmd)

    # ASSERT
    saved = repo.recorded[0]
    assert saved.user_id is None
    assert saved.metadata == {"email": "***er@example.com"}
    assert saved.resource_type is None
    assert saved.resource_id is None


def test_record_audit_event_defaults_optional_fields_to_none() -> None:
    # ARRANGE
    repo = FakeAuditRepository()
    uc = RecordAuditEvent(audit_repo=repo)
    cmd = RecordAuditEventCommand(event_type=AuditEventType.AUTH_LOGOUT, user_id=7)

    # ACT
    uc.execute(cmd)

    # ASSERT
    saved = repo.recorded[0]
    assert saved.resource_type is None
    assert saved.resource_id is None
    assert saved.request_id is None
    assert saved.ip_address is None
    assert saved.metadata is None
