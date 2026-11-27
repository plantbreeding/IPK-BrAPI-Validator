#!/usr/bin/env bash
# Copy a BRAVA source bundle to a server over a single ssh connection and unpack it there.
#
# Usage: deploy/copy-to-server.sh [-b BUNDLE] [-d REMOTE_DIR] [--deploy [--mode advanced|simple]] USER@HOST
#   -b, --bundle FILE      bundle to copy (default: create a new one with make-bundle.sh)
#   -d, --dir REMOTE_DIR   target directory on the server (default: ~/brava)
#   --deploy               afterwards build the image and (re)start the service on the server
#                          (runs deploy/build-image.sh and deploy/deploy.sh remotely)
#   --mode MODE            with --deploy: advanced (default) or simple, passed to deploy.sh
#
# Extra ssh options can be passed via SSH_OPTS, e.g. SSH_OPTS="-p 2222 -i ~/.ssh/id_ed25519".
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
bundle_name="$(basename "$BUNDLE")"
for v in "$REMOTE_DIR" "$bundle_name"; do
    [[ "$v" != *\'* ]] || { echo "Single quotes are not allowed in: $v" >&2; exit 1; }
done
unpack_args="--mode $MODE"
[[ $DEPLOY -eq 1 ]] && unpack_args="--deploy $unpack_args"

# One ssh connection (one password prompt): the bundle goes over stdin, then the server unpacks
# it with deploy/unpack-bundle.sh taken from the bundle itself.
echo "Copying $bundle_name to $TARGET:$REMOTE_DIR/ and unpacking it there"
ssh ${SSH_ARGS[@]+"${SSH_ARGS[@]}"} "$TARGET" \
    "set -e; mkdir -p '$REMOTE_DIR'; cd '$REMOTE_DIR'; cat > '$bundle_name'; \
     tar -xzf '$bundle_name' -O deploy/unpack-bundle.sh > .unpack-bundle.sh; \
     bash .unpack-bundle.sh '$bundle_name' $unpack_args" \
    < "$BUNDLE"
