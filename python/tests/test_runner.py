from __future__ import annotations

import json

import respx

from brava import reports
from brava.runner import run_suite, substitute_json, substitute_url
from tests.conftest import BASE, VERSION


def _items(report: reports.SuiteReport) -> dict[str, reports.ItemResult | str]:
    out: dict[str, reports.ItemResult | str] = {}
    for folder in report.collection.folders:
        out.update(folder.testsShort)
    return out


def test_substitution() -> None:
    variables = {"baseurl": "http://x", "id": "a b", "n": 3, "nested": "{id}"}
    assert substitute_url("{baseurl}/p/{id}/{unknown}", variables) == "http://x/p/a b/{unknown}"
    assert substitute_url("{nested}", variables) == "a b"
    assert substitute_json('{"ids": [${id}], "n": ${n}, "x": ${missing}}', variables) == (
        '{"ids": ["a b"], "n": 3, "x": ${missing}}'
    )


async def test_full_run(settings, fake_server: respx.MockRouter) -> None:
    report = await run_suite(BASE + "/", VERSION, token="tok", strict=False)
    items = _items(report)

    # endpoint selection
    assert items["GET /trials"] == reports.SKIPPED
    assert items["GET /needs"] == reports.MISSING_REQS
    assert isinstance(items["GET /serverinfo"], reports.ItemResult)

    # token, query parameters and body substitution
    req = fake_server.routes[1].calls.last.request
    assert req.headers["authorization"] == "Bearer tok"
    assert req.url.params["json"] == ""
    search_req = fake_server.routes[3].calls.last.request
    assert json.loads(search_req.content) == {"programDbIds": ["p1"]}

    programs = items["GET /programs"]
    assert programs.testStatus == []  # additional property allowed (not strict), uri reference accepted
    assert programs.test[-1].message == ['Stored value: "p1"']

    detail = items["GET /programs/{programDbId}"]
    assert detail.endpoint == BASE + "/programs/p1"
    assert detail.test[-1].passed and detail.test[-1].message == ['Found: "p1". Expected: "p1"']

    search = items["POST /search/programs"]
    assert [t.name for t in search.test] == [
        "Status code should be 202,200",
        "Save variable at: /result/searchResultsDbId | Key: searchId",
        f"Json matches schema: /schemas/{VERSION}/search202.json",
        "Search matches schema",
    ]
    assert search.testStatus == []
    assert items["GET /search/programs/{searchResultsDbId}"].testStatus == []

    broken = items["GET /broken"]
    assert len(broken.test) == 1  # breakiffalse stopped the item
    assert broken.test[0].message[-1] == "Test failed. Won't continue testing this resource."
    assert broken.testStatus == ["wrong status code"]

    assert items["GET /plain"].testStatus == ["wrong ContentType"]

    odd = items["GET /odd"]
    assert [t.name for t in odd.test] == ["Invalid exec command"] * 2 + ["Invalid exec command"]
    assert odd.test[0].message == ["Missing parameters"]

    # summaries
    col = report.collection
    assert col.variables.variables["programDbId0"] == "p1"
    assert col.variables.variablesAsString["serviceResult"].startswith("[{service:programs}")
    assert col.total == 8 and col.fails == 3
    mini = reports.mini_report(report)
    assert set(mini.failedTests) == {"GET /broken", "GET /plain", "GET /odd"}
    assert mini.warningTests == []
    assert "GET /trials" not in mini.totalTests


async def test_strict_mode_reports_additional_properties(settings, fake_server) -> None:
    report = await run_suite(BASE, VERSION, strict=True)
    programs = _items(report)["GET /programs"]
    schema_check = programs.test[1]
    assert not schema_check.passed
    assert schema_check.error[0]["keyword"] == "additionalProperties"
    assert schema_check.error[0]["unwanted"] == ["extra"]
    assert programs.testStatus == ["schema mismatch"]


async def test_schema_errors_are_fge_like(settings, fake_server) -> None:
    fake_server.get("/programs").respond(json={"result": {"data": [{"programDbId": 5}]}})
    report = await run_suite(BASE, VERSION)
    errors = _items(report)["GET /programs"].test[1].error
    by_kw = {e["keyword"]: e for e in errors}
    assert by_kw["type"]["instance"]["pointer"] == "/result/data/0/programDbId"
    assert by_kw["type"]["found"] == "integer" and by_kw["type"]["expected"] == ["string"]
    assert by_kw["required"]["missing"] == ["programName"]
    assert all(e["domain"] == "validation" and e["level"] == "error" for e in errors)


async def test_cant_connect(settings) -> None:
    with respx.mock(base_url=BASE) as router:
        router.get("/serverinfo").mock(side_effect=__import__("httpx").ConnectError("refused"))
        report = await run_suite(BASE, VERSION)
    first = report.collection.folders[0].tests[0]
    assert first.testStatus == ["can't connect"]
    assert first.test[0].name == "Can't connect to tested server or missing parameters. Test cancelled."
    # nothing advertised -> everything else skipped
    assert set(report.collection.folders[1].testsShort.values()) == {reports.SKIPPED}


async def test_scheduled_runs_reject_unusual_ports(settings) -> None:
    report = await run_suite("http://brapi.test:8080/brapi/v2", VERSION, scheduled=True)
    assert report.collection.folders[0].tests[0].testStatus == ["can't connect"]


async def test_report_roundtrip_and_summaries(settings, fake_server) -> None:
    report = await run_suite(BASE, VERSION)
    decoded = reports.decode(reports.encode(report))
    assert reports.short_report(decoded) == reports.short_report(report)
    csv_text = reports.short_summary_csv(report, "Name", "http://link")
    header, row = csv_text.splitlines()
    assert header.split("\t")[0] == "resourceName"
    assert row.split("\t")[:2] == ["Name", BASE]
    assert "Failed: 3" in reports.summary_text(report)
