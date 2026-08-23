#!/usr/bin/env bash
# Apply the AprismJDK patch series onto openjdk-25/.
# Safe to re-run: verifies clean tree first, then overlay + patches.
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$PROJ/openjdk-25"
OVERLAY="$PROJ/jdk/overlay"
PATCHES="$PROJ/jdk/patches"

[ -d "$SRC" ] || { echo "ERROR: $SRC missing"; exit 1; }
[ -d "$SRC/.git" ] || { echo "ERROR: $SRC is not a git checkout (needed for clean verification)"; exit 1; }

cd "$SRC"
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "ERROR: openjdk-25 working tree has tracked modifications. Run scripts/revert-patches.sh first."
  exit 1
fi
UNTRACKED="$(git ls-files --others --exclude-standard | head -5)"
if [ -n "$UNTRACKED" ]; then
  echo "ERROR: openjdk-25 working tree has untracked files (patch overlay leftovers?):"
  echo "$UNTRACKED"
  echo "Run scripts/revert-patches.sh first."
  exit 1
fi

echo "=== overlay copy ==="
if [ -d "$OVERLAY/src" ]; then
  cp -rv "$OVERLAY/src" "$SRC/"
fi

echo "=== applying patches ==="
shopt -s nullglob
for p in "$PATCHES"/[0-9][0-9][0-9]-*.patch; do
  echo "-- $(basename "$p")"
  git apply -p1 --check "$p" || { echo "FAILED check: $p"; exit 1; }
  git apply -p1 "$p"
done

echo "=== series applied cleanly ==="
