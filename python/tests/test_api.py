from __future__ import annotations

import asyncio
import base64
import re
from collections.abc import AsyncIterator
from typing import Any

import pytest
from litestar.testing import AsyncTestClient

from brava import jobs, mailer
from brava.api import create_app
from brava.config import get_settings
from tests.conftest import BASE, VERSION

API = "/brapivalidator/api"
RESOURCE_LIST = "https://brapi.org/json/brapi-resources.json"
ADMIN = {"Authorization": "Basic " + base64.b64encode(b"admin:secret").decode()}


@pytest.fixture
def sent(monkeypatch: pytest.MonkeyPatch) -> list[dict[str, str]]:
    mails: list[dict[str, str]] = []

    async def fake_send(to: str, subject: str, html: str) -> bool:
        mails.append({"to": to, "subject": subject, "html": html})
        return True

    monkeypatch.setattr(mailer, "send", fake_send)
    return mails


@pytest.fixture
async def client(settings, sent) -> AsyncIterator[AsyncTestClient[Any]]:
    async with AsyncTestClient(create_app()) as c:
        yield c


async def test_frontend_and_static(client) -> None:
    r = await client.get("/brapivalidator/")
    assert r.status_code == 200 and "build 2026-11-26" in r.text and "${" not in r.text
    assert "advanced=true" in r.text
    r = await client.get("/brapivalidator", follow_redirects=False)
    assert r.status_code in (301, 302, 307, 308) and r.headers["location"].endswith("/brapivalidator/")
    assert (await client.get("/brapivalidator/app.js")).status_code == 200
    assert (await client.get(f"/brapivalidator/schemas/{VERSION}/search.json")).json()["required"] == [
        "result"
    ]


async def test_versions_and_call(client, fake_server) -> None:
    assert (await client.get(f"{API}/test/brapiversions")).json() == [VERSION]

    r = await client.get(f"{API}/test/call", params={"url": BASE, "brapiversion": VERSION, "strict": "on"})
    assert r.status_code == 200
    body = r.json()
    assert body["reportId"] is None and body["resourceUrl"] == BASE and isinstance(body["date"], int)
    assert body["shortReport"]["Programs"]["GET /trials"] == "skipped"
    assert body["shortReport"]["Programs"]["GET /programs"]["testStatus"] == ["schema mismatch"]
    assert set(body["miniReport"]) == {"time", "totalTests", "passedTests", "failedTests", "warningTests"}


@pytest.mark.parametrize(
    ("params", "message"),
    [
        ({"brapiversion": VERSION}, "Missing or invalid url parameter"),
        ({"url": "not a url", "brapiversion": VERSION}, "Missing or invalid url parameter"),
        ({"url": BASE, "brapiversion": "v0"}, "Missing or invalid version parameter"),
    ],
)
async def test_call_errors(client, params, message) -> None:
    r = await client.get(f"{API}/test/call", params=params)
    assert r.status_code == 400 and r.json() == {"status": 400, "message": message, "code": 4202}


async def test_registration_flow(client, sent, fake_server, settings) -> None:
    payload = {"base-url": BASE, "email": "user@example.org", "frequency": "daily", "crop": "wheat"}
    r = await client.post(f"{API}/ci/resources", json=payload)
    assert r.status_code == 202 and r.json()["code"] == 2101
    assert len(sent) == 1 and sent[0]["subject"] == "BrAVA: Confirm your email"
    key = re.search(r"confirmation\?key=([0-9a-f-]+)", sent[0]["html"])[1]
    assert "http://brava.test/brapivalidator/api/ci/confirmation" in sent[0]["html"]  # no "//api"

    # unconfirmed duplicate -> confirmation is sent again
    r = await client.post(f"{API}/ci/resources", json=payload)
    assert r.status_code == 202 and r.json()["code"] == 2100 and len(sent) == 2

    r = await client.get(f"{API}/ci/confirmation", params={"key": key})
    assert r.status_code == 200 and "has been confirmed" in r.text
    r = await client.get(f"{API}/ci/confirmation", params={"key": key})
    assert "already confirmed" in r.text

    r = await client.post(f"{API}/ci/resources", json=payload)
    assert r.status_code == 400 and r.json()["code"] == 4101

    # the scheduled daily run tests it, stores a report and mails it
    sessions = client.app.state.sessions
    assert await jobs.run_scheduled(sessions, "daily") == 1
    report_mail = sent[-1]
    assert report_mail["subject"].startswith(
        "[BrAVa - Report for scheduled BrAPI test] Passed tests: 4/8"
    )  # strict: /programs has an extra property
    assert f"/api/ci/resources/{key}/frequency?frequency=weekly" in report_mail["html"]
    report_id = re.search(r"\?report=([0-9a-f-]+)", report_mail["html"])[1]

    r = await client.get(f"{API}/testreport/{report_id}")
    assert r.status_code == 200 and r.json()["reportId"] == report_id
    r = await client.get(f"{API}/shortreport/{report_id}/json")
    assert r.json()["linkFullTest"] == f"http://brava.test/brapivalidator?report={report_id}"
    r = await client.get(f"{API}/shortreport/{report_id}/csv")
    assert r.headers["content-type"].startswith("text/csv") and r.text.startswith("resourceName\t")
    assert (await client.get(f"{API}/shortreport/{report_id}/xml")).json()["code"] == 4400

    # retention: keep_reports (3) previous + the newest
    for _ in range(5):
        await jobs.run_scheduled(sessions, "daily")
    assert "Older reports" in sent[-1]["html"]
    async with sessions() as s:
        from brava import db

        resource = (await db.resources_to_test(s, "daily"))[0]
        assert len(await db.last_reports(s, resource.id, 100)) == settings.keep_reports + 1

    r = await client.get(f"{API}/ci/resources/{key}/frequency", params={"frequency": "hourly"})
    assert r.status_code == 400 and r.json()["code"] == 4103
    r = await client.get(f"{API}/ci/resources/{key}/frequency", params={"frequency": "monthly"})
    assert r.status_code == 200 and "frequency has been updated" in r.text

    r = await client.get(f"{API}/ci/unsubscribe", params={"key": key})
    assert r.status_code == 200 and "unsubscribed" in r.text
    assert (await client.get(f"{API}/testreport/{report_id}")).status_code == 404  # cascade
    assert (await client.get(f"{API}/ci/unsubscribe", params={"key": key})).json()["code"] == 4105


async def test_registration_validation(client) -> None:
    r = await client.post(f"{API}/ci/resources", json={"url": BASE})
    assert r.status_code == 400 and r.json()["code"] == 4100
    r = await client.post(f"{API}/ci/resources", json={"url": "ftp://x", "email": "a@b.cd"})
    assert r.json()["code"] == 4106
    r = await client.post(
        f"{API}/ci/resources", content=b"{not json", headers={"content-type": "application/json"}
    )
    assert r.status_code == 400 and "message" in r.json()
    assert (await client.get(f"{API}/ci/confirmation", params={"key": "nope"})).status_code == 404
    assert (await client.get(f"{API}/testreport/nope")).status_code == 404


async def test_admin(client, fake_server, monkeypatch) -> None:
    assert (await client.get(f"{API}/admin/updateproviders")).status_code == 401
    bad = {"Authorization": "Basic " + base64.b64encode(b"admin:wrong").decode()}
    assert (await client.get(f"{API}/admin/updateproviders", headers=bad)).json()["code"] == 4002
    assert (
        await client.get(f"{API}/admin/updateproviders", headers={"Authorization": "Bearer x"})
    ).status_code == 401

    provider = {"name": "IPK", "description": "d", "resources": [{"name": "Test", "v2-url": BASE}]}
    r = await client.post(f"{API}/admin/providers", json=provider, headers=ADMIN)
    assert r.status_code == 202 and r.json()["code"] == 2102
    r = await client.post(
        f"{API}/admin/resources", json={"name": "Other", "url": BASE + "x", "provider": "IPK"}, headers=ADMIN
    )
    assert r.status_code == 202

    # updateproviders upserts (no duplicates on repeated runs)
    fake_server.get(RESOURCE_LIST).respond(json=[provider])
    for _ in range(2):
        r = await client.get(f"{API}/admin/updateproviders", headers=ADMIN)
        assert r.json() == {"status": 200, "message": "1 resources updated.", "code": 2000}

    assert (
        await client.get(f"{API}/admin/testallpublic", params={"version": "v0"}, headers=ADMIN)
    ).status_code == 400
    await jobs.run_public(client.app.state.sessions, version=VERSION)
    resources = (await client.get(f"{API}/public/resources")).json()
    assert [r["name"] for r in resources] == ["Other", "Test"]
    test = resources[1]
    assert test["provider"]["name"] == "IPK" and test["url"] == BASE and test["v2-url"] == BASE
    assert len(test["lastTestReports"]) == 1
    assert test["lastTestReports"][0]["shortReport"]["Server Info"]["GET /serverinfo"]["testStatus"] == []
    assert "email" not in test


async def _public_after_startup() -> list[tuple[str, int]]:
    """Name and number of test reports of each public resource once the startup tasks are done."""
    async with AsyncTestClient(create_app()) as c:
        async with asyncio.timeout(10):
            # The tasks run on the app's event loop, so they cannot be awaited here.
            while c.app.state.tasks:  # noqa: ASYNC110
                await asyncio.sleep(0.01)
        resources = (await c.get(f"{API}/public/resources")).json()
        return [(r["name"], len(r["lastTestReports"])) for r in resources]


async def test_startup_imports_and_tests_resources_only_when_empty(
    monkeypatch, settings, fake_server
) -> None:
    monkeypatch.setenv("BRAVA_AUTO_IMPORT_RESOURCES", "true")
    get_settings.cache_clear()
    provider = {"name": "IPK", "resources": [{"name": "Test", "v2-url": BASE}]}
    route = fake_server.get(RESOURCE_LIST).respond(500)

    # an unavailable list does not stop the app; the next start tries again
    assert await _public_after_startup() == []
    route.respond(json=[provider])
    assert await _public_after_startup() == [("Test", 1)]
    # with resources present, a restart neither imports nor tests again
    assert await _public_after_startup() == [("Test", 1)]
    assert route.call_count == 2


async def test_startup_import_can_be_disabled(client, fake_server) -> None:
    route = fake_server.get(RESOURCE_LIST).respond(json=[])
    assert await _public_after_startup() == []
    assert not route.called


async def test_simple_mode(monkeypatch, settings) -> None:
    monkeypatch.setenv("BRAVA_ADVANCED_MODE", "false")
    get_settings.cache_clear()
    async with AsyncTestClient(create_app()) as c:
        assert "advanced=false" in (await c.get("/brapivalidator/")).text
        assert (await c.get(f"{API}/public/resources")).status_code == 501
        assert (await c.get(f"{API}/testreport/x")).status_code == 501
        assert (await c.post(f"{API}/ci/resources", json={})).status_code in (404, 405)
        assert (await c.get(f"{API}/test/brapiversions")).status_code == 200
