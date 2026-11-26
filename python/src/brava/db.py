"""Database models and queries (advanced mode)."""

from __future__ import annotations

import asyncio
import uuid
from collections.abc import Sequence
from datetime import UTC, datetime
from typing import Any

from sqlalchemy import (
    Boolean,
    CheckConstraint,
    DateTime,
    ForeignKey,
    Index,
    String,
    Text,
    Uuid,
    delete,
    select,
)
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship, selectinload

from brava import reports

FREQUENCIES = ("daily", "weekly", "monthly")


def _now() -> datetime:
    return datetime.now(UTC)


class Base(DeclarativeBase):
    pass


class Provider(Base):
    __tablename__ = "provider"

    id: Mapped[uuid.UUID] = mapped_column(Uuid, primary_key=True, default=uuid.uuid4)
    name: Mapped[str] = mapped_column(String(255), unique=True)
    description: Mapped[str] = mapped_column(Text, default="")
    logo: Mapped[str] = mapped_column(String(1024), default="")

    resources: Mapped[list[Resource]] = relationship(back_populates="provider")

    def to_json(self) -> dict[str, Any]:
        return {"id": str(self.id), "name": self.name, "description": self.description, "logo": self.logo}


class Resource(Base):
    __tablename__ = "resource"
    __table_args__ = (
        CheckConstraint("frequency IN ('daily', 'weekly', 'monthly')", name="ck_resource_frequency"),
        Index("ix_resource_url_provider", "url", "provider_id"),
    )

    id: Mapped[uuid.UUID] = mapped_column(Uuid, primary_key=True, default=uuid.uuid4)
    url: Mapped[str] = mapped_column(String(1024))
    v1_url: Mapped[str | None] = mapped_column(String(1024))
    v2_url: Mapped[str | None] = mapped_column(String(1024))
    crop: Mapped[str | None] = mapped_column(String(255))
    email: Mapped[str | None] = mapped_column(String(255), index=True)
    frequency: Mapped[str] = mapped_column(String(16), default="weekly")
    confirmed: Mapped[bool] = mapped_column(Boolean, default=False)
    is_public: Mapped[bool] = mapped_column(Boolean, default=False, index=True)
    submit_to_repo: Mapped[bool] = mapped_column(Boolean, default=False)
    name: Mapped[str] = mapped_column(String(255), default="")
    description: Mapped[str] = mapped_column(Text, default="")
    certificate: Mapped[str] = mapped_column(String(1024), default="")
    logo: Mapped[str] = mapped_column(String(1024), default="")
    provider_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("provider.id", ondelete="SET NULL"))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)

    provider: Mapped[Provider | None] = relationship(back_populates="resources", lazy="joined")
    reports: Mapped[list[TestReport]] = relationship(
        back_populates="resource",
        cascade="all, delete-orphan",
        passive_deletes=True,
        order_by="TestReport.created_at.desc()",
    )

    def to_json(self, last_reports: Sequence[TestReport] = ()) -> dict[str, Any]:
        """Same keys as the Java ``Resource`` JSON (email and flags are not exposed)."""
        return {
            "id": str(self.id),
            "url": self.url,
            "v1-url": self.v1_url,
            "v2-url": self.v2_url,
            "access-token": None,
            "crop": self.crop,
            "frequency": self.frequency,
            "name": self.name,
            "description": self.description,
            "certificate": self.certificate,
            "logo": self.logo,
            "provider": self.provider.to_json() if self.provider else None,
            "lastTestReports": [r.to_json() for r in last_reports],
        }


class TestReport(Base):
    __tablename__ = "test_report"
    __table_args__ = (Index("ix_test_report_resource_created", "resource_id", "created_at"),)

    id: Mapped[uuid.UUID] = mapped_column(Uuid, primary_key=True, default=uuid.uuid4)
    resource_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("resource.id", ondelete="CASCADE"))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)
    report: Mapped[str] = mapped_column(Text)

    resource: Mapped[Resource] = relationship(back_populates="reports")

    @property
    def suite(self) -> reports.SuiteReport:
        return reports.decode(self.report)

    @property
    def date_ms(self) -> int:
        created = self.created_at if self.created_at.tzinfo else self.created_at.replace(tzinfo=UTC)
        return int(created.timestamp() * 1000)

    def to_json(self) -> dict[str, Any]:
        return reports.report_view(self.suite, str(self.id), self.date_ms)


# --- engine / sessions ---------------------------------------------------------------------


def make_engine(url: str) -> AsyncEngine:
    engine = create_async_engine(url)
    if engine.dialect.name == "sqlite":
        from sqlalchemy import event

        @event.listens_for(engine.sync_engine, "connect")
        def _fk_on(dbapi_conn: Any, _: Any) -> None:  # SQLite needs this for ON DELETE CASCADE
            dbapi_conn.execute("PRAGMA foreign_keys=ON")

    return engine


def make_sessionmaker(engine: AsyncEngine) -> async_sessionmaker[AsyncSession]:
    return async_sessionmaker(engine, expire_on_commit=False)


async def create_schema(engine: AsyncEngine) -> None:
    """Create missing tables without Alembic (used by tests)."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


def _alembic_upgrade(url: str) -> None:
    from alembic import command
    from alembic.config import Config

    cfg = Config()
    cfg.set_main_option("script_location", "brava:migrations")
    cfg.set_main_option("sqlalchemy.url", url.replace("%", "%%"))
    command.upgrade(cfg, "head")


async def upgrade_schema(url: str) -> None:
    """Apply the Alembic migrations (runs in a thread because env.py has its own event loop)."""
    await asyncio.to_thread(_alembic_upgrade, url)


# --- queries -------------------------------------------------------------------------------


async def public_resources(session: AsyncSession, last: int = 3) -> list[tuple[Resource, list[TestReport]]]:
    rows = (
        await session.scalars(
            select(Resource)
            .where(Resource.is_public)
            .options(selectinload(Resource.reports))
            .order_by(Resource.name)
        )
    ).all()
    return [(r, list(r.reports[:last])) for r in rows]


async def resources_to_test(session: AsyncSession, frequency: str) -> Sequence[Resource]:
    return (
        await session.scalars(
            select(Resource).where(Resource.frequency == frequency, Resource.confirmed, ~Resource.is_public)
        )
    ).all()


async def all_public(session: AsyncSession) -> Sequence[Resource]:
    return (await session.scalars(select(Resource).where(Resource.is_public))).all()


async def find_private_duplicate(
    session: AsyncSession, email: str, url: str, frequency: str
) -> Resource | None:
    return await session.scalar(
        select(Resource)
        .where(
            Resource.email == email, Resource.url == url, Resource.frequency == frequency, ~Resource.is_public
        )
        .limit(1)
    )


async def last_reports(session: AsyncSession, resource_id: uuid.UUID, n: int) -> Sequence[TestReport]:
    return (
        await session.scalars(
            select(TestReport)
            .where(TestReport.resource_id == resource_id)
            .order_by(TestReport.created_at.desc())
            .limit(n)
        )
    ).all()


async def prune_reports(session: AsyncSession, resource_id: uuid.UUID, keep: int) -> None:
    """Delete all but the ``keep`` newest reports of a resource."""
    keep_ids = select(TestReport.id).where(TestReport.resource_id == resource_id)
    keep_ids = keep_ids.order_by(TestReport.created_at.desc()).limit(keep)
    ids = (await session.scalars(keep_ids)).all()
    await session.execute(
        delete(TestReport).where(TestReport.resource_id == resource_id, TestReport.id.not_in(ids))
    )


async def provider_by_name(session: AsyncSession, name: str) -> Provider | None:
    return await session.scalar(select(Provider).where(Provider.name == name))


async def public_resource_by_url(session: AsyncSession, provider_id: uuid.UUID, url: str) -> Resource | None:
    return await session.scalar(
        select(Resource)
        .where(Resource.provider_id == provider_id, Resource.url == url, Resource.is_public)
        .limit(1)
    )
