"""Unit tests for audit domain entities and enums."""

from __future__ import annotations

from datetime import UTC, datetime

from weighttogo.audit.domain.entities import AuditEvent, AuditEventType, ResourceType


def test_audit_event_type_has_all_auth_values() -> None:
    # ARRANGE / ACT — just verify the enum members exist at import time
    assert str(AuditEventType.AUTH_REGISTER) == "auth.register"
    assert str(AuditEventType.AUTH_LOGIN_SUCCEEDED) == "auth.login_succeeded"
    assert str(AuditEventType.AUTH_LOGIN_FAILED) == "auth.login_failed"
    assert str(AuditEventType.AUTH_LOGOUT) == "auth.logout"
    assert str(AuditEventType.AUTH_TOKEN_REFRESHED) == "auth.token_refreshed"
    assert str(AuditEventType.AUTH_TOKEN_REUSE_DETECTED) == "auth.token_reuse_detected"
    assert str(AuditEventType.AUTH_ACCOUNT_LOCKED) == "auth.account_locked"


def test_audit_event_type_has_all_mutation_values() -> None:
    assert str(AuditEventType.WEIGHT_ENTRY_CREATED) == "weight_entry.created"
    assert str(AuditEventType.WEIGHT_ENTRY_UPDATED) == "weight_entry.updated"
    assert str(AuditEventType.WEIGHT_ENTRY_DELETED) == "weight_entry.deleted"
    assert str(AuditEventType.GOAL_CREATED) == "goal.created"
    assert str(AuditEventType.GOAL_UPDATED) == "goal.updated"
    assert str(AuditEventType.GOAL_ABANDONED) == "goal.abandoned"
    assert str(AuditEventType.PREFERENCE_UPDATED) == "preference.updated"


def test_resource_type_values() -> None:
    assert str(ResourceType.WEIGHT_ENTRY) == "weight_entry"
    assert str(ResourceType.GOAL) == "goal"
    assert str(ResourceType.PREFERENCE) == "preference"


def test_audit_event_constructs_with_all_fields() -> None:
    # ARRANGE
    now = datetime.now(UTC)

    # ACT
    event = AuditEvent(
        audit_id=None,
        user_id=42,
        event_type=AuditEventType.WEIGHT_ENTRY_CREATED,
        resource_type=ResourceType.WEIGHT_ENTRY,
        resource_id=7,
        request_id="req-123",
        ip_address="192.168.1.1",
        metadata=None,
        created_at=now,
    )

    # ASSERT
    assert event.audit_id is None
    assert event.user_id == 42
    assert event.event_type == AuditEventType.WEIGHT_ENTRY_CREATED
    assert event.resource_type == ResourceType.WEIGHT_ENTRY
    assert event.resource_id == 7
    assert event.created_at == now


def test_audit_event_allows_null_user_for_unauthenticated_events() -> None:
    # ARRANGE
    now = datetime.now(UTC)

    # ACT
    event = AuditEvent(
        audit_id=None,
        user_id=None,
        event_type=AuditEventType.AUTH_LOGIN_FAILED,
        resource_type=None,
        resource_id=None,
        request_id=None,
        ip_address=None,
        metadata={"email": "***er@example.com"},
        created_at=now,
    )

    # ASSERT
    assert event.user_id is None
    assert event.metadata == {"email": "***er@example.com"}
