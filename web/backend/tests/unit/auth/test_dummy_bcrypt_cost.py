"""C5 regression: dummy verify hash must match the adapter's current cost factor."""

from weighttogo.auth.infrastructure.password import BcryptPasswordAdapter


def test_verify_dummy_uses_current_rounds() -> None:
    """verify_dummy must produce a hash at _ROUNDS cost, not a hardcoded cost-12 hash."""
    original = BcryptPasswordAdapter._ROUNDS
    try:
        BcryptPasswordAdapter._ROUNDS = 4  # fast for tests
        BcryptPasswordAdapter._dummy_hash = None  # reset cached dummy

        adapter = BcryptPasswordAdapter()
        adapter.verify_dummy("anypassword")  # must not raise

        # The cached dummy must start with the $2b$04$ cost prefix
        dummy = BcryptPasswordAdapter._dummy_hash
        assert dummy is not None
        assert dummy.startswith("$2b$04$"), f"expected $2b$04$ prefix, got: {dummy[:8]}"
    finally:
        BcryptPasswordAdapter._ROUNDS = original
        BcryptPasswordAdapter._dummy_hash = None
