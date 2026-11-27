"""HTTP API and static frontend (Litestar)."""

from __future__ import annotations

import asyncio
import base64
import binascii
import logging
import re
import secrets
import time
import uuid
from collections.abc import AsyncGenerator, AsyncIterator
from contextlib import asynccontextmanager
from importlib.metadata import version as package_version
from typing import Any
from urllib.parse import urlsplit

import httpx
from litestar import Litestar, MediaType, Request, Response, Router, get, post
from litestar.config.cors import CORSConfig
from litestar.connection import ASGIConnection
from litestar.datastructures import State
from litestar.di import NamedDependency, Provide
from litestar.exceptions import HTTPException, ValidationException
from litestar.handlers.base import BaseRouteHandler
from litestar.openapi import OpenAPIConfig
from litestar.params import FromPath, FromQuery, JSONBody
from litestar.response import Redirect
from litestar.static_files import create_static_files_router
from litestar.status_codes import HTTP_500_INTERNAL_SERVER_ERROR
from sqlalchemy.ext.asyncio import AsyncSession

from brava import db, jobs, mailer, reports
from brava.collection import available_versions
from brava.config import Settings, get_settings
from brava.runner import run_suite

log = logging.getLogger(__name__)

Session = NamedDependency[AsyncSession]
OptQuery = FromQuery[str | None]
Body = JSONBody[dict[str, Any]]

_EMAIL_RE = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


class ApiError(Exception):
    """An error answered with the ``{"status", "message", "code"}`` body of the Java version."""

    def __init__(self, status: int, message: str, code: int, headers: dict[str, str] | None = None) -> None:
        super().__init__(message)
        self.status, self.message, self.code, self.headers = status, message, code, headers


def message(status: int, text: str, code: int, *, http_status: int | None = None) -> Response[dict[str, Any]]:
    return Response(
        {"status": status, "message": text, "code": code},
        status_code=http_status or status,
        media_type=MediaType.JSON,
    )


def _api_error_handler(_: Request, exc: ApiError) -> Response[dict[str, Any]]:
    resp = message(exc.status, exc.message, exc.code)
    if exc.headers:
        resp.headers.update(exc.headers)
    return resp


def _validation_handler(_: Request, exc: ValidationException) -> Response[dict[str, Any]]:
    return message(400, f"Invalid request: {exc.detail}", 4000)


def _http_error_handler(_: Request, exc: HTTPException) -> Response[dict[str, Any]]:
    if exc.status_code >= 500:
        log.error("Unhandled error", exc_info=exc)
        return message(exc.status_code, "internal server error", 5000)
    resp = message(exc.status_code, exc.detail, exc.status_code * 10)
    if exc.headers:
        resp.headers.update(exc.headers)
    return resp


def _server_error_handler(_: Request, exc: Exception) -> Response[dict[str, Any]]:
    if isinstance(exc, HTTPException):
        return _http_error_handler(_, exc)
    log.exception("Unhandled error", exc_info=exc)
    return message(500, "internal server error", 5000)


def _html(name: str, status: int = 200) -> Response[str]:
    return Response(mailer.render(name), media_type=MediaType.HTML, status_code=status)


def _parse_id(value: str | None, code: int, what: str = "endpoint") -> uuid.UUID:
    try:
        return uuid.UUID(str(value))
    except ValueError:
        raise ApiError(404, f"{what} not found", code) from None


def _valid_url(url: Any) -> bool:
    if not isinstance(url, str):
        return False
    try:
        parts = urlsplit(url.strip())
        _ = parts.port
    except ValueError:
        return False
    return parts.scheme in ("http", "https") and bool(parts.hostname)


def _check_version(version: str | None) -> str:
    if not version or version not in available_versions():
        raise ApiError(400, "Missing or invalid version parameter", 4202)
    return version


def _now_ms() -> int:
    return int(time.time() * 1000)


async def provide_session(state: State) -> AsyncGenerator[AsyncSession]:
    async with state.sessions() as session:
        yield session


# --- /test ---------------------------------------------------------------------------------


@get("/test/brapiversions", sync_to_thread=False)
def brapi_versions() -> list[str]:
    return available_versions()


@get("/test/call")
async def test_call(
    url: OptQuery = None, brapiversion: OptQuery = None, accessToken: OptQuery = None, strict: OptQuery = None
) -> dict[str, Any]:
    if not _valid_url(url):
        raise ApiError(400, "Missing or invalid url parameter", 4202)
    version = _check_version(brapiversion)
    try:
        report = await run_suite(url.strip(), version, token=accessToken, strict=strict == "on")
    except FileNotFoundError as e:
        log.error("%s", e)
        raise ApiError(500, "internal server error", 5201) from e
    return reports.report_view(report, None, _now_ms())


# --- stored reports and public resources -----------------------------------------------------


async def _report(session: AsyncSession, report_id: str) -> db.TestReport:
    row = await session.get(db.TestReport, _parse_id(report_id, 4300, "testReport"))
    if row is None:
        raise ApiError(404, "testReport not found", 4300)
    return row


@get("/testreport/{report_id:str}")
async def test_report(report_id: FromPath[str], session: Session) -> dict[str, Any]:
    return (await _report(session, report_id)).to_json()


@get("/shortreport/{report_id:str}/{fmt:str}")
async def short_report(report_id: FromPath[str], fmt: FromPath[str], session: Session) -> Response[Any]:
    fmt = fmt.lower()
    if fmt not in ("csv", "json"):
        raise ApiError(400, "Invalid format.", 4400)
    row = await _report(session, report_id)
    resource = await session.get(db.Resource, row.resource_id)
    name = resource.name if resource and resource.is_public else ""
    base = get_settings().base_domain
    link = f"{base}?report={row.id}" if base else None
    if fmt == "json":
        return Response(reports.short_summary(row.suite, name, link), media_type=MediaType.JSON)
    return Response(
        reports.short_summary_csv(row.suite, name, link),
        media_type="text/csv",
        headers={"Content-Disposition": 'attachment; filename="report.csv"'},
    )


@get("/public/resources")
async def public_resources(session: Session) -> list[dict[str, Any]]:
    return [r.to_json(last) for r, last in await db.public_resources(session)]


# --- /ci: registration of private resources ---------------------------------------------------


@post("/ci/resources", status_code=202)
async def register_resource(data: Body, session: Session) -> Response[dict[str, Any]]:
    url = next((data[k] for k in ("url", "base-url", "v2-url", "v1-url") if data.get(k)), None)
    if not _valid_url(url):
        raise ApiError(400, "Invalid url", 4106)
    url = url.strip()
    email = data.get("email")
    if not isinstance(email, str) or not _EMAIL_RE.match(email.strip()):
        raise ApiError(400, "Invalid email", 4100)
    email = email.strip()
    frequency = data.get("frequency") or "weekly"
    if frequency not in db.FREQUENCIES:
        raise ApiError(400, "Invalid frequency", 4103)

    existing = await db.find_private_duplicate(session, email, url, frequency)
    if existing is not None and existing.confirmed:
        raise ApiError(400, "Url already in use", 4101)
    if existing is not None:
        await mailer.send_confirmation(existing)
        return message(200, "We'll send you a confirmation email.", 2100, http_status=202)

    resource = db.Resource(
        url=url,
        v1_url=data.get("v1-url"),
        v2_url=data.get("v2-url"),
        crop=data.get("crop") or None,
        email=email,
        frequency=frequency,
        submit_to_repo=bool(data.get("submitToRepo")),
        name=data.get("name") or "",
        description=data.get("description") or "",
    )
    session.add(resource)
    await session.commit()
    await mailer.send_confirmation(resource)
    return message(200, "We'll send you a confirmation email.", 2101, http_status=202)


async def _private_resource(session: AsyncSession, key: str | None, code: int) -> db.Resource:
    resource = await session.get(db.Resource, _parse_id(key, code))
    if resource is None or resource.is_public:
        raise ApiError(404, "endpoint not found", code)
    return resource


@get("/ci/confirmation")
async def confirm(session: Session, key: OptQuery = None) -> Response[str]:
    resource = await _private_resource(session, key, 4104)
    if resource.confirmed:
        return _html("already-confirmed.html")
    resource.confirmed = True
    await session.commit()
    return _html("confirmation.html")


@get("/ci/unsubscribe")
async def unsubscribe(session: Session, key: OptQuery = None) -> Response[str]:
    resource = await _private_resource(session, key, 4105)
    await session.delete(resource)
    await session.commit()
    return _html("unsubscribe.html")


@get("/ci/resources/{resource_id:str}/frequency")
async def change_frequency(
    resource_id: FromPath[str], session: Session, frequency: OptQuery = None
) -> Response[str]:
    if frequency not in db.FREQUENCIES:
        raise ApiError(400, "Invalid frequency", 4103)
    resource = await _private_resource(session, resource_id, 4102)
    resource.frequency = frequency
    await session.commit()
    return _html("freq-updated.html")


# --- /admin ---------------------------------------------------------------------------------


def admin_guard(connection: ASGIConnection[Any, Any, Any, Any], _: BaseRouteHandler) -> None:
    key = get_settings().admin_key
    unauthorized = ApiError(401, "unauthorized", 4001, {"WWW-Authenticate": 'Basic realm="BRAVA admin"'})
    header = connection.headers.get("authorization", "")
    if not key or not header.lower().startswith("basic "):
        raise unauthorized
    try:
        decoded = base64.b64decode(header[6:].strip(), validate=True).decode()
    except binascii.Error, UnicodeDecodeError:
        raise unauthorized from None
    password = decoded.split(":", 1)[1] if ":" in decoded else decoded
    if not secrets.compare_digest(password.encode(), key.encode()):
        raise ApiError(401, "unauthorized", 4002, unauthorized.headers)


async def _provider_from(session: AsyncSession, value: Any) -> db.Provider | None:
    name = value.get("name") if isinstance(value, dict) else value
    return await db.provider_by_name(session, name) if isinstance(name, str) and name else None


def _public_resource(data: dict[str, Any]) -> db.Resource:
    fields = jobs._resource_fields(data)
    if not _valid_url(fields["url"]):
        raise ApiError(400, "Invalid url", 4106)
    return db.Resource(**fields, is_public=True, email=None)


@post("/admin/resources", guards=[admin_guard], status_code=202)
async def add_public_resource(data: Body, session: Session) -> Response[dict[str, Any]]:
    resource = _public_resource(data)
    provider = await _provider_from(session, data.get("provider"))
    resource.provider_id = provider.id if provider else None
    session.add(resource)
    await session.commit()
    return message(200, "Public endpoint added.", 2101, http_status=202)


@post("/admin/providers", guards=[admin_guard], status_code=202)
async def add_provider(data: Body, session: Session) -> Response[dict[str, Any]]:
    name = data.get("name")
    if not isinstance(name, str) or not name.strip():
        raise ApiError(400, "Invalid provider name", 4003)
    if await db.provider_by_name(session, name):
        raise ApiError(400, "Provider already exists", 4004)
    provider = db.Provider(name=name, description=data.get("description") or "", logo=data.get("logo") or "")
    session.add(provider)
    await session.flush()
    for entry in data.get("resources") or []:
        resource = _public_resource(entry)
        resource.provider_id = provider.id
        session.add(resource)
    await session.commit()
    return message(200, "Provider added.", 2102, http_status=202)


@get("/admin/testallpublic", guards=[admin_guard], status_code=202)
async def test_all_public(state: State, version: OptQuery = None) -> Response[dict[str, Any]]:
    version = _check_version(version)
    task = asyncio.create_task(jobs.run_public(state.sessions, strict=False, version=version))
    state.tasks.add(task)
    task.add_done_callback(state.tasks.discard)
    return message(202, "Testing of all public resources started.", 2003)


@get("/admin/updateproviders", guards=[admin_guard])
async def update_providers(state: State) -> Response[dict[str, Any]]:
    try:
        count = await jobs.update_providers(state.sessions)
    except (httpx.HTTPError, ValueError) as e:
        log.error("Loading %s failed: %s", get_settings().resource_json_url, e)
        raise ApiError(502, "Could not load the resource list", 5004) from e
    return message(200, f"{count} resources updated.", 2000)


# --- app ------------------------------------------------------------------------------------


def _index_html(settings: Settings) -> str:
    html = (settings.frontend_dir / "index.html").read_text(encoding="utf-8")
    return (
        html.replace("${buildDate}", settings.build_date or "-")
        .replace("${git.commit.id.abbrev}", settings.build_version or package_version("brava"))
        # Lets the frontend hide features that need advanced mode (resource list, reports, scheduling).
        .replace("${advancedMode}", "true" if settings.advanced_mode else "false")
    )


def create_app(settings: Settings | None = None) -> Litestar:
    settings = settings or get_settings()
    index = _index_html(settings) if (settings.frontend_dir / "index.html").is_file() else None
    advanced = settings.advanced_mode

    @get("/", include_in_schema=False)
    async def frontend_index(request: Request) -> Response[str]:
        raw_path = request.scope.get("raw_path", b"").decode() or request.url.path
        if settings.base_path and not raw_path.endswith("/"):
            # The frontend uses relative URLs ("api/..."), which need the trailing slash.
            return Redirect(raw_path + "/")  # type: ignore[return-value]
        if index is None:
            raise ApiError(404, "frontend not found", 4040)
        return Response(index, media_type=MediaType.HTML)

    db_handlers = [test_report, short_report, public_resources]
    api_handlers: list[Any] = [brapi_versions, test_call]
    if advanced:
        api_handlers += [
            *db_handlers, register_resource, confirm, unsubscribe, change_frequency,
            add_public_resource, add_provider, test_all_public, update_providers,
        ]  # fmt: skip
    else:

        @get(["/testreport/{p:path}", "/shortreport/{p:path}", "/public/resources"], sync_to_thread=False)
        def not_available() -> None:
            raise ApiError(501, "Not available: advanced mode is off", 5010)

        api_handlers.append(not_available)

    api = Router(path="/api", route_handlers=api_handlers, dependencies={"session": Provide(provide_session)})
    handlers: list[Any] = [
        api,
        frontend_index,
        create_static_files_router(
            path="/schemas", directories=[settings.schemas_dir], include_in_schema=False
        ),
    ]
    if settings.frontend_dir.is_dir():
        handlers.append(
            create_static_files_router(path="/", directories=[settings.frontend_dir], include_in_schema=False)
        )

    @asynccontextmanager
    async def lifespan(app: Litestar) -> AsyncIterator[None]:
        app.state.tasks = set()
        app.state.sessions = None
        scheduler = None
        engine = None
        if advanced:
            await db.upgrade_schema(settings.database_url)
            engine = db.make_engine(settings.database_url)
            app.state.sessions = db.make_sessionmaker(engine)
            scheduler = jobs.create_scheduler(app.state.sessions)
            scheduler.start()
        try:
            yield
        finally:
            if scheduler:
                scheduler.shutdown(wait=False)
            for task in list(app.state.tasks):
                task.cancel()
            if engine:
                await engine.dispose()

    root = Router(path=settings.base_path or "/", route_handlers=handlers)
    app_handlers: list[Any] = [root]
    if settings.base_path:

        @get("/", include_in_schema=False, sync_to_thread=False)
        def to_base_path() -> Redirect:
            return Redirect(settings.base_path + "/")

        app_handlers.append(to_base_path)

    return Litestar(
        route_handlers=app_handlers,
        lifespan=[lifespan],
        cors_config=CORSConfig(
            allow_origins=["*"],
            allow_methods=["GET", "POST"],
            allow_headers=["origin", "content-type", "accept", "authorization", "x-requested-with"],
        ),
        exception_handlers={
            ApiError: _api_error_handler,
            ValidationException: _validation_handler,
            HTTPException: _http_error_handler,
            HTTP_500_INTERNAL_SERVER_ERROR: _server_error_handler,
        },
        openapi_config=OpenAPIConfig(
            title="BRAVA API", version=package_version("brava"), path=f"{settings.base_path}/api/schema"
        ),
        debug=False,
    )
