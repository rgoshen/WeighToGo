"""Create audit_log table.

Revision ID: 0009
Revises: 0008
Create Date: 2026-06-02

SRS §8.2.7 / ADR-0024. The seventh and final table in the schema.

Design highlights:
- user_id has ON DELETE SET NULL so audit rows survive actor deletion.
- event_type is CHECK-constrained against a fixed 14-value taxonomy.
- Both CHECKs are also declared in AuditLogModel.__table_args__ so
  Base.metadata.create_all enforces them in the SQLite integration suite.
- ip_address uses String(45) instead of INET for SQLite portability.
- metadata uses JSON instead of JSONB for SQLite portability.
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op

revision: str = "0009"
down_revision: str = "0008"
branch_labels: str | None = None
depends_on: str | None = None

_VALID_EVENT_TYPES = ", ".join(
    f"'{v}'"
    for v in (
        "auth.register",
        "auth.login_succeeded",
        "auth.login_failed",
        "auth.logout",
        "auth.token_refreshed",
        "auth.token_reuse_detected",
        "auth.account_locked",
        "weight_entry.created",
        "weight_entry.updated",
        "weight_entry.deleted",
        "goal.created",
        "goal.updated",
        "goal.abandoned",
        "preference.updated",
    )
)


def upgrade() -> None:
    """Create audit_log table with constraints and indexes."""
    op.create_table(
        "audit_log",
        sa.Column("audit_id", sa.BigInteger(), primary_key=True, autoincrement=True),
        sa.Column(
            "user_id",
            sa.BigInteger(),
            sa.ForeignKey("users.user_id", ondelete="SET NULL"),
            nullable=True,
        ),
        sa.Column("event_type", sa.String(50), nullable=False),
        sa.Column("resource_type", sa.String(30), nullable=True),
        sa.Column("resource_id", sa.BigInteger(), nullable=True),
        sa.Column("request_id", sa.String(64), nullable=True),
        sa.Column("ip_address", sa.String(45), nullable=True),
        sa.Column("metadata", sa.JSON(), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.func.now(),
        ),
        sa.CheckConstraint(
            f"event_type IN ({_VALID_EVENT_TYPES})",
            name="audit_log_event_type_valid",
        ),
        sa.CheckConstraint(
            "resource_id IS NULL OR resource_type IS NOT NULL",
            name="audit_log_resource_consistency",
        ),
    )
    op.create_index("idx_audit_log_user_created", "audit_log", ["user_id", "created_at"])
    op.create_index("idx_audit_log_event_type_created", "audit_log", ["event_type", "created_at"])


def downgrade() -> None:
    """Drop audit_log table and its indexes."""
    op.drop_index("idx_audit_log_event_type_created", table_name="audit_log")
    op.drop_index("idx_audit_log_user_created", table_name="audit_log")
    op.drop_table("audit_log")
