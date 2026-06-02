"""Shared fixtures for unit tests that require an in-memory SQLite session.

Provides a minimal ``db_session`` fixture for model-level constraint rejection
tests.  No app wiring — just create_all and a session.  The integration conftest
has the equivalent fixture wired to the FastAPI test client.
"""

from __future__ import annotations

from collections.abc import Generator

import pytest
from sqlalchemy import create_engine, text
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

# Import all models so Base.metadata knows every table before create_all.
from weighttogo.achievements.infrastructure.models import AchievementModel  # noqa: F401
from weighttogo.audit.infrastructure.models import AuditLogModel  # noqa: F401
from weighttogo.auth.infrastructure.models import Base
from weighttogo.goals.infrastructure.models import GoalModel  # noqa: F401
from weighttogo.preferences.infrastructure.models import UserPreferenceModel  # noqa: F401
from weighttogo.weight_tracking.infrastructure.models import WeightEntryModel  # noqa: F401


@pytest.fixture()
def db_session() -> Generator[Session, None, None]:
    """Yield a fresh in-memory SQLite session per test.

    Uses StaticPool so all accesses share the same in-memory connection —
    required because SQLite ``:memory:`` is per-connection.
    """
    engine = create_engine(
        "sqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    with engine.connect() as conn:
        conn.execute(text("PRAGMA foreign_keys=ON"))
    Base.metadata.create_all(engine)
    session_factory = sessionmaker(bind=engine, autocommit=False, autoflush=False)
    session = session_factory()
    try:
        yield session
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()
        Base.metadata.drop_all(engine)
        engine.dispose()
