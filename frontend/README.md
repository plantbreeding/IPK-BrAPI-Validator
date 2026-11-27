# BRAVA frontend (Svelte 5)

Svelte 5 + TypeScript + Vite web UI of BRAVA. It talks to the API (`api/test/call`,
`api/public/resources`, `api/testreport/{id}`, `api/ci/resources`, ...) of the Python backend in
`../python`.

- Custom CSS (`src/app.css` plus scoped component styles), inline SVG icons, native `<dialog>` and
  `<details>`, dark mode.
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
To use another backend, set `BRAVA_BACKEND`, e.g. a deployed container:

```
BRAVA_BACKEND=http://localhost:10000/brapivalidator npm run dev
```

## Serving the build

`dist/` uses relative URLs, so it works under any context path. The `${buildDate}` and
`${git.commit.id.abbrev}` placeholders in `index.html` (meta tags read by the footer) are
filled in by the backend. So is `${advancedMode}`: the Python backend sets it to `false` in simple
mode, and the app then shows only "Test your own" (no resource list, saved reports or scheduling,
which need the database). An unfilled placeholder (dev server) counts as advanced mode.

The Python backend serves `frontend/dist` by default (`uv run brava serve` in `../python`);
`BRAVA_FRONTEND_DIR` points it to another directory.

## Layout

| Path | Purpose |
|---|---|
| `src/App.svelte` | page shell: header, tabs, footer, modals |
| `src/lib/api.ts` | backend calls and error messages |
| `src/lib/report.ts` | outcome classification, stats, median |
| `src/lib/types.ts` | API/report types |
| `src/lib/components/ResourcesTab.svelte` | public resource table + report of the selected resource |
| `src/lib/components/TestOwnTab.svelte` | test form and its result |
| `src/lib/components/SavedReportTab.svelte` | `?report=<id>` links from report emails |
| `src/lib/components/ReportView.svelte` → `EndpointResult` → `CheckResult` → `ValidationErrorView` | report tree |
| `src/lib/components/{Modal,ScheduleModal,HelpModal,Spinner,Icon}.svelte` | UI building blocks |
