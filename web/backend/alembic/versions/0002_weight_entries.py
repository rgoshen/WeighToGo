"""Create weight_entries table with constraints and indexes.

Revision ID: 0002
Revises: 0001
Create Date: 2026-05-23

Implements SRS §8.2.3 and §8.3 migration 0002_weight_entries.
Five CHECK constraints enforce value-domain rules at the database level,
closing the gap identified in the Android code review (§1.2).

Two partial indexes support the active-entries query path:
  - A UNIQUE partial index prevents duplicate (user_id, observation_date) pairs
    among non-deleted entries.
  - A DESC partial index drives the paginated list query efficiently.

Both indexes use ``postgresql_where`` so the WHERE clause is only emitted on
PostgreSQL; SQLite falls back to a plain index for test compatibility.
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op

revision: str = "0002"
down_revision: str = "0001"
branch_labels: str | None = None
depends_on: str | None = None


def upgrade() -> None:
    """Create weight_entries table with all SRS-specified constraints and indexes."""
    op.create_table(
        "weight_entries",
        sa.Column("entry_id", sa.BigInteger(), primary_key=True, autoincrement=True),
        sa.Column(
            "user_id",
            sa.BigInteger(),
            sa.ForeignKey("users.user_id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column("weight_value", sa.Numeric(precision=6, scale=2), nullable=False),
        sa.Column("weight_unit", sa.String(3), nullable=False),
        sa.Column("observation_date", sa.Date(), nullable=False),
        sa.Column("notes", sa.String(500), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.Column("is_deleted", sa.Boolean(), nullable=False, server_default="false"),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
        # CHECK constraints (SRS §8.2.3)
        sa.CheckConstraint("weight_value > 0", name="weight_entries_value_positive"),
        sa.CheckConstraint("weight_value <= 1500", name="weight_entries_value_max"),
        sa.CheckConstraint("weight_unit IN ('lbs', 'kg')", name="weight_entries_unit_valid"),
        sa.CheckConstraint(
            "observation_date <= CURRENT_DATE",
            name="weight_entries_observation_not_future",
        ),
        sa.CheckConstraint(
            "(is_deleted = FALSE AND deleted_at IS NULL)"
            " OR (is_deleted = TRUE AND deleted_at IS NOT NULL)",
            name="weight_entries_deletion_consistency",
        ),
    )

    # UNIQUE partial index: one active entry per (user_id, observation_date)
    op.create_index(
        "idx_weight_entries_user_date_active",
        "weight_entries",
        ["user_id", "observation_date"],
        unique=True,
        postgresql_where=sa.text("is_deleted = FALSE"),
    )

    # Covering partial index for the paginated list query (DESC by observation_date)
    op.create_index(
        "idx_weight_entries_user_observation_desc",
        "weight_entries",
        ["user_id", sa.text("observation_date DESC")],
        postgresql_where=sa.text("is_deleted = FALSE"),
    )


def downgrade() -> None:
    """Drop weight_entries table and its indexes."""
    op.drop_index("idx_weight_entries_user_observation_desc", table_name="weight_entries")
    op.drop_index("idx_weight_entries_user_date_active", table_name="weight_entries")
    op.drop_table("weight_entries")
