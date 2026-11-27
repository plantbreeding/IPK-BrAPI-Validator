from __future__ import annotations

import json
from collections.abc import Iterator
from pathlib import Path
from typing import Any

import httpx
import pytest
import respx

from brava import collection, config, validation

BASE = "https://brapi.test/brapi/v2"
VERSION = "v9.9"


def _item(
    name: str, endpoint: str, url: str, exec_: list[str], method: str = "GET", **extra: Any
) -> dict[str, Any]:
    return {
        "name": name,
        "endpoint": endpoint,
        "request": {"method": method, "url": url},
        "event": [{"listen": "test", "type": "text/plain", "exec": exec_}],
        "requires": extra.pop("requires", []),
        "parameters": extra.pop("parameters", []),
    }


COLLECTION = {
    "info": {"name": "Test collection"},
    "item": [
        {
            "name": "Server Info",
            "item": [
                _item(
                    "GET /serverinfo",
                    "/serverinfo",
                    "{baseurl}/serverinfo",
                    [
                        "StatusCode:200:breakiffalse",
                        "ContentType:application/json",
                        f"Schema:/{VERSION}/serverinfo",
                        "SaveCalls:V2",
                    ],
                )  # fmt: skip
            ],
        },
        {
            "name": "Programs",
            "item": [
                _item(
                    "GET /programs",
                    "/programs",
                    "{baseurl}/programs",
                    [
                        "StatusCode:200",
                        f"Schema:/{VERSION}/programs",
                        "GetValue:/result/data/0/programDbId:programDbId0",
                    ],
                    parameters=[{"param": "json", "value": ""}],
                ),
                _item(
                    "GET /programs/{programDbId}",
                    "/programs/{programDbId}",
                    "{baseurl}/programs/{programDbId0}",
                    ["StatusCode:200", "IsEqual:/result/programDbId:programDbId0"],
                    requires=["programDbId0"],
                ),
                _item("GET /trials", "/trials", "{baseurl}/trials", ["StatusCode:200"]),
                _item("GET /needs", "/needs", "{baseurl}/needs", ["StatusCode:200"], requires=["nothere"]),
                _item(
                    "POST /search/programs",
                    "/search/programs",
                    "{baseurl}/search/programs",
                    ["StatusCode:202,200:breakiffalse", f"SearchSchema:/{VERSION}/search:searchId"],
                    method="POST",
                    parameters=[{"param": "json", "value": '{"programDbIds": [${programDbId0}]}'}],
                ),
                _item(
                    "GET /search/programs/{searchResultsDbId}",
                    "/search/programs/{searchResultsDbId}",
                    "{baseurl}/search/programs/{searchId}",
                    ["StatusCode:202,200", f"SearchSchema:/{VERSION}/search:searchId"],
                    requires=["searchId"],
                ),
                _item(
                    "GET /broken",
                    "/broken",
                    "{baseurl}/broken",
                    ["StatusCode:200:breakiffalse", "ContentType:json"],
                ),
                _item(
                    "GET /plain",
                    "/plain",
                    "{baseurl}/plain",
                    ["StatusCode:200", "ContentType:application/json"],
                ),
                _item(
                    "GET /odd", "/odd", "{baseurl}/odd", ["StatusCode", "Frobnicate:x", "GetValue:/result"]
                ),
            ],
        },
    ],
}

SCHEMAS = {
    "serverinfo.json": {"type": "object", "required": ["result"]},
    "programs.json": {
        "type": "object",
        "required": ["result"],
        "additionalProperties": False,
        "properties": {
            "metadata": {"type": "object"},
            "result": {
                "type": "object",
                "properties": {
                    "data": {
                        "type": "array",
                        "minItems": 1,
                        "items": {"$ref": "definitions.json#/definitions/program"},
                    }
                },
            },
        },
    },
    "definitions.json": {
        "definitions": {
            "program": {
                "type": "object",
                "required": ["programDbId", "programName"],
                "properties": {
                    "programDbId": {"type": "string"},
                    "documentationURL": {"type": "string", "format": "uri"},
                },
            }
        }
    },
    "search.json": {"type": "object", "required": ["result"]},
    "search202.json": {
        "type": "object",
        "required": ["result"],
        "properties": {"result": {"type": "object", "required": ["searchResultsDbId"]}},
    },
}


def _reset_caches() -> None:
    config.get_settings.cache_clear()
    collection.load_collection.cache_clear()
    validation._validator.cache_clear()


@pytest.fixture
def resources_dir(tmp_path: Path) -> Path:
    root = tmp_path / "resources"
    (root / "collections").mkdir(parents=True)
    (root / "collections" / f"CompleteBrapiTest.{VERSION}.json").write_text(json.dumps(COLLECTION))
    schemas = root / "schemas" / VERSION
    schemas.mkdir(parents=True)
    for name, schema in SCHEMAS.items():
        (schemas / name).write_text(json.dumps(schema))
    return root


@pytest.fixture
def settings(
    monkeypatch: pytest.MonkeyPatch, resources_dir: Path, tmp_path: Path
) -> Iterator[config.Settings]:
    frontend = tmp_path / "frontend"
    frontend.mkdir()
    (frontend / "index.html").write_text(
        "<html>build ${buildDate} ${git.commit.id.abbrev} advanced=${advancedMode}</html>"
    )
    (frontend / "app.js").write_text("// js")
    env = {
        "BRAVA_RESOURCES_DIR": str(resources_dir),
        "BRAVA_FRONTEND_DIR": str(frontend),
        "BRAVA_DATABASE_URL": f"sqlite+aiosqlite:///{(tmp_path / 'brava.db').as_posix()}",
        "BRAVA_ADVANCED_MODE": "true",
        "BRAVA_ADMIN_KEY": "secret",
        "BRAVA_BASE_DOMAIN": "http://brava.test/brapivalidator/",
        "BRAVA_TESTED_VERSION": VERSION,
        "BRAVA_BUILD_DATE": "2026-11-26",
        "BRAVA_AUTO_IMPORT_RESOURCES": "false",  # no requests to brapi.org on app startup
    }
    for key, value in env.items():
        monkeypatch.setenv(key, value)
    monkeypatch.chdir(tmp_path)  # no stray .env
    _reset_caches()
    yield config.get_settings()
    _reset_caches()


SERVERINFO = {
    "metadata": {},
    "result": {"calls": [{"service": s} for s in (
        "programs", "programs/{programDbId}", "needs", "search/programs",
        "search/programs/{searchResultsDbId}", "broken", "plain", "odd",
    )]},
}  # fmt: skip

PROGRAMS = {
    "metadata": {},
    "result": {"data": [{"programDbId": "p1", "programName": "One", "documentationURL": "docs/p1"}]},
    "extra": 1,
}


@pytest.fixture
def fake_server() -> Iterator[respx.MockRouter]:
    with respx.mock(base_url=BASE, assert_all_called=False, assert_all_mocked=False) as router:
        router.get("/serverinfo").respond(json=SERVERINFO)
        router.get("/programs").respond(json=PROGRAMS)
        router.get("/programs/p1").respond(json={"result": {"programDbId": "p1"}})
        router.post("/search/programs").respond(202, json={"result": {"searchResultsDbId": "s1"}})
        router.get("/search/programs/s1").respond(200, json={"result": {"data": []}})
        router.get("/broken").respond(500, text="boom")
        router.get("/plain").respond(200, text="hello", headers={"content-type": "text/plain"})
        router.get("/odd").respond(json={"result": {}})
        yield router


def make_report_client() -> httpx.AsyncClient:
    return httpx.AsyncClient()
