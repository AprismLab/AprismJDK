# OpenJDK Source Setup

AprismJDK is built on top of OpenJDK. To build AprismJDK, you need to obtain the OpenJDK source code first.

## Obtaining OpenJDK Source

### For Java 25 (Primary Target)

```bash
# Clone OpenJDK 25 from the official repository
git clone --depth 1 --branch jdk-25+10 https://github.com/openjdk/jdk25u.git openjdk-25
```

### For Java 21 (LTS)

```bash
# Clone OpenJDK 21 LTS
git clone --depth 1 --branch jdk-21.0.5+11 https://github.com/openjdk/jdk21u.git openjdk-21
```

### For Java 17 (LTS)

```bash
# Clone OpenJDK 17 LTS
git clone --depth 1 --branch jdk-17.0.13+11 https://github.com/openjdk/jdk17u.git openjdk-17
```

## Why OpenJDK Source is Not Tracked

The OpenJDK source directories (`openjdk-*/`) are excluded from version control for several reasons:

1. **Size**: OpenJDK source is large (~500MB+) and would bloat the repository
2. **Upstream Tracking**: We track upstream OpenJDK separately
3. **Build Flexibility**: Different developers may work on different Java versions
4. **Clean Separation**: AprismJDK patches are separate from OpenJDK baseline

## Directory Structure

After cloning, your workspace should look like:

```
AprismJDK/
├── aprismate-agent/      # AprismJDK agent implementation
├── aprismate-api/        # Public API for mods
├── docs/                 # Documentation
├── scripts/              # Build scripts
├── openjdk-25/          # OpenJDK 25 source (git ignored)
├── openjdk-21/          # OpenJDK 21 source (git ignored, optional)
└── openjdk-17/          # OpenJDK 17 source (git ignored, optional)
```

## Verifying the Setup

Check that you have the correct OpenJDK version:

```bash
cd openjdk-25
git describe --tags
# Should show: jdk-25+10 or similar
```

## Next Steps

After obtaining the OpenJDK source:

1. Review [OpenJDK Build Requirements](./05-openjdk-build-requirements.md)
2. Install required build tools
3. Run `./gradlew buildOpenJDK` to build AprismJDK

## Updating OpenJDK Source

To update to a newer OpenJDK version:

```bash
cd openjdk-25
git fetch --tags
git checkout jdk-25+11  # or whatever the latest tag is
```

Then rebuild AprismJDK with the updated source.
