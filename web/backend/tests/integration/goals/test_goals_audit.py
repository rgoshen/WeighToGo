"""Integration tests: goals router wires audit events correctly."""

from __future__ import annotations

from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from weighttogo.audit.infrastructure.models import AuditLogModel


def _setup(client: TestClient) -> int:
    r = client.post(
        "/api/v1/auth/register",
        json={"email": "goals.audit@example.com", "password": "SecurePass1!", "display_name": "GA"},
    )
    return int(r.json()["user_id"])


def _goal_rows(db_session: Session) -> list[AuditLogModel]:
    return db_session.query(AuditLogModel).filter(AuditLogModel.event_type.like("goal.%")).all()


def test_create_goal_writes_goal_created_event(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    user_id = _setup(client)

    # ACT
    r = client.post(
        "/api/v1/goals",
        json={
            "target_value": 75.0,
            "target_unit": "kg",
            "start_value": 85.0,
            "goal_type": "lose",
            "target_date": None,
        },
    )
    assert r.status_code == 201

    # ASSERT
    rows = _goal_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "goal.created"
    assert rows[0].user_id == user_id
    assert rows[0].resource_type == "goal"
    assert rows[0].resource_id == r.json()["goal_id"]


def test_update_goal_writes_goal_updated_event(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    _setup(client)
    r = client.post(
        "/api/v1/goals",
        json={
            "target_value": 75.0,
            "target_unit": "kg",
            "start_value": 85.0,
            "goal_type": "lose",
            "target_date": None,
        },
    )
    goal_id = int(r.json()["goal_id"])
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT — update endpoint is PUT (confirmed from router.py)
    r2 = client.put(
        f"/api/v1/goals/{goal_id}",
        json={"target_value": 74.0},
    )
    assert r2.status_code == 200

    # ASSERT
    rows = _goal_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "goal.updated"
    assert rows[0].resource_id == goal_id


def test_abandon_goal_writes_goal_abandoned_event(client: TestClient, db_session: Session) -> None:
    # ARRANGE
    _setup(client)
    r = client.post(
        "/api/v1/goals",
        json={
            "target_value": 75.0,
            "target_unit": "kg",
            "start_value": 85.0,
            "goal_type": "lose",
            "target_date": None,
        },
    )
    goal_id = int(r.json()["goal_id"])
    db_session.query(AuditLogModel).delete()
    db_session.flush()

    # ACT
    r2 = client.delete(f"/api/v1/goals/{goal_id}")
    assert r2.status_code == 204

    # ASSERT
    rows = _goal_rows(db_session)
    assert len(rows) == 1
    assert rows[0].event_type == "goal.abandoned"
    assert rows[0].resource_id == goal_id
