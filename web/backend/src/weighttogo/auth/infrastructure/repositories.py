"""SQLAlchemy repository adapters for the auth bounded context.

These adapters implement the ``IUserRepository`` and ``IRefreshTokenRepository``
port interfaces using SQLAlchemy ORM sessions.  They are the only components
that import SQLAlchemy — the domain and application layers remain framework-free.

Each repository receives a ``Session`` at construction time.  FastAPI dependency
injection provides the session per request; unit tests inject an in-memory
SQLite session.
"""

from __future__ import annotations

import uuid
from datetime import UTC, datetime

from sqlalchemy import update
from sqlalchemy.orm import Session

from weighttogo.auth.domain.entities import RefreshToken, User
from weighttogo.auth.infrastructure.models import RefreshTokenModel, UserModel


def _user_to_domain(row: UserModel) -> User:
    """Convert a ``UserModel`` ORM row to a domain ``User`` entity.

    Args:
        row: A fully-loaded ``UserModel`` ORM instance.

    Returns:
        The equivalent domain entity.
    """
    return User(
        user_id=row.user_id,
        email=row.email,
        hashed_password=row.password_hash,
        display_name=row.display_name,
        is_active=row.is_active,
        failed_login_count=row.failed_login_count,
        locked_until=row.locked_until,
        created_at=row.created_at,
        updated_at=row.updated_at,
        last_login_at=row.last_login_at,
    )


def _token_to_domain(row: RefreshTokenModel) -> RefreshToken:
    """Convert a ``RefreshTokenModel`` ORM row to a domain ``RefreshToken`` entity.

    Args:
        row: A fully-loaded ``RefreshTokenModel`` ORM instance.

    Returns:
        The equivalent domain entity.
    """
    return RefreshToken(
        token_id=row.token_id,
        user_id=row.user_id,
        token_hash=row.token_hash,
        family_id=row.family_id,
        expires_at=row.expires_at,
        issued_at=row.issued_at,
        revoked_at=row.revoked_at,
        replaced_by=row.replaced_by,
    )


class SqlAlchemyUserRepository:
    """SQLAlchemy implementation of ``IUserRepository``.

    Args:
        session: An active SQLAlchemy ``Session``.
    """

    def __init__(self, session: Session) -> None:
        """Initialise with an active SQLAlchemy session."""
        self._session = session

    def save(self, user: User) -> User:
        """Persist *user* and return it with ``user_id`` populated.

        Performs an INSERT for new entities (``user_id`` is ``None``) or an
        UPDATE for existing ones.

        Args:
            user: The domain entity to persist.

        Returns:
            The same entity with the database-assigned ``user_id`` and any
            server-side defaults populated.
        """
        if user.user_id is None:
            row = UserModel(
                email=user.email,
                password_hash=user.hashed_password,
                display_name=user.display_name,
                is_active=user.is_active,
                failed_login_count=user.failed_login_count,
                locked_until=user.locked_until,
                last_login_at=user.last_login_at,
            )
            self._session.add(row)
            self._session.flush()  # populate user_id from DB sequence
        else:
            row_or_none = self._session.get(UserModel, user.user_id)
            if row_or_none is None:
                raise ValueError(f"User {user.user_id} not found in database.")
            row = row_or_none
            row.email = user.email
            row.password_hash = user.hashed_password
            row.display_name = user.display_name
            row.is_active = user.is_active
            row.failed_login_count = user.failed_login_count
            row.locked_until = user.locked_until
            row.last_login_at = user.last_login_at
            row.updated_at = datetime.now(UTC)
            self._session.flush()

        return _user_to_domain(row)

    def get_by_email(self, email: str) -> User | None:
        """Look up a user by email address (case-insensitive via CITEXT).

        Args:
            email: The email address to look up.

        Returns:
            The matching domain entity, or ``None`` if not found.
        """
        row = self._session.query(UserModel).filter_by(email=email.lower()).first()
        return _user_to_domain(row) if row else None

    def get_by_id(self, user_id: int) -> User | None:
        """Look up a user by primary key.

        Args:
            user_id: The surrogate primary key.

        Returns:
            The matching domain entity, or ``None`` if not found.
        """
        row = self._session.get(UserModel, user_id)
        return _user_to_domain(row) if row else None


class SqlAlchemyRefreshTokenRepository:
    """SQLAlchemy implementation of ``IRefreshTokenRepository``.

    Args:
        session: An active SQLAlchemy ``Session``.
    """

    def __init__(self, session: Session) -> None:
        """Initialise with an active SQLAlchemy session."""
        self._session = session

    def save(self, token: RefreshToken) -> RefreshToken:
        """Persist *token* and return it with ``token_id`` populated.

        Args:
            token: The domain entity to persist.

        Returns:
            The same entity with the database-assigned ``token_id``.
        """
        if token.token_id is None:
            row = RefreshTokenModel(
                user_id=token.user_id,
                token_hash=token.token_hash,
                family_id=token.family_id,
                expires_at=token.expires_at,
                issued_at=token.issued_at,
                revoked_at=token.revoked_at,
                replaced_by=token.replaced_by,
            )
            self._session.add(row)
            self._session.flush()
        else:
            row_or_none = self._session.get(RefreshTokenModel, token.token_id)
            if row_or_none is None:
                raise ValueError(f"RefreshToken {token.token_id} not found.")
            row = row_or_none
            row.revoked_at = token.revoked_at
            row.replaced_by = token.replaced_by
            self._session.flush()

        return _token_to_domain(row)

    def get_by_hash(self, token_hash: str) -> RefreshToken | None:
        """Look up a token by its SHA-256 hash.

        Args:
            token_hash: The 64-character hex digest to look up.

        Returns:
            The matching domain entity, or ``None`` if not found.
        """
        row = self._session.query(RefreshTokenModel).filter_by(token_hash=token_hash).first()
        return _token_to_domain(row) if row else None

    def revoke_family(self, family_id: uuid.UUID) -> None:
        """Mark every token in *family_id* as revoked.

        Args:
            family_id: The UUID shared by all tokens in the rotation chain.
        """
        now = datetime.now(UTC)
        self._session.execute(
            update(RefreshTokenModel)
            .where(RefreshTokenModel.family_id == family_id)
            .where(RefreshTokenModel.revoked_at.is_(None))
            .values(revoked_at=now)
        )
        self._session.flush()
