"""initial schema

Revision ID: 0001
Revises:
Create Date: 2026-11-26
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0001"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "provider",
        sa.Column("id", sa.Uuid(), primary_key=True),
        sa.Column("name", sa.String(255), nullable=False, unique=True),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("logo", sa.String(1024), nullable=False),
    )
    op.create_table(
        "resource",
        sa.Column("id", sa.Uuid(), primary_key=True),
        sa.Column("url", sa.String(1024), nullable=False),
        sa.Column("v1_url", sa.String(1024)),
        sa.Column("v2_url", sa.String(1024)),
        sa.Column("crop", sa.String(255)),
        sa.Column("email", sa.String(255)),
        sa.Column("frequency", sa.String(16), nullable=False),
        sa.Column("confirmed", sa.Boolean(), nullable=False),
        sa.Column("is_public", sa.Boolean(), nullable=False),
        sa.Column("submit_to_repo", sa.Boolean(), nullable=False),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("certificate", sa.String(1024), nullable=False),
        sa.Column("logo", sa.String(1024), nullable=False),
        sa.Column("provider_id", sa.Uuid(), sa.ForeignKey("provider.id", ondelete="SET NULL")),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint("frequency IN ('daily', 'weekly', 'monthly')", name="ck_resource_frequency"),
    )
    op.create_index("ix_resource_email", "resource", ["email"])
    op.create_index("ix_resource_is_public", "resource", ["is_public"])
    op.create_index("ix_resource_url_provider", "resource", ["url", "provider_id"])
    op.create_table(
        "test_report",
        sa.Column("id", sa.Uuid(), primary_key=True),
        sa.Column("resource_id", sa.Uuid(), sa.ForeignKey("resource.id", ondelete="CASCADE"), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("report", sa.Text(), nullable=False),
    )
    op.create_index("ix_test_report_resource_created", "test_report", ["resource_id", "created_at"])


def downgrade() -> None:
    op.drop_table("test_report")
    op.drop_table("resource")
    op.drop_table("provider")
