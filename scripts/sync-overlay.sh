#!/usr/bin/env bash
# Sync aprismate-api sources into the fork overlay tree.
# jdk/overlay/src/jdk.aprismate/share/classes mirrors the JDK module layout.
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
API_SRC="$PROJ/aprismate-api/src/main/java"
DEST="$PROJ/jdk/overlay/src/jdk.aprismate/share/classes"

[ -d "$API_SRC" ] || { echo "ERROR: $API_SRC missing"; exit 1; }
mkdir -p "$DEST"

# Wipe previous overlay classes (module-info.java lives outside this dir)
rm -rf "$DEST/aprism" "$DEST/jdk"
cp -rv "$API_SRC/." "$DEST/" | tail -1

echo "=== overlay synced: $(find "$DEST" -name '*.java' | wc -l) files ==="
