#!/usr/bin/env bash
# Build the BRAVA container image (Python backend + Svelte frontend) with podman.
#
# Usage: deploy/build-image.sh [-t TAG] [--no-cache]
#   -t, --tag TAG   image tag (default: latest); the image is always also tagged with a timestamp
#   --no-cache      build without the layer cache
#
# Run it from anywhere inside the repository or an extracted bundle.
set -euo pipefail

IMAGE="localhost/brava"
TAG="latest"
BUILD_ARGS=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        -t|--tag) TAG="${2:?missing value for $1}"; shift 2 ;;
        --no-cache) BUILD_ARGS+=(--no-cache); shift ;;
        -h|--help) sed -n '2,9p' "$0"; exit 0 ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
done

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! command -v podman >/dev/null 2>&1; then
    echo "podman is not installed or not on PATH" >&2
    exit 1
fi
for path in deploy/Containerfile python/pyproject.toml frontend/package.json resources/collections; do
    [[ -e "$path" ]] || { echo "Missing $ROOT/$path - is this a complete repository/bundle?" >&2; exit 1; }
done

STAMP="$(date +%Y%m%d-%H%M%S)"
echo "Building $IMAGE:$TAG (also tagged $IMAGE:$STAMP) from $ROOT"
podman build ${BUILD_ARGS[@]+"${BUILD_ARGS[@]}"} \
    -f deploy/Containerfile \
    -t "$IMAGE:$TAG" \
    -t "$IMAGE:$STAMP" \
    .

echo
echo "Built $IMAGE:$TAG"
echo "Install/restart the service with: deploy/deploy.sh"
