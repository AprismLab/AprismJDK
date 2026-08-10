# AprismJDK Build System Documentation (Doc 07)

> Complete guide to building AprismJDK from OpenJDK source.
> Covers build system architecture, patch strategy, toolchain requirements,
> and cross-platform considerations.
>
> Author: BlockConnect@StarsailsClover
> Delivered with: v26.0-Alpha.3
> Updated: 2026-08-11

---

## 1. OpenJDK Build System Overview

OpenJDK uses a custom build system based on **GNU Make** and **autoconf**:

- **Configure phase** (`bash configure`): Detects toolchain, boot JDK, and platform capabilities; generates build configuration
- **Build phase** (`make images`): Compiles native code (C/C++), Java code, and assembles JDK/JRE images
- **Test phase** (`make test`): Runs tier1/tier2/tier3 test suites

### 1.1 Build Output Structure

```
build/
├── <platform>-<arch>-<variant>/     # e.g., linux-x64-server-release
│   ├── configure-support/           # Configuration artifacts
│   ├── hotspot/                     # HotSpot VM build artifacts
│   ├── jdk/                         # JDK class libraries build artifacts
│   ├── images/                      # Final distributable images
│   │   ├── jdk/                     # Full JDK (includes javac, tools)
│   │   └── jre/                     # JRE only (runtime environment)
│   ├── support/                     # Intermediate build files
│   └── make-support/                # Make infrastructure
```

### 1.2 Key Build Targets

| Target | Description | Typical Use |
|--------|-------------|-------------|
| `images` | Build full JDK and JRE images | Standard release build |
| `jdk-image` | Build JDK image only | When JRE not needed |
| `jre-image` | Build JRE image only | Runtime-only distribution |
| `bootcycle-images` | Build JDK, then rebuild with itself | Validation build |
| `test` | Run test suites | CI and release validation |
| `clean` | Clean build artifacts | Fresh rebuild |
| `dist-clean` | Clean everything including configuration | Reset build state |

### 1.3 Build Variants

| Variant | Optimization | Debug Info | Use Case |
|---------|--------------|------------|----------|
| `release` | Full (`-O3`) | Minimal | Production distribution |
| `fastdebug` | Moderate (`-O2`) | Full | Development and debugging |
| `slowdebug` | None (`-O0`) | Full | Deep VM debugging |

---

## 2. AprismJDK Patch Strategy

AprismJDK modifications are applied through a **patch overlay** system:

### 2.1 Patch Directory Structure

```
jdk/
├── src/                             # OpenJDK source (git submodule or subtree)
├── patches/                         # AprismJDK patches
│   ├── 001-version-branding.patch   # Version string modification
│   ├── 002-add-aprismate-module.patch
│   ├── 003-export-aprismate-api.patch
│   ├── 004-integrate-agent.patch
│   ├── 005-agent-manifest.patch
│   ├── 006-vm-structural-redefinition.patch
│   ├── 007-jit-aware-hooks.patch
│   ├── 008-thread-statistics.patch
│   ├── 009-heap-region-info.patch
│   └── README.md                    # Patch documentation
├── overlay/                         # File overlays (alternative to patches)
│   └── src/
│       └── jdk.aprismate/           # New modules added by AprismJDK
└── make/                            # Build script modifications
    └── ajr-build.sh                 # AprismJDK build wrapper
```

### 2.2 Patch Application Workflow

**Option A: Git patch application** (preferred for small changes)
```bash
cd jdk/src
git apply ../patches/001-version-branding.patch
git apply ../patches/002-add-aprismate-module.patch
# ... apply all patches
```

**Option B: Overlay copy** (preferred for new modules)
```bash
cp -r jdk/overlay/src/* jdk/src/
```

**Option C: Hybrid approach** (recommended)
- Use patches for modifications to existing OpenJDK files
- Use overlays for new AprismJDK-specific modules

### 2.3 Patch Maintenance

- Each patch must be **self-documenting** (header comments explain purpose)
- Patches are **versioned** (named `NNN-description.patch` where NNN is sequence)
- Patches must **apply cleanly** on target OpenJDK tag (e.g., `jdk-25+<build>`)
- When rebasing to new OpenJDK version, patches are re-generated and tested

---

## 3. Toolchain Requirements

### 3.1 Linux Build Requirements

**Boot JDK**: OpenJDK N-1 (for building JDK N)
- Building JDK 25: Requires JDK 24 or JDK 23

**Compiler**:
- GCC 11.x - 13.x (recommended: GCC 13)
- Clang 15.x - 17.x (alternative)

**Build Tools**:
- GNU Make 4.0+
- autoconf 2.69+
- m4
- pkg-config

**Utilities**:
- zip, unzip
- tar
- gawk
- sed
- grep
- file

**Libraries**:
- X11 development headers (`libx11-dev`, `libxext-dev`, `libxrender-dev`, `libxtst-dev`)
- ALSA development headers (`libasound2-dev`)
- Fontconfig development headers (`libfontconfig1-dev`)
- Freetype development headers (`libfreetype6-dev`)
- CUPS development headers (`libcups2-dev`)

**Example (Ubuntu 22.04)**:
```bash
sudo apt-get update
sudo apt-get install -y \
  build-essential \
  autoconf \
  zip unzip \
  libx11-dev libxext-dev libxrender-dev libxtst-dev \
  libasound2-dev \
  libfontconfig1-dev libfreetype6-dev \
  libcups2-dev
```

### 3.2 Windows Build Requirements

**Boot JDK**: Same as Linux (JDK N-1)

**Compiler**:
- Visual Studio 2022 (Community, Professional, or Enterprise)
  - Workload: "Desktop development with C++"
  - Components: MSVC v143, Windows 11 SDK

**POSIX Layer** (one of):
- **MSYS2** (recommended): Lightweight, modern
- **Cygwin**: Traditional, well-tested

**Build Tools** (via MSYS2 or Cygwin):
- GNU Make
- autoconf
- zip, unzip
- gawk, sed, grep

**Example (MSYS2 setup)**:
```bash
# Install MSYS2 from https://www.msys2.org/
# Then in MSYS2 shell:
pacman -S base-devel autoconf zip unzip
```

### 3.3 macOS Build Requirements

**Boot JDK**: Same as Linux (JDK N-1)

**Compiler**:
- Xcode 14.x - 15.x
- Command Line Tools for Xcode

**Build Tools** (via Homebrew):
```bash
brew install autoconf make
```

**Note**: macOS uses Apple Clang; GNU Make may conflict with system `make`, use `gmake` alias.

---

## 4. Boot JDK Selection

### 4.1 Boot JDK Version Rules

OpenJDK follows the **N-1 rule**: Building JDK version N requires JDK version N-1 (or sometimes N-2 if N-1 not yet released).

| Target JDK | Minimum Boot JDK | Recommended Boot JDK |
|------------|------------------|----------------------|
| JDK 25 | JDK 23 | JDK 24 (if available) |
| JDK 21 | JDK 20 | JDK 20 or JDK 21 |
| JDK 17 | JDK 16 | JDK 17 (self-hosting) |

### 4.2 Boot JDK Location

The build system searches for Boot JDK in:
1. `--with-boot-jdk=<path>` configure option (explicit)
2. `JAVA_HOME` environment variable
3. Common installation paths (`/usr/lib/jvm/`, `C:\Program Files\Java\`, etc.)

**Recommendation**: Always specify explicitly via `--with-boot-jdk` to avoid version confusion.

---

## 5. Build Profiles and Configuration

### 5.1 Standard Release Build

```bash
cd jdk/src
bash configure \
  --with-boot-jdk=/path/to/jdk-24 \
  --with-version-pre="" \
  --with-version-opt="AprismJDK" \
  --with-vendor-name="AprismLab" \
  --with-vendor-url="https://github.com/AprismLab/AprismJDK" \
  --with-vendor-bug-url="https://github.com/AprismLab/AprismJDK/issues" \
  --with-version-build=1 \
  --disable-warnings-as-errors

make images
```

### 5.2 Debug Build (fastdebug)

```bash
bash configure \
  --with-boot-jdk=/path/to/jdk-24 \
  --with-debug-level=fastdebug \
  --with-native-debug-symbols=internal \
  --disable-warnings-as-errors

make images
```

### 5.3 Configuration Options Reference

| Option | Purpose | Example |
|--------|---------|---------|
| `--with-boot-jdk` | Specify Boot JDK path | `--with-boot-jdk=/usr/lib/jvm/jdk-24` |
| `--with-debug-level` | Set optimization level | `release`, `fastdebug`, `slowdebug` |
| `--with-version-pre` | Pre-release identifier | `--with-version-pre="Alpha.1"` |
| `--with-version-opt` | Optional version string | `--with-version-opt="AprismJDK"` |
| `--with-vendor-name` | Vendor name | `--with-vendor-name="AprismLab"` |
| `--with-native-debug-symbols` | Debug symbol handling | `internal`, `external`, `zipped` |
| `--disable-warnings-as-errors` | Allow warnings (patch compatibility) | (no value) |
| `--with-toolchain-type` | Force toolchain | `gcc`, `clang`, `microsoft` |
| `--with-jobs` | Parallel build jobs | `--with-jobs=8` |

---

## 6. Cross-Compilation

### 6.1 Cross-Compilation Overview

Cross-compilation builds JDK for a different platform/architecture than the build host:
- Example: Build ARM64 JDK on x64 Linux

**Requirements**:
- **Build JDK**: Native JDK for build platform (runs build tools like `javac`)
- **Boot JDK**: JDK for target platform (used as baseline)
- **Target toolchain**: Cross-compiler (e.g., `aarch64-linux-gnu-gcc`)

### 6.2 Cross-Compilation Example (Linux x64 → ARM64)

```bash
bash configure \
  --with-boot-jdk=/path/to/jdk-24-aarch64 \
  --openjdk-target=aarch64-linux-gnu \
  --with-toolchain-type=gcc \
  --with-sysroot=/usr/aarch64-linux-gnu \
  --disable-warnings-as-errors

make images
```

### 6.3 macOS Universal Binaries

macOS supports **universal binaries** (x64 + ARM64 in one binary):

**Option A: Separate builds + lipo merge**
```bash
# Build x64
bash configure --openjdk-target=x86_64-apple-darwin
make images

# Build ARM64
bash configure --openjdk-target=aarch64-apple-darwin
make images

# Merge with lipo (custom script)
```

**Option B: Use Xcode multi-arch support** (OpenJDK 17+ has limited support)

---

## 7. Build Performance Optimization

### 7.1 Parallel Build

By default, `make` uses all available CPU cores. Adjust with:

```bash
make JOBS=8 images
```

Or configure at configure time:
```bash
bash configure --with-jobs=8
```

### 7.2 Incremental Builds

After initial full build, incremental builds are much faster:

```bash
# Modify source
# Then rebuild only changed parts:
make images
```

**Important**: Incremental builds may miss dependencies. For release builds, always use **clean build**:

```bash
make clean
make images
```

### 7.3 Build Time Estimates

| Platform | Configuration | Build Time (estimate) |
|----------|---------------|------------------------|
| Linux x64 (16 cores) | release | 45-60 minutes |
| Linux x64 (16 cores) | fastdebug | 50-70 minutes |
| Windows x64 (16 cores) | release | 60-90 minutes |
| macOS ARM64 (10 cores) | release | 30-45 minutes |

**Note**: First build is always slowest; subsequent builds benefit from caching.

### 7.4 Build Caching (CI)

For CI environments (GitHub Actions), cache these directories:

```yaml
- name: Cache build artifacts
  uses: actions/cache@v3
  with:
    path: |
      jdk/build/**/configure-support
      jdk/build/**/support
      ~/.gradle/caches
    key: jdk-build-${{ runner.os }}-${{ hashFiles('jdk/src/**/*.java') }}
```

---

## 8. AprismJDK-Specific Build Workflow

### 8.1 AprismJDK Build Script (`ajr-build.sh`)

AprismJDK provides a **wrapper script** to simplify builds:

```bash
#!/bin/bash
# ajr-build.sh - AprismJDK build wrapper

set -e

JDK_SRC="jdk/src"
BOOT_JDK="${BOOT_JDK:-/usr/lib/jvm/jdk-24}"
BUILD_VARIANT="${BUILD_VARIANT:-release}"
APRISM_VERSION="${APRISM_VERSION:-26.0-Alpha.1}"

echo "==> Applying AprismJDK patches..."
cd "$JDK_SRC"
for patch in ../patches/*.patch; do
  echo "Applying $patch..."
  git apply --check "$patch" || { echo "Patch check failed: $patch"; exit 1; }
  git apply "$patch"
done

echo "==> Copying overlay files..."
cp -r ../overlay/src/* .

echo "==> Configuring build..."
bash configure \
  --with-boot-jdk="$BOOT_JDK" \
  --with-debug-level="$BUILD_VARIANT" \
  --with-version-pre="${APRISM_VERSION##*-}" \
  --with-version-opt="AprismJDK" \
  --with-vendor-name="AprismLab" \
  --with-vendor-url="https://github.com/AprismLab/AprismJDK" \
  --disable-warnings-as-errors

echo "==> Building JDK..."
make images JOBS="${JOBS:-$(nproc)}"

echo "==> Build complete!"
echo "JDK image: build/*/images/jdk/"
echo "JRE image: build/*/images/jre/"
```

Usage:
```bash
export BOOT_JDK=/path/to/jdk-24
export BUILD_VARIANT=release
export APRISM_VERSION=26.0-Alpha.1
bash jdk/make/ajr-build.sh
```

### 8.2 Build Verification

After build completes, verify:

```bash
# Check version string
build/linux-x64-server-release/images/jdk/bin/java -version

# Expected output:
# openjdk version "25-AprismJDK" 2025-09-16
# OpenJDK Runtime Environment (build 25-AprismJDK+1-AprismLab)
# OpenJDK 64-Bit Server VM (build 25-AprismJDK+1-AprismLab, mixed mode, sharing)

# Check jdk.aprismate module
build/linux-x64-server-release/images/jdk/bin/java --list-modules | grep aprismate

# Expected output:
# jdk.aprismate@25-AprismJDK

# Run basic test
echo 'public class Test { public static void main(String[] args) { System.out.println("Hello AprismJDK!"); } }' > Test.java
build/linux-x64-server-release/images/jdk/bin/javac Test.java
build/linux-x64-server-release/images/jdk/bin/java Test

# Expected output:
# Hello AprismJDK!
```

---

## 9. Troubleshooting

### 9.1 Common Build Failures

**Problem**: `configure: error: Cannot locate a valid Boot JDK`

**Solution**: Specify Boot JDK explicitly:
```bash
bash configure --with-boot-jdk=/path/to/jdk-24
```

---

**Problem**: `error: invalid use of incomplete type 'struct ...`

**Solution**: Compiler version mismatch. Use supported GCC/Clang version (see §3.1).

---

**Problem**: `fatal error: X11/Xlib.h: No such file or directory`

**Solution**: Install X11 development headers:
```bash
sudo apt-get install libx11-dev libxext-dev
```

---

**Problem**: Patch application fails with conflicts

**Solution**: Patches may be out of sync with OpenJDK version. Rebase patches:
```bash
cd jdk/src
git fetch --tags
git checkout jdk-25+<latest-build>
cd ../patches
# Regenerate patches manually
```

---

**Problem**: Build succeeds but `java -version` crashes

**Solution**: Likely ABI incompatibility. Rebuild from clean state:
```bash
make dist-clean
bash configure ...
make images
```

---

### 9.2 Performance Issues

**Problem**: Build takes >2 hours

**Solution**: Increase parallelism:
```bash
make JOBS=16 images
```

**Problem**: Out of memory during build

**Solution**: Reduce parallel jobs:
```bash
make JOBS=4 images
```

---

## 10. References

- [OpenJDK Build Documentation](https://github.com/openjdk/jdk/blob/master/doc/building.md)
- [OpenJDK Testing Documentation](https://github.com/openjdk/jdk/blob/master/doc/testing.md)
- [AprismJDK Architecture (Doc 01)](01-architecture.md)
- [AprismJDK Release Process (Doc 08)](08-release-process.md)

---

**Document status**: Delivered with v26.0-Alpha.3
**Next review**: v26.0-Alpha.5 (after first OpenJDK build)
