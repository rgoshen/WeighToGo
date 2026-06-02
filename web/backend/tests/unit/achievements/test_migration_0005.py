"""Structure test for migration 0005_achievements.

Verifies revision chain, callables, table name, downgrade drop,
and the three achievement index names.
"""

from __future__ import annotations

import importlib.util
import pathlib
import types

_PATH = pathlib.Path("alembic/versions/0005_achievements.py")


def _load_migration() -> types.ModuleType:
    spec = importlib.util.spec_from_file_location("migration_0005", _PATH)
    assert spec is not None and spec.loader is not None
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def test_migration_0005_file_exists() -> None:
    assert _PATH.is_file(), f"Migration not found at {_PATH}"


def test_migration_0005_revision_identifiers() -> None:
    mod = _load_migration()
    assert mod.revision == "0005"
    assert mod.down_revision == "0004"


def test_migration_0005_has_upgrade_and_downgrade() -> None:
    mod = _load_migration()
    assert callable(mod.upgrade)
    assert callable(mod.downgrade)


def test_migration_0005_creates_achievements_table() -> None:
    assert '"achievements"' in _PATH.read_text()


def test_migration_0005_downgrade_is_nontrivial() -> None:
    # String scan — confirms the downgrade body is non-trivial (drops table)
    assert "drop_table" in _PATH.read_text()


def test_migration_0005_declares_achievement_indexes() -> None:
    src = _PATH.read_text()
    assert "idx_achievements_user_earned" in src
    assert "idx_achievements_unique_goal_reached" in src
    assert "idx_achievements_unique_milestone" in src
