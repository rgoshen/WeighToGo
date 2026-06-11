"""Architecture tests: verify the Clean Architecture dependency rule via import-linter."""

import shutil
import subprocess


def test_import_contracts_pass() -> None:
    """All domains must satisfy the Clean Architecture dependency rule.

    Verifies that domain and application layers do not import framework
    packages (fastapi, sqlalchemy, pydantic, alembic), enforcing the
    dependency rule from SRS §4.2.
    """
    lint_imports = shutil.which("lint-imports")
    assert lint_imports is not None, "lint-imports binary not found on PATH"

    result = subprocess.run(
        [lint_imports, "--config", "pyproject.toml"],
        capture_output=True,
        text=True,
        cwd=".",  # run from web/backend/
    )
    assert result.returncode == 0, f"Import contracts failed:\n{result.stdout}\n{result.stderr}"


def test_audit_context_isolation_contract_is_enforced() -> None:
    """No other context's domain/application layer may import ``weighttogo.audit``.

    ADR-0024 documents that the ``audit`` context is never imported by another
    context's inner layers — only the four interface routers wire it at the
    composition root. This verifies a *named* import-linter ``forbidden``
    contract mechanically enforces that claim rather than leaving it to
    convention (M4 Web App Quality Review, finding 3).
    """
    lint_imports = shutil.which("lint-imports")
    assert lint_imports is not None, "lint-imports binary not found on PATH"

    result = subprocess.run(
        [lint_imports, "--config", "pyproject.toml"],
        capture_output=True,
        text=True,
        cwd=".",  # run from web/backend/
    )

    assert "audit: other contexts must not import audit KEPT" in result.stdout, (
        f"audit context-isolation contract missing or broken:\n{result.stdout}\n{result.stderr}"
    )
