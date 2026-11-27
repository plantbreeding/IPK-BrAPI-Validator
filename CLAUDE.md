# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

BRAVA (IPK BrAPI Validator) is a Java 11 / Jakarta EE (Jersey 3, Jetty 11 / Tomcat 10) web app. It checks a remote BrAPI server against the BrAPI spec: it sends HTTP requests with RestAssured and validates the responses against JSON Schemas. It runs in two modes:

- **Simple mode**: tests a BrAPI server on demand (web UI, REST API or headless CLI).
- **Advanced mode** (`advancedMode=true` in config): adds a database (ORMLite over JDBC; H2/Oracle/MySQL/Postgres), a public resource list, Quartz-scheduled tests (daily/weekly/monthly at 08:00) and email reports.

## Build and run

`config.properties` must be on the classpath at `src/main/resources/config.properties` (gitignored; copy it from `config.properties.example`). If it is missing, `Config.init()` throws an NPE at startup. A `config.properties` in the repo root is **not** on the classpath.

```
mvn -U clean package jetty:run        # build WAR and run on http://localhost:8080/brapivalidator
mvn -f pom-cli.xml clean package      # build headless CLI fat jar (BrAPI-Validator-Headless, main class HeadlessMain)
mvn tomcat7:deploy                    # deploy; needs appserver.* properties in ~/.m2/settings.xml
mvn jib:build                         # container image via jib-maven-plugin (Tomcat base image)
docker build -t brava .               # Dockerfile build (older: maven 3.6/jdk8 + tomcat 9; may lag behind pom.xml)
```

Headless CLI options: `-u/--url` (base URL, e.g. `.../brapi/v2`), `-t/--token`, `-v/--version` (default `v2.0`), `-s/--strict`, plus `--json` / `--csv` / `--output <dir>` for the report format.

There are **no unit tests** (no `src/test`). JUnit is a dependency only. To verify a change, run the app and call `/api/test/call` against a BrAPI server, or run the headless jar.

### Simple vs advanced webapp

`src/main/webapp/` (simple) and `src/main/webapp/advanced/` (advanced) are two separate frontends, each with its own `WEB-INF/web.xml`, `index.html`, `js/script.js` and CSS. `pom.xml` chooses one through `<warSourceDirectory>` in maven-war-plugin and `<webAppSourceDirectory>` in jetty-maven-plugin; both currently point to `${webappHome}/advanced`. Jetty always uses `src/main/webapp/WEB-INF/web.xml` as its descriptor. A frontend change usually has to be made in both trees, and a `web.xml` change in both `web.xml` files.

### Svelte frontend (`frontend/`)

A Svelte 5 + TypeScript + Vite port of the advanced frontend lives in `frontend/` (see `frontend/README.md`). It is kept side by side with the vanilla frontend for comparison, so a UI change usually belongs in both. In `frontend/`, run `npm run dev`, which proxies `/api` and `/schemas` to `BRAVA_BACKEND` (default `http://localhost:8765/brapivalidator`). `npm run check` runs svelte-check and `npm run build` writes `dist/`. The Python backend serves the build with `BRAVA_FRONTEND_DIR=../frontend/dist`; the Java WAR still ships the vanilla frontend.

## Architecture

Package root: `de.ipk_gatersleben.bit.bi.bridge.brapicomp`.

- **Startup**: `AppServletContextListener` (`@WebListener`) loads `Config`, sets the RestAssured proxy from `http.proxyHost/Port`, and in advanced mode opens the JDBC pool (`utils/DataSourceManager`), creates the tables and DAOs for `Resource`, `Provider` and `TestReport`, and starts the Quartz scheduler (`scheduling/*Job`).
- **REST API** (`apiresources/`, Jersey, mounted at `/api/*`, configured by `ApplicationConfig`):
  - `SingleTestResource`: `/test/call?url=&brapiversion=&accessToken=&strict=on`, `/test/brapiversions`
  - `TestReportResource`: `/testreport/{id}`
  - `PublicResoucesResource`: `/public/resources`
  - `ContinuousIntegrationResource`: `/ci/...` (register a resource, confirm or unsubscribe through email links, change frequency)
  - `AdminResource`: `/admin/...` (create resources and providers, `testallpublic`, `updateproviders` from `resourceJsonUrl`), guarded by `adminkey`
- **Test engine** (`testing/`):
  - `testing/config/*` are Jackson POJOs for a Postman-like test collection: `TestCollection` → `Folder` → `Item` → `Request`/`Event`. They are loaded from `src/main/resources/collections/CompleteBrapiTest.<version>.json`.
  - Runner chain: `RunnerService.testEndpointWithCall` → `CallTestSuiteRunner` (or `CustomTestSuiteRunner`) → `TestCollectionRunner` → `TestFolderRunner` → `TestItemRunner`. `TestFolderRunner.runTestsFromCall` first reads the server's `/calls` or `/serverinfo` response (saved by `SaveCalls`) and runs only the endpoints the server advertises.
  - `TestItemRunner` interprets the string commands in each item's test event (`StatusCode`, `ContentType`, `Schema`, `GetValue`, `IsEqual`, `SaveCalls`, `SearchSchema`, optional `:breakiffalse` suffix). `GetValue` puts values into `VariableStorage` and later requests substitute them (`RunnerService` replaces placeholders). `SearchSchema` also handles 202 async-search responses (`<schema>202.json`).
  - `SchemaValidator` (java-json-tools) validates against `/schemas` + path + `.json` on the classpath, i.e. `src/main/resources/schemas/<version>/...`. The README's mention of `src/test/resources/schemas` is outdated. Non-strict mode (`allowAdditional`) tolerates additional properties. `singleTest=true` (a manual web test) relaxes some checks that apply to scheduled runs.
  - Results build up as `testing/reports/*Report` (suite → collection → folder → item → exec).
- **Persistence** (`dbentities/`): ORMLite entities plus static `*Service` helpers. `Cache` holds `dbType` (`oracle` or `other`) because some queries branch on it.
- **Email** (`ci/`): `EmailManager`/`EmailSender` fill the HTML templates in `src/main/resources/templates/` (`TemplateHTML`) and send them over SMTP using the `mail*` config keys.

## Python backend (`python/`)

A Python 3.14 / Litestar port of the whole backend lives in `python/` (see `python/README.md`). It runs side by side with the Java code until parity is confirmed. It reuses `src/main/resources/{collections,schemas}` and the frontend in `src/main/webapp/advanced` (paths configurable via `BRAVA_RESOURCES_DIR` / `BRAVA_FRONTEND_DIR`), so changes there affect both backends. Config comes from `BRAVA_*` env vars or `python/.env`. Run `uv run pytest` and `uv run ruff check .` in `python/`; `uv run brava serve` starts the app at `/brapivalidator/`.

Deployment (`deploy/`, documented in the root `README.md`): `deploy/Containerfile` builds Python backend + Svelte frontend into one simple-mode image; `make-bundle.sh` → `copy-to-server.sh` → `build-image.sh` (podman, on the server) → `deploy.sh` installs the quadlet `brava.container` as rootless systemd user service `brava.service` on host port 10000. Files in `deploy/` and `*.sh` must keep LF line endings (`.gitattributes`).

## Adding or supporting a BrAPI version

1. Add `schemas/<version>/` and `collections/CompleteBrapiTest.<version>.json`.
2. Add the version to `availableVersions` in the config. `SingleTestResource` rejects any version that is not listed there.

Test behavior lives in the collection JSON files; the Java code only interprets the command strings.
