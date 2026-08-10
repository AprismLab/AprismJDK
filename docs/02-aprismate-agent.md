# AprismateAgent Design Document

> AprismJDK subproject documentation. Describes the AprismateAgent component:
> entry points, capabilities, and fail-safe contract.
>
> Author: BlockConnect@StarsailsClover. Bilingual: EN (canonical) + ZH (mirror).
> Delivered in v26.0-Alpha.1 (design), implementation in v26.1 line.

---

## 1. Overview

**AprismateAgent** is the flagship component of AprismJDK: a JavaAgent-like
agent that is *bundled with* the JDK image and reachable without any external
jar. It provides deep VM integration for the Aprism loader ecosystem while
maintaining compatibility with stock OpenJDK through graceful degradation.

The agent is designed around three principles:

1. **Fail-safe** — bad hooks or transformations are logged and isolated, never
   crashing the host application or JVM.
2. **Optional** — all Aprism capabilities work on stock OpenJDK; AprismateAgent
   merely unlocks deeper tiers.
3. **Versioned** — capabilities are exposed through a versioned descriptor;
   mods query capabilities rather than assume them.

---

## 2. Entry Points

AprismateAgent supports both standard JavaAgent modes:

### 2.1 Premain (Load-Time Attachment)

```java
public static void premain(String agentArgs, Instrumentation inst)
```

Invoked before the application's `main` method when the agent is specified via
`-javaagent` flag:

```bash
java -javaagent:aprismate-agent.jar=key1=value1;key2=value2 ...
```

**Use cases:**
- Load-time bytecode weaving
- Boot-time hook registration
- Early VM state capture

### 2.2 Agentmain (Runtime Attachment)

```java
public static void agentmain(String agentArgs, Instrumentation inst)
```

Invoked when attached to a running JVM via the Attach API:

```java
VirtualMachine vm = VirtualMachine.attach(pid);
vm.loadAgent("/path/to/aprismate-agent.jar", "args");
vm.detach();
```

**Use cases:**
- Hot-attach hooks to running applications
- Runtime diagnostics and profiling
- Dynamic mod loading

### 2.3 Auto-Load Mode (AprismJDK Only)

On AprismJDK, the agent can be auto-loaded via JVM flag:

```bash
java -XX:+AprismateAgent ...
```

This wires the agent before `main` without requiring `-javaagent`. The VM
knows about the agent and can install hooks at points stock agents cannot
reach.

**Status:** Deferred to v26.2+ (requires VM-level integration)

---

## 3. Agent Arguments

Arguments are passed as semicolon-separated key=value pairs:

```
key1=value1;key2=value2;flag
```

### Supported Arguments (v26.0-Alpha.1)

None yet (skeleton implementation). Planned for v26.0-Alpha.5:

- `debug=true` — Enable verbose logging
- `transform=<pattern>` — Class transformation filter
- `hooks=<file>` — Load hook definitions from file

---

## 4. Capabilities

The agent exposes capabilities through the `jdk.aprismate.Agent` API.

### 4.1 ClassRedefiner+ (v26.1-Alpha.2)

Redefine classes including structural changes (add/remove fields/methods) that
stock `Instrumentation.redefineClasses` refuses.

**Stock JDK fallback:** Use standard `Instrumentation.redefineClasses` with
limitations (no structural changes).

**Fail-safe:** Invalid redefinitions are logged and rejected; existing class
definition remains unchanged.

### 4.2 MethodHookRegistry+ (v26.1-Alpha.4)

Register entry/exit hooks on any method, including JIT-compiled ones. The VM
guarantees hooks survive inlining by treating hook points as deoptimization
anchors.

**Stock JDK fallback:** Use ASM method wrapping or Java agent transformation
(less efficient, no JIT guarantees).

**Fail-safe:** Bad hooks (exceptions, infinite loops) are caught and disabled;
the hooked method continues to execute normally.

### 4.3 BytecodeTransformer (v26.1-Alpha.5)

ASM-backed pipeline hook that sees classes at load time, before verification,
enabling Mixin-style weaving without a separate Mixin runtime.

**Stock JDK fallback:** Use standard `ClassFileTransformer` (works on all JVMs).

**Fail-safe:** Failing transformations fall back to the untransformed class;
verification errors are logged.

### 4.4 VmIntrospection (v26.1-Alpha.6+)

Read thread stacks, class statistics, heap summaries, JIT/GC state through
named methods instead of JMX reflection.

**Stock JDK fallback:** Use `ManagementFactory` MXBeans (standard but
reflection-based).

**Fail-safe:** Query failures return null or empty results; never throw.

---

## 5. Fail-Safe Contract

Every agent capability is **fail-closed into the application, never the VM**:

- **Bad hooks** — Logged and skipped; method executes normally
- **Bad transformations** — Logged; untransformed class is loaded
- **Invalid redefinitions** — Logged; existing class remains unchanged
- **Query failures** — Return null/empty; never crash

**Guarantee:** The agent must never crash the JVM. This mirrors the fail-safe
discipline Aprism applies to its loader.

---

## 6. Manifest Attributes

The agent jar includes these manifest attributes:

```
Premain-Class: com.aprismate.agent.AprismateAgent
Agent-Class: com.aprismate.agent.AprismateAgent
Can-Redefine-Classes: true
Can-Retransform-Classes: true
Can-Set-Native-Method-Prefix: true
Implementation-Title: AprismateAgent
Implementation-Version: v26.0-Alpha.1
Implementation-Vendor: AprismLab
```

---

## 7. Integration with Aprism Loader

The Aprism loader detects AprismateAgent at runtime:

```java
if (jdk.aprismate.VmInfo.isAprismJdk()) {
    // Use AprismJDK-specific APIs
    ClassRedefiner redefiner = (ClassRedefiner) Agent.getClassRedefiner();
} else {
    // Fall back to stock JDK behavior
    instrumentation.redefineClasses(...);
}
```

This detection happens in Aprism's deep API layer (v26.4 line) and is
transparent to mods.

---

## 8. Implementation Status

- **v26.0-Alpha.1**: Skeleton (premain/agentmain entry points, basic initialization)
- **v26.0-Alpha.5**: Argument parsing, logging infrastructure
- **v26.0-Alpha.7**: Instrumentation integration, basic attachment verified
- **v26.1-Alpha.2**: ClassRedefiner+ implementation
- **v26.1-Alpha.4**: MethodHookRegistry+ implementation
- **v26.1-Alpha.5**: BytecodeTransformer implementation
- **v26.1-Alpha.6+**: VmIntrospection implementation

---

## 9. Testing Strategy

Agent testing requires special test infrastructure:

1. **Unit tests** — API contracts, argument parsing
2. **Integration tests** — Agent attachment (premain/agentmain)
3. **Transformation tests** — Bytecode weaving verification
4. **Fail-safe tests** — Bad hooks, invalid transformations (must not crash)
5. **Real-game tests** — Integration with Aprism loader on Minecraft

Test count tracking: 0 (v26.0-Alpha.1) → ~30 (v26.0-Alpha.7) → ~150 (v26.1-Alpha.5)

---

## 10. References

- `java.lang.instrument.Instrumentation` — Stock Java agent API
- JVMTI — JVM Tool Interface (native agent foundation)
- ASM library — Bytecode manipulation framework
- Aprism ClassRedefiner/MethodHookRegistry — Loader-side integration points
