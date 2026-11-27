# BRAVA – Python backend

Python 3.14 / [Litestar](https://litestar.dev) port of the BRAVA (BrAPI Validator) backend.
It serves the existing frontend (`../src/main/webapp/advanced`) and uses the existing test
collections and JSON schemas (`../src/main/resources/{collections,schemas}`).

## Quick start

```
uv sync
cp .env.example .env            # adjust; every key is optional
uv run brava serve --port 8765  # http://localhost:8765/brapivalidator/
```

Headless test of a server (same options as the Java CLI):

```
uv run brava test -u https://test-server.brapi.org/brapi/v2 -v v2.1 [--json] [--csv] [-o DIR] [-s] [-t TOKEN] [--fail-on-error]
```

## Advanced mode

`BRAVA_ADVANCED_MODE=true` enables the database, the registration and admin endpoints and the
scheduled tests (daily 08:00, Mondays 08:00 plus all public resources, 1st of the month 08:00).
The schema is managed by Alembic and upgraded on startup (`uv run brava upgrade-db` does it by hand).
Database drivers: SQLite is included; install the `oracle`, `postgres` or `mysql` extra for the others.

While the database holds no public resource, startup imports the list from `BRAVA_RESOURCE_JSON_URL`
(default: brapi.org) and then tests every resource like the weekly run does (strict,
`BRAVA_TESTED_VERSION`), all in the background; `BRAVA_AUTO_IMPORT_RESOURCES=false` turns this off.
`/api/admin/updateproviders` refreshes the list later, and `/api/admin/testallpublic?version=v2.1`
tests all public resources without waiting for the weekly run.

Copy the data of the Java version once:

```
uv run brava migrate-legacy --from "oracle+oracledb://user:pass@host:1521/?service_name=svc" --timezone Europe/Berlin
```

## Layout

| Module | Purpose |
|---|---|
| `config.py` | `BRAVA_*` settings (pydantic-settings, `.env`) |
| `collection.py` | test collection models and loader |
| `validation.py` | draft-04 schema validation, errors shaped like java-json-tools messages |
| `runner.py` | the test engine (requests, test commands, endpoint selection) |
| `reports.py` | report structures (same JSON as the Java version) and summaries |
| `db.py`, `migrations/` | SQLAlchemy models, queries, Alembic migrations |
| `legacy.py` | import of the Java version's database |
| `jobs.py`, `mailer.py` | scheduled runs, provider import, emails |
| `api.py` | Litestar app: `/api/...`, static frontend and schemas |
| `cli.py` | `brava serve / test / migrate-legacy / upgrade-db` |

## Development

```
uv run pytest
uv run ruff check . && uv run ruff format --check .
uv run python tools/compare_reports.py --java http://localhost:8080/brapivalidator --url <server> --version v2.1
```

Docker (build from the repository root): `docker build -f python/Dockerfile -t brava .`
