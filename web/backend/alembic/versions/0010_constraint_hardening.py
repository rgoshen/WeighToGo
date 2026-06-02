"""Add achievements_threshold_positive and goals_target_date_epoch CHECKs; goals listing index.

Revision ID: 0010
Revises: 0009
Create Date: 2026-06-02

Closes two genuine constraint gaps identified in the M4 Phase 2 audit (GH-98, ADR-0025):

  achievements_threshold_positive:
      threshold IS NULL OR threshold > 0
      Enforces that milestone/streak thresholds are positive values; NULL is
      reserved for goal_reached rows (ADR-0025).

  goals_target_date_epoch:
      target_date IS NULL OR target_date >= '2020-01-01'
      Rejects clearly impossible historical target dates using a dialect-portable
      lower-bound epoch.  Cross-column (>= created_at) was rejected for SQLite
      portability reasons documented in ADR-0025.

  idx_goals_user_created (user_id, created_at DESC):
      idx_goals_one_active_per_user is partial (WHERE is_active = TRUE) and only
      covers the active-goal lookup.  A full user-goals listing has no index
      without this composite.

Both CHECKs are also declared in the owning model __table_args__ so that
Base.metadata.create_all enforces them in the SQLite integration suite.
Follows the 0004/0005/0008 pattern — no batch_alter_table needed.
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op

revision: str = "0010"
down_revision: str = "0009"
branch_labels: str | None = None
depends_on: str | None = None


def upgrade() -> None:
    """Add two new CHECK constraints and one composite index."""
    op.create_check_constraint(
        "achievements_threshold_positive",
        "achievements",
        sa.text("threshold IS NULL OR threshold > 0"),
    )
    op.create_check_constraint(
        "goals_target_date_epoch",
        "goals",
        sa.text("target_date IS NULL OR target_date >= '2020-01-01'"),
    )
    op.create_index(
        "idx_goals_user_created",
        "goals",
        ["user_id", sa.text("created_at DESC")],
    )


def downgrade() -> None:
    """Remove the two CHECK constraints and the composite index."""
    op.drop_index("idx_goals_user_created", table_name="goals")
    op.drop_constraint("goals_target_date_epoch", "goals", type_="check")
    op.drop_constraint("achievements_threshold_positive", "achievements", type_="check")
