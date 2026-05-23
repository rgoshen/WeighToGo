"""Verify migration 0002 applies cleanly and has correct revision identifiers."""

from __future__ import annotations

import importlib.util
import pathlib
import types


def _load_migration() -> types.ModuleType:
    spec = importlib.util.spec_from_file_location(
        "migration_0002",
        pathlib.Path("alembic/versions/0002_weight_entries.py"),
    )
    assert spec is not None and spec.loader is not None
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def test_migration_0002_file_exists() -> None:
    path = pathlib.Path("alembic/versions/0002_weight_entries.py")
    assert path.is_file(), f"Migration not found at {path}"


def test_migration_0002_revision_identifiers() -> None:
    mod = _load_migration()
    assert mod.revision == "0002"
    assert mod.down_revision == "0001"


def test_migration_0002_has_upgrade_and_downgrade() -> None:
    mod = _load_migration()
    assert callable(mod.upgrade)
    assert callable(mod.downgrade)


def test_migration_0002_creates_weight_entries_table() -> None:
    """upgrade() must produce a weight_entries table with all 10 columns."""
    from alembic.operations import Operations
    from alembic.runtime.migration import MigrationContext
    from sqlalchemy import create_engine, inspect, text
    from sqlalchemy.pool import StaticPool

    engine = create_engine(
        "sqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )

    # Prerequisite: create the users table (0001 output, sans PostgreSQL CITEXT)
    with engine.begin() as conn:
        conn.execute(
            text(
                "CREATE TABLE users ("
                "  user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                "  email TEXT NOT NULL UNIQUE,"
                "  password_hash TEXT NOT NULL,"
                "  display_name TEXT NOT NULL,"
                "  is_active INTEGER NOT NULL DEFAULT 1,"
                "  failed_login_count INTEGER NOT NULL DEFAULT 0,"
                "  locked_until TEXT,"
                "  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                "  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                "  last_login_at TEXT"
                ")"
            )
        )

    mod = _load_migration()

    with engine.begin() as conn:
        migration_ctx = MigrationContext.configure(conn)
        with Operations.context(migration_ctx):
            mod.upgrade()

    insp = inspect(engine)
    assert "weight_entries" in insp.get_table_names()

    col_names = {col["name"] for col in insp.get_columns("weight_entries")}
    expected = {
        "entry_id",
        "user_id",
        "weight_value",
        "weight_unit",
        "observation_date",
        "notes",
        "created_at",
        "updated_at",
        "is_deleted",
        "deleted_at",
    }
    assert expected <= col_names


def test_migration_0002_downgrade_removes_weight_entries_table() -> None:
    """downgrade() must drop weight_entries cleanly."""
    from alembic.operations import Operations
    from alembic.runtime.migration import MigrationContext
    from sqlalchemy import create_engine, inspect, text
    from sqlalchemy.pool import StaticPool

    engine = create_engine(
        "sqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )

    with engine.begin() as conn:
        conn.execute(
            text(
                "CREATE TABLE users ("
                "  user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                "  email TEXT NOT NULL,"
                "  password_hash TEXT NOT NULL,"
                "  display_name TEXT NOT NULL,"
                "  is_active INTEGER NOT NULL DEFAULT 1,"
                "  failed_login_count INTEGER NOT NULL DEFAULT 0,"
                "  locked_until TEXT,"
                "  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                "  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                "  last_login_at TEXT"
                ")"
            )
        )

    mod = _load_migration()

    with engine.begin() as conn:
        ctx = MigrationContext.configure(conn)
        with Operations.context(ctx):
            mod.upgrade()

    with engine.begin() as conn:
        ctx = MigrationContext.configure(conn)
        with Operations.context(ctx):
            mod.downgrade()

    assert "weight_entries" not in inspect(engine).get_table_names()
