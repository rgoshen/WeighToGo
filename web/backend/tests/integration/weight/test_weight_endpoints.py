"""Integration tests for /api/v1/weight-entries endpoints (subtask 17)."""

from __future__ import annotations

from datetime import date, timedelta
from typing import Any

from fastapi.testclient import TestClient

# ── Helpers ───────────────────────────────────────────────────────────────────


def _register_and_login(client: TestClient) -> TestClient:
    """Register a user and return a client with session cookies set."""
    client.post(
        "/api/v1/auth/register",
        json={
            "email": "weight@example.com",
            "password": "ValidPass123!",
            "display_name": "Weight Tester",
        },
    )
    return client


def _valid_payload(**overrides: Any) -> dict[str, Any]:
    base: dict[str, Any] = {
        "weight_value": 175.5,
        "weight_unit": "lbs",
        "observation_date": date.today().isoformat(),
        "notes": None,
    }
    base.update(overrides)
    return base


# ── Auth guard ────────────────────────────────────────────────────────────────


def test_list_entries_requires_auth(client: TestClient) -> None:
    resp = client.get("/api/v1/weight-entries")
    assert resp.status_code == 401


def test_create_entry_requires_auth(client: TestClient) -> None:
    resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    assert resp.status_code == 401


def test_get_entry_requires_auth(client: TestClient) -> None:
    resp = client.get("/api/v1/weight-entries/1")
    assert resp.status_code == 401


def test_update_entry_requires_auth(client: TestClient) -> None:
    resp = client.put("/api/v1/weight-entries/1", json=_valid_payload())
    assert resp.status_code == 401


def test_delete_entry_requires_auth(client: TestClient) -> None:
    resp = client.delete("/api/v1/weight-entries/1")
    assert resp.status_code == 401


# ── Create ────────────────────────────────────────────────────────────────────


def test_create_entry_returns_201_with_body(client: TestClient) -> None:
    _register_and_login(client)
    resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    assert resp.status_code == 201
    data = resp.json()
    assert data["weight_unit"] == "lbs"
    assert "entry_id" in data


def test_create_entry_duplicate_date_returns_409(client: TestClient) -> None:
    _register_and_login(client)
    client.post("/api/v1/weight-entries", json=_valid_payload())
    resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    assert resp.status_code == 409


def test_create_entry_future_date_returns_422(client: TestClient) -> None:
    _register_and_login(client)
    future = (date.today() + timedelta(days=1)).isoformat()
    resp = client.post("/api/v1/weight-entries", json=_valid_payload(observation_date=future))
    assert resp.status_code == 422


# ── List ──────────────────────────────────────────────────────────────────────


def test_list_entries_returns_paginated_envelope(client: TestClient) -> None:
    _register_and_login(client)
    client.post("/api/v1/weight-entries", json=_valid_payload())
    resp = client.get("/api/v1/weight-entries")
    assert resp.status_code == 200
    data = resp.json()
    assert "items" in data
    assert "next_cursor" in data
    assert len(data["items"]) == 1


def test_list_entries_rejects_zero_limit(client: TestClient) -> None:
    """limit=0 must be rejected as a validation error, not 500 (PR #30 review)."""
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries?limit=0")
    assert resp.status_code == 422


def test_list_entries_rejects_negative_limit(client: TestClient) -> None:
    """limit=-1 must be rejected as a validation error, not 500 (PR #30 review)."""
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries?limit=-1")
    assert resp.status_code == 422


def test_list_entries_rejects_limit_above_max(client: TestClient) -> None:
    """limit greater than _MAX_PAGE_SIZE must 422 instead of silently clamping
    (PR #30 review: reviewer recommended Query(ge=1, le=_MAX_PAGE_SIZE))."""
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries?limit=101")
    assert resp.status_code == 422


def test_list_entries_accepts_limit_one(client: TestClient) -> None:
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries?limit=1")
    assert resp.status_code == 200


def test_list_entries_accepts_limit_max(client: TestClient) -> None:
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries?limit=100")
    assert resp.status_code == 200


def test_list_entries_paginates_without_skipping_boundary_rows(client: TestClient) -> None:
    """The compound-cursor pagination must include the page-boundary row on the
    next page. The original entry_id-only cursor used the peek row as the
    boundary and the strict `<` filter then excluded it (PR #30 review, ADR-0015).
    """
    _register_and_login(client)
    today = date.today()
    # Three entries on three distinct, descending dates so the natural sort
    # order is (today, yesterday, two_days_ago).
    for i in range(3):
        client.post(
            "/api/v1/weight-entries",
            json=_valid_payload(observation_date=(today - timedelta(days=i)).isoformat()),
        )

    page1 = client.get("/api/v1/weight-entries?limit=2").json()
    assert len(page1["items"]) == 2
    cursor = page1["next_cursor"]
    assert isinstance(cursor, str)

    page2 = client.get(f"/api/v1/weight-entries?limit=2&cursor={cursor}").json()
    page1_ids = {item["entry_id"] for item in page1["items"]}
    page2_ids = {item["entry_id"] for item in page2["items"]}

    # Every entry appears exactly once across the two pages.
    assert page1_ids.isdisjoint(page2_ids), (
        f"boundary row appears on both pages: {page1_ids & page2_ids}"
    )
    assert len(page1_ids | page2_ids) == 3, "an entry was skipped between pages"


def test_list_entries_paginates_across_backfilled_older_dates(client: TestClient) -> None:
    """An entry_id-only cursor breaks when newer entries have older observation
    dates: page 1 may return entry id=1 (today) and set next_cursor=2; page 2
    filter ``entry_id < 2`` then returns entry id=1 *again* and omits id=2
    entirely. The compound cursor (date+id) prevents this (ADR-0015).
    """
    _register_and_login(client)
    today = date.today()
    # Insert today first (entry_id=1), then backfill an older date
    # (entry_id=2). entry_id ordering and date ordering now disagree.
    client.post("/api/v1/weight-entries", json=_valid_payload(observation_date=today.isoformat()))
    client.post(
        "/api/v1/weight-entries",
        json=_valid_payload(observation_date=(today - timedelta(days=30)).isoformat()),
    )

    page1 = client.get("/api/v1/weight-entries?limit=1").json()
    assert len(page1["items"]) == 1
    assert page1["items"][0]["entry_id"] == 1, "today's entry sorts first"
    cursor = page1["next_cursor"]
    assert cursor is not None

    page2 = client.get(f"/api/v1/weight-entries?limit=1&cursor={cursor}").json()
    assert len(page2["items"]) == 1
    assert page2["items"][0]["entry_id"] == 2, (
        "backfilled older-date entry must appear on page 2, not be skipped"
    )


def test_list_entries_returns_string_cursor_not_integer(client: TestClient) -> None:
    """ADR-0015: cursor is an opaque base64 string on the wire."""
    _register_and_login(client)
    today = date.today()
    for i in range(2):
        client.post(
            "/api/v1/weight-entries",
            json=_valid_payload(observation_date=(today - timedelta(days=i)).isoformat()),
        )
    page = client.get("/api/v1/weight-entries?limit=1").json()
    assert isinstance(page["next_cursor"], str)


def test_list_entries_rejects_malformed_cursor(client: TestClient) -> None:
    """Malformed cursors must 422, not 500 (ADR-0015 router edge contract)."""
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries?cursor=!!!not-base64!!!")
    assert resp.status_code == 422


# ── Get single ────────────────────────────────────────────────────────────────


def test_get_entry_returns_200(client: TestClient) -> None:
    _register_and_login(client)
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    resp = client.get(f"/api/v1/weight-entries/{entry_id}")
    assert resp.status_code == 200
    assert resp.json()["entry_id"] == entry_id


def test_get_nonexistent_entry_returns_404(client: TestClient) -> None:
    _register_and_login(client)
    resp = client.get("/api/v1/weight-entries/99999")
    assert resp.status_code == 404


def test_get_soft_deleted_entry_returns_404(client: TestClient) -> None:
    """Port contract: get_by_id returns an active entry. A soft-deleted entry
    must surface as 404 from GET /weight-entries/{id} (PR #30 review)."""
    _register_and_login(client)
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    client.delete(f"/api/v1/weight-entries/{entry_id}")
    resp = client.get(f"/api/v1/weight-entries/{entry_id}")
    assert resp.status_code == 404


# ── Update ────────────────────────────────────────────────────────────────────


def test_update_entry_returns_200_with_new_value(client: TestClient) -> None:
    _register_and_login(client)
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    resp = client.put(
        f"/api/v1/weight-entries/{entry_id}",
        json=_valid_payload(weight_value=180.0),
    )
    assert resp.status_code == 200
    assert resp.json()["weight_value"] == 180.0


def test_update_soft_deleted_entry_returns_404(client: TestClient) -> None:
    """Port contract: get_by_id returns an active entry. A PUT against a
    soft-deleted entry must surface as 404, not mutate the deleted row
    (PR #30 review)."""
    _register_and_login(client)
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    client.delete(f"/api/v1/weight-entries/{entry_id}")
    resp = client.put(
        f"/api/v1/weight-entries/{entry_id}",
        json=_valid_payload(weight_value=180.0),
    )
    assert resp.status_code == 404


def test_update_entry_conflict_date_returns_409(client: TestClient) -> None:
    _register_and_login(client)
    today = date.today()
    yesterday = (today - timedelta(days=1)).isoformat()
    client.post("/api/v1/weight-entries", json=_valid_payload(observation_date=yesterday))
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    resp = client.put(
        f"/api/v1/weight-entries/{entry_id}",
        json=_valid_payload(observation_date=yesterday),
    )
    assert resp.status_code == 409


# ── Delete ────────────────────────────────────────────────────────────────────


def test_delete_entry_returns_204(client: TestClient) -> None:
    _register_and_login(client)
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    resp = client.delete(f"/api/v1/weight-entries/{entry_id}")
    assert resp.status_code == 204


def test_delete_entry_is_idempotent(client: TestClient) -> None:
    _register_and_login(client)
    create_resp = client.post("/api/v1/weight-entries", json=_valid_payload())
    entry_id = create_resp.json()["entry_id"]
    client.delete(f"/api/v1/weight-entries/{entry_id}")
    resp = client.delete(f"/api/v1/weight-entries/{entry_id}")
    assert resp.status_code == 204


def test_delete_nonexistent_entry_returns_404(client: TestClient) -> None:
    _register_and_login(client)
    resp = client.delete("/api/v1/weight-entries/99999")
    assert resp.status_code == 404
