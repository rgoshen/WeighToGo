"""Unit tests for audit domain entities and enums."""

from __future__ import annotations

from datetime import UTC, datetime

from weighttogo.audit.domain.entities import AuditEvent, AuditEventType, ResourceType


def test_audit_event_type_has_all_auth_values() -> None:
    # ARRANGE / ACT — just verify the enum members exist at import time
    assert AuditEventType.AUTH_REGISTER.value == "auth.register"
    assert AuditEventType.AUTH_LOGIN_SUCCEEDED.value == "auth.login_succeeded"
    assert AuditEventType.AUTH_LOGIN_FAILED.value == "auth.login_failed"
    assert AuditEventType.AUTH_LOGOUT.value == "auth.logout"
    assert AuditEventType.AUTH_TOKEN_REFRESHED.value == "auth.token_refreshed"
    assert AuditEventType.AUTH_TOKEN_REUSE_DETECTED.value == "auth.token_reuse_detected"
    assert AuditEventType.AUTH_ACCOUNT_LOCKED.value == "auth.account_locked"


def test_audit_event_type_has_all_mutation_values() -> None:
    assert AuditEventType.WEIGHT_ENTRY_CREATED.value == "weight_entry.created"
    assert AuditEventType.WEIGHT_ENTRY_UPDATED.value == "weight_entry.updated"
    assert AuditEventType.WEIGHT_ENTRY_DELETED.value == "weight_entry.deleted"
    assert AuditEventType.GOAL_CREATED.value == "goal.created"
    assert AuditEventType.GOAL_UPDATED.value == "goal.updated"
    assert AuditEventType.GOAL_ABANDONED.value == "goal.abandoned"
    assert AuditEventType.PREFERENCE_UPDATED.value == "preference.updated"


def test_resource_type_values() -> None:
    assert ResourceType.WEIGHT_ENTRY.value == "weight_entry"
    assert ResourceType.GOAL.value == "goal"
    assert ResourceType.PREFERENCE.value == "preference"


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
