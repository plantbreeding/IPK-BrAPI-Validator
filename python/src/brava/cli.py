"""Command line: ``brava serve``, ``brava test`` (headless validator), ``brava migrate-legacy``."""

from __future__ import annotations

import argparse
import asyncio
import json
import logging
import os
import sys
from datetime import datetime
from pathlib import Path

from brava import reports


def _cmd_test(args: argparse.Namespace) -> int:
    from brava.collection import available_versions
    from brava.runner import run_suite

    if args.version not in available_versions():
        print(
            f"Invalid BrAPI version {args.version!r}. Available: {', '.join(available_versions())}",
            file=sys.stderr,
        )
        return 1
    out_dir: Path | None = args.output
    if out_dir is not None and not (out_dir.is_dir() and os.access(out_dir, os.W_OK)):
        print(f"Output directory {out_dir} does not exist or is not writable", file=sys.stderr)
        return 1

    report = asyncio.run(run_suite(args.url, args.version, token=args.token, strict=args.strict))
    outputs: list[tuple[str, str]] = []
    if args.json:
        outputs.append(("json", json.dumps(reports.short_summary(report), indent=2)))
    if args.csv:
        outputs.append(("csv", reports.short_summary_csv(report)))
    if not outputs:
        outputs.append(("txt", reports.summary_text(report)))

    stamp = datetime.now().strftime("%d-%m-%Y_%H-%M-%S")
    for ext, text in outputs:
        if out_dir is None:
            print(text)
        else:
            path = out_dir / f"BRAVA_Test_Report_{stamp}.{ext}"
            path.write_text(text, encoding="utf-8")
            print(f"Writing report: {path}")
    if args.fail_on_error and reports.mini_report(report).failedTests:
        return 2
    return 0


def _cmd_serve(args: argparse.Namespace) -> int:
    import uvicorn

    uvicorn.run("brava.api:create_app", factory=True, host=args.host, port=args.port, proxy_headers=True)
    return 0


def _cmd_migrate_legacy(args: argparse.Namespace) -> int:
    from brava.config import get_settings
    from brava.db import make_engine, make_sessionmaker, upgrade_schema
    from brava.legacy import migrate_legacy

    async def run() -> dict[str, int]:
        url = get_settings().database_url
        await upgrade_schema(url)
        engine = make_engine(url)
        try:
            return await migrate_legacy(args.source, make_sessionmaker(engine), args.timezone)
        finally:
            await engine.dispose()

    for key, value in asyncio.run(run()).items():
        print(f"{key}: {value}")
    return 0


def _cmd_upgrade_db(_: argparse.Namespace) -> int:
    from brava.config import get_settings
    from brava.db import upgrade_schema

    asyncio.run(upgrade_schema(get_settings().database_url))
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="brava", description="BRAVA - BrAPI Validator")
    sub = parser.add_subparsers(dest="command", required=True)

    t = sub.add_parser("test", help="test a BrAPI server and print a report (headless mode)")
    t.add_argument("-u", "--url", required=True, help="base URL of the server, e.g. https://host/brapi/v2")
    t.add_argument("-t", "--token", help="access token sent as 'Authorization: Bearer <token>'")
    t.add_argument("-v", "--version", default="v2.0", help="BrAPI version (default: v2.0)")
    t.add_argument("-s", "--strict", action="store_true", help="fail on additional properties")
    t.add_argument("-c", "--csv", action="store_true", help="CSV (tab separated) summary")
    t.add_argument("-j", "--json", action="store_true", help="JSON summary")
    t.add_argument("-o", "--output", type=Path, help="write the report(s) to this directory")
    t.add_argument("--fail-on-error", action="store_true", help="exit with code 2 if any test failed")
    t.set_defaults(func=_cmd_test)

    s = sub.add_parser("serve", help="run the web application")
    s.add_argument("--host", default="127.0.0.1")
    s.add_argument("--port", type=int, default=8000)
    s.set_defaults(func=_cmd_serve)

    m = sub.add_parser("migrate-legacy", help="copy data from the Java version's database")
    m.add_argument("--from", dest="source", required=True, help="SQLAlchemy URL of the old database")
    m.add_argument("--timezone", default="UTC", help="time zone of the old DATE column (default: UTC)")
    m.set_defaults(func=_cmd_migrate_legacy)

    u = sub.add_parser("upgrade-db", help="apply database migrations")
    u.set_defaults(func=_cmd_upgrade_db)
    return parser


def main(argv: list[str] | None = None) -> int:
    logging.basicConfig(level=os.environ.get("BRAVA_LOG_LEVEL", "WARNING"))
    args = build_parser().parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
