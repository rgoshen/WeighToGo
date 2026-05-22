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
