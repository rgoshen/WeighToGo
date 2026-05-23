"""Bcrypt password hashing adapter.

Implements the password-hashing port using the ``bcrypt`` library directly
with a cost factor of 12 (SRS §NFR-S-2, §4.3.2).

Plain-text passwords are never logged, stored, or passed beyond this module.
Constant-time verification is provided by ``bcrypt.checkpw``, which prevents
timing-based side-channel attacks.
"""

import bcrypt as _bcrypt


class BcryptPasswordAdapter:
    """Adapter for the password-hashing port backed by bcrypt (cost factor 12).

    This is the sole location in the codebase that touches ``bcrypt`` directly.
    Use cases receive this adapter through dependency injection, keeping the
    domain and application layers decoupled from the hashing implementation.
    """

    _ROUNDS: int = 12
    _dummy_hash: str | None = None  # lazily computed at current _ROUNDS

    def hash(self, plaintext: str) -> str:
        """Hash *plaintext* with bcrypt and return the hash string.

        A new random salt is generated for every call, so two hashes of the
        same password will always differ.

        Args:
            plaintext: The raw password string supplied by the user.

        Returns:
            A bcrypt hash string in ``$2b$...`` format.
        """
        salt = _bcrypt.gensalt(rounds=self._ROUNDS)
        hashed = _bcrypt.hashpw(plaintext.encode(), salt)
        return hashed.decode()

    def verify(self, plaintext: str, hashed: str) -> bool:
        """Return ``True`` if *plaintext* matches *hashed* using constant-time comparison.

        Args:
            plaintext: The raw password string to check.
            hashed: A previously returned bcrypt hash string.

        Returns:
            ``True`` when the password is correct; ``False`` otherwise.
        """
        return bool(_bcrypt.checkpw(plaintext.encode(), hashed.encode()))

    def verify_dummy(self, plaintext: str) -> None:
        """Run a constant-time verify against a dummy hash derived from ``_ROUNDS``.

        Called when no real hash is available (unknown/inactive account) to make
        the response time indistinguishable from a real verify.  The dummy hash is
        lazily computed and cached at the class level so that ``_ROUNDS`` changes
        are reflected without per-call overhead.

        Args:
            plaintext: The raw password string from the login attempt.
        """
        if BcryptPasswordAdapter._dummy_hash is None:
            salt = _bcrypt.gensalt(rounds=self._ROUNDS)
            BcryptPasswordAdapter._dummy_hash = _bcrypt.hashpw(b"dummy", salt).decode()
        self.verify(plaintext, BcryptPasswordAdapter._dummy_hash)
