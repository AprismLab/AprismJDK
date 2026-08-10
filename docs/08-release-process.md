# AprismJDK Release Process Documentation (Doc 08)

> Complete release process for AprismJDK distributions.
> Covers artifact structure, packaging formats, signing, verification,
> and distribution workflow for JDK and JRE releases.
>
> Author: BlockConnect@StarsailsClover
> Delivered with: v26.0-Alpha.3
> Updated: 2026-08-11

---

## 1. Release Artifact Structure

AprismJDK produces two primary artifacts per release:

### 1.1 JDK (Java Development Kit)

**Purpose**: Complete development environment (compiler, tools, runtime)

**Contents**:
```
aprismjdk-<version>-<platform>-<arch>/
├── bin/                    # Executables
│   ├── java                # Java launcher
│   ├── javac               # Java compiler
│   ├── jar                 # JAR tool
│   ├── jlink               # JLink modular image creator
│   ├── jshell              # Java REPL
│   └── ...                 # Other JDK tools
├── lib/                    # Libraries and modules
│   ├── modules             # JImage module repository
│   ├── jrt-fs.jar          # JRT filesystem
│   ├── libjvm.so           # HotSpot VM (Linux)
│   └── ...
├── conf/                   # Configuration files
│   ├── security/           # Security policies
│   └── ...
├── include/                # C header files (JNI)
│   ├── jni.h
│   ├── jawt.h
│   └── ...
├── legal/                  # License files
├── release                 # Release metadata
└── README.md               # Distribution README
```

**Typical size**: 300-400 MB (compressed), 500-700 MB (unpacked)

### 1.2 JRE (Java Runtime Environment)

**Purpose**: Runtime-only environment (no compiler or development tools)

**Contents**:
```
aprismjre-<version>-<platform>-<arch>/
├── bin/
│   ├── java                # Java launcher only
│   └── keytool             # Basic tools
├── lib/                    # Same as JDK
├── conf/                   # Same as JDK
├── legal/
├── release
└── README.md
```

**Typical size**: 180-250 MB (compressed), 300-450 MB (unpacked)

**Difference from JDK**: JRE excludes `javac`, `jar`, `jlink`, `jshell`, `include/` headers, and other development tools.

---

## 2. Version Naming Convention

AprismJDK follows the Aprism project version control standard:

### 2.1 Version Format

```
v<MAJOR>.<MINOR>[-<PRE>][+<BUILD>]
```

**Components**:
- `MAJOR`: Major version (26)
- `MINOR`: Minor version (0, 1, 2, ...)
- `PRE`: Pre-release identifier (`Alpha.1`, `Alpha.2`, ..., `Alpha.9`)
- `BUILD`: Build number (optional, incremental)

**Examples**:
- `v26.0-Alpha.1` - First alpha of v26.0
- `v26.0-Alpha.9` - Ninth alpha of v26.0
- `v26.0` - GA release of v26.0
- `v26.1-Alpha.5` - Fifth alpha of v26.1
- `v26.1` - GA release of v26.1

### 2.2 Artifact Naming

Artifacts follow this naming pattern:

```
aprismjdk-<version>-<platform>-<arch>.<format>
aprismjre-<version>-<platform>-<arch>.<format>
```

**Platform identifiers**:
- `linux` - Linux distributions
- `windows` - Windows
- `macos` - macOS

**Architecture identifiers**:
- `x64` - x86_64/AMD64
- `aarch64` - ARM64
- `x86` - 32-bit x86 (if supported)

**Format**:
- `.tar.gz` - Linux/macOS tarball
- `.zip` - Windows/cross-platform ZIP
- `.dmg` - macOS disk image (optional)
- `.msi` - Windows installer (optional)

**Examples**:
- `aprismjdk-26.0-Alpha.1-linux-x64.tar.gz`
- `aprismjdk-26.0-Alpha.1-windows-x64.zip`
- `aprismjre-26.0-linux-aarch64.tar.gz`
- `aprismjdk-26.1-macos-x64.tar.gz`

---

## 3. Release Workflow

### 3.1 Pre-Release Checklist

Before initiating release:

- [ ] All planned features implemented and tested
- [ ] All tests pass (`make test-tier1 test-tier2`)
- [ ] Documentation updated (CHANGELOG.md, README.md)
- [ ] Version string updated in build configuration
- [ ] ROADMAP.md milestones marked complete
- [ ] Security review completed (if applicable)
- [ ] Legal review completed (licensing, attributions)

### 3.2 Build Release Artifacts

**Step 1: Clean build**

```bash
cd jdk/src
make dist-clean
```

**Step 2: Configure release build**

```bash
bash configure \
  --with-boot-jdk=/path/to/jdk-24 \
  --with-debug-level=release \
  --with-version-pre="Alpha.1" \
  --with-version-opt="AprismJDK" \
  --with-version-build=1 \
  --with-vendor-name="AprismLab" \
  --with-vendor-url="https://github.com/AprismLab/AprismJDK" \
  --with-vendor-bug-url="https://github.com/AprismLab/AprismJDK/issues" \
  --with-native-debug-symbols=external \
  --disable-warnings-as-errors
```

**Step 3: Build images**

```bash
make images JOBS=$(nproc)
```

**Step 4: Run tests**

```bash
make test-tier1
# Optional: make test-tier2
```

**Step 5: Package artifacts**

```bash
cd build/linux-x64-server-release/images/

# JDK tarball
tar -czf aprismjdk-26.0-Alpha.1-linux-x64.tar.gz jdk/

# JRE tarball
tar -czf aprismjre-26.0-Alpha.1-linux-x64.tar.gz jre/
```

For Windows (use 7-Zip or PowerShell):
```powershell
Compress-Archive -Path jdk -DestinationPath aprismjdk-26.0-Alpha.1-windows-x64.zip
Compress-Archive -Path jre -DestinationPath aprismjre-26.0-Alpha.1-windows-x64.zip
```

### 3.3 Generate Checksums

Generate SHA256 checksums for all artifacts:

```bash
sha256sum aprismjdk-26.0-Alpha.1-linux-x64.tar.gz > aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.sha256
sha256sum aprismjre-26.0-Alpha.1-linux-x64.tar.gz > aprismjre-26.0-Alpha.1-linux-x64.tar.gz.sha256
```

**Checksum file format**:
```
a1b2c3d4e5f6...  aprismjdk-26.0-Alpha.1-linux-x64.tar.gz
```

### 3.4 Sign Artifacts

**Option A: GPG signing** (recommended for open-source)

```bash
gpg --armor --detach-sign aprismjdk-26.0-Alpha.1-linux-x64.tar.gz
# Produces: aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.asc

gpg --armor --detach-sign aprismjre-26.0-Alpha.1-linux-x64.tar.gz
# Produces: aprismjre-26.0-Alpha.1-linux-x64.tar.gz.asc
```

**Option B: Code signing certificate** (for Windows/macOS installers)

Windows:
```powershell
signtool sign /f certificate.pfx /p password /tr http://timestamp.digicert.com aprismjdk-26.0-Alpha.1-windows-x64.zip
```

macOS:
```bash
codesign --sign "Developer ID Application: AprismLab" --timestamp aprismjdk-26.0-Alpha.1-macos-x64.tar.gz
```

### 3.5 Verify Artifacts

Before publishing, verify artifacts:

```bash
# Verify checksum
sha256sum -c aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.sha256

# Verify GPG signature
gpg --verify aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.asc aprismjdk-26.0-Alpha.1-linux-x64.tar.gz

# Extract and test
tar -xzf aprismjdk-26.0-Alpha.1-linux-x64.tar.gz
cd jdk/bin
./java -version
# Expected output: openjdk version "26.0-Alpha.1-AprismJDK" ...

# Run basic sanity test
echo 'public class Test { public static void main(String[] a) { System.out.println("OK"); } }' > Test.java
./javac Test.java
./java Test
# Expected output: OK
```

---

## 4. GitHub Release Process

### 4.1 Create Git Tag

```bash
git tag -a v26.0-Alpha.1 -m "AprismJDK v26.0-Alpha.1

Release notes:
- Initial API stubs for jdk.aprismate module
- Project structure and documentation
- Build system setup

See docs/ROADMAP.md for details."

git push origin v26.0-Alpha.1
```

### 4.2 Create GitHub Release

**Using GitHub CLI (`gh`)**:

```bash
gh release create v26.0-Alpha.1 \
  --title "AprismJDK v26.0-Alpha.1" \
  --notes-file RELEASE_NOTES.md \
  --prerelease \
  aprismjdk-26.0-Alpha.1-linux-x64.tar.gz \
  aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.sha256 \
  aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.asc \
  aprismjre-26.0-Alpha.1-linux-x64.tar.gz \
  aprismjre-26.0-Alpha.1-linux-x64.tar.gz.sha256 \
  aprismjre-26.0-Alpha.1-linux-x64.tar.gz.asc
```

**Using GitHub Web UI**:

1. Navigate to `https://github.com/AprismLab/AprismJDK/releases/new`
2. Select tag: `v26.0-Alpha.1`
3. Release title: `AprismJDK v26.0-Alpha.1`
4. Description: Copy from `RELEASE_NOTES.md`
5. Check "This is a pre-release" (for Alpha/Beta releases)
6. Attach artifacts (drag-and-drop)
7. Click "Publish release"

### 4.3 Release Notes Template

Create `RELEASE_NOTES.md` for each release:

```markdown
# AprismJDK v26.0-Alpha.1

**Release date**: 2026-08-11
**Release type**: Pre-Release (Alpha 1)

## Overview

First alpha release of AprismJDK v26.0 line. Establishes project structure, API surface definition, and documentation foundation.

## What's New

- **jdk.aprismate module**: API stubs for `VmInfo` and `Agent` interfaces
- **Documentation**: Architecture, agent design, opened interfaces, compatibility matrix
- **Build system**: Gradle multi-module setup with JUnit 5 testing
- **CI/CD**: GitHub Actions workflow for automated builds and tests

## Deliverables

- JDK distribution (includes compiler and tools)
- JRE distribution (runtime only)
- Documentation (English + Chinese)

## Download

| Platform | Architecture | JDK | JRE |
|----------|--------------|-----|-----|
| Linux | x64 | [tar.gz](link) \| [sha256](link) \| [asc](link) | [tar.gz](link) \| [sha256](link) \| [asc](link) |
| Windows | x64 | [zip](link) \| [sha256](link) \| [asc](link) | [zip](link) \| [sha256](link) \| [asc](link) |
| macOS | x64 | [tar.gz](link) \| [sha256](link) \| [asc](link) | [tar.gz](link) \| [sha256](link) \| [asc](link) |

## Verification

### Checksum verification

```bash
sha256sum -c aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.sha256
```

### GPG signature verification

```bash
# Import AprismLab public key
curl https://aprismlab.org/pgp-key.asc | gpg --import

# Verify signature
gpg --verify aprismjdk-26.0-Alpha.1-linux-x64.tar.gz.asc aprismjdk-26.0-Alpha.1-linux-x64.tar.gz
```

## Installation

### Linux / macOS

```bash
tar -xzf aprismjdk-26.0-Alpha.1-linux-x64.tar.gz
export JAVA_HOME=$PWD/jdk
export PATH=$JAVA_HOME/bin:$PATH
java -version
```

### Windows

```powershell
Expand-Archive aprismjdk-26.0-Alpha.1-windows-x64.zip
$env:JAVA_HOME = "$PWD\jdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

## Known Issues

- None (initial release)

## Next Release

- **v26.0-Alpha.2**: Dependency management and module system (ETA: 2026-08-18)

## Documentation

- [Architecture Overview](docs/01-architecture.md)
- [Aprismate Agent Design](docs/02-aprismate-agent.md)
- [Opened Interfaces](docs/03-opened-interfaces.md)
- [Build System](docs/07-build-system.md)
- [Roadmap](docs/ROADMAP.md)

## Feedback

Report issues at: https://github.com/AprismLab/AprismJDK/issues
```

---

## 5. Automated Release with GitHub Actions

### 5.1 Release Workflow (`.github/workflows/release.yml`)

```yaml
name: Release AprismJDK

on:
  push:
    tags:
      - 'v*.*.*'
      - 'v*.*.*-Alpha.*'
      - 'v*.*.*-Beta.*'

jobs:
  build-and-release:
    strategy:
      matrix:
        os: [ubuntu-22.04, windows-2022, macos-13]
        include:
          - os: ubuntu-22.04
            platform: linux
            arch: x64
            artifact_ext: tar.gz
          - os: windows-2022
            platform: windows
            arch: x64
            artifact_ext: zip
          - os: macos-13
            platform: macos
            arch: x64
            artifact_ext: tar.gz
    
    runs-on: ${{ matrix.os }}
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          submodules: recursive
      
      - name: Set up Boot JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '24'
      
      - name: Install build dependencies (Linux)
        if: runner.os == 'Linux'
        run: |
          sudo apt-get update
          sudo apt-get install -y \
            build-essential autoconf zip unzip \
            libx11-dev libxext-dev libxrender-dev libxtst-dev \
            libasound2-dev libfontconfig1-dev libfreetype6-dev libcups2-dev
      
      - name: Apply patches and configure
        run: |
          cd jdk/src
          for patch in ../patches/*.patch; do
            git apply "$patch"
          done
          cp -r ../overlay/src/* .
          
          bash configure \
            --with-boot-jdk=$JAVA_HOME \
            --with-debug-level=release \
            --with-version-pre="${GITHUB_REF_NAME#v*-}" \
            --with-version-opt="AprismJDK" \
            --with-vendor-name="AprismLab" \
            --disable-warnings-as-errors
      
      - name: Build JDK
        run: |
          cd jdk/src
          make images JOBS=$(nproc || sysctl -n hw.ncpu || echo 4)
      
      - name: Run tests
        run: |
          cd jdk/src
          make test-tier1
      
      - name: Package artifacts
        run: |
          cd jdk/src/build/*/images/
          
          if [ "${{ runner.os }}" = "Windows" ]; then
            7z a aprismjdk-${{ github.ref_name }}-${{ matrix.platform }}-${{ matrix.arch }}.zip jdk/
            7z a aprismjre-${{ github.ref_name }}-${{ matrix.platform }}-${{ matrix.arch }}.zip jre/
          else
            tar -czf aprismjdk-${{ github.ref_name }}-${{ matrix.platform }}-${{ matrix.arch }}.tar.gz jdk/
            tar -czf aprismjre-${{ github.ref_name }}-${{ matrix.platform }}-${{ matrix.arch }}.tar.gz jre/
          fi
        shell: bash
      
      - name: Generate checksums
        run: |
          cd jdk/src/build/*/images/
          sha256sum aprismjdk-* > checksums.txt
          sha256sum aprismjre-* >> checksums.txt
        shell: bash
      
      - name: Sign artifacts (GPG)
        if: runner.os == 'Linux'
        run: |
          echo "${{ secrets.GPG_PRIVATE_KEY }}" | gpg --import
          cd jdk/src/build/*/images/
          for file in aprismjdk-*.tar.gz aprismjre-*.tar.gz; do
            gpg --armor --detach-sign "$file"
          done
      
      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: aprismjdk-${{ matrix.platform }}-${{ matrix.arch }}
          path: |
            jdk/src/build/*/images/aprismjdk-*
            jdk/src/build/*/images/aprismjre-*
            jdk/src/build/*/images/checksums.txt
  
  create-release:
    needs: build-and-release
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Download all artifacts
        uses: actions/download-artifact@v4
        with:
          path: artifacts/
      
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        with:
          name: AprismJDK ${{ github.ref_name }}
          body_path: RELEASE_NOTES.md
          prerelease: ${{ contains(github.ref_name, 'Alpha') || contains(github.ref_name, 'Beta') }}
          files: |
            artifacts/**/*
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### 5.2 Workflow Triggers

The release workflow triggers on:
- Tag push matching `v*.*.*` (GA releases)
- Tag push matching `v*.*.*-Alpha.*` (Alpha releases)
- Tag push matching `v*.*.*-Beta.*` (Beta releases, if used)

**To trigger a release**:
```bash
git tag v26.0-Alpha.1
git push origin v26.0-Alpha.1
```

GitHub Actions will automatically:
1. Build JDK/JRE for Linux, Windows, macOS
2. Run tests
3. Package artifacts
4. Generate checksums
5. Sign artifacts (if GPG key configured)
6. Create GitHub Release with all artifacts attached

---

## 6. Distribution Channels

### 6.1 Primary Distribution

**GitHub Releases** (primary):
- URL: `https://github.com/AprismLab/AprismJDK/releases`
- All releases (Pre-Release + GA)
- Automatic via GitHub Actions

### 6.2 Secondary Distribution (Future)

**Package managers** (post-GA):
- **SDKMAN!** (Linux/macOS): `sdk install java 26.0-aprism`
- **Homebrew** (macOS): `brew install aprismjdk`
- **Chocolatey** (Windows): `choco install aprismjdk`
- **Scoop** (Windows): `scoop install aprismjdk`

**Docker images** (post-GA):
- Docker Hub: `docker pull aprismlab/aprismjdk:26.0`
- GitHub Container Registry: `docker pull ghcr.io/aprismlab/aprismjdk:26.0`

### 6.3 Official Website (Future)

**Download portal** (post-GA):
- URL: `https://aprismjdk.org/download`
- Release archive
- Installation guides
- Verification instructions

---

## 7. Post-Release Tasks

After publishing release:

- [ ] Update README.md with latest release link
- [ ] Announce on project channels (Discord, Twitter, etc.)
- [ ] Update ROADMAP.md status
- [ ] Create CHANGELOG.md entry
- [ ] Update documentation site (if exists)
- [ ] Monitor issue tracker for release feedback
- [ ] Plan next release milestone

---

## 8. Release Cadence

AprismJDK follows a **time-based release schedule**:

### 8.1 v26.0 Line (10 releases)

| Release | Type | Target Date | Duration |
|---------|------|-------------|----------|
| v26.0-Alpha.1 | Pre-Release | 2026-08-11 | - |
| v26.0-Alpha.2 | Pre-Release | 2026-08-18 | 1 week |
| v26.0-Alpha.3 | Pre-Release | 2026-08-25 | 1 week |
| v26.0-Alpha.4 | Pre-Release | 2026-09-01 | 1 week |
| v26.0-Alpha.5 | Pre-Release | 2026-09-08 | 1 week |
| v26.0-Alpha.6 | Pre-Release | 2026-09-15 | 1 week |
| v26.0-Alpha.7 | Pre-Release | 2026-09-22 | 1 week |
| v26.0-Alpha.8 | Pre-Release | 2026-09-29 | 1 week |
| v26.0-Alpha.9 | Pre-Release | 2026-10-06 | 1 week |
| v26.0 | GA Release | 2026-10-13 | 1 week |

### 8.2 v26.1 Line (10 releases)

| Release | Type | Target Date | Duration |
|---------|------|-------------|----------|
| v26.1-Alpha.1 | Pre-Release | 2026-10-20 | 1 week |
| v26.1-Alpha.2 | Pre-Release | 2026-10-27 | 1 week |
| ... | ... | ... | ... |
| v26.1-Alpha.9 | Pre-Release | 2026-12-15 | 1 week |
| v26.1 | GA Release | 2026-12-22 | 1 week |

**Total timeline**: ~4.5 months (20 releases)

---

## 9. Quality Gates

Before tagging a release, ensure:

### 9.1 Code Quality

- [ ] All CI checks pass (build, test, lint)
- [ ] No P0/P1 bugs outstanding
- [ ] Code review completed for all changes
- [ ] Static analysis clean (no critical warnings)

### 9.2 Testing

- [ ] Unit tests pass (100% for new code)
- [ ] Integration tests pass
- [ ] Tier1 tests pass (jtreg suite)
- [ ] Tier2 tests pass (for GA releases)
- [ ] Manual smoke testing completed

### 9.3 Documentation

- [ ] API documentation complete (Javadoc)
- [ ] User documentation updated
- [ ] CHANGELOG.md updated
- [ ] RELEASE_NOTES.md prepared
- [ ] ROADMAP.md milestones marked

### 9.4 Legal and Security

- [ ] License compliance verified (OpenJDK GPL+CE)
- [ ] Attribution files complete (LEGAL/)
- [ ] Security scan clean (no vulnerabilities)
- [ ] Export compliance verified (if applicable)

---

## 10. Rollback Procedure

If a critical issue is discovered after release:

### 10.1 Immediate Actions

1. **Mark release as defective**:
   - Edit GitHub Release, add "⚠️ DEFECTIVE RELEASE" warning
   - Update release notes with issue description
   
2. **Notify users**:
   - Post announcement on all channels
   - Update documentation with warnings

### 10.2 Remediation

**Option A: Hotfix release** (preferred for GA)
```bash
git checkout v26.0
git cherry-pick <fix-commit>
git tag v26.0.1
git push origin v26.0.1
# Trigger release process
```

**Option B: Yank release** (for Pre-Release only)
```bash
gh release delete v26.0-Alpha.5 --yes
git tag -d v26.0-Alpha.5
git push origin :refs/tags/v26.0-Alpha.5
# Re-release as v26.0-Alpha.5 (same tag)
```

**Option C: Skip to next release** (for Alpha)
- Document issue in current release notes
- Fix in next Alpha release
- Update ROADMAP.md

---

## 11. References

- [OpenJDK Release Process](https://openjdk.org/projects/jdk/21/release-notes)
- [Semantic Versioning](https://semver.org/)
- [AprismJDK Build System (Doc 07)](07-build-system.md)
- [AprismJDK Architecture (Doc 01)](01-architecture.md)
- [Aprism Version Control Standard](../../docs/version-control.md)

---

**Document status**: Delivered with v26.0-Alpha.3
**Next review**: v26.0-Alpha.5 (after first real release)
