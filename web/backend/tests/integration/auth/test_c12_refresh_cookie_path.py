"""C12 regression: refresh_token cookie must be scoped to /api/v1/auth."""

from fastapi.testclient import TestClient


def test_login_sets_refresh_cookie_with_auth_path(client: TestClient) -> None:
    """After login the refresh_token cookie must carry Path=/api/v1/auth."""
    client.post(
        "/api/v1/auth/register",
        json={"email": "pathtest@example.com", "password": "SecurePass1!", "display_name": "Path"},
    )
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "pathtest@example.com", "password": "SecurePass1!"},
    )
    assert response.status_code == 200

    cookie_headers = response.headers.get_list("set-cookie")
    refresh_headers = [h for h in cookie_headers if "refresh_token" in h]
    assert refresh_headers, "expected refresh_token Set-Cookie header"
    combined = " ".join(h.lower() for h in refresh_headers)
    assert "path=/api/v1/auth" in combined, (
        f"refresh cookie missing Path=/api/v1/auth, got: {refresh_headers}"
    )
