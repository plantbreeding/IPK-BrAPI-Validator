# BRAVA: IPK BrAPI Validator

Test suite for BrAPI servers. BRAVA sends requests to a BrAPI server and validates the responses
against the BrAPI JSON schemas. Public instance:
[http://webapps.ipk-gatersleben.de/brapivalidator/](http://webapps.ipk-gatersleben.de/brapivalidator/).

The application consists of

| Part | Path | Notes |
|---|---|---|
| Python backend (Python 3.14, Litestar) | `python/` | API, test engine, optional database/scheduler (see `python/README.md`) |
| Svelte frontend (Svelte 5, TypeScript, Vite) | `frontend/` | web UI (see `frontend/README.md`) |
| Test collections and JSON schemas | `src/main/resources/{collections,schemas}` | shared by all backends |
| Deployment (podman, quadlet) | `deploy/` | image build, bundle, server install |

The original Java backend (`pom.xml`, `src/main/java`, vanilla frontend in `src/main/webapp`) is
still in the repository; see `CLAUDE.md` for its build commands.

## Development

Prerequisites: [uv](https://docs.astral.sh/uv/) (installs Python 3.14 if needed) and
Node.js ≥ 20.19 with npm.

### Python backend

```
cd python
uv sync
cp .env.example .env              # optional, every key has a default
uv run brava serve --port 8765    # http://localhost:8765/brapivalidator/
```

Without further settings the backend serves the vanilla frontend from `src/main/webapp/advanced`.
To serve a built Svelte frontend instead: `BRAVA_FRONTEND_DIR=../frontend/dist uv run brava serve --port 8765`.

Tests and lint:

```
uv run pytest
uv run ruff check . && uv run ruff format --check .
```

Headless test of a BrAPI server from the command line:

```
uv run brava test -u https://test-server.brapi.org/brapi/v2 -v v2.1 [--json] [--csv] [-o DIR]
```

### Svelte frontend

Start the Python backend on port 8765 first (see above), then in a second terminal:

```
cd frontend
npm install
npm run dev       # http://localhost:5173, proxies /api and /schemas to the backend
```

The dev server proxies to `http://localhost:8765/brapivalidator`; set `BRAVA_BACKEND` to use another
backend, e.g. `BRAVA_BACKEND=http://localhost:8080/brapivalidator npm run dev`.

```
npm run check     # svelte-check (types, a11y)
npm run build     # production build in frontend/dist
```

## Deployment with podman

The deployment runs the Python backend with the Svelte frontend in one container (simple mode:
no database, no scheduled tests), as a rootless podman **quadlet**, i.e. a systemd *user* service
(`systemctl --user`). No root/sudo is needed on the server. The app is published on **host port
10000**: `http://<server>:10000/brapivalidator/`.

Server requirements: Linux with systemd, podman ≥ 4.4, network access to the container registries
(docker.io, ghcr.io) and npm/PyPI during the image build.

| File | Purpose |
|---|---|
| `deploy/make-bundle.sh` | creates a source bundle `build/brava-bundle-<date>-<commit>.tar.gz` |
| `deploy/copy-to-server.sh` | copies the bundle via ssh/scp to the server and extracts it |
| `deploy/build-image.sh` | builds the image `localhost/brava:latest` with podman (`deploy/Containerfile`) |
| `deploy/brava.container` | quadlet: runs the image as `brava.service`, port mapping `10000:8000` |
| `deploy/deploy.sh` | installs the quadlet for the current user and (re)starts the service |
| `deploy/brava.env.example` | optional settings, installed as `~/.config/brava/brava.env` |

### 1. Create the bundle (local machine)

```
deploy/make-bundle.sh
```

The bundle contains `python/`, `frontend/`, the collections and schemas and `deploy/` — without
`.venv`, `.env`, local databases, `node_modules` or `frontend/dist`. Uncommitted changes are included
(the file name then ends in `-dirty`). On Windows run the scripts in Git Bash.

### 2. Copy it to the server

```
deploy/copy-to-server.sh user@server
```

This creates a new bundle (or use `-b <bundle.tar.gz>`), copies it to `~/brava` on the server
(`-d <dir>` for another directory) and extracts it there, replacing the previous `python/`,
`frontend/`, `src/` and `deploy/`. Extra ssh options: `SSH_OPTS="-p 2222 -i ~/.ssh/key"`.

With `--deploy` it also runs steps 3 and 4 on the server:

```
deploy/copy-to-server.sh --deploy user@server
```

### 3. Build the image (server)

```
cd ~/brava
deploy/build-image.sh
```

Tags the image as `localhost/brava:latest` and `localhost/brava:<timestamp>`. Options: `-t TAG`, `--no-cache`.

### 4. Install and start the service (server)

```
deploy/deploy.sh            # or: deploy/deploy.sh --build  (runs build-image.sh first)
```

The script copies `brava.container` to `~/.config/containers/systemd/`, creates
`~/.config/brava/brava.env` if it does not exist, reloads systemd and restarts `brava.service`.

**Lingering:** a user service only keeps running after logout and starts at boot when lingering
is enabled for the user. `deploy.sh` tries `loginctl enable-linger` (allowed without root on most
systems) and prints a warning otherwise; then an admin has to run `loginctl enable-linger <user>` once.

### Operating the service

```
systemctl --user status brava
systemctl --user restart brava
systemctl --user stop brava
journalctl --user -u brava -f
```

Settings (proxy for outgoing requests, TLS verification, offered BrAPI versions, ...) go into
`~/.config/brava/brava.env`; restart the service afterwards.

**Update:** repeat steps 1–4 (or `copy-to-server.sh --deploy user@server`).
**Uninstall:** `deploy/deploy.sh --uninstall` (keeps the image and the env file).

Old image tags can be removed with `podman image prune` / `podman rmi localhost/brava:<timestamp>`.

## API

```
/api/test/call?url={serverURL}&brapiversion={brapiVersion}[&accessToken=...][&strict=on]
/api/test/brapiversions
/api/testreport/{reportID}         (advanced mode)
```

Example: `/api/test/call?url=https://test-server.brapi.org/brapi/v2&brapiversion=v2.1`

## Test collections

Test behaviour lives in `src/main/resources/collections/CompleteBrapiTest.<version>.json`; the
schemas are in `src/main/resources/schemas/<version>/`. The collections roughly follow Postman's
format, but use a few simple commands instead of JavaScript tests:

| Command  | Description  | Example  |
|---|---|---|
|StatusCode:{int} | Check the status code of the response  | StatusCode:200 |
|ContentType:{String} | Check the Content Type of the response  | ContentType:application/json |
|Schema:{path} | Check the response against a schema in `src/main/resources/schemas` | Schema:/metadata |
|GetValue:{jsonpointer}:{variableName} | Stores the value of the response at the pointer location | GetValue:/result/data/0/germplasmDbId:germplasmDbId |
|IsEqual:{jsonpointer}:{variableName} | Compares the value at the pointer location with a stored variable |IsEqual:/result/germplasmDbId:germplasmDbId |
|{command}:{value}:breakiffalse | Stops testing the current endpoint if the check fails | StatusCode:200:breakiffalse |
