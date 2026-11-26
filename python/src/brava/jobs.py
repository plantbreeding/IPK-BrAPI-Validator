"""Scheduled and administrative test runs (advanced mode)."""

from __future__ import annotations

import asyncio
import logging
from typing import Any

import httpx
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from brava import db, mailer, reports
from brava.config import get_settings
from brava.runner import run_suite

log = logging.getLogger(__name__)


def url_for_version(resource: db.Resource, version: str) -> str:
    """The resource's base URL for a BrAPI major version (public resources list v1 and v2 URLs)."""
    preferred = resource.v1_url if version.startswith("v1") else resource.v2_url
    return preferred or resource.url


async def test_and_store(
    sessions: async_sessionmaker[AsyncSession],
    resource: db.Resource,
    version: str,
    *,
    strict: bool,
    send_mail: bool,
) -> str:
    """Test one resource, store the report, prune old reports and optionally mail the result."""
    s = get_settings()
    report = await run_suite(
        url_for_version(resource, version),
        version,
        strict=strict,
        scheduled=True,
        resource_id=str(resource.id),
    )
    async with sessions.begin() as session:
        previous = await db.last_reports(session, resource.id, s.keep_reports)
        row = db.TestReport(resource_id=resource.id, report=reports.encode(report))
        session.add(row)
        await session.flush()
        await db.prune_reports(session, resource.id, s.keep_reports + 1)
    if send_mail:
        await mailer.send_report(resource, report, str(row.id), previous)
    return str(row.id)


async def _test_many(
    sessions: async_sessionmaker[AsyncSession],
    resources: list[db.Resource],
    *,
    strict: bool,
    send_mail: bool,
    version: str | None = None,
) -> int:
    s = get_settings()
    limit = asyncio.Semaphore(s.scheduler_concurrency)
    version = version or s.tested_version

    async def one(resource: db.Resource) -> bool:
        async with limit:
            try:
                await test_and_store(sessions, resource, version, strict=strict, send_mail=send_mail)
                return True
            except Exception:  # one broken resource must not stop the others
                log.exception("Testing resource %s (%s) failed", resource.id, resource.url)
                return False

    results = await asyncio.gather(*(one(r) for r in resources))
    return sum(results)


async def run_scheduled(sessions: async_sessionmaker[AsyncSession], frequency: str) -> int:
    async with sessions() as session:
        resources = list(await db.resources_to_test(session, frequency))
    done = await _test_many(sessions, resources, strict=True, send_mail=True)
    log.info("Scheduled %s run: %d/%d resources tested", frequency, done, len(resources))
    return done


async def run_public(
    sessions: async_sessionmaker[AsyncSession], *, strict: bool = True, version: str | None = None
) -> int:
    async with sessions() as session:
        resources = list(await db.all_public(session))
    done = await _test_many(sessions, resources, strict=strict, send_mail=False, version=version)
    log.info("Public resources run: %d/%d resources tested", done, len(resources))
    return done


async def _weekly(sessions: async_sessionmaker[AsyncSession]) -> None:
    await run_scheduled(sessions, "weekly")
    await run_public(sessions)


def create_scheduler(sessions: async_sessionmaker[AsyncSession]) -> AsyncIOScheduler:
    tz = get_settings().timezone
    scheduler = AsyncIOScheduler(timezone=tz) if tz else AsyncIOScheduler()
    jobs: list[tuple[Any, dict[str, Any], dict[str, Any]]] = [
        (run_scheduled, {"args": [sessions, "daily"]}, {"hour": 8, "minute": 0}),
        (_weekly, {"args": [sessions]}, {"day_of_week": "mon", "hour": 8, "minute": 0}),
        (run_scheduled, {"args": [sessions, "monthly"]}, {"day": 1, "hour": 8, "minute": 0}),
    ]
    for func, kwargs, cron in jobs:
        scheduler.add_job(
            func, CronTrigger(**cron, timezone=scheduler.timezone), misfire_grace_time=3600, **kwargs
        )
    return scheduler


# --- provider list from brapi.org ------------------------------------------------------------


def _resource_fields(entry: dict[str, Any]) -> dict[str, Any]:
    v1, v2 = entry.get("v1-url"), entry.get("v2-url")
    return {
        "url": entry.get("url") or v2 or v1 or "",
        "v1_url": v1,
        "v2_url": v2,
        "name": entry.get("name") or "",
        "description": entry.get("description") or "",
        "certificate": entry.get("certificate") or "",
        "logo": entry.get("logo") or "",
        "crop": entry.get("crop"),
    }


async def update_providers(sessions: async_sessionmaker[AsyncSession]) -> int:
    """Upsert providers and their public resources from ``BRAVA_RESOURCE_JSON_URL``."""
    s = get_settings()
    async with httpx.AsyncClient(proxy=s.http_proxy or None, timeout=60, follow_redirects=True) as client:
        response = await client.get(s.resource_json_url)
        response.raise_for_status()
        data = response.json()
    providers = data if isinstance(data, list) else [data]
    count = 0
    async with sessions.begin() as session:
        for p in providers:
            if not isinstance(p, dict) or not p.get("name"):
                continue
            provider = await db.provider_by_name(session, p["name"])
            if provider is None:
                provider = db.Provider(name=p["name"])
                session.add(provider)
            provider.description = p.get("description") or ""
            provider.logo = p.get("logo") or ""
            await session.flush()
            for entry in p.get("resources") or []:
                fields = _resource_fields(entry)
                if not fields["url"]:
                    continue
                resource = await db.public_resource_by_url(session, provider.id, fields["url"])
                if resource is None:
                    resource = db.Resource(provider_id=provider.id, is_public=True, confirmed=False)
                    session.add(resource)
                for key, value in fields.items():
                    setattr(resource, key, value)
                count += 1
    return count
