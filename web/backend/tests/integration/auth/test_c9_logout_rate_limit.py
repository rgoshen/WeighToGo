"""C9 regression: /logout must be rate-limited to prevent session-DoS."""

from fastapi.testclient import TestClient

from weighttogo.auth.interface.router import limiter


def _login(client: TestClient) -> None:
    client.post(
        "/api/v1/auth/register",
        json={"email": "rl@example.com", "password": "SecurePass1!", "display_name": "RL User"},
    )
    client.post(
        "/api/v1/auth/login",
        json={"email": "rl@example.com", "password": "SecurePass1!"},
    )


def test_logout_returns_429_after_limit(client: TestClient) -> None:
    """After 10 logout calls within a minute the 11th must return 429."""
    _login(client)

    # Enable the limiter only for this test
    limiter.enabled = True
    try:
        statuses = []
        for _ in range(11):
            r = client.post("/api/v1/auth/logout")
            statuses.append(r.status_code)
    finally:
        limiter.enabled = False

    assert 429 in statuses, f"expected 429 in: {statuses}"
