"""Report data structures and the summaries derived from them.

Field names match the JSON the Java version produced and stored, so the frontend and
migrated reports keep working.
"""

from __future__ import annotations

import csv
import io
import statistics
from typing import Any

import msgspec

# testStatus values that make an item "failed" (any other status makes it a "warning").
FAIL_STATUSES = frozenset({"wrong status code", "wrong ContentType", "can't connect"})
SKIPPED = "skipped"
MISSING_REQS = "missingReqs"


class ExecResult(msgspec.Struct, omit_defaults=False):
    name: str
    type: str = ""
    passed: bool = False
    message: list[str] = msgspec.field(default_factory=list)
    error: list[dict[str, Any]] = msgspec.field(default_factory=list)
    schema: str | None = None


class ItemResult(msgspec.Struct):
    name: str
    endpoint: str = ""
    method: str = ""
    test: list[ExecResult] = msgspec.field(default_factory=list)
    cached: bool = False
    testStatus: list[str] = msgspec.field(default_factory=list)
    responseTime: int = 0

    @property
    def failed(self) -> bool:
        return any(not t.passed for t in self.test)


class FolderResult(msgspec.Struct):
    name: str
    url: str = ""
    description: str = ""
    tests: list[ItemResult] = msgspec.field(default_factory=list)
    total: int = 0
    fails: int = 0
    testsShort: dict[str, ItemResult | str] = msgspec.field(default_factory=dict)


class Variables(msgspec.Struct):
    variables: dict[str, Any] = msgspec.field(default_factory=dict)
    variablesAsString: dict[str, str] = msgspec.field(default_factory=dict)
    keys: list[str] = msgspec.field(default_factory=list)


class CollectionResult(msgspec.Struct):
    name: str = ""
    url: str = ""
    folders: list[FolderResult] = msgspec.field(default_factory=list)
    total: int = 0
    fails: int = 0
    failListAsHTML: str = "<ul>\n</ul>"
    variables: Variables = msgspec.field(default_factory=Variables)


class SuiteReport(msgspec.Struct):
    testCollections: list[CollectionResult]
    id: str = ""
    baseUrl: str = ""

    @property
    def collection(self) -> CollectionResult:
        return self.testCollections[0]


def encode(report: SuiteReport) -> str:
    return msgspec.json.encode(report).decode()


def decode(data: str | bytes) -> SuiteReport:
    return msgspec.json.decode(data, type=SuiteReport)


def add_stats(report: SuiteReport) -> list[str]:
    """Fill total/fails/failListAsHTML (an item fails if any of its checks failed).

    Returns the fail list as ``"<METHOD> <name>"`` strings.
    """
    col = report.collection
    fail_list: list[str] = []
    for folder in col.folders:
        failed = [t for t in folder.tests if t.failed]
        folder.total, folder.fails = len(folder.tests), len(failed)
        fail_list += [f"{t.method} {t.name}" for t in failed]
    col.total = sum(f.total for f in col.folders)
    col.fails = sum(f.fails for f in col.folders)
    col.failListAsHTML = "<ul>\n" + "".join(f"<li>{x}</li>\n" for x in fail_list) + "</ul>"
    return fail_list


def short_report(report: SuiteReport) -> dict[str, dict[str, Any]]:
    """``{folder name: {item name: item report | "skipped" | "missingReqs"}}``."""
    return {
        f.name: {k: (v if isinstance(v, str) else msgspec.to_builtins(v)) for k, v in f.testsShort.items()}
        for f in report.collection.folders
    }


class MiniReport(msgspec.Struct):
    time: float
    totalTests: list[str]
    passedTests: list[str]
    failedTests: list[str]
    warningTests: list[str]


def mini_report(report: SuiteReport) -> MiniReport:
    times: list[int] = []
    total, passed, failed, warning = [], [], [], []
    for index, folder in enumerate(report.collection.folders):
        for name, item in folder.testsShort.items():
            if isinstance(item, str):
                continue
            if index > 0:  # the first folder only reads the server's call list
                times.append(item.responseTime)
            total.append(name)
            if not item.testStatus:
                passed.append(name)
            elif FAIL_STATUSES.intersection(item.testStatus):
                failed.append(name)
            else:
                warning.append(name)
    median = float(statistics.median(times)) if times else 0.0
    return MiniReport(median, total, passed, failed, warning)


def report_view(report: SuiteReport, report_id: str | None, date_ms: int) -> dict[str, Any]:
    """The JSON returned by ``/api/test/call`` and ``/api/testreport/{id}``."""
    return {
        "reportId": report_id,
        "date": date_ms,
        "resourceUrl": report.baseUrl,
        "shortReport": short_report(report),
        "miniReport": msgspec.to_builtins(mini_report(report)),
    }


def short_summary(report: SuiteReport, resource_name: str = "", link: str | None = None) -> dict[str, Any]:
    mini = mini_report(report)
    out: dict[str, Any] = {
        "resourceName": resource_name,
        "baseURL": report.baseUrl,
        "implementedCalls": mini.totalTests,
        "successfulCalls": mini.passedTests,
        "warningCalls": mini.warningTests,
        "failedCalls": mini.failedTests,
        "medianTestTimeMS": mini.time,
    }
    if link:
        out["linkFullTest"] = link
    return out


_CSV_HEADER = [
    "resourceName", "baseURL", "warningCalls", "failedCalls", "testedCalls",
    "warningCallsCount", "failedCallsCount", "testedCallsCount", "medianTestTimeMS", "linkFullTest",
]  # fmt: skip


def _list(values: list[str]) -> str:
    return "[" + ", ".join(values) + "]"


def short_summary_csv(report: SuiteReport, resource_name: str = "", link: str | None = None) -> str:
    """Tab separated, two lines, same columns as the Java version."""
    mini = mini_report(report)
    buf = io.StringIO()
    writer = csv.writer(buf, delimiter="\t", lineterminator="\n")
    writer.writerow(_CSV_HEADER)
    writer.writerow([
        resource_name, report.baseUrl,
        _list(mini.warningTests), _list(mini.failedTests), _list(mini.totalTests),
        len(mini.warningTests), len(mini.failedTests), len(mini.totalTests), mini.time, link or "",
    ])  # fmt: skip
    return buf.getvalue()


def summary_text(report: SuiteReport) -> str:
    mini = mini_report(report)
    lines = [
        "Test Summary",
        f"  Total Tests Run: {len(mini.totalTests)}",
        f"  Passed: {len(mini.passedTests)}",
        f"  Warning: {len(mini.warningTests)}",
        f"  Failed: {len(mini.failedTests)}",
    ]
    if mini.failedTests:
        lines += ["", "  Failed Tests List:", *(f"    {n}" for n in mini.failedTests)]
    return "\n".join(lines)
