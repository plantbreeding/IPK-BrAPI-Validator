# BRAVA: IPK BrAPI Validator

Test suite for BrAPI servers. BRAVA sends requests to a BrAPI server and validates the responses
against the BrAPI JSON schemas. Public instance:
[http://webapps.ipk-gatersleben.de/brapivalidator/](http://webapps.ipk-gatersleben.de/brapivalidator/).

The application consists of

| Part | Path | Notes |
|---|---|---|
| Python backend (Python 3.14, Litestar) | `python/` | API, test engine, optional database/scheduler (see `python/README.md`) |
| Svelte frontend (Svelte 5, TypeScript, Vite) | `frontend/` | web UI (see `frontend/README.md`) |
| Test collections and JSON schemas | `resources/{collections,schemas}` | read by the backend, schemas also served at `/schemas` |
| Deployment (podman, quadlet) | `deploy/` | image build, bundle, server install |

The original Java backend and its vanilla-JS frontend were removed; they are in the git history
before this change.

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

The backend serves the built Svelte frontend from `frontend/dist` (run `npm run build` in
`frontend/` first; without a build it serves only the API). `BRAVA_FRONTEND_DIR` points it elsewhere.

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
backend, e.g. `BRAVA_BACKEND=http://localhost:10000/brapivalidator npm run dev`.

```
npm run check     # svelte-check (types, a11y)
npm run build     # production build in frontend/dist
```

## Deployment with podman

The deployment runs the Python backend with the Svelte frontend in one container, as a rootless
podman **quadlet**, i.e. a systemd *user* service (`systemctl --user`). No root/sudo is needed on
the server. The app is published on **host port 10000**: `http://<server>:10000/brapivalidator/`.

Two modes, chosen with `--mode` of `deploy.sh` / `copy-to-server.sh` (same image for both):

| Mode | Features |
|---|---|
| `advanced` (default) | database (SQLite in the podman volume `brava-data`), public resource list, saved reports, registration of resources, scheduled tests (08:00 daily/weekly/monthly), emails, `/api/admin` |
| `simple` | on-demand tests only ("Test your own"); no database |

Server requirements: Linux with systemd, podman ≥ 4.4, network access to the container registries
(docker.io, ghcr.io) and npm/PyPI during the image build.

| File | Purpose |
|---|---|
| `deploy/make-bundle.sh` | creates a source bundle `build/brava-bundle-<date>-<commit>.tar.gz` |
| `deploy/make-bundle.ps1` | the same for Windows PowerShell |
| `deploy/copy-to-server.sh` | sends the bundle over one ssh connection and unpacks it on the server (optionally deploys) |
| `deploy/copy-to-server.ps1`, `.cmd` | the same for Windows PowerShell (`.cmd` = wrapper that ignores the execution policy) |
| `deploy/unpack-bundle.sh` | server side of the copy scripts (unpack, optionally build and deploy) |
| `deploy/build-image.sh` | builds the image `localhost/brava:latest` with podman (`deploy/Containerfile`) |
| `deploy/brava.container` | quadlet: runs the image as `brava.service`, port mapping `10000:8000` |
| `deploy/deploy.sh` | installs the quadlet for the current user and (re)starts the service |
| `deploy/brava.env.example` | settings template, installed as `~/.config/brava/brava.env` |

### 1. Create the bundle (local machine)

```
deploy/make-bundle.sh
```

The bundle contains `python/`, `frontend/`, the collections and schemas and `deploy/` — without
`.venv`, `.env`, local databases, `node_modules` or `frontend/dist`. Uncommitted changes are included
(the file name then ends in `-dirty`). On Windows use Git Bash or `deploy\make-bundle.ps1`
(see [From Windows](#from-windows-powershell) below).

### 2. Copy it to the server

```
deploy/copy-to-server.sh user@server
```

This creates a new bundle (or use `-b <bundle.tar.gz>`), sends it to `~/brava` on the server
(`-d <dir>` for another directory) and extracts it there, replacing the previous `python/`,
`frontend/`, `resources/` and `deploy/`. Extra ssh options: `SSH_OPTS="-p 2222 -i ~/.ssh/key"`.

Everything runs over a single ssh connection, so the password is asked once. To avoid the prompt
completely, use an ssh key: `ssh-keygen -t ed25519` once, then append `~/.ssh/id_ed25519.pub`
to `~/.ssh/authorized_keys` on the server (`ssh-copy-id user@server` in Git Bash). A warning about a
missing "post-quantum key exchange" comes from the server's older OpenSSH version and does not
stop the deployment.

With `--deploy` it also runs steps 3 and 4 on the server:

```
deploy/copy-to-server.sh --deploy user@server                 # advanced mode
deploy/copy-to-server.sh --deploy --mode simple user@server   # simple mode
```

#### From Windows (PowerShell)

Without Git Bash, use the PowerShell scripts. They need only tools built into Windows 10/11:
`tar.exe` and the OpenSSH client (Settings > System > Optional features > OpenSSH Client).

```powershell
.\deploy\copy-to-server.ps1 user@server -Deploy                 # advanced mode
.\deploy\copy-to-server.ps1 user@server -Deploy -Mode simple    # simple mode
.\deploy\copy-to-server.ps1 user@server                         # copy and unpack only
```

Further options: `-Bundle <file>` (default: new bundle from `make-bundle.ps1`), `-RemoteDir <dir>`
(default `brava`), `-SshArgs @('-p', '2222', '-i', "$HOME\.ssh\id_ed25519")`; `Get-Help
.\deploy\copy-to-server.ps1 -Detailed` shows all. If script execution is disabled on the PC, use the
wrapper, which starts PowerShell with `-ExecutionPolicy Bypass` for this script only:

```powershell
deploy\copy-to-server.cmd user@server -Deploy
```

For a non-default ssh port or key, an entry in `%USERPROFILE%\.ssh\config` works for all scripts:

```
Host brava-server
    HostName server.example.org
    User myuser
    Port 2222
    IdentityFile ~/.ssh/id_ed25519
```

Then deploy with `.\deploy\copy-to-server.ps1 brava-server -Deploy`.

### 3. Build the image (server)

```
cd ~/brava
deploy/build-image.sh
```

Tags the image as `localhost/brava:latest` and `localhost/brava:<timestamp>`. Options: `-t TAG`, `--no-cache`.

### 4. Install and start the service (server)

```
deploy/deploy.sh                  # advanced mode (default)
deploy/deploy.sh --mode simple    # simple mode
deploy/deploy.sh --build          # runs build-image.sh first
```

The script installs `brava.container` to `~/.config/containers/systemd/` with the chosen mode,
creates `~/.config/brava/brava.env` if it does not exist, reloads systemd and restarts
`brava.service`. Instead of `--mode` the environment variable `BRAVA_MODE=advanced|simple` works too.
Running `deploy.sh` again with the other mode switches the mode; the database volume is kept.

In advanced mode, `deploy.sh` appends these settings to `brava.env` when they are missing:
`BRAVA_ADMIN_KEY` (random key for `/api/admin`, HTTP basic auth with the key as password),
`BRAVA_BASE_DOMAIN` (`http://<hostname>:10000/brapivalidator`, used for links in emails) and
`BRAVA_TIMEZONE=Europe/Berlin` (time zone of the scheduled tests). Check them, and set the mail
server (`BRAVA_SMTP_HOST` etc.) — without it, registration confirmations and reports are not sent
and `deploy.sh` prints a warning. To use Oracle, PostgreSQL or MySQL instead of SQLite, set
`BRAVA_DATABASE_URL` (examples in the file); the schema is created/upgraded on startup.

**Public resource list:** while the database holds no public resource, BRAVA imports the list from
brapi.org on startup (`BRAVA_RESOURCE_JSON_URL`) and tests every resource in the background;
results appear one by one (disable with `BRAVA_AUTO_IMPORT_RESOURCES=false`). Later tests run
weekly (Mondays 08:00). A refresh updates resources by provider and name and takes resources that
left the list off the public list. To refresh the list or to test all public resources right away,
e.g. if a restart interrupted the first run, run on the server:

```
key=$(sed -n 's/^BRAVA_ADMIN_KEY=//p' ~/.config/brava/brava.env)
api=http://localhost:10000/brapivalidator/api/admin
curl -u "admin:$key" "$api/updateproviders"
curl -u "admin:$key" "$api/testallpublic?version=v2.1"
```

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

Settings (mail, admin key, proxy for outgoing requests, TLS verification, offered BrAPI versions,
...) go into `~/.config/brava/brava.env`; restart the service afterwards.

The SQLite database lives in the podman volume `brava-data` and survives image updates. Backup:

```
podman volume export brava-data > brava-data-backup.tar
```

**Update:** repeat steps 1–4 (or `copy-to-server.sh --deploy user@server`).
**Uninstall:** `deploy/deploy.sh --uninstall` (keeps the image, the data volume and the env file;
remove the data with `podman volume rm brava-data`).

Old image tags can be removed with `podman image prune` / `podman rmi localhost/brava:<timestamp>`.

## API

```
/api/test/call?url={serverURL}&brapiversion={brapiVersion}[&accessToken=...][&strict=on]
/api/test/brapiversions
/api/testreport/{reportID}         (advanced mode)
```

Example: `/api/test/call?url=https://test-server.brapi.org/brapi/v2&brapiversion=v2.1`

## Test collections

Test behaviour lives in `resources/collections/CompleteBrapiTest.<version>.json`; the
schemas are in `resources/schemas/<version>/`. The collections roughly follow Postman's
format, but use a few simple commands instead of JavaScript tests:

| Command  | Description  | Example  |
|---|---|---|
|StatusCode:{int} | Check the status code of the response  | StatusCode:200 |
|ContentType:{String} | Check the Content Type of the response  | ContentType:application/json |
|Schema:{path} | Check the response against a schema in `resources/schemas` | Schema:/metadata |
|GetValue:{jsonpointer}:{variableName} | Stores the value of the response at the pointer location | GetValue:/result/data/0/germplasmDbId:germplasmDbId |
|IsEqual:{jsonpointer}:{variableName} | Compares the value at the pointer location with a stored variable |IsEqual:/result/germplasmDbId:germplasmDbId |
|{command}:{value}:breakiffalse | Stops testing the current endpoint if the check fails | StatusCode:200:breakiffalse |
