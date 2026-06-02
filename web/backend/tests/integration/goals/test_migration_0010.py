"""Structure test for migration 0010_constraint_hardening.

Verifies the migration file contains the expected constraint names, index
name, and revision chain without executing it against a database.  Mirrors
the 0004/0005/0008/0009 precedent for constraint-defining migrations.

Placed in tests/integration/goals/ following the test_migration_0009.py
pattern in tests/integration/audit/ — not in tests/unit/ despite being
a text-only test, for consistency with the most recent migration test precedent.
"""

from __future__ import annotations

import pathlib

MIGRATION_PATH = (
    pathlib.Path(__file__).parents[3] / "alembic" / "versions" / "0010_constraint_hardening.py"
)


def _src() -> str:
    return MIGRATION_PATH.read_text()


def test_migration_file_exists() -> None:
    assert MIGRATION_PATH.exists(), f"Migration not found at {MIGRATION_PATH}"


def test_migration_sets_correct_revision_chain() -> None:
    src = _src()
    assert 'revision: str = "0010"' in src
    assert 'down_revision: str = "0009"' in src


def test_migration_declares_threshold_positive_constraint() -> None:
    assert "achievements_threshold_positive" in _src()


def test_migration_declares_target_date_epoch_constraint() -> None:
    assert "goals_target_date_epoch" in _src()


def test_migration_declares_goals_user_created_index() -> None:
    assert "idx_goals_user_created" in _src()


def test_migration_has_downgrade_function() -> None:
    src = _src()
    assert "def downgrade" in src
    assert "drop_constraint" in src
    assert "drop_index" in src
