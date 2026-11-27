# BRAVA frontend (Svelte 5)

Svelte 5 + TypeScript + Vite port of the vanilla-JS/jQuery frontend in
`../src/main/webapp/advanced`. Both frontends live side by side so they can be compared. They
talk to the same backend API (`api/test/call`, `api/public/resources`, `api/testreport/{id}`,
`api/ci/resources`, ...).

Differences from the vanilla version:

- No jQuery, Bootstrap, FooTable, spin.js or Font Awesome. It uses custom CSS (`src/app.css`
  plus scoped component styles), inline SVG icons, native `<dialog>` and `<details>`, and
  supports dark mode.
- The schedule dialog is pre-filled with the URL from the test form.
- A running test can be cancelled. Switching tabs keeps the form and its result.
- The resource table sorts by name, provider, crop and status. Description and base URL move into an
  expandable detail row, and so do provider and crop on narrow screens.

## Development

```
npm install
npm run dev     # http://localhost:5173, proxies /api and /schemas to a backend
npm run check   # svelte-check (types + a11y)
npm run build   # production build in dist/
```

The dev proxy targets `http://localhost:8765/brapivalidator` (Python: `uv run brava serve --port 8765`).
To use another backend, set `BRAVA_BACKEND`, e.g. for Java (`mvn jetty:run`):

```
BRAVA_BACKEND=http://localhost:8080/brapivalidator npm run dev
```

## Serving the build

`dist/` uses relative URLs, so it works under any context path. The `${buildDate}` and
`${git.commit.id.abbrev}` placeholders in `index.html` (meta tags read by the footer) are
filled in by the backend.

- Python backend: `BRAVA_FRONTEND_DIR=../frontend/dist uv run brava serve`
- Java backend: still serves the vanilla frontend; `pom.xml` is unchanged.

## Layout

| Path | Purpose |
|---|---|
| `src/App.svelte` | page shell: header, tabs, footer, modals |
| `src/lib/api.ts` | backend calls and error messages |
| `src/lib/report.ts` | outcome classification, stats, median (same rules as the vanilla `script.js`) |
| `src/lib/types.ts` | API/report types |
| `src/lib/components/ResourcesTab.svelte` | public resource table + report of the selected resource |
| `src/lib/components/TestOwnTab.svelte` | test form and its result |
| `src/lib/components/SavedReportTab.svelte` | `?report=<id>` links from report emails |
| `src/lib/components/ReportView.svelte` → `EndpointResult` → `CheckResult` → `ValidationErrorView` | report tree |
| `src/lib/components/{Modal,ScheduleModal,HelpModal,Spinner,Icon}.svelte` | UI building blocks |
