"""Tests for application settings."""

from pathlib import Path

import pytest

from weighttogo.config import Settings


def test_settings_defaults_environment_to_development(
    monkeypatch: pytest.MonkeyPatch,
    tmp_path: Path,
) -> None:
    monkeypatch.delenv("ENVIRONMENT", raising=False)
    monkeypatch.chdir(tmp_path)

    settings = Settings()

    assert settings.environment == "development"


def test_settings_provides_a_default_database_url(
    monkeypatch: pytest.MonkeyPatch,
    tmp_path: Path,
) -> None:
    monkeypatch.delenv("DATABASE_URL", raising=False)
    monkeypatch.chdir(tmp_path)

    settings = Settings()

    assert settings.database_url.startswith("postgresql+psycopg://")
