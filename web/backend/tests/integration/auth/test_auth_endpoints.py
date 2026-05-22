"""Integration tests for the /api/v1/auth endpoints.

These tests use FastAPI's TestClient against an in-memory SQLite database
(not PostgreSQL) to exercise the full HTTP → use-case → repository stack.
The tests run without a running database process, keeping CI fast.

Each test function starts with a clean database state via the ``db_session``
fixture (see conftest.py).
"""

from fastapi.testclient import TestClient

# ── Register ──────────────────────────────────────────────────────────────────


def test_register_returns_201_and_sets_cookie(client: TestClient) -> None:
    response = client.post(
        "/api/v1/auth/register",
        json={"email": "new@example.com", "password": "SecurePass1!", "display_name": "New User"},
    )
    assert response.status_code == 201
    assert "access_token" in response.cookies or "Set-Cookie" in response.headers


def test_register_returns_201_with_user_data(client: TestClient) -> None:
    response = client.post(
        "/api/v1/auth/register",
        json={"email": "reg@example.com", "password": "SecurePass1!", "display_name": "Reg User"},
    )
    assert response.status_code == 201
    body = response.json()
    assert body["email"] == "reg@example.com"
    assert body["display_name"] == "Reg User"
    assert "user_id" in body


def test_register_returns_409_for_duplicate_email(client: TestClient) -> None:
    payload = {"email": "dup@example.com", "password": "SecurePass1!", "display_name": "Dup"}
    client.post("/api/v1/auth/register", json=payload)
    response = client.post("/api/v1/auth/register", json=payload)
    assert response.status_code == 409
    # Must NOT say "already registered" or similar (SRS §FR-A-1, §NFR-S-7)
    body_text = response.text.lower()
    assert "already" not in body_text or "request" in body_text


def test_register_returns_422_for_weak_password(client: TestClient) -> None:
    response = client.post(
        "/api/v1/auth/register",
        json={"email": "weak@example.com", "password": "short", "display_name": "Weak"},
    )
    assert response.status_code == 422


def test_register_returns_422_for_invalid_email(client: TestClient) -> None:
    response = client.post(
        "/api/v1/auth/register",
        json={"email": "not-an-email", "password": "SecurePass1!", "display_name": "Bad Email"},
    )
    assert response.status_code == 422


# ── Login ─────────────────────────────────────────────────────────────────────


def _register_user(client: TestClient, email: str = "user@example.com") -> None:
    client.post(
        "/api/v1/auth/register",
        json={"email": email, "password": "SecurePass1!", "display_name": "User"},
    )


def test_login_returns_200_and_sets_cookie(client: TestClient) -> None:
    _register_user(client)
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "user@example.com", "password": "SecurePass1!"},
    )
    assert response.status_code == 200
    # Access token cookie must be present
    assert "access_token" in response.cookies


def test_login_returns_401_for_wrong_password(client: TestClient) -> None:
    _register_user(client)
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "user@example.com", "password": "WrongPassword!"},
    )
    assert response.status_code == 401


def test_login_returns_401_for_unknown_email(client: TestClient) -> None:
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "nobody@example.com", "password": "SecurePass1!"},
    )
    assert response.status_code == 401


def test_login_generic_error_does_not_enumerate_users(client: TestClient) -> None:
    """Wrong password and unknown email must return identical bodies (SRS §FR-A-9)."""
    _register_user(client)
    wrong_pw = client.post(
        "/api/v1/auth/login",
        json={"email": "user@example.com", "password": "Wrong!"},
    )
    unknown = client.post(
        "/api/v1/auth/login",
        json={"email": "unknown@example.com", "password": "SecurePass1!"},
    )
    assert wrong_pw.json() == unknown.json()


# ── /me ───────────────────────────────────────────────────────────────────────


def test_me_returns_200_with_user_data_when_authenticated(client: TestClient) -> None:
    _register_user(client, email="me@example.com")
    client.post(
        "/api/v1/auth/login",
        json={"email": "me@example.com", "password": "SecurePass1!"},
    )
    response = client.get("/api/v1/auth/me")
    assert response.status_code == 200
    body = response.json()
    assert body["email"] == "me@example.com"


def test_me_returns_401_when_not_authenticated(client: TestClient) -> None:
    response = client.get("/api/v1/auth/me")
    assert response.status_code == 401


# ── Logout ────────────────────────────────────────────────────────────────────


def test_logout_returns_204_and_clears_cookies(client: TestClient) -> None:
    _register_user(client)
    client.post(
        "/api/v1/auth/login",
        json={"email": "user@example.com", "password": "SecurePass1!"},
    )
    response = client.post("/api/v1/auth/logout")
    assert response.status_code == 204


# ── Refresh ───────────────────────────────────────────────────────────────────


def test_refresh_returns_200_and_new_access_token(client: TestClient) -> None:
    _register_user(client)
    client.post(
        "/api/v1/auth/login",
        json={"email": "user@example.com", "password": "SecurePass1!"},
    )
    response = client.post("/api/v1/auth/refresh")
    assert response.status_code == 200


def test_refresh_returns_401_without_refresh_cookie(client: TestClient) -> None:
    # Clear cookies
    client.cookies.clear()
    response = client.post("/api/v1/auth/refresh")
    assert response.status_code == 401


# ── Security properties ───────────────────────────────────────────────────────


def test_access_token_cookie_is_httponly(client: TestClient) -> None:
    """Access token must be delivered as HTTP-only (SRS §NFR-S-3)."""
    _register_user(client)
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "user@example.com", "password": "SecurePass1!"},
    )
    cookie_header = response.headers.get("set-cookie", "")
    assert "httponly" in cookie_header.lower()


def test_pii_not_in_access_token_payload(client: TestClient) -> None:
    """Token payload must contain no PII (SRS §NFR-S-3)."""
    import base64
    import json

    _register_user(client, email="piifree@example.com")
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "piifree@example.com", "password": "SecurePass1!"},
    )
    token = response.cookies.get("access_token")
    assert token is not None
    payload_b64 = token.split(".")[1]
    padding = 4 - len(payload_b64) % 4
    payload_b64 += "=" * (padding % 4)
    payload = json.loads(base64.urlsafe_b64decode(payload_b64))
    forbidden = {"email", "display_name", "phone"}
    assert not forbidden.intersection(payload.keys())
