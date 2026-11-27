#!/usr/bin/env bash
# Install BRAVA as a rootless podman quadlet / systemd user service (no sudo needed).
#
# Usage: deploy/deploy.sh [--build] [--uninstall]
#   --build      build the image first (deploy/build-image.sh)
#   --uninstall  stop the service and remove the quadlet (the image and env file are kept)
#
# Afterwards the app runs as "brava.service" (systemctl --user ...) and listens on host port 10000.
set -euo pipefail

SERVICE="brava.service"
IMAGE="localhost/brava:latest"
BUILD=0
UNINSTALL=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --build) BUILD=1; shift ;;
        --uninstall) UNINSTALL=1; shift ;;
        -h|--help) sed -n '2,9p' "$0"; exit 0 ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
done

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_HOME="${XDG_CONFIG_HOME:-$HOME/.config}"
QUADLET_DIR="$CONFIG_HOME/containers/systemd"
ENV_DIR="$CONFIG_HOME/brava"
ENV_FILE="$ENV_DIR/brava.env"

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
    echo "Removed $SERVICE. Kept $ENV_FILE and the image (remove with: podman rmi $IMAGE)."
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
    echo "Created $ENV_FILE (edit it to override settings)"
fi
install -m 0644 "$DEPLOY_DIR/brava.container" "$QUADLET_DIR/brava.container"
echo "Installed $QUADLET_DIR/brava.container"

systemctl --user daemon-reload
if ! systemctl --user cat "$SERVICE" >/dev/null 2>&1; then
    for gen in /usr/libexec/podman/quadlet /usr/lib/podman/quadlet; do
        [[ -x "$gen" ]] && { "$gen" -dryrun -user >&2 || true; break; }
    done
    die "systemd did not generate $SERVICE from the quadlet (see output above)"
fi
systemctl --user restart "$SERVICE"

# Without lingering the service stops at logout and does not start at boot.
if [[ "$(loginctl show-user "$USER" -p Linger --value 2>/dev/null || echo unknown)" != "yes" ]]; then
    if loginctl enable-linger "$USER" 2>/dev/null; then
        echo "Enabled lingering for $USER (service keeps running after logout and starts at boot)."
    else
        echo "WARNING: lingering is not enabled for $USER and could not be enabled without root." >&2
        echo "         The service stops when you log out. Ask an admin to run: loginctl enable-linger $USER" >&2
    fi
fi

sleep 2
systemctl --user --no-pager status "$SERVICE" || true
echo
echo "BRAVA should be available at http://$(hostname -f 2>/dev/null || hostname):10000/brapivalidator/"
echo "Logs: journalctl --user -u $SERVICE -f"
