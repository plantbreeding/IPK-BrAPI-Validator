#!/usr/bin/env bash
# Copy a BRAVA source bundle to a server via ssh/scp and extract it there.
#
# Usage: deploy/copy-to-server.sh [-b BUNDLE] [-d REMOTE_DIR] [--deploy [--mode advanced|simple]] USER@HOST
#   -b, --bundle FILE      bundle to copy (default: create a new one with make-bundle.sh)
#   -d, --dir REMOTE_DIR   target directory on the server (default: ~/brava)
#   --deploy               afterwards build the image and (re)start the service on the server
#                          (runs deploy/build-image.sh and deploy/deploy.sh remotely)
#   --mode MODE            with --deploy: advanced (default) or simple, passed to deploy.sh
#
# Extra ssh/scp options can be passed via SSH_OPTS, e.g. SSH_OPTS="-p 2222 -i ~/.ssh/id_ed25519".
set -euo pipefail

BUNDLE=""
REMOTE_DIR="brava"   # relative to the remote home directory unless absolute
DEPLOY=0
MODE="advanced"
TARGET=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        -b|--bundle) BUNDLE="${2:?missing value for $1}"; shift 2 ;;
        -d|--dir) REMOTE_DIR="${2:?missing value for $1}"; shift 2 ;;
        --deploy) DEPLOY=1; shift ;;
        --mode) MODE="${2:?missing value for $1}"; shift 2 ;;
        --mode=*) MODE="${1#*=}"; shift ;;
        -h|--help) sed -n '2,12p' "$0"; exit 0 ;;
        -*) echo "Unknown option: $1" >&2; exit 1 ;;
        *) [[ -z "$TARGET" ]] || { echo "Only one USER@HOST allowed" >&2; exit 1; }; TARGET="$1"; shift ;;
    esac
done
[[ -n "$TARGET" ]] || { sed -n '2,12p' "$0" >&2; exit 1; }
REMOTE_DIR="${REMOTE_DIR#\~/}"
case "$MODE" in
    advanced|simple) ;;
    *) echo "Invalid mode '$MODE' (use advanced or simple)" >&2; exit 1 ;;
esac

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -z "$BUNDLE" ]]; then
    BUNDLE="$("$DEPLOY_DIR/make-bundle.sh" | tail -n 1)"
fi
[[ -f "$BUNDLE" ]] || { echo "Bundle not found: $BUNDLE" >&2; exit 1; }

read -r -a SSH_ARGS <<< "${SSH_OPTS:-}"
bundle_dir="$(cd "$(dirname "$BUNDLE")" && pwd)"
bundle_name="$(basename "$BUNDLE")"

echo "Copying $bundle_name to $TARGET:$REMOTE_DIR/"
ssh ${SSH_ARGS[@]+"${SSH_ARGS[@]}"} "$TARGET" "mkdir -p '$REMOTE_DIR'"
# scp from inside the bundle directory: a "C:/..." source path would be taken as a host name.
(cd "$bundle_dir" && scp ${SSH_ARGS[@]+"${SSH_ARGS[@]}"} "$bundle_name" "$TARGET:$REMOTE_DIR/")

echo "Extracting on $TARGET"
ssh ${SSH_ARGS[@]+"${SSH_ARGS[@]}"} "$TARGET" bash -s -- "$REMOTE_DIR" "$bundle_name" "$DEPLOY" "$MODE" <<'REMOTE'
set -euo pipefail
dir="$1"; bundle="$2"; deploy="$3"; mode="$4"
cd "$dir"
# Replace the previous bundle contents, but only what a bundle brings along.
rm -rf python frontend src deploy BUNDLE_INFO
tar -xzf "$bundle"
chmod +x deploy/*.sh
# Keep the 3 most recent bundle archives.
ls -1t brava-bundle-*.tar.gz 2>/dev/null | tail -n +4 | xargs -r rm -f
echo "Extracted into $(pwd):"
cat BUNDLE_INFO
if [[ "$deploy" == 1 ]]; then
    deploy/build-image.sh
    deploy/deploy.sh --mode "$mode"
else
    echo
    echo "Next steps on the server:"
    echo "  cd $(pwd) && deploy/build-image.sh && deploy/deploy.sh --mode $mode"
fi
REMOTE
