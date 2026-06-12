"""Architecture tests: verify the Clean Architecture dependency rule via import-linter."""

import re
import shutil
import subprocess
from pathlib import Path

import pytest

# Anchor the import-linter config to the backend root (web/backend/) regardless
# of the working directory pytest was launched from. ``__file__`` is
# web/backend/tests/architecture/test_import_contracts.py, so parents[2] is
# web/backend/. Without this, a run started from the repo root resolves the
# wrong (or a missing) pyproject.toml and the contract tests fail for an
# environmental reason unrelated to the contracts.
_BACKEND_ROOT = Path(__file__).parents[2]
_CONFIG = _BACKEND_ROOT / "pyproject.toml"
# A wedged lint-imports should fail fast rather than hang the worker until the
# CI job-level timeout.
_LINT_TIMEOUT_SECONDS = 120


@pytest.fixture(scope="session")
def import_lint_result() -> subprocess.CompletedProcess[str]:
    """Run import-linter once and share the result across the architecture suite.

    Building the import graph ("Analyzed N files, M dependencies") is the
    expensive step; running it once per session and asserting against the
    captured output keeps the suite fast and avoids rebuilding the graph for
    every contract assertion.
    """
    lint_imports = shutil.which("lint-imports")
    assert lint_imports is not None, "lint-imports binary not found on PATH"

    return subprocess.run(
        [lint_imports, "--config", str(_CONFIG)],
        capture_output=True,
        text=True,
        cwd=_BACKEND_ROOT,
        timeout=_LINT_TIMEOUT_SECONDS,
    )


def test_import_contracts_pass(
    import_lint_result: subprocess.CompletedProcess[str],
) -> None:
    """All contracts must hold: the Clean Architecture dependency rule and the
    bounded-context isolation rules from SRS §4.2.
    """
    assert import_lint_result.returncode == 0, (
        f"Import contracts failed:\n{import_lint_result.stdout}\n{import_lint_result.stderr}"
    )


def test_audit_is_importable_only_by_the_four_routers(
    import_lint_result: subprocess.CompletedProcess[str],
) -> None:
    """``weighttogo.audit`` may be imported only by the four interface routers.

    ADR-0024 documents that audit is wired solely at the composition root of
    the auth, goals, weight_tracking, and preferences routers. A ``protected``
    import-linter contract enforces that exclusivity: any other importer — a
    fifth interface router, an inner (domain/application/infrastructure) layer,
    the top-level request middleware, or a future bounded context — fails CI
    (M4 Web App Quality Review, finding 3).
    """
    assert import_lint_result.returncode == 0, (
        f"Import contracts failed:\n{import_lint_result.stdout}\n{import_lint_result.stderr}"
    )
    # Match the machine-meaningful signal, not the exact rendered string:
    # import-linter renders through rich, which soft-wraps to the console width,
    # so collapse all whitespace before asserting the contract's KEPT status.
    # This survives a narrow $COLUMNS in CI and a future import-linter reformat.
    normalized = re.sub(r"\s+", " ", import_lint_result.stdout)
    contract = "audit: only the four interface routers may import audit"
    assert f"{contract} KEPT" in normalized, (
        "audit protection contract missing or broken:\n"
        f"{import_lint_result.stdout}\n{import_lint_result.stderr}"
    )
