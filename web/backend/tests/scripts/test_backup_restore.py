"""Subprocess tests for the backup/restore shell scripts.

The scripts (``web/backend/scripts/backup.sh`` and ``restore.sh``) are thin
wrappers around ``pg_dump``/``pg_restore``. These tests prove the guard logic —
argument and ``--help`` handling, the ``DATABASE_URL`` requirement, restore
dump-file existence, the ``postgresql+psycopg`` -> ``postgresql`` DSN rewrite,
and aborting on an underlying-tool failure — without a live database.
``pg_dump`` and ``pg_restore`` are stubbed on ``PATH``, so the suite runs in the
existing ``uv``/``pytest`` harness with no extra tooling.
"""

from __future__ import annotations

import os
import subprocess
from pathlib import Path

import pytest

_SCRIPTS_DIR = Path(__file__).resolve().parents[2] / "scripts"
_BACKUP = _SCRIPTS_DIR / "backup.sh"
_RESTORE = _SCRIPTS_DIR / "restore.sh"

_DSN = "postgresql+psycopg://u:p@localhost:5432/db"
_STRIPPED_DSN = "postgresql://u:p@localhost:5432/db"

_SUCCESS_PG_DUMP = (
    "#!/usr/bin/env bash\n"
    'for arg in "$@"; do\n'
    '  case "$arg" in --file=*) : >"${arg#--file=}" ;; esac\n'
    "done\n"
    'echo "FAKE pg_dump $*"\n'
)
_SUCCESS_PG_RESTORE = '#!/usr/bin/env bash\necho "FAKE pg_restore $*"\n'
_FAILING_TOOL = "#!/usr/bin/env bash\nexit 1\n"


def _write_stub(directory: Path, name: str, body: str) -> None:
    """Write an executable stub *name* containing *body* into *directory*."""
    stub = directory / name
    stub.write_text(body)
    stub.chmod(0o755)


def _run(
    script: Path,
    *args: str,
    path_prefix: Path,
    cwd: Path,
    database_url: str | None,
) -> subprocess.CompletedProcess[str]:
    """Run *script* with *path_prefix* shadowing PATH and a controlled env."""
    env = os.environ.copy()
    env["PATH"] = f"{path_prefix}{os.pathsep}{env['PATH']}"
    env.pop("DATABASE_URL", None)
    if database_url is not None:
        env["DATABASE_URL"] = database_url
    return subprocess.run(
        [str(script), *args],
        cwd=cwd,
        env=env,
        capture_output=True,
        text=True,
        check=False,
    )


@pytest.fixture
def stub_bin(tmp_path: Path) -> Path:
    """A PATH-prefix directory with success stubs for pg_dump and pg_restore."""
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    _write_stub(bin_dir, "pg_dump", _SUCCESS_PG_DUMP)
    _write_stub(bin_dir, "pg_restore", _SUCCESS_PG_RESTORE)
    return bin_dir


# --- backup.sh -------------------------------------------------------------


def test_backup_help_exits_zero(stub_bin: Path, tmp_path: Path) -> None:
    result = _run(_BACKUP, "--help", path_prefix=stub_bin, cwd=tmp_path, database_url=None)
    assert result.returncode == 0
    assert "Usage: backup.sh" in result.stdout


def test_backup_requires_database_url(stub_bin: Path, tmp_path: Path) -> None:
    result = _run(_BACKUP, "out.dump", path_prefix=stub_bin, cwd=tmp_path, database_url=None)
    assert result.returncode != 0
    assert "DATABASE_URL is not set" in result.stderr


def test_backup_invokes_pg_dump_and_strips_dsn(stub_bin: Path, tmp_path: Path) -> None:
    result = _run(_BACKUP, "out.dump", path_prefix=stub_bin, cwd=tmp_path, database_url=_DSN)
    assert result.returncode == 0
    assert (tmp_path / "out.dump").is_file()
    assert "FAKE pg_dump" in result.stdout
    assert "--file=out.dump" in result.stdout
    assert _STRIPPED_DSN in result.stdout


def test_backup_fails_when_pg_dump_fails(tmp_path: Path) -> None:
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    _write_stub(bin_dir, "pg_dump", _FAILING_TOOL)
    result = _run(
        _BACKUP, "out.dump", path_prefix=bin_dir, cwd=tmp_path, database_url=_STRIPPED_DSN
    )
    assert result.returncode != 0
    assert "backup written" not in result.stdout


# --- restore.sh ------------------------------------------------------------


def test_restore_help_exits_zero(stub_bin: Path, tmp_path: Path) -> None:
    result = _run(_RESTORE, "--help", path_prefix=stub_bin, cwd=tmp_path, database_url=None)
    assert result.returncode == 0
    assert "Usage: restore.sh" in result.stdout


def test_restore_usage_error_without_dump_file(stub_bin: Path, tmp_path: Path) -> None:
    result = _run(_RESTORE, path_prefix=stub_bin, cwd=tmp_path, database_url=_STRIPPED_DSN)
    assert result.returncode == 2
    assert "Usage: restore.sh" in result.stderr


def test_restore_requires_database_url(stub_bin: Path, tmp_path: Path) -> None:
    (tmp_path / "in.dump").touch()
    result = _run(_RESTORE, "in.dump", path_prefix=stub_bin, cwd=tmp_path, database_url=None)
    assert result.returncode != 0
    assert "DATABASE_URL is not set" in result.stderr


def test_restore_fails_when_dump_file_missing(stub_bin: Path, tmp_path: Path) -> None:
    result = _run(
        _RESTORE, "missing.dump", path_prefix=stub_bin, cwd=tmp_path, database_url=_STRIPPED_DSN
    )
    assert result.returncode != 0
    assert "dump file not found" in result.stderr


def test_restore_invokes_pg_restore_and_strips_dsn(stub_bin: Path, tmp_path: Path) -> None:
    (tmp_path / "in.dump").touch()
    result = _run(_RESTORE, "in.dump", path_prefix=stub_bin, cwd=tmp_path, database_url=_DSN)
    assert result.returncode == 0
    assert "FAKE pg_restore" in result.stdout
    assert _STRIPPED_DSN in result.stdout


def test_restore_uses_single_transaction_for_atomicity(stub_bin: Path, tmp_path: Path) -> None:
    # ARRANGE — a present dump file so the script reaches the pg_restore invocation.
    (tmp_path / "in.dump").touch()

    # ACT
    result = _run(_RESTORE, "in.dump", path_prefix=stub_bin, cwd=tmp_path, database_url=_DSN)

    # ASSERT — --single-transaction makes the restore commit-or-roll-back atomically
    # (and implies --exit-on-error), so a mid-restore failure cannot leave a partially
    # clobbered database. See docs/runbooks/backup-restore.md section 4.
    assert result.returncode == 0
    assert "--single-transaction" in result.stdout


def test_restore_fails_when_pg_restore_fails(tmp_path: Path) -> None:
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    _write_stub(bin_dir, "pg_restore", _FAILING_TOOL)
    (tmp_path / "in.dump").touch()
    result = _run(
        _RESTORE, "in.dump", path_prefix=bin_dir, cwd=tmp_path, database_url=_STRIPPED_DSN
    )
    assert result.returncode != 0
    assert "restore complete" not in result.stdout
