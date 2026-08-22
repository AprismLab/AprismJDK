#!/usr/bin/env bash
# Revert the AprismJDK patch series: restore pristine upstream tree.
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$PROJ/openjdk-25"

[ -d "$SRC/.git" ] || { echo "ERROR: $SRC is not a git checkout"; exit 1; }

cd "$SRC"
git checkout -- .
# Remove untracked files that came from overlay copy (keep .git, build)
git clean -fd -- src/jdk.aprismate 2>/dev/null || true

echo "=== upstream tree restored ==="
git status --short | head -5
