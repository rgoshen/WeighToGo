"""Verify migration 0008 widens the achievement_type CHECK to allow 'streak' (FR-Ach-3).

The CHECK-constraint DDL (ALTER TABLE ... DROP CONSTRAINT) is not executable on
SQLite, so — like migrations 0004 and 0005 — this test verifies the migration's
identifiers, callables, and constraint definition rather than executing it.
PostgreSQL exercises the DDL in CI/production.
"""

from __future__ import annotations

import importlib.util
import pathlib
import types

_PATH = pathlib.Path("alembic/versions/0008_streak_achievement_type.py")


def _load_migration() -> types.ModuleType:
    spec = importlib.util.spec_from_file_location("migration_0008", _PATH)
    assert spec is not None and spec.loader is not None
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def test_migration_0008_file_exists() -> None:
    assert _PATH.is_file(), f"Migration not found at {_PATH}"


def test_migration_0008_revision_identifiers() -> None:
    mod = _load_migration()
    assert mod.revision == "0008"
    assert mod.down_revision == "0007"


def test_migration_0008_has_upgrade_and_downgrade() -> None:
    mod = _load_migration()
    assert callable(mod.upgrade)
    assert callable(mod.downgrade)


def test_migration_0008_new_constraint_includes_streak() -> None:
    mod = _load_migration()
    assert "streak" in mod._NEW
    assert "streak" not in mod._OLD


def test_migration_0008_downgrade_deletes_streak_rows_before_narrowing() -> None:
    # Downgrade must DELETE streak achievements before recreating the narrow CHECK;
    # PostgreSQL validates existing rows on create_check_constraint, so any streak
    # row present during downgrade would raise a constraint violation without this.
    src = _PATH.read_text()
    assert "DELETE FROM achievements WHERE achievement_type = 'streak'" in src
