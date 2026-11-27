#!/usr/bin/env bash
# Create a source bundle (tar.gz) that can be built into a container image on the server.
#
# Usage: deploy/make-bundle.sh [-o OUTPUT_DIR]
#   -o, --output DIR   where to write the bundle (default: <repo>/build)
#
# Prints the path of the created bundle as the last line. Works on Linux, macOS and Git Bash.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT/build"

while [[ $# -gt 0 ]]; do
    case "$1" in
        -o|--output) OUT_DIR="${2:?missing value for $1}"; shift 2 ;;
        -h|--help) sed -n '2,7p' "$0"; exit 0 ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
done

cd "$ROOT"
rev="$(git rev-parse --short HEAD 2>/dev/null || echo nogit)"
if [[ "$rev" != nogit ]] && [[ -n "$(git status --porcelain 2>/dev/null)" ]]; then
    rev="$rev-dirty"
fi
stamp="$(date +%Y%m%d-%H%M%S)"
name="brava-bundle-$stamp-$rev"

mkdir -p "$OUT_DIR"
OUT_DIR="$(cd "$OUT_DIR" && pwd)"
bundle="$OUT_DIR/$name.tar.gz"

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
printf 'bundle=%s\ncommit=%s\ncreated=%s\n' "$name" "$rev" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" > "$tmp/BUNDLE_INFO"

# Write via stdout: a "C:/..." path given to -f would be taken as a remote host by GNU tar.
tar -cz -f - \
    --exclude='python/.venv' \
    --exclude='python/.env' \
    --exclude='python/*.db' \
    --exclude='python/.pytest_cache' \
    --exclude='python/.ruff_cache' \
    --exclude='__pycache__' \
    --exclude='*.pyc' \
    --exclude='frontend/node_modules' \
    --exclude='frontend/dist' \
    --exclude='frontend/.env*' \
    python \
    frontend \
    resources/collections \
    resources/schemas \
    deploy \
    -C "$tmp" BUNDLE_INFO \
    > "$bundle"

echo "Created bundle ($(du -h "$bundle" | cut -f1)):"
echo "$bundle"
