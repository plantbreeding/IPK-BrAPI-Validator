"""One-off copy of the data in the Java version's database into the new schema.

The legacy tables (``resources``, ``providers``, ``testreports``; upper-case on Oracle) are
reflected, so table and column name casing does not matter.
"""

from __future__ import annotations

import logging
import uuid
from datetime import UTC, datetime
from typing import Any
from zoneinfo import ZoneInfo

from sqlalchemy import MetaData, Table, create_engine, inspect, select
from sqlalchemy.engine import Connection
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from brava.db import Provider, Resource, TestReport

log = logging.getLogger(__name__)


def _table(conn: Connection, name: str) -> Table:
    names = {n.lower(): n for n in inspect(conn).get_table_names()}
    if name not in names:
        raise LookupError(f"Legacy table {name!r} not found")
    return Table(names[name], MetaData(), autoload_with=conn)


def _rows(conn: Connection, name: str) -> list[dict[str, Any]]:
    table = _table(conn, name)
    return [{k.lower(): v for k, v in row._mapping.items()} for row in conn.execute(select(table))]


def _bool(value: Any) -> bool:
    if isinstance(value, str):
        return value.strip().lower() in ("1", "true", "t", "y")
    return bool(value)


def _uuid(value: Any) -> uuid.UUID | None:
    try:
        return uuid.UUID(str(value).strip()) if value is not None else None
    except ValueError:
        return None


def _text(value: Any) -> str:
    if value is None:
        return ""
    if hasattr(value, "read"):  # Oracle LOBs
        value = value.read()
    return str(value)


def read_legacy(source_url: str) -> dict[str, list[dict[str, Any]]]:
    engine = create_engine(source_url)
    try:
        with engine.connect() as conn:
            return {n: _rows(conn, n) for n in ("providers", "resources", "testreports")}
    finally:
        engine.dispose()


async def migrate_legacy(
    source_url: str, sessions: async_sessionmaker[AsyncSession], timezone: str = "UTC"
) -> dict[str, int]:
    """Copy providers, resources and reports. Existing ids are skipped, so it can be re-run."""
    data = read_legacy(source_url)
    tz = ZoneInfo(timezone)
    counts = dict.fromkeys(
        (
            "providers",
            "resources",
            "reports",
            "skipped_existing",
            "skipped_orphan_reports",
            "skipped_invalid",
        ),
        0,
    )

    async with sessions.begin() as session:
        known_providers: set[uuid.UUID] = set()
        for row in data["providers"]:
            pid = _uuid(row.get("id"))
            if pid is None:
                counts["skipped_invalid"] += 1
                continue
            known_providers.add(pid)
            if await session.get(Provider, pid):
                counts["skipped_existing"] += 1
                continue
            session.add(
                Provider(
                    id=pid,
                    name=_text(row.get("name")) or str(pid),
                    description=_text(row.get("description")),
                    logo=_text(row.get("logo")),
                )
            )
            counts["providers"] += 1
        await session.flush()

        known_resources: set[uuid.UUID] = set()
        for row in data["resources"]:
            rid = _uuid(row.get("id"))
            url = row.get("url") or row.get("v1_url")  # the Java version never wrote URL
            if rid is None or not url:
                counts["skipped_invalid"] += 1
                continue
            known_resources.add(rid)
            if await session.get(Resource, rid):
                counts["skipped_existing"] += 1
                continue
            provider_id = _uuid(row.get("provider"))
            frequency = (row.get("frequency") or "weekly").lower()
            session.add(
                Resource(
                    id=rid,
                    url=url,
                    v1_url=row.get("v1_url"),
                    v2_url=row.get("v2_url"),
                    crop=row.get("crop"),
                    email=row.get("email"),
                    frequency=frequency if frequency in ("daily", "weekly", "monthly") else "weekly",
                    confirmed=_bool(row.get("confirmed")),
                    is_public=_bool(row.get("ispublic")),
                    submit_to_repo=_bool(row.get("submit_to_repo")),
                    name=_text(row.get("name")),
                    description=_text(row.get("description")),
                    certificate=_text(row.get("certificate")),
                    logo=_text(row.get("logo")),
                    provider_id=provider_id if provider_id in known_providers else None,
                )
            )
            counts["resources"] += 1
        await session.flush()

        for row in data["testreports"]:
            rep_id, res_id = _uuid(row.get("reportid")), _uuid(row.get("resourceid"))
            if rep_id is None or res_id is None:
                counts["skipped_invalid"] += 1
                continue
            if res_id not in known_resources:
                counts["skipped_orphan_reports"] += 1
                continue
            if await session.get(TestReport, rep_id):
                counts["skipped_existing"] += 1
                continue
            created = row.get("date")
            if not isinstance(created, datetime):
                created = datetime.now(UTC)
            elif created.tzinfo is None:
                created = created.replace(tzinfo=tz)
            session.add(
                TestReport(
                    id=rep_id, resource_id=res_id, created_at=created, report=_text(row.get("reportjson"))
                )
            )
            counts["reports"] += 1

    log.info("Legacy migration finished: %s", counts)
    return counts
