#!/usr/bin/env bash
# Revert the AprismJDK patch series: restore pristine upstream tree.
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$PROJ/openjdk-25"

[ -d "$SRC/.git" ] || { echo "ERROR: $SRC is not a git checkout"; exit 1; }

cd "$SRC"
git checkout -- .
# Remove untracked non-ignored files (overlay copies, strays); build/ is
# upstream-ignored so plain -fd leaves it intact. The staged agent jar is
# a build INPUT and survives reverts.
git clean -fd -e lib/aprismate.jar

echo "=== upstream tree restored ==="
git status --short | head -5
