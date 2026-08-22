#!/usr/bin/env bash
# Configure + build unmodified OpenJDK 25 inside WSL2.
# Usage: wsl -d Ubuntu -e bash scripts/build-openjdk-wsl.sh [--images-only]
set -euo pipefail

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$PROJ/openjdk-25"
BOOT_JDK="$HOME/bootjdk"
CONF="linux-x86_64-server-release"

mkdir -p "$SRC/build"
cd "$SRC"

if [ ! -f "build/$CONF/spec.gmk" ]; then
  echo "=== configure ==="
  bash configure \
    --with-boot-jdk="$BOOT_JDK" \
    --with-debug-level=release \
    --with-jvm-variants=server \
    --enable-warnings-as-errors=no \
    --with-vendor-name=AprismLab \
    --with-vendor-url=https://github.com/AprismLab/AprismJDK \
    --with-vendor-bug-url=https://github.com/AprismLab/AprismJDK/issues \
    --with-vendor-vm-bug-url=https://github.com/AprismLab/AprismJDK/issues \
    --with-version-string=26.2.1 \
    --with-version-pre=alpha \
    --with-version-build=1
fi

echo "=== make images ==="
make images JOBS="$(nproc)" LOG=info

IMG="$SRC/build/$CONF/images/jdk"
"$IMG/bin/java" -version

cat <<EOF

=== BUILD VERIFICATION ===
Image: $IMG
Version output above should read: openjdk version "26.2.1-alpha" ... AprismLab vendor line appears after branding patch (Alpha.4).
Next: scripts/verify-build.ps1 (Windows-side smoke) or proceed to jdk/patches/.
EOF
