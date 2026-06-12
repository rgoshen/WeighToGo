"""Structure test for migration 0009_audit_log.

Verifies the migration file contains the expected constraint names, column
definitions, and index names without executing it against a database.  This
mirrors the 0004/0005/0008 precedent for constraint-defining migrations.
"""

from __future__ import annotations

import pathlib
import re

from weighttogo.audit.domain.entities import AuditEventType

MIGRATION_PATH = pathlib.Path(__file__).parents[3] / "alembic" / "versions" / "0009_audit_log.py"


def _src() -> str:
    return MIGRATION_PATH.read_text()


def test_migration_file_exists() -> None:
    assert MIGRATION_PATH.exists(), f"Migration not found at {MIGRATION_PATH}"


def test_migration_sets_correct_revision_chain() -> None:
    src = _src()
    assert 'revision: str = "0009"' in src
    assert 'down_revision: str = "0008"' in src


def test_migration_creates_audit_log_table() -> None:
    assert "op.create_table" in _src()
    assert '"audit_log"' in _src()


def test_migration_declares_event_type_check_constraint() -> None:
    assert "audit_log_event_type_valid" in _src()


def test_migration_declares_resource_consistency_check_constraint() -> None:
    assert "audit_log_resource_consistency" in _src()


def test_migration_declares_user_created_index() -> None:
    assert "idx_audit_log_user_created" in _src()


def test_migration_declares_event_type_created_index() -> None:
    assert "idx_audit_log_event_type_created" in _src()


def test_migration_uses_on_delete_set_null() -> None:
    # Confirms the FK retains the audit row when the actor is deleted (ADR-0024)
    assert "SET NULL" in _src()


def test_migration_has_downgrade_function() -> None:
    src = _src()
    assert "def downgrade" in src
    assert "drop_table" in src


def test_migration_indexes_use_desc_ordering() -> None:
    # Commit 3e830e8 fixed the indexes from ASC to DESC; guard against regression
    assert "created_at DESC" in _src()


def test_event_type_check_constraint_matches_enum() -> None:
    # The ORM model derives its event-type CHECK from AuditEventType, but this
    # migration hard-codes the list. Guard the hard-coded copy against drift:
    # a future enum addition without a matching CHECK update must fail here.
    # When a later migration re-declares the audit event-type CHECK, move this
    # assertion to that migration's structure test (the new "latest" one).
    src = _src()
    # Scope to the `for v in (...)` tuple feeding _VALID_EVENT_TYPES (the
    # migration's authoritative list). Scoping avoids unrelated dotted literals
    # elsewhere in the file, e.g. the "users.user_id" foreign key. Values are
    # double-quoted in source; the single quotes in f"'{v}'" only appear at runtime.
    tuple_match = re.search(r"for v in \((.*?)\)", src, re.DOTALL)
    assert tuple_match is not None, "could not locate the _VALID_EVENT_TYPES tuple"
    migration_values = set(re.findall(r"""["']([a-z_]+\.[a-z_]+)["']""", tuple_match.group(1)))

    assert migration_values == {e.value for e in AuditEventType}
