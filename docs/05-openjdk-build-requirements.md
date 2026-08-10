# OpenJDK Build Requirements

This document describes the requirements and setup process for building OpenJDK on different platforms.

## Overview

AprismJDK integrates customized OpenJDK builds for Java 17, 21, and 25. Building OpenJDK from source requires specific toolchains, build tools, and environment setup.

## Platform Requirements

### Windows

#### Required Tools

1. **Visual Studio 2019-2022**
   - Minimum: Visual Studio 2019 version 16.8 (MSVC 14.28)
   - Maximum: Visual Studio 2022
   - Required components:
     - Desktop development with C++
     - Windows SDK (10.0.17763.0 or newer)
     - US English language pack (mandatory)

2. **Bash Environment** (choose one):
   - **Cygwin** (recommended for OpenJDK build)
     - Required packages: make, m4, autoconf, zip, unzip
     - Git client options: Cygwin git or Git for Windows
   - **MSYS2** (alternative)
   - **WSL2** (Windows Subsystem for Linux)

3. **Boot JDK**
   - To build JDK N, you need JDK N-1
   - For JDK 25: Need JDK 21 or later
   - For JDK 21: Need JDK 20 or later
   - For JDK 17: Need JDK 16 or later

4. **Additional Tools**
   - Git
   - make (via Cygwin/MSYS2)
   - autoconf

### Linux

#### Required Tools

1. **GCC**
   - Minimum: GCC 11.x
   - Recommended: GCC 13.2.0 or later
   - C11 and C++14 support required

2. **Build Tools**
   - make
   - autoconf
   - m4
   - pkg-config

3. **Libraries**
   - libX11, libXext, libXrender, libXrandr, libXtst, libXt, libXi
   - CUPS (Common UNIX Printing System)
   - fontconfig
   - ALSA (Advanced Linux Sound Architecture)
   - freetype

4. **Boot JDK** (same as Windows)

### macOS

#### Required Tools

1. **Xcode**
   - Minimum: Xcode 12.x
   - Recommended: Xcode 14.3.1 or later (using clang 14.0.3)
   - Command Line Tools installed

2. **Build Tools**
   - autoconf (via Homebrew: `brew install autoconf`)

3. **Boot JDK** (same as Windows)

## Build Process

### 1. Configure

```bash
cd openjdk-<version>
bash configure \
  --with-boot-jdk=/path/to/boot-jdk \
  --with-toolchain-version=2022 \
  --enable-warnings-as-errors=no
```

Common configure options:
- `--with-boot-jdk=<path>`: Specify boot JDK location
- `--with-toolchain-version=<year>`: Select Visual Studio version (Windows)
- `--with-jvm-variants=<variants>`: Build specific JVM variants (server, client, zero)
- `--with-debug-level=<level>`: Set debug level (release, fastdebug, slowdebug)
- `--enable-ccache`: Use ccache for faster rebuilds

### 2. Build

```bash
# Full JDK image (includes JRE)
make images

# JRE only
make jre-image

# Specific components
make hotspot
make java.base
```

### 3. Test

```bash
# Run tier 1 tests (quick smoke tests)
make test-tier1

# Run tier 2 tests (more comprehensive)
make test-tier2

# Run specific test suites
make test TEST="jdk_lang"
```

### 4. Verify

```bash
# Check JDK version
./build/*/images/jdk/bin/java -version

# Check JRE version
./build/*/images/jre/bin/java -version
```

## Build Output

After successful build, artifacts are located in:

```
openjdk-<version>/
└── build/
    └── <platform>-<arch>-server-<debug-level>/
        ├── images/
        │   ├── jdk/           # Full JDK
        │   └── jre/           # JRE runtime
        ├── support/           # Intermediate build files
        └── configure-support/ # Configure cache
```

## Troubleshooting

### Windows: Visual Studio Not Detected

**Problem**: `configure` cannot find Visual Studio even though it's installed.

**Solutions**:
1. Check for spaces in path - use `subst` to create short paths
2. Install US English language pack
3. Explicitly specify toolchain version: `--with-toolchain-version=2022`
4. Check Visual C++ build tools are installed

### Windows: Cygwin Issues

**Problem**: Path conversion or line ending errors.

**Solutions**:
1. Use Cygwin git, not Git for Windows
2. Ensure `core.autocrlf=false` in git config
3. Create directories using Cygwin `mkdir`, not Windows Explorer
4. Don't put source under Cygwin home directory if username has spaces

### Boot JDK Not Found

**Problem**: `configure` fails with "Cannot locate a valid Boot JDK".

**Solutions**:
1. Set `JAVA_HOME` environment variable
2. Use `--with-boot-jdk=/path/to/jdk` explicitly
3. Ensure boot JDK version is N-1 for building JDK N

### Out of Memory During Build

**Problem**: Build fails with Java heap space errors.

**Solutions**:
1. Reduce parallel jobs: `make JOBS=4 images`
2. Increase heap size: `export _JAVA_OPTIONS="-Xmx4g"`
3. Use fastdebug or release build instead of slowdebug

## AprismJDK Integration

For AprismJDK, we will:

1. **Checkout OpenJDK** for each target version (17, 21, 25)
2. **Apply Aprism patches** for custom features
3. **Build with Aprism branding** (version strings, etc.)
4. **Package as distributions**:
   - `aprism-jdk-<version>-<platform>.tar.gz` (full JDK)
   - `aprism-jre-<version>-<platform>.tar.gz` (runtime only)
5. **Integrate with Gradle build** via custom tasks

## References

- [OpenJDK Building Guide](https://github.com/openjdk/jdk/blob/master/doc/building.md)
- [OpenJDK Testing Guide](https://github.com/openjdk/jdk/blob/master/doc/testing.md)
- [Adopt OpenJDK Build Scripts](https://github.com/adoptium/temurin-build)

## Next Steps

For v26.0-Alpha.2, we will:
1. Create build wrapper scripts
2. Implement basic OpenJDK integration in Gradle
3. Generate versioned JDK/JRE packages
4. Test on Windows with JDK 21 boot
