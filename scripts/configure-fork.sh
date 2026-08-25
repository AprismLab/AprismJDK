#!/usr/bin/env bash
# Canonical AprismJDK fork configure invocation (Cygwin host).
# Encodes every decision recorded in FACT.md Session 12 so builds are
# reproducible. Usage: cygwin bash scripts/configure-fork.sh [extra args...]
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$PROJ/openjdk-25"

# 8.3 short path avoids m4 eval breakage on spaces (VS "18" layout is
# unknown to jdk-25+10 well-known-name search).
TOOLS_DIR="/cygdrive/c/PROGRA~1/MICROS~4/18/COMMUN~1/VC/bin"
BOOT_JDK="${BOOT_JDK:-/cygdrive/c/Users/Sails/Java/jdk-25.0.3+9}"

cd "$SRC"
exec bash configure \
  --with-boot-jdk="$BOOT_JDK" \
  --with-debug-level=release \
  --with-jvm-variants=server \
  --enable-warnings-as-errors=no \
  --disable-javac-server \
  --with-tools-dir="$TOOLS_DIR" \
  --with-vendor-name=AprismLab \
  --with-vendor-version-string=AJR \
  --with-vendor-url=https://github.com/AprismLab/AprismJDK \
  --with-vendor-bug-url=https://github.com/AprismLab/AprismJDK/issues \
  --with-vendor-vm-bug-url=https://github.com/AprismLab/AprismJDK/issues \
  --with-version-string=25.2.1 \
  "$@"
