"""Shared database session dependency for FastAPI.

Provides the ``get_db_session`` dependency function used by all route handlers
that need a SQLAlchemy session.  The session is committed on success and rolled
back on exception.

Usage in route handlers::

    from weighttogo.shared.db import get_db_session

    @router.post("/example")
    def create(session: Session = Depends(get_db_session)):
        ...
"""

from __future__ import annotations

from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from weighttogo.config import get_settings

_engine = None
_SessionLocal: sessionmaker[Session] | None = None


def _get_engine() -> object:
    """Lazily build the engine from settings so tests can override dependencies."""
    global _engine, _SessionLocal  # noqa: PLW0603
    if _engine is None:
        settings = get_settings()
        _engine = create_engine(settings.database_url, pool_pre_ping=True)
        _SessionLocal = sessionmaker(bind=_engine, autocommit=False, autoflush=False)
    return _engine


def get_db_session() -> Generator[Session, None, None]:
    """FastAPI dependency that yields a database session per request.

    The session is committed before yielding control back to FastAPI;
    on exception it is rolled back.  The session is always closed.

    Yields:
        An active SQLAlchemy ``Session``.
    """
    _get_engine()
    assert _SessionLocal is not None
    session = _SessionLocal()
    try:
        yield session
        session.commit()
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()
