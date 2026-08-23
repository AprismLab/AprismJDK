#!/usr/bin/env bash
# Stage the newest aprismate-agent jar as openjdk-25/lib/aprismate.jar
# so Images.gmk (Patch 003) copies it into the JDK image.
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
LIBS="$PROJ/aprismate-agent/build/libs"
DEST="$PROJ/openjdk-25/lib"

JAR=$(ls -t "$LIBS"/aprismate-agent-v*.jar 2>/dev/null \
      | grep -v -e sources -e javadoc | head -1)
[ -n "$JAR" ] || { echo "ERROR: no agent jar under $LIBS"; exit 1; }

mkdir -p "$DEST"
cp -f "$JAR" "$DEST/aprismate.jar"
echo "staged: $(basename "$JAR") -> openjdk-25/lib/aprismate.jar ($(stat -c%s "$DEST/aprismate.jar") bytes)"
