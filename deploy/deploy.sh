#!/usr/bin/env bash
# Install BRAVA as a rootless podman quadlet / systemd user service (no sudo needed).
#
# Usage: deploy/deploy.sh [--mode advanced|simple] [--build] [--uninstall]
#   --mode MODE  advanced (default): database, public resource list, saved reports, scheduled
#                tests and emails; simple: on-demand tests only. Default from $BRAVA_MODE.
#   --build      build the image first (deploy/build-image.sh)
#   --uninstall  stop the service and remove the quadlet (image, data volume and env file are kept)
#
# Afterwards the app runs as "brava.service" (systemctl --user ...) and listens on host port 10000.
set -euo pipefail

SERVICE="brava.service"
IMAGE="localhost/brava:latest"
MODE="${BRAVA_MODE:-advanced}"
BUILD=0
UNINSTALL=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --mode) MODE="${2:?missing value for $1}"; shift 2 ;;
        --mode=*) MODE="${1#*=}"; shift ;;
        --build) BUILD=1; shift ;;
        --uninstall) UNINSTALL=1; shift ;;
        -h|--help) sed -n '2,11p' "$0"; exit 0 ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
done
case "$MODE" in
    advanced) ADVANCED=true ;;
    simple) ADVANCED=false ;;
    *) echo "Invalid mode '$MODE' (use advanced or simple)" >&2; exit 1 ;;
esac

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_HOME="${XDG_CONFIG_HOME:-$HOME/.config}"
QUADLET_DIR="$CONFIG_HOME/containers/systemd"
ENV_DIR="$CONFIG_HOME/brava"
ENV_FILE="$ENV_DIR/brava.env"
USER_NAME="${USER:-$(id -un)}"

# systemctl --user needs the user's runtime dir (may be unset in non-login ssh sessions).
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-/run/user/$(id -u)}"

die() { echo "ERROR: $*" >&2; exit 1; }

command -v podman >/dev/null 2>&1 || die "podman is not installed or not on PATH"
command -v systemctl >/dev/null 2>&1 || die "systemctl not found (systemd is required)"
systemctl --user show-environment >/dev/null 2>&1 \
    || die "cannot reach the systemd user instance (XDG_RUNTIME_DIR=$XDG_RUNTIME_DIR). Log in via ssh/console, or ask an admin to enable lingering."

if [[ $UNINSTALL -eq 1 ]]; then
    systemctl --user stop "$SERVICE" 2>/dev/null || true
    rm -f "$QUADLET_DIR/brava.container"
    systemctl --user daemon-reload
    echo "Removed $SERVICE. Kept $ENV_FILE, the image (podman rmi $IMAGE)"
    echo "and the data volume (podman volume rm brava-data)."
    exit 0
fi

# Quadlet needs podman >= 4.4.
version="$(podman version --format '{{.Client.Version}}' 2>/dev/null || podman --version | awk '{print $NF}')"
major="${version%%.*}"; rest="${version#*.}"; minor="${rest%%.*}"
if (( major < 4 || (major == 4 && minor < 4) )); then
    die "podman $version is too old for quadlets (need >= 4.4)"
fi

if [[ $BUILD -eq 1 ]]; then
    "$DEPLOY_DIR/build-image.sh"
fi
podman image exists "$IMAGE" || die "image $IMAGE not found; build it first: $DEPLOY_DIR/build-image.sh (or pass --build)"

mkdir -p "$QUADLET_DIR" "$ENV_DIR"
if [[ ! -f "$ENV_FILE" ]]; then
    cp "$DEPLOY_DIR/brava.env.example" "$ENV_FILE"
    chmod 600 "$ENV_FILE"
    echo "Created $ENV_FILE (edit it to override settings)"
fi

# True if KEY is set (uncommented) in the env file.
env_has() { grep -Eq "^[[:space:]]*$1=" "$ENV_FILE"; }

# Make sure appended lines start on a new line.
if [[ -s "$ENV_FILE" && -n "$(tail -c 1 "$ENV_FILE")" ]]; then
    echo >> "$ENV_FILE"
fi
if env_has BRAVA_ADVANCED_MODE; then
    echo "WARNING: remove BRAVA_ADVANCED_MODE from $ENV_FILE; the mode is set with deploy.sh --mode." >&2
fi

if [[ $ADVANCED == true ]]; then
    # Settings advanced mode needs; added once, existing values are never touched.
    added=()
    if ! env_has BRAVA_ADMIN_KEY; then
        key="$(od -An -tx1 -N24 /dev/urandom | tr -d ' \n')"
        echo "BRAVA_ADMIN_KEY=$key" >> "$ENV_FILE"; added+=(BRAVA_ADMIN_KEY)
    fi
    if ! env_has BRAVA_BASE_DOMAIN; then
        echo "BRAVA_BASE_DOMAIN=http://$(hostname -f 2>/dev/null || hostname):10000/brapivalidator" >> "$ENV_FILE"
        added+=(BRAVA_BASE_DOMAIN)
    fi
    if ! env_has BRAVA_TIMEZONE; then
        # Scheduled tests run at 08:00 in this time zone (the container itself runs in UTC).
        echo "BRAVA_TIMEZONE=Europe/Berlin" >> "$ENV_FILE"; added+=(BRAVA_TIMEZONE)
    fi
    if [[ ${#added[@]} -gt 0 ]]; then
        echo "Added to $ENV_FILE: ${added[*]} (review them)"
    fi
    if ! env_has BRAVA_SMTP_HOST; then
        echo "WARNING: BRAVA_SMTP_HOST is not set in $ENV_FILE - registration and report emails cannot be sent." >&2
    fi
fi

sed "s/@MODE@/$ADVANCED/" "$DEPLOY_DIR/brava.container" > "$QUADLET_DIR/brava.container"
chmod 0644 "$QUADLET_DIR/brava.container"
echo "Installed $QUADLET_DIR/brava.container ($MODE mode)"

systemctl --user daemon-reload
if ! systemctl --user cat "$SERVICE" >/dev/null 2>&1; then
    for gen in /usr/libexec/podman/quadlet /usr/lib/podman/quadlet; do
        [[ -x "$gen" ]] && { "$gen" -dryrun -user >&2 || true; break; }
    done
    die "systemd did not generate $SERVICE from the quadlet (see output above)"
fi
systemctl --user restart "$SERVICE"

# Without lingering the service stops at logout and does not start at boot.
if [[ "$(loginctl show-user "$USER_NAME" -p Linger --value 2>/dev/null || echo unknown)" != "yes" ]]; then
    if loginctl enable-linger "$USER_NAME" 2>/dev/null; then
        echo "Enabled lingering for $USER_NAME (service keeps running after logout and starts at boot)."
    else
        echo "WARNING: lingering is not enabled for $USER_NAME and could not be enabled without root." >&2
        echo "         The service stops when you log out. Ask an admin to run: loginctl enable-linger $USER_NAME" >&2
    fi
fi

sleep 2
systemctl --user --no-pager status "$SERVICE" || true
echo
echo "Mode: $MODE"
echo "BRAVA should be available at http://$(hostname -f 2>/dev/null || hostname):10000/brapivalidator/"
echo "Logs: journalctl --user -u $SERVICE -f"
