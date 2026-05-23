"""Shared pytest fixtures for the backend test suite.

Sets required environment variables at session start so that Settings can be
constructed during test collection without a real ``.env`` file.  Tests that
exercise the full HTTP stack (integration/) override the DB dependency in their
own conftest.
"""

import os

# Set required env vars before importing the app so that pydantic-settings
# can construct Settings during module load (called by the CORS middleware).
os.environ.setdefault("DATABASE_URL", "sqlite:///:memory:")
os.environ.setdefault("SECRET_KEY", "test-secret-key-that-is-at-least-32-bytes!!")
os.environ.setdefault("ENVIRONMENT", "test")

from collections.abc import Iterator  # noqa: E402

import pytest  # noqa: E402
from fastapi.testclient import TestClient  # noqa: E402

from weighttogo.main import app  # noqa: E402


@pytest.fixture
def client() -> Iterator[TestClient]:
    with TestClient(app) as test_client:
        yield test_client
