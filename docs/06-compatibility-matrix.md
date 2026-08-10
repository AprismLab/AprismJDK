# Compatibility Matrix

> AprismJDK subproject documentation. Defines cross-Java-version compatibility
> contract and stock JDK fallback behavior.
>
> Author: BlockConnect@StarsailsClover. Bilingual: EN (canonical) + ZH (mirror).
> Delivered in v26.0-Alpha.1 (design), verified in v26.1-Alpha.8.

---

## 1. Overview

AprismJDK commits to **extreme compatibility**: mods built against one
AprismJDK line keep running across updates. This is achieved through:

1. **Capability-based detection** — Mods query what's available, never assume
2. **Stock JDK fallback** — Every API has a documented equivalent or no-op
3. **LTS rebasing** — AprismJDK tracks upstream LTS lines with stable patch sets
4. **Semantic versioning** — Breaking changes require major version bump

---

## 2. Supported Java Versions

AprismJDK v26.x supports three Java versions with tiered capabilities:

### 2.1 Java 25 (Primary Target)

- **Status:** Primary development target
- **OpenJDK Base:** OpenJDK 25 LTS (GA 2025-09, support to ~2032)
- **AprismJDK Capabilities:** Full (all features available)
- **Platform Coverage:** Windows, Linux, macOS (x64 + aarch64)

**Full Feature Set:**
- AprismateAgent (premain/agentmain/auto-load)
- ClassRedefiner+ (structural redefinition)
- MethodHookRegistry+ (JIT-safe hooks)
- BytecodeTransformer (load-time weaving)
- VmIntrospection (ThreadInsight, HeapInsight, JitInsight)
- FFM API support (final in JDK 22+)

### 2.2 Java 21 (Secondary Target)

- **Status:** Previous LTS, full support via backport
- **OpenJDK Base:** OpenJDK 21 LTS (GA 2023-09, support to 2029)
- **AprismJDK Capabilities:** Full (backported from Java 25)
- **Platform Coverage:** Windows, Linux, macOS

**Feature Set:**
- All Java 25 capabilities backported
- FFM API available (preview in JDK 21, stable APIs used)
- Identical API surface to Java 25 variant

**Backport Strategy:**
- AprismJDK patches applied to OpenJDK 21 baseline
- `jdk.aprismate` module compiled for Java 21 target
- Agent jar uses `-release 21` for compatibility

### 2.3 Java 17 (Maintenance Target)

- **Status:** Extended LTS, limited support
- **OpenJDK Base:** OpenJDK 17 LTS (GA 2021-09, support to 2029)
- **AprismJDK Capabilities:** Subset (FFM limitations)
- **Platform Coverage:** Windows, Linux, macOS

**Limited Feature Set:**
- AprismateAgent (premain/agentmain only, no auto-load)
- ClassRedefiner+ (basic support, limited structural changes)
- MethodHookRegistry+ (hook registry works, JIT integration limited)
- BytecodeTransformer (full support)
- VmIntrospection (ThreadInsight, HeapInsight only; JitInsight limited)
- No FFM API (not available in Java 17)

**Graceful Degradation:**
- FFM-dependent features return `null` or `false` from capability queries
- Mods detect absence via `VmInfo.hasXxx()` methods

---

## 3. Stock JDK Fallback Behavior

Every AprismJDK-specific API documents stock OpenJDK equivalent:

| AprismJDK API | Stock JDK Fallback | Notes |
|---------------|-------------------|-------|
| `VmInfo.isAprismJdk()` | Returns `false` | Detection method |
| `VmInfo.hasClassRedefinerPlus()` | Returns `false` | Use standard `Instrumentation` |
| `Agent.getClassRedefiner()` | Returns `null` | Use `Instrumentation.redefineClasses()` |
| `Agent.getMethodHookRegistry()` | Returns `null` | Use ASM method wrapping |
| `Agent.getBytecodeTransformer()` | Returns `null` | Use `ClassFileTransformer` |
| `ThreadInsight.getAllThreads()` | Use `ThreadMXBean` | `ManagementFactory.getThreadMXBean()` |
| `HeapInsight.getHeapSummary()` | Use `MemoryMXBean` | `ManagementFactory.getMemoryMXBean()` |
| `JitInsight.getCompiledMethods()` | Use `CompilationMXBean` | Limited info available |

### Fallback Example

```java
// Detect AprismJDK and use appropriate API
ClassRedefiner redefiner;
if (VmInfo.isAprismJdk() && VmInfo.hasClassRedefinerPlus()) {
    redefiner = (ClassRedefiner) Agent.getClassRedefiner();
} else {
    // Fallback: use standard Instrumentation
    redefiner = new StandardRedefinerWrapper(instrumentation);
}
```

---

## 4. Forward Compatibility Contract

### 4.1 Capability Descriptor Stability

Once a capability is marked **stable** (GA release), it follows semantic
versioning:

- **Minor updates** (v26.1 → v26.2) — Additive only, no breaking changes
- **Major updates** (v26.x → v27.x) — May deprecate old capabilities with
  migration path
- **Deprecation notice** — Minimum 2 minor versions before removal

### 4.2 API Evolution

```java
// v26.1 GA - Initial stable release
public static ThreadSnapshot[] getAllThreads()

// v26.2 - Additive: new overload (compatible)
public static ThreadSnapshot[] getAllThreads(ThreadFilter filter)

// v26.5 - Deprecation notice
@Deprecated(since="v26.5", forRemoval=true)
public static ThreadSnapshot[] getAllThreads()

// v27.0 - Removal allowed after deprecation period
// Method removed, new API introduced
```

### 4.3 Cross-Version Testing

AprismJDK releases are tested against:

1. **Upstream LTS updates** — OpenJDK 25.0.1, 25.0.2, etc.
2. **Previous AprismJDK versions** — Binary compatibility verified
3. **Stock OpenJDK** — Fallback behavior validated on stock 25, 21, 17

Test matrix:
- v26.1 GA → OpenJDK 25.0.0, 25.0.1, 25.0.2
- v26.1 GA → AprismJDK 21 variant
- v26.1 GA → AprismJDK 17 variant  
- v26.1 GA → Stock OpenJDK 25, 21, 17

---

## 5. LTS Rebasing Strategy

AprismJDK tracks upstream OpenJDK LTS releases:

### Current State (v26.x)
- **Base:** OpenJDK 25 LTS
- **Patch Set:** AprismJDK v26 patches
- **Support:** Until OpenJDK 26 LTS (estimated 2028)

### Future LTS Transition (v2X.x)
When upstream releases next LTS (e.g., OpenJDK 28 in 2028):

1. **Rebase:** Apply AprismJDK patch set to OpenJDK 28 baseline
2. **Test:** Verify all capabilities on new base
3. **Release:** New major line (v28.0-Alpha.1...)
4. **Maintain:** Continue v26.x security updates for overlap period

### Overlap Period

During LTS transitions, both lines receive updates:
- **v26.x** — Security fixes only (1 year overlap)
- **v28.x** — Active feature development

---

## 6. Platform-Specific Compatibility

### 6.1 Windows

- **Supported:** Windows 10 1809+, Windows 11, Windows Server 2019+
- **Architecture:** x64 (primary), ARM64 (future)
- **Toolchain:** MSVC 2022 (for native components in v26.1+)

### 6.2 Linux

- **Supported:** glibc 2.27+ (Ubuntu 18.04+, RHEL 8+, Debian 10+)
- **Architecture:** x64 (primary), aarch64 (secondary)
- **Toolchain:** GCC 11+ or Clang 14+

### 6.3 macOS

- **Supported:** macOS 12 (Monterey)+
- **Architecture:** x64, aarch64 (Apple Silicon)
- **Toolchain:** Xcode 14+

### 6.4 Unsupported Platforms

- **Android/iOS:** Not applicable (no desktop JDK)
- **Consoles:** Not supported (no Java runtime)
- **Embedded:** Not tested (use stock OpenJDK)

---

## 7. Binary Compatibility

### 7.1 JAR Compatibility

A jar compiled against one AprismJDK version runs on any other v26.x release:

```
Mod compiled with AprismJDK v26.0 GA
→ Runs on AprismJDK v26.1, v26.2, ... v26.9
→ Runs on Stock OpenJDK 25/21/17 (with fallback)
```

### 7.2 Module Compatibility

The `jdk.aprismate` module is additive-only:
- **v26.0 GA:** Basic VmInfo, Agent stubs
- **v26.1 GA:** + Runtime APIs (ThreadInsight, HeapInsight, JitInsight)
- **v26.2 GA:** + Performance APIs (CpuFeatures, CacheTopology)
- **v26.3 GA:** + FFM bridges (Cpp2Java, Rust2Java)

Code using v26.0 APIs compiles and runs unchanged on v26.3.

---

## 8. Dependency Compatibility

### 8.1 ASM Library

AprismateAgent embeds ASM library (relocated):
- **Embedded Version:** ASM 9.7+
- **Relocation:** `org.objectweb.asm` → `com.aprismate.agent.shadow.asm`
- **Isolation:** No conflicts with application ASM dependencies

### 8.2 JNI/Native Libraries

Native components (v26.1+) follow platform ABI:
- **Windows:** MSVC 2022 runtime, C++17
- **Linux:** glibc 2.27+, GCC 11+ libstdc++
- **macOS:** macOS 12+ SDK, Clang 14+

---

## 9. Upgrade Path

### From Stock OpenJDK to AprismJDK

1. Replace JDK installation directory
2. Update `JAVA_HOME` environment variable
3. No code changes required (additive APIs)
4. Optional: Use AprismJDK-specific capabilities

### From AprismJDK v26.0 to v26.1

1. Replace JDK/JRE distribution
2. No recompilation required (binary compatible)
3. New capabilities available via `VmInfo.hasXxx()` queries

### From AprismJDK v26.x to v27.x (Future)

1. Review deprecation notices in v26.x releases
2. Migrate to new APIs before v27.0
3. Recompile recommended (major version boundary)

---

## 10. Testing Matrix (v26.1 GA Target)

| Test Dimension | Coverage |
|----------------|----------|
| **Java Versions** | OpenJDK 25, 21, 17 (all AprismJDK variants + stock) |
| **Platforms** | Windows x64, Linux x64, macOS x64/aarch64 |
| **Capabilities** | All APIs with/without AprismJDK features |
| **Fallback** | Stock JDK behavior for all APIs |
| **Compatibility** | Cross-version binary compatibility |

**Total Test Scenarios:** ~230 tests at v26.1 GA

---

## 11. Version Detection at Runtime

Recommended pattern for cross-version code:

```java
public class AprismJdkCompat {
    private static final boolean IS_APRISMJDK = detectAprismJdk();
    private static final int JAVA_VERSION = Runtime.version().feature();
    
    private static boolean detectAprismJdk() {
        try {
            Class.forName("jdk.aprismate.VmInfo");
            return jdk.aprismate.VmInfo.isAprismJdk();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static boolean canUseDeepRedefine() {
        return IS_APRISMJDK 
            && JAVA_VERSION >= 21
            && jdk.aprismate.VmInfo.hasClassRedefinerPlus();
    }
}
```

---

## 12. References

- JEP 223 — New Version-String Scheme (version detection)
- JEP 403 — Strongly Encapsulate JDK Internals (motivation for stable APIs)
- Semantic Versioning 2.0.0 — Versioning contract
- OpenJDK LTS roadmap — Upstream tracking strategy
