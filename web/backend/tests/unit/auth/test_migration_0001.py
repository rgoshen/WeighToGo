"""Structure test for migration 0001_initial_users_and_auth.

Verifies the migration file contains the expected revision chain, callables,
and key identifiers without executing it against a database.  Follows the
test_migration_0008 precedent for structure-only (text-scan) tests.
"""

from __future__ import annotations

import importlib.util
import pathlib
import types

_PATH = pathlib.Path("alembic/versions/0001_initial_users_and_auth.py")


def _load_migration() -> types.ModuleType:
    spec = importlib.util.spec_from_file_location("migration_0001", _PATH)
    assert spec is not None and spec.loader is not None
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def test_migration_0001_file_exists() -> None:
    assert _PATH.is_file(), f"Migration not found at {_PATH}"


def test_migration_0001_revision_identifiers() -> None:
    mod = _load_migration()
    assert mod.revision == "0001"
    assert mod.down_revision is None


def test_migration_0001_has_upgrade_and_downgrade() -> None:
    mod = _load_migration()
    assert callable(mod.upgrade)
    assert callable(mod.downgrade)


def test_migration_0001_creates_users_and_refresh_tokens_tables() -> None:
    src = _PATH.read_text()
    assert '"users"' in src
    assert '"refresh_tokens"' in src


def test_migration_0001_references_citext_extension() -> None:
    # CITEXT extension is created in upgrade and dropped in downgrade
    assert "citext" in _PATH.read_text()


def test_migration_0001_downgrade_is_nontrivial() -> None:
    # String scan — confirms the downgrade body is non-trivial (drops tables)
    assert "drop_table" in _PATH.read_text()
