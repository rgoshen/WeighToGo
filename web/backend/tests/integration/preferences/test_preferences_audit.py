"""Integration tests: preferences router wires audit events correctly."""

from __future__ import annotations

from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from weighttogo.audit.infrastructure.models import AuditLogModel


def _setup(client: TestClient) -> int:
    r = client.post(
        "/api/v1/auth/register",
        json={"email": "prefs.audit@example.com", "password": "SecurePass1!", "display_name": "PA"},
    )
    return int(r.json()["user_id"])


def test_set_preference_writes_preference_updated_event(
    client: TestClient, db_session: Session
) -> None:
    # ARRANGE
    user_id = _setup(client)

    # ACT
    r = client.put(
        "/api/v1/preferences/weight_unit",
        json={"value": "lbs"},
    )
    assert r.status_code == 200

    # ASSERT
    rows = (
        db_session.query(AuditLogModel)
        .filter(AuditLogModel.event_type == "preference.updated")
        .all()
    )
    assert len(rows) == 1
    assert rows[0].user_id == user_id
    assert rows[0].resource_type == "preference"
