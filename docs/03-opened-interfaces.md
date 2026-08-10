# Opened Interfaces & Capabilities

> AprismJDK subproject documentation. Describes the `jdk.aprismate` API module:
> stable, versioned interfaces exposing JVM internals.
>
> Author: BlockConnect@StarsailsClover. Bilingual: EN (canonical) + ZH (mirror).
> Delivered in v26.0-Alpha.1 (design), implementation across v26.0-v26.1 lines.

---

## 1. Overview

AprismJDK promotes frequently-used JVM internals into a **stable, versioned
API module** (`jdk.aprismate`). These interfaces are:

- **Additive** — No removal or renaming of stock OpenJDK APIs
- **Versioned** — Each capability tracks introduction and stability guarantees
- **Optional** — Programs detect availability and fall back to stock APIs
- **Fail-safe** — Query failures return null/defaults, never throw

A program compiled against stock OpenJDK runs unchanged on AprismJDK, and vice
versa for the non-AprismJDK subset.

---

## 2. Module Structure

```
jdk.aprismate/
  jdk.aprismate.Agent           — AprismateAgent programmatic entry
  jdk.aprismate.VmInfo          — VM identity and capability descriptor
  jdk.aprismate.runtime/
    ThreadInsight               — Thread-stack and scheduling introspection
    HeapInsight                 — Heap region and GC introspection
    JitInsight                  — Compilation-queue and method-compilation introspection
```

---

## 3. Core API: `jdk.aprismate.VmInfo`

VM build identity and capability detection.

### Methods

```java
// Identity queries
public static String getAprismJdkVersion()
public static boolean isAprismJdk()
public static int getOpenJdkVersion()
public static String getVendor()

// Capability detection (query before use)
public static boolean hasClassRedefinerPlus()
public static boolean hasMethodHookRegistryPlus()
public static boolean hasBytecodeTransformer()
public static boolean hasVmIntrospection()
```

### Usage Pattern

```java
if (VmInfo.isAprismJdk()) {
    System.out.println("Running on " + VmInfo.getAprismJdkVersion());
    
    if (VmInfo.hasClassRedefinerPlus()) {
        // Use deep redefine capabilities
    } else {
        // Fall back to stock Instrumentation
    }
}
```

**Stock JDK behavior:** All `has*` methods return `false`; version methods
return `null`.

---

## 4. Core API: `jdk.aprismate.Agent`

Programmatic entry point for AprismateAgent capabilities.

### Methods

```java
// Agent state
public static boolean isAgentLoaded()
public static String getAgentVersion()

// Capability accessors (return null if not available)
public static Object getClassRedefiner()      // v26.1-Alpha.2
public static Object getMethodHookRegistry()  // v26.1-Alpha.4
public static Object getBytecodeTransformer() // v26.1-Alpha.5
```

### Usage Pattern

```java
if (Agent.isAgentLoaded()) {
    ClassRedefiner redefiner = (ClassRedefiner) Agent.getClassRedefiner();
    if (redefiner != null) {
        // Use structural redefinition
    }
}
```

**Stock JDK behavior:** `isAgentLoaded()` returns `false` unless attached via
standard `-javaagent`; capability accessors return `null`.

---

## 5. Runtime API: `jdk.aprismate.runtime.ThreadInsight`

Thread-stack and scheduling introspection (v26.1-Alpha.6).

### Methods (Planned)

```java
// Thread state queries
public static ThreadSnapshot[] getAllThreads()
public static ThreadSnapshot getThread(long threadId)
public static StackFrame[] captureStack(long threadId)

// Scheduling hints (advisory only)
public static long getThreadCpuTime(long threadId)
public static long getThreadUserTime(long threadId)
public static boolean isThreadBlocked(long threadId)
```

**Stock JDK fallback:** Use `ThreadMXBean` from `ManagementFactory`.

---

## 6. Runtime API: `jdk.aprismate.runtime.HeapInsight`

Heap region and GC introspection (v26.1-Alpha.6).

### Methods (Planned)

```java
// Heap state queries
public static HeapSummary getHeapSummary()
public static long getUsedMemory()
public static long getCommittedMemory()
public static long getMaxMemory()

// GC introspection
public static GcInfo[] getRecentCollections()
public static long getTotalCollectionTime()
public static long getCollectionCount()
```

**Stock JDK fallback:** Use `MemoryMXBean` and `GarbageCollectorMXBean`.

---

## 7. Runtime API: `jdk.aprismate.runtime.JitInsight`

Compilation-queue and method-compilation introspection (v26.1-Alpha.7).

### Methods (Planned)

```java
// Compilation state
public static CompilationInfo[] getCompiledMethods()
public static boolean isMethodCompiled(Method method)
public static int getCompilationQueueSize()

// Compilation hints
public static long getTotalCompilationTime()
public static void requestCompilation(Method method)  // advisory
```

**Stock JDK fallback:** Use `CompilationMXBean`; limited information available.

---

## 8. Capability Descriptor Format

Each capability has a descriptor returned by query methods:

```java
public class CapabilityDescriptor {
    String name;              // e.g., "ClassRedefinerPlus"
    String version;           // e.g., "v26.1-Alpha.2"
    boolean available;        // true if capability is active
    String fallbackStrategy;  // describes stock JDK equivalent
}
```

This allows runtime introspection of what's available.

---

## 9. Forward Compatibility Contract

1. **Query capabilities, never assume them.** A mod written for AprismJDK 25
   must run on AprismJDK 26+ with reduced-but-working capability.

2. **Graceful degradation.** Every AprismJDK-specific API has a documented
   stock-JDK equivalent or no-op behavior.

3. **Versioned stability.** Once a method is marked stable (GA release), it
   follows semantic versioning: compatible changes only, deprecation with
   notice.

---

## 10. Implementation Status

| API | v26.0-Alpha.1 | v26.0-Alpha.6 | v26.1-Alpha.6 | v26.1 GA |
|-----|---------------|---------------|---------------|----------|
| VmInfo (stubs) | ✓ | ✓ | ✓ | ✓ |
| Agent (stubs) | ✓ | ✓ | ✓ | ✓ |
| VmInfo (capabilities) | — | ✓ | ✓ | ✓ |
| Agent (capabilities) | — | — | ✓ | ✓ |
| ThreadInsight | — | — | ✓ | ✓ |
| HeapInsight | — | — | ✓ | ✓ |
| JitInsight | — | — | ✓ | ✓ |

---

## 11. Module Declaration

The `jdk.aprismate` module is part of the JDK image:

```java
module jdk.aprismate {
    exports jdk.aprismate;
    exports jdk.aprismate.runtime;
    
    requires java.base;
    requires java.management;
    requires java.instrument;
}
```

**Stock JDK:** Module does not exist; code using it must detect absence via
try-catch on `ClassNotFoundException`.

---

## 12. Testing Strategy

API testing focuses on:

1. **Presence tests** — APIs are accessible on AprismJDK
2. **Absence tests** — Graceful behavior on stock JDK
3. **Contract tests** — Fail-safe behavior (null returns, no throws)
4. **Fallback tests** — Stock JDK equivalents produce similar results

Test count: ~20 (v26.0-Alpha.6) → ~80 (v26.1-Alpha.7)

---

## 13. Security Considerations

VM introspection APIs respect Java security model:

- **SecurityManager checks** (if enabled) — Fail with `SecurityException` if
  insufficient permissions
- **Module boundaries** — Respect `--add-exports`/`--add-opens` requirements
- **No privilege escalation** — APIs do not bypass existing access controls

---

## 14. References

- `java.lang.management` — Stock JDK management beans (fallback APIs)
- `java.lang.instrument` — Standard instrumentation API
- JEP 403 — Strongly Encapsulate JDK Internals (motivation for stable surface)
