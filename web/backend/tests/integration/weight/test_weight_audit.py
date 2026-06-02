"""Integration tests: weight_tracking router wires audit events correctly."""

from __future__ import annotations

from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from weighttogo.audit.infrastructure.models import AuditLogModel


def _register_and_login(client: TestClient) -> int:
    r = client.post(
        "/api/v1/auth/register",
        json={
            "email": "weight.audit@example.com",
            "password": "SecurePass1!",
            "display_name": "Weight Audit",
        },
    )
    return int(r.json()["user_id"])


def _weight_rows(db_session: Session) -> list[AuditLogModel]:
    return (
        db_session.query(AuditLogModel)
        .filter(AuditLogModel.event_type.like("weight_entry.%"))
        .all()
    )


def test_create_weight_entry_writes_audit_row(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    user_id = _register_and_login(client)

    # ACT
    r = client.post(
        "/api/v1/weight-entries",
        json={
            "weight_value": 80.5,
            "weight_unit": "kg",
            "observation_date": "2026-01-15",
            "notes": None,
        },
    )
    assert r.status_code == 201

    # ASSERT
    rows = _weight_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "weight_entry.created"
    assert rows[0].user_id == user_id
    assert rows[0].resource_type == "weight_entry"
    assert rows[0].resource_id == r.json()["entry_id"]


def test_update_weight_entry_writes_audit_row(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    _register_and_login(client)
    r = client.post(
        "/api/v1/weight-entries",
        json={
            "weight_value": 80.5,
            "weight_unit": "kg",
            "observation_date": "2026-01-15",
            "notes": None,
        },
    )
    entry_id = r.json()["entry_id"]
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    r2 = client.put(
        f"/api/v1/weight-entries/{entry_id}",
        json={
            "weight_value": 79.0,
            "weight_unit": "kg",
            "observation_date": "2026-01-15",
            "notes": None,
        },
    )
    assert r2.status_code == 200

    # ASSERT
    rows = _weight_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "weight_entry.updated"
    assert rows[0].resource_id == entry_id


def test_delete_weight_entry_writes_audit_row(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    _register_and_login(client)
    r = client.post(
        "/api/v1/weight-entries",
        json={
            "weight_value": 80.5,
            "weight_unit": "kg",
            "observation_date": "2026-01-15",
            "notes": None,
        },
    )
    entry_id = r.json()["entry_id"]
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    r2 = client.delete(f"/api/v1/weight-entries/{entry_id}")
    assert r2.status_code == 204

    # ASSERT
    rows = _weight_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "weight_entry.deleted"
    assert rows[0].resource_id == entry_id
