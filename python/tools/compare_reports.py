"""Compare the Python test engine with a running Java BRAVA instance.

Usage (from python/):
    uv run python tools/compare_reports.py --java http://localhost:8080/brapivalidator \
        --url https://test-server.brapi.org/brapi/v2 --version v2.1 [--strict] [--token T]

Both validators test the same BrAPI server. The tool compares, per item:
skipped/missingReqs, the testStatus list and the pass/fail of every check. Some differences
are expected and are reported separately:
  * "wrong ContentType" now counts as failed (Java counted it as a warning) - affects only
    the summaries, not the per-item data compared here;
  * java-json-tools reported unknown formats ("double", "boolean") and its error texts differ;
  * the server's data may change between the two runs.
Exit code 0 when there are no differences in statuses, 1 otherwise.
"""

from __future__ import annotations

import argparse
import asyncio
import sys
from typing import Any

import httpx

from brava import reports
from brava.runner import run_suite


def _summary(item: Any) -> Any:
    if isinstance(item, str):
        return item
    return {
        "testStatus": sorted(item["testStatus"]),
        "checks": [(t["name"], t["passed"]) for t in item["test"]],
    }


async def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--java", required=True, help="base URL of a running Java BRAVA (without /api)")
    parser.add_argument("--url", required=True, help="BrAPI server to test")
    parser.add_argument("--version", required=True)
    parser.add_argument("--token")
    parser.add_argument("--strict", action="store_true")
    args = parser.parse_args()

    params = {"url": args.url, "brapiversion": args.version, "accessToken": args.token or ""}
    if args.strict:
        params["strict"] = "on"
    async with httpx.AsyncClient(timeout=1800) as client:
        java_task = client.get(args.java.rstrip("/") + "/api/test/call", params=params)
        python_task = run_suite(args.url, args.version, token=args.token, strict=args.strict)
        java_resp, py_report = await asyncio.gather(java_task, python_task)
    java_resp.raise_for_status()
    java_short = java_resp.json()["shortReport"]
    py_short = reports.short_report(py_report)

    diffs = 0
    for folder in sorted(set(java_short) | set(py_short)):
        j_items, p_items = java_short.get(folder, {}), py_short.get(folder, {})
        for name in sorted(set(j_items) | set(p_items)):
            j = _summary(j_items[name]) if name in j_items else "<missing>"
            p = _summary(p_items[name]) if name in p_items else "<missing>"
            if j == p:
                continue
            diffs += 1
            print(f"--- {folder} / {name}")
            if isinstance(j, dict) and isinstance(p, dict):
                if j["testStatus"] != p["testStatus"]:
                    print(f"    testStatus java={j['testStatus']} python={p['testStatus']}")
                for (jn, jp), (pn, pp) in zip(j["checks"], p["checks"], strict=False):
                    if (jn, jp) != (pn, pp):
                        print(f"    check java=({jn}, {jp}) python=({pn}, {pp})")
                if len(j["checks"]) != len(p["checks"]):
                    print(f"    number of checks java={len(j['checks'])} python={len(p['checks'])}")
            else:
                print(f"    java={j!r}\n    python={p!r}")

    jm, pm = java_resp.json()["miniReport"], reports.mini_report(py_report)
    print(
        f"\nJava:   {len(jm['totalTests'])} tests, {len(jm['passedTests'])} passed, "
        f"{len(jm['warningTests'])} warnings, {len(jm['failedTests'])} failed"
    )
    print(
        f"Python: {len(pm.totalTests)} tests, {len(pm.passedTests)} passed, "
        f"{len(pm.warningTests)} warnings, {len(pm.failedTests)} failed"
    )
    print(f"{diffs} items differ")
    return 1 if diffs else 0


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))
