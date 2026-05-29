"""Integration tests: achievement detection on weight-entry create (FR-Ach-1/2, FR-G-4)."""

from __future__ import annotations

from typing import Any, cast

from fastapi.testclient import TestClient


def _register_and_login(client: TestClient, email: str = "detect@example.com") -> None:
    client.post(
        "/api/v1/auth/register",
        json={"email": email, "password": "ValidPass123!", "display_name": "Detect User"},
    )


def _create_goal(
    client: TestClient,
    start: float = 200.0,
    target: float = 150.0,
    goal_type: str = "lose",
) -> None:
    client.post(
        "/api/v1/goals",
        json={
            "goal_type": goal_type,
            "target_value": target,
            "target_unit": "lbs",
            "start_value": start,
            "target_date": None,
        },
    )


def _post_entry(client: TestClient, weight: float, obs_date: str = "2026-01-10") -> dict[str, Any]:
    resp = client.post(
        "/api/v1/weight-entries",
        json={"weight_value": weight, "weight_unit": "lbs", "observation_date": obs_date},
    )
    assert resp.status_code == 201, resp.json()
    return cast(dict[str, Any], resp.json())


# ── Response shape ────────────────────────────────────────────────────────────


def test_weight_entry_response_includes_newly_earned_achievements_field(
    client: TestClient,
) -> None:
    _register_and_login(client)
    data = _post_entry(client, 198.0)
    assert "newly_earned_achievements" in data


def test_weight_entry_without_active_goal_returns_empty_achievements(
    client: TestClient,
) -> None:
    _register_and_login(client, "nogoal@example.com")
    data = _post_entry(client, 198.0)
    assert data["newly_earned_achievements"] == []


# ── Milestone detection ───────────────────────────────────────────────────────


def test_weight_entry_crossing_5lb_threshold_returns_milestone(client: TestClient) -> None:
    _register_and_login(client, "milestone@example.com")
    _create_goal(client)
    # start=200, current=195 → delta=5, crosses 5 lb threshold
    data = _post_entry(client, 195.0)
    achievements = data["newly_earned_achievements"]
    assert len(achievements) == 1
    assert achievements[0]["achievement_type"] == "milestone"
    # Pydantic serialises Decimal as a string in JSON
    assert float(achievements[0]["threshold"]) == 5.0


def test_second_entry_at_same_weight_returns_no_new_achievements(client: TestClient) -> None:
    _register_and_login(client, "idempotent@example.com")
    _create_goal(client)
    _post_entry(client, 195.0, "2026-01-10")  # earns 5 lb milestone
    # Second entry at same weight on a different date — threshold already recorded
    data = _post_entry(client, 195.0, "2026-01-11")
    assert data["newly_earned_achievements"] == []


# ── Goal-reached detection (FR-G-4) ──────────────────────────────────────────


def test_weight_entry_reaching_target_returns_goal_reached(client: TestClient) -> None:
    _register_and_login(client, "goalreached@example.com")
    _create_goal(client, start=200.0, target=195.0)  # small gap for easy testing
    data = _post_entry(client, 195.0)
    types = [a["achievement_type"] for a in data["newly_earned_achievements"]]
    assert "goal_reached" in types
