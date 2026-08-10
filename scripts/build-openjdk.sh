#!/bin/bash
# OpenJDK Build Wrapper for AprismJDK
# This script wraps OpenJDK build process with Aprism customizations

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
OPENJDK_DIR="${PROJECT_ROOT}/openjdk-25"

# Configuration
BOOT_JDK="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk}"
BUILD_TYPE="${BUILD_TYPE:-release}"
JVM_VARIANTS="${JVM_VARIANTS:-server}"
DEBUG_LEVEL="${DEBUG_LEVEL:-release}"

# Platform detection
OS_NAME="$(uname -s)"
case "${OS_NAME}" in
    Linux*)     PLATFORM=linux;;
    Darwin*)    PLATFORM=macosx;;
    CYGWIN*)    PLATFORM=windows;;
    MINGW*)     PLATFORM=windows;;
    MSYS*)      PLATFORM=windows;;
    *)          PLATFORM="unknown";;
esac

echo "=== AprismJDK OpenJDK Build Wrapper ==="
echo "Platform: ${PLATFORM}"
echo "OpenJDK Directory: ${OPENJDK_DIR}"
echo "Boot JDK: ${BOOT_JDK}"
echo "Build Type: ${BUILD_TYPE}"
echo "Debug Level: ${DEBUG_LEVEL}"
echo "========================================"

# Verify OpenJDK source exists
if [ ! -d "${OPENJDK_DIR}" ]; then
    echo "ERROR: OpenJDK source directory not found at ${OPENJDK_DIR}"
    exit 1
fi

# Verify boot JDK
if [ ! -d "${BOOT_JDK}" ]; then
    echo "ERROR: Boot JDK not found at ${BOOT_JDK}"
    echo "Please set JAVA_HOME or provide --boot-jdk option"
    exit 1
fi

cd "${OPENJDK_DIR}"

# Run configure if not already configured
if [ ! -f "build/.configure-support/config.status" ]; then
    echo "Running configure..."
    
    CONFIGURE_ARGS=(
        "--with-boot-jdk=${BOOT_JDK}"
        "--with-debug-level=${DEBUG_LEVEL}"
        "--with-jvm-variants=${JVM_VARIANTS}"
        "--enable-warnings-as-errors=no"
        "--with-vendor-name=Aprism"
        "--with-vendor-url=https://github.com/anomalyco/aprism"
        "--with-vendor-bug-url=https://github.com/anomalyco/aprism/issues"
        "--with-vendor-vm-bug-url=https://github.com/anomalyco/aprism/issues"
    )
    
    # Platform-specific configure options
    if [ "${PLATFORM}" = "windows" ]; then
        # On Windows, try to detect Visual Studio
        if [ -n "${VS_VERSION}" ]; then
            CONFIGURE_ARGS+=("--with-toolchain-version=${VS_VERSION}")
        fi
    fi
    
    bash configure "${CONFIGURE_ARGS[@]}"
    
    if [ $? -ne 0 ]; then
        echo "ERROR: Configure failed"
        exit 1
    fi
fi

# Build
echo "Building OpenJDK images..."
make images

if [ $? -ne 0 ]; then
    echo "ERROR: Build failed"
    exit 1
fi

# Show build output location
BUILD_OUTPUT=$(find build -maxdepth 1 -type d -name '*-server-*' | head -1)
if [ -n "${BUILD_OUTPUT}" ]; then
    echo ""
    echo "=== Build Successful ==="
    echo "JDK Location: ${BUILD_OUTPUT}/images/jdk"
    echo "JRE Location: ${BUILD_OUTPUT}/images/jre"
    echo "========================"
    
    # Verify the build
    if [ -f "${BUILD_OUTPUT}/images/jdk/bin/java" ]; then
        echo ""
        echo "Java version:"
        "${BUILD_OUTPUT}/images/jdk/bin/java" -version
    fi
else
    echo "ERROR: Could not locate build output"
    exit 1
fi
