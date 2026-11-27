# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

BRAVA (IPK BrAPI Validator) checks a remote BrAPI server against the BrAPI spec: it sends HTTP requests and validates the responses against JSON Schemas. It runs in two modes:

- **Simple mode**: tests a BrAPI server on demand (web UI, REST API or headless CLI `brava test`).
- **Advanced mode** (`BRAVA_ADVANCED_MODE=true`): adds a database, a public resource list (imported from brapi.org), scheduled tests (daily/weekly/monthly at 08:00) and email reports.

| Part | Path |
|---|---|
| Python 3.14 / Litestar backend | `python/` (see `python/README.md`) |
| Svelte 5 + TypeScript + Vite frontend | `frontend/` (see `frontend/README.md`) |
| Test collections and JSON schemas | `resources/{collections,schemas}` |
| Deployment (podman quadlet) | `deploy/`, documented in the root `README.md` |

The former Java backend and vanilla-JS frontend were removed; the Python code still mirrors their JSON shapes (reports, error bodies, java-json-tools-like validation messages) and `brava migrate-legacy` imports the Java version's database.

## Python backend (`python/`)

Config comes from `BRAVA_*` env vars or `python/.env` (`config.py`). Defaults: `BRAVA_RESOURCES_DIR=../resources`, `BRAVA_FRONTEND_DIR=../frontend/dist` (without a frontend build only the API is served).

```
uv run pytest
uv run ruff check . && uv run ruff format --check .
uv run brava serve --port 8765     # http://localhost:8765/brapivalidator/
uv run brava test -u <base url> -v v2.1 [--json] [--csv] [-o DIR] [-s] [-t TOKEN]
```

Modules: `collection.py` (collection models/loader, `available_versions`), `runner.py` (test engine), `validation.py` (draft-04 schema validation), `reports.py`, `db.py` + `migrations/` (SQLAlchemy, Alembic), `jobs.py`/`mailer.py` (scheduler, provider import, emails, templates in `src/brava/templates/`), `api.py` (Litestar app: `/api/...`, `/schemas`, static frontend), `cli.py`, `legacy.py`.

## Svelte frontend (`frontend/`)

`npm run dev` proxies `/api` and `/schemas` to `BRAVA_BACKEND` (default `http://localhost:8765/brapivalidator`). `npm run check` runs svelte-check, `npm run build` writes `dist/`. The backend fills the `${buildDate}`, `${git.commit.id.abbrev}` and `${advancedMode}` placeholders in `index.html`.

## Test collections

`resources/collections/CompleteBrapiTest.<version>.json` are Postman-like collections (folder → item → request + test event). The test event holds string commands (`StatusCode`, `ContentType`, `Schema`, `GetValue`, `IsEqual`, `SaveCalls`, `SearchSchema`, optional `:breakiffalse` suffix) that `runner.py` interprets; `GetValue` stores values that later requests substitute. Schemas are looked up as `resources/schemas/<version>/<path>.json`. Test behavior lives in the collection files.

To add a BrAPI version: add `resources/schemas/<version>/` and `resources/collections/CompleteBrapiTest.<version>.json`. Every collection file found is offered unless `BRAVA_AVAILABLE_VERSIONS` restricts the list.

## Deployment

`deploy/Containerfile` builds Python backend + Svelte frontend into one image; `make-bundle.sh` → `copy-to-server.sh` (one ssh connection, bundle on stdin, server runs `unpack-bundle.sh` from the bundle; PowerShell ports `make-bundle.ps1` / `copy-to-server.ps1` keep CRLF) → `build-image.sh` (podman, on the server) → `deploy.sh` installs the quadlet `brava.container` as rootless systemd user service `brava.service` on host port 10000. `deploy.sh --mode advanced|simple` (default advanced, SQLite in podman volume `brava-data`) sets `BRAVA_ADVANCED_MODE` in the installed quadlet. Files in `deploy/` and `*.sh` must keep LF line endings (`.gitattributes`).
