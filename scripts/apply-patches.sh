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
UNTRACKED="$(git ls-files --others --exclude-standard | grep -v '^lib/aprismate\.jar$' | head -5 || true)"
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
  # The git on PATH may be the Windows build (no /cygdrive support);
  # hand it mixed-mode paths.
  if command -v cygpath >/dev/null 2>&1; then
    p_arg="$(cygpath -m "$p")"
  else
    p_arg="$p"
  fi
  git apply -p1 --check "$p_arg" || { echo "FAILED check: $p"; exit 1; }
  git apply -p1 "$p_arg"
done

echo "=== series applied cleanly ==="
