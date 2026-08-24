# Compatibility Matrix

## Overview

AprismJDK v26.0 is designed with cross-version compatibility as a core principle. This document outlines the compatibility guarantees and test coverage across different Java versions and JDK distributions.

## Supported Java Versions

| Java Version | Support Status | Notes |
|--------------|----------------|-------|
| Java 25      | Primary Target | Full feature set, optimized performance |
| Java 21 LTS  | Fully Supported | All APIs available, extensive testing |
| Java 17 LTS  | Fully Supported | Backward-compatible API surface |
| Java 11 LTS  | Not Supported  | Use Java 17+ |
| Java 8       | Not Supported  | Use Java 17+ |

## JDK Distribution Compatibility

### AprismJDK

| Feature | v26.0-Alpha.8 | v26.0-GA (planned) |
|---------|---------------|-------------------|
| VmInfo API | ✅ Full | ✅ Full |
| Event System | ✅ Full | ✅ Full |
| Mod Loading | ✅ Full | ✅ Full |
| Resource Management | ✅ Full | ✅ Full |
| Configuration | ✅ Full | ✅ Full |
| Network API | ✅ Full | ✅ Full |
| Data Serialization | ⏳ Planned | ✅ Full |
| Security Framework | ⏳ Planned | ✅ Full |

### Stock JDK (Oracle, OpenJDK, Adoptium, etc.)

All AprismJDK APIs are designed to degrade gracefully when running on Stock JDK:

| API Category | Behavior on Stock JDK |
|--------------|----------------------|
| `VmInfo` | Returns system properties, all capability flags return `false` |
| `EventBus` | Basic event dispatching works (no VM-level optimization) |
| `ModLoader` | Classpath-based loading only |
| `ResourcePool` | Standard pooling implementation |
| `ConfigManager` | File-based configuration works |
| `PacketRegistry` | Standard serialization (no VM optimization) |

**Key Guarantee**: No `NullPointerException` or `UnsupportedOperationException` when running on Stock JDK. All APIs return safe defaults.

## API Compatibility

### Binary Compatibility

AprismJDK follows [Semantic Versioning](https://semver.org/):

- **Major version** (26): Breaking API changes allowed
- **Minor version** (0): New features, backward compatible
- **Alpha/Beta**: API may change between pre-releases
- **GA (General Availability)**: Stable API, no breaking changes in patch releases

### Source Compatibility

Code compiled against AprismJDK v26.0-Alpha.1 will compile against v26.0-GA without changes (within the same major version).

### Runtime Compatibility

JARs compiled with any v26.0 Alpha/Beta/GA release are interoperable at runtime within the v26.0 line.

## Language Feature Support

### Java 21 Features

| Feature | Support Status | Notes |
|---------|----------------|-------|
| Virtual Threads | ✅ Supported | Full compatibility |
| Pattern Matching | ✅ Supported | Record patterns, switch patterns |
| Sequenced Collections | ✅ Supported | All collection APIs |
| String Templates | ✅ Supported | Preview feature in Java 21 |
| Unnamed Classes | ✅ Supported | Preview feature |

### Java 17 Features

| Feature | Support Status | Notes |
|---------|----------------|-------|
| Sealed Classes | ✅ Supported | Full support |
| Pattern Matching for `instanceof` | ✅ Supported | Stable feature |
| Records | ✅ Supported | Used in API design |
| Text Blocks | ✅ Supported | Everywhere |
| Switch Expressions | ✅ Supported | Extensively used |

## Platform Support

| OS | Architecture | Java 17 | Java 21 | Java 25 |
|----|--------------|---------|---------|---------|
| Windows 10/11 | x64 | ✅ | ✅ | ✅ |
| Windows 10/11 | aarch64 | ✅ | ✅ | ✅ |
| Linux (glibc 2.17+) | x64 | ✅ | ✅ | ✅ |
| Linux (glibc 2.17+) | aarch64 | ✅ | ✅ | ✅ |
| macOS 11+ | x64 | ✅ | ✅ | ✅ |
| macOS 11+ | aarch64 (Apple Silicon) | ✅ | ✅ | ✅ |

## Testing Matrix

### Test Coverage by Java Version

| Test Suite | Java 17 | Java 21 | Java 25 |
|------------|---------|---------|---------|
| VmInfo API Tests | ✅ 5 tests | ✅ 5 tests | ⏳ Planned |
| Agent Tests | ✅ 3 tests | ✅ 3 tests | ⏳ Planned |
| Bytecode Tests | ✅ 25 tests | ✅ 25 tests | ⏳ Planned |
| HeapInsight Tests | ✅ 17 tests | ✅ 17 tests | ⏳ Planned |
| JitInsight Tests | ✅ 19 tests | ✅ 19 tests | ⏳ Planned |
| Performance Tests | ✅ 19 tests | ✅ 19 tests | ⏳ Planned |
| ThreadInsight Tests | ✅ 12 tests | ✅ 12 tests | ⏳ Planned |
| Compatibility Tests | ✅ 21 tests | ✅ 21 tests | ⏳ Planned |
| **Total** | **121 tests** | **121 tests** | **Planned** |

### Test Coverage by JDK Distribution

| Test Category | AprismJDK | Stock JDK | Notes |
|---------------|-----------|-----------|-------|
| Basic API | ✅ Full | ✅ Full | All methods callable |
| Capability Detection | ✅ Full | ✅ Full | Returns `false` on Stock JDK |
| Stock JDK Fallback | ✅ 5 tests | ✅ 5 tests | Verifies safe degradation |
| Cross-Version Compatibility | ✅ 11 tests | ✅ 11 tests | Java 17/21/25 interop |

## Version Migration Guide

### Migrating from Java 17 to Java 21

**No code changes required**. AprismJDK APIs are fully compatible.

Optional improvements:
- Use Virtual Threads for async operations
- Leverage Pattern Matching for cleaner code
- Adopt Sequenced Collections APIs

### Migrating from Stock JDK to AprismJDK

**No code changes required**. Simply replace the JDK:

```bash
# Set JAVA_HOME to AprismJDK
export JAVA_HOME=/path/to/aprismjdk-26.0
export PATH=$JAVA_HOME/bin:$PATH

# Verify
java -version
# Output: AprismJDK version 26.0-Alpha.8
```

**New capabilities unlocked**:
- VM introspection APIs
- Advanced bytecode transformation
- Performance optimizations
- Heap and JIT insights

### Migrating Between AprismJDK Versions

**Alpha → Alpha**: May require code changes (pre-release API evolution)

**Alpha → GA**: Source compatible within v26.0 line

**GA → GA (patch)**: Drop-in replacement, no changes needed

**v26.0 → v26.1**: Minor version bump, backward compatible

**v26.x → v27.0**: Major version, breaking changes possible (will be documented)

## Compatibility Testing

### Running Compatibility Tests

```bash
# Test on Java 21
export JAVA_HOME=/path/to/jdk-21
./gradlew :aprismate-tests:test --tests "*.compatibility.*"

# Test on Java 17
export JAVA_HOME=/path/to/jdk-17
./gradlew :aprismate-tests:test --tests "*.compatibility.*"

# Test Stock JDK fallback
./gradlew :aprismate-tests:test --tests "*StockJdkFallbackTest"
```

### Continuous Integration

AprismJDK runs compatibility tests on every commit across:
- 3 Java versions (17, 21, 25)
- 6 platforms (Windows/Linux/macOS × x64/aarch64)
- 2 JDK types (AprismJDK, Stock JDK)

**Total CI matrix**: 36 configurations per commit

## Known Limitations

### Stock JDK Limitations

When running on Stock JDK, the following features are **not available**:

- **VM Introspection**: `VmInfo.hasVmIntrospection()` returns `false`
- **Advanced Bytecode Transformation**: Limited to Java Agent capabilities
- **Heap Insights**: `HeapInsight` APIs return empty/default data
- **JIT Insights**: `JitInsight` APIs return empty/default data
- **Performance Optimizations**: Thread-local caches, object pooling work but are not VM-optimized

### Java 17 Limitations

Java 17 lacks some language features available in Java 21+:

- **Virtual Threads**: Not available (use platform threads)
- **Pattern Matching for switch**: Not available
- **String Templates**: Not available (use `String.format` or text blocks)

## Deprecation Policy

- **Alpha/Beta**: APIs may be removed without warning
- **GA**: Deprecated APIs remain for at least one major version (e.g., v26 → v27)
- **Deprecation Notice**: Minimum 6 months before removal
- **Migration Guide**: Provided for all deprecated APIs

## Support Timeline

| Version | Release Date | End of Support | LTS |
|---------|--------------|----------------|-----|
| v26.0-Alpha.8 | 2026-08-11 | v26.0-GA | No |
| v26.0-GA | 2026-Q4 (planned) | 2029-Q4 | Yes |
| v26.1-GA | 2027-Q2 (planned) | 2030-Q2 | Yes |
| v27.0-GA | 2028-Q1 (planned) | 2031-Q1 | Yes |

**LTS (Long Term Support)**: 3 years of security and bug fixes

## References

- [Java Version Compatibility (javadoc)](../api/jdk/aprismate/util/JavaVersion.html)
- [Stock JDK Fallback Tests](../../aprismate-tests/src/test/java/jdk/aprismate/test/compatibility/StockJdkFallbackTest.java)
- [Cross-Version Tests](../../aprismate-tests/src/test/java/jdk/aprismate/test/compatibility/CrossVersionCompatibilityTest.java)
- [Semantic Versioning Specification](https://semver.org/)

## v26.2-Alpha.8 Measured Matrix (2026-08-23)

Baseline correction: the API surface compiles against Java 25 only
(FFM final since 22; `--enable-preview` removed in Alpha.1). The
Java 21/17 rows above are aspirational until the MRJAR line lands —
tracked for v26.3+.

Measured this alpha via `scripts/compat-sweep.ps1` (8 checks):

| Runtime | Identity | jdk.aprismate | Agent attach | Gradle suite |
|---|---|---|---|---|
| Temurin 25.0.3 (stock) | openjdk 25.0.3 | absent (correct) | FULL attach (self-contained jar) | PASS (678 tests) |
| AprismJDK fork image | aprismjdk 26.2.1-alpha AJR | present, exported, isAJR=true | full attach + `-XX:+AprismateAgent` | **SKIP — KI-1** |

**KI-1 (known issue)**: the fork advertises feature version 26
(calendar release naming) on a 25 codebase; tools that pick classfile
targets from `Runtime.version().feature()` may emit class files
(70) newer than the runtime accepts (69). Repro: user-global Gradle
Kotlin init scripts under a fork daemon. Resolution options tracked
for v26.2-GA planning (align version-string base vs document).

Agent portability note: since Alpha.8 the embedded jar bundles
aprismate-api classes; attaching it to a STOCK JDK gives full premain
functionality (not just graceful degradation).
