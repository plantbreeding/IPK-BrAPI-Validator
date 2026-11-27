#!/usr/bin/env bash
# Server side of copy-to-server.sh / copy-to-server.ps1: unpack a bundle in the current directory
# and optionally build the image and (re)start the service.
#
# Usage (run in the target directory, e.g. ~/brava):
#   bash unpack-bundle.sh BUNDLE.tar.gz [--deploy] [--mode advanced|simple]
#
# The copy scripts extract this file from the bundle to .unpack-bundle.sh and run it from there,
# because the old deploy/ directory is replaced below.
set -euo pipefail

bundle="${1:?usage: unpack-bundle.sh BUNDLE.tar.gz [--deploy] [--mode advanced|simple]}"
shift
deploy=0
mode="advanced"
while [[ $# -gt 0 ]]; do
    case "$1" in
        --deploy) deploy=1; shift ;;
        --mode) mode="${2:?missing value for $1}"; shift 2 ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
done
[[ -f "$bundle" ]] || { echo "Bundle not found: $(pwd)/$bundle" >&2; exit 1; }

# Replace the previous bundle contents, but only what a bundle brings along.
rm -rf python frontend src deploy BUNDLE_INFO
tar -xzf "$bundle"
chmod +x deploy/*.sh
# Keep the 3 most recent bundle archives.
ls -1t brava-bundle-*.tar.gz 2>/dev/null | tail -n +4 | xargs -r rm -f
echo "Extracted into $(pwd):"
cat BUNDLE_INFO

if [[ $deploy -eq 1 ]]; then
    deploy/build-image.sh
    deploy/deploy.sh --mode "$mode"
else
    echo
    echo "Next steps on the server:"
    echo "  cd $(pwd) && deploy/build-image.sh && deploy/deploy.sh --mode $mode"
fi
