from __future__ import annotations

import json
import sqlite3
from datetime import datetime

from brava import cli, db, reports
from brava.legacy import migrate_legacy
from tests.conftest import BASE, VERSION

# A report as stored by the Java version (TestSuiteReport serialized by Jackson).
JAVA_REPORT = {
    "testCollections": [{
        "folders": [{
            "tests": [], "url": "http://old/brapi/v1", "name": "Calls", "description": "", "total": 0, "fails": 0,
            "testsShort": {
                "/calls": {"name": "/calls", "test": [{"name": "Status code should be 200", "type": "wrong status code",
                           "passed": True, "message": ["Response Status code: 200"], "error": [], "schema": None}],
                           "endpoint": "http://old/brapi/v1/calls", "method": "GET", "cached": False,
                           "testStatus": [], "responseTime": 42},
                "/germplasm": "skipped",
            },
        }],
        "url": "http://old/brapi/v1/", "name": "Complete BrAPI test", "fails": 0, "failListAsHTML": "<ul>\n</ul>",
        "total": 0, "variables": {"variables": {"baseurl": "http://old/brapi/v1"},
                                  "variablesAsString": {"baseurl": "http://old/brapi/v1"}, "keys": ["baseurl"]},
    }],
    "id": "11111111-1111-1111-1111-111111111111",
    "baseUrl": "http://old/brapi/v1/",
}  # fmt: skip


def _legacy_db(path) -> None:
    con = sqlite3.connect(path)
    con.executescript("""
        CREATE TABLE providers ("ID" VARCHAR(48) PRIMARY KEY, "NAME" VARCHAR(255), "DESCRIPTION" TEXT, "LOGO" VARCHAR(255));
        CREATE TABLE resources ("ID" VARCHAR(48) PRIMARY KEY, "URL" VARCHAR(255), "V1_URL" VARCHAR(255),
            "V2_URL" VARCHAR(255), "CROP" VARCHAR(255), "EMAIL" VARCHAR(255), "FREQUENCY" VARCHAR(255),
            "CONFIRMED" INTEGER, "STOREPREV" INTEGER, "ISPUBLIC" INTEGER, "SUBMIT_TO_REPO" INTEGER,
            "NAME" VARCHAR(255), "DESCRIPTION" VARCHAR(255), "PROVIDER" VARCHAR(48), "CERTIFICATE" VARCHAR(255),
            "LOGO" VARCHAR(255));
        CREATE TABLE testreports ("REPORTID" VARCHAR(48) PRIMARY KEY, "RESOURCEID" VARCHAR(48), "REPORTJSON" TEXT,
            "DATE" TIMESTAMP);
    """)
    con.execute("INSERT INTO providers VALUES ('22222222-2222-2222-2222-222222222222', 'IPK', 'desc', '')")
    con.execute(
        "INSERT INTO resources VALUES ('11111111-1111-1111-1111-111111111111', NULL, 'http://old/brapi/v1/', NULL,"
        " 'wheat', 'a@b.cd', 'daily', 1, 3, 0, 0, '', '', NULL, '', '')"
    )
    con.execute(
        "INSERT INTO resources VALUES ('33333333-3333-3333-3333-333333333333', NULL, 'http://pub/v1', 'http://pub/v2',"
        " NULL, NULL, 'weekly', 0, 3, 1, 0, 'Public', 'd', '22222222-2222-2222-2222-222222222222', '', '')"
    )
    con.execute(
        "INSERT INTO testreports VALUES ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111',"
        " ?, '2024-05-01 08:00:00')",
        (json.dumps(JAVA_REPORT),),
    )
    con.execute(
        "INSERT INTO testreports VALUES ('55555555-5555-5555-5555-555555555555', 'deadbeef-0000-0000-0000-000000000000',"
        " '{}', '2024-05-01 08:00:00')"
    )
    con.commit()
    con.close()


async def test_migrate_legacy(settings, tmp_path) -> None:
    legacy = tmp_path / "legacy.db"
    _legacy_db(legacy)
    await db.upgrade_schema(settings.database_url)
    engine = db.make_engine(settings.database_url)
    sessions = db.make_sessionmaker(engine)
    try:
        counts = await migrate_legacy(f"sqlite:///{legacy.as_posix()}", sessions)
        assert counts["providers"] == 1 and counts["resources"] == 2 and counts["reports"] == 1
        assert counts["skipped_orphan_reports"] == 1
        again = await migrate_legacy(f"sqlite:///{legacy.as_posix()}", sessions)
        assert again["resources"] == 0 and again["skipped_existing"] == 4

        async with sessions() as s:
            private = (await db.resources_to_test(s, "daily"))[0]
            assert private.url == "http://old/brapi/v1/" and private.confirmed and not private.is_public
            ((pub, last),) = await db.public_resources(s)
            assert pub.provider.name == "IPK" and pub.v2_url == "http://pub/v2" and last == []
            report = (await db.last_reports(s, private.id, 5))[0]
            view = report.to_json()
            assert view["date"] == int(datetime.fromisoformat("2024-05-01T08:00:00+00:00").timestamp() * 1000)
            assert view["shortReport"]["Calls"]["/germplasm"] == "skipped"
            assert view["miniReport"]["passedTests"] == ["/calls"]
    finally:
        await engine.dispose()


def test_cli_test_command(settings, fake_server, tmp_path, capsys) -> None:
    assert cli.main(["test", "-u", BASE, "-v", VERSION]) == 0
    out = capsys.readouterr().out
    assert "Total Tests Run: 8" in out and "Failed: 3" in out and "GET /broken" in out

    assert cli.main(["test", "-u", BASE, "-v", VERSION, "--json", "--csv", "-o", str(tmp_path)]) == 0
    files = sorted(p.suffix for p in tmp_path.glob("BRAVA_Test_Report_*"))
    assert files == [".csv", ".json"]
    data = json.loads(next(tmp_path.glob("BRAVA_Test_Report_*.json")).read_text())
    assert data["baseURL"] == BASE and "linkFullTest" not in data

    assert cli.main(["test", "-u", BASE, "-v", VERSION, "--fail-on-error"]) == 2
    assert cli.main(["test", "-u", BASE, "-v", "v0"]) == 1
    assert cli.main(["test", "-u", BASE, "-v", VERSION, "-o", str(tmp_path / "missing")]) == 1


def test_mini_report_median() -> None:
    def item(t: int) -> reports.ItemResult:
        return reports.ItemResult("x", responseTime=t)

    folders = [
        reports.FolderResult("info", testsShort={"GET /serverinfo": item(1000)}),
        reports.FolderResult("f", testsShort={"a": item(10), "b": item(15)}),
    ]
    report = reports.SuiteReport([reports.CollectionResult(folders=folders)])
    assert reports.mini_report(report).time == 12.5  # real median, call-info folder excluded
