"""C8 regression: mixed-case email registered then logged in must succeed."""

from fastapi.testclient import TestClient


def test_mixed_case_email_register_then_login(client: TestClient) -> None:
    """Registering with Foo@Bar.com and logging in with the same string must return 200."""
    email = "Foo@Bar.com"
    client.post(
        "/api/v1/auth/register",
        json={"email": email, "password": "SecurePass1!", "display_name": "Case User"},
    )
    response = client.post(
        "/api/v1/auth/login",
        json={"email": email, "password": "SecurePass1!"},
    )
    assert response.status_code == 200


def test_email_stored_lowercased(client: TestClient) -> None:
    """Registration response email must be lowercase regardless of input case."""
    response = client.post(
        "/api/v1/auth/register",
        json={"email": "Upper@Example.COM", "password": "SecurePass1!", "display_name": "Up User"},
    )
    assert response.status_code == 201
    assert response.json()["email"] == "upper@example.com"
