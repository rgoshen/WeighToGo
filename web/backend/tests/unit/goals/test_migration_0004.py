"""Structure test for migration 0004_goals_direction_check.

Verifies revision chain, callables, and constraint name.  The DROP/ADD
CONSTRAINT DDL cannot execute on SQLite, so this test mirrors the
test_migration_0008 precedent: structure verification only.
"""

from __future__ import annotations

import importlib.util
import pathlib
import types

_PATH = pathlib.Path("alembic/versions/0004_goals_direction_check.py")


def _load_migration() -> types.ModuleType:
    spec = importlib.util.spec_from_file_location("migration_0004", _PATH)
    assert spec is not None and spec.loader is not None
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def test_migration_0004_file_exists() -> None:
    assert _PATH.is_file(), f"Migration not found at {_PATH}"


def test_migration_0004_revision_identifiers() -> None:
    mod = _load_migration()
    assert mod.revision == "0004"
    assert mod.down_revision == "0003"


def test_migration_0004_has_upgrade_and_downgrade() -> None:
    mod = _load_migration()
    assert callable(mod.upgrade)
    assert callable(mod.downgrade)


def test_migration_0004_declares_direction_invariant_constraint() -> None:
    assert "goals_direction_invariant" in _PATH.read_text()


def test_migration_0004_downgrade_reverses_constraint() -> None:
    src = _PATH.read_text()
    assert "create_check_constraint" in src
    assert "drop_constraint" in src
