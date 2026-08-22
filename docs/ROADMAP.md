# AprismJDK Development Roadmap (v26.0 - v26.1)

> Complete development plan for AprismJDK v26.0 and v26.1 lines.
> 20 releases total: v26.0-Alpha.1 through v26.0-Alpha.9 + v26.0 GA +
> v26.1-Alpha.1 through v26.1-Alpha.9 + v26.1 GA.
>
> Author: BlockConnect@StarsailsClover
> Status: Planning document (delivered with v26.0-Alpha.1)
> Updated: 2026-08-11

---

## Overview

AprismJDK development follows a phased approach across 20 releases:

- **v26.0 line (Foundation)**: 9 Alphas + 1 GA = 10 releases
- **v26.1 line (Capabilities)**: 9 Alphas + 1 GA = 10 releases

Each release is individually tested, tagged, and published. The v26.0 line establishes the build infrastructure, OpenJDK integration, and basic agent framework. The v26.1 line implements the deep capabilities (ClassRedefiner+, MethodHookRegistry+, VM introspection).

---

## v26.0 Line: Foundation (10 releases)

### v26.0-Alpha.1: Project Skeleton & API Stubs

**Goal**: Establish project structure, documentation, and API surface definition.

**Deliverables**:
- Repository structure (`aprismate-api/`, `aprismate-agent/`, `aprismate-tests/`, `docs/`)
- Core API stubs:
  - `jdk.aprismate.VmInfo` - JDK identity and capability query
  - `jdk.aprismate.Agent` - Agent entry point interface
- Documentation:
  - `docs/01-architecture.md` (EN + ZH)
  - `docs/02-aprismate-agent.md` (EN + ZH)
  - `docs/03-opened-interfaces.md` (EN + ZH)
  - `docs/06-compatibility-matrix.md` (EN + ZH)
  - `docs/ROADMAP.md` (this document)
- Gradle multi-module build setup
- JUnit 5 + AssertJ test framework
- GitHub Actions CI workflow (build + test)

**Technical focus**: No OpenJDK fork yet; pure Java API design and project governance setup.

**Exit criteria**:
- `./gradlew build test` passes with 0 failures
- All docs complete (EN + ZH pairs)
- CI workflow green
- Tag: `v26.0-Alpha.1` (signed, Pre-Release)

---

### v26.0-Alpha.2: Dependency Management & Module System

**Goal**: Establish dependency management, module-info declarations, and versioning infrastructure.

**Deliverables**:
- `gradle/libs.versions.toml` centralized dependency catalog
- Module descriptors (`module-info.java`) for all modules:
  - `jdk.aprismate` (exports `com.aprismate.api`, `com.aprismate.vminfo`)
  - `jdk.aprismate.agent` (requires `jdk.aprismate`, `java.instrument`)
- Version injection from `gradle.properties` into `VmInfo.getAprismJdkVersion()`
- SBOM generation (CycloneDX format)
- Enhanced CI: checksum generation, artifact signing prep

**Technical focus**: Java module system compliance; prepare for OpenJDK integration.

**Exit criteria**:
- Module graph validates (`jdeps --check`)
- Version string correct at runtime
- SBOM generated
- Tag: `v26.0-Alpha.2` (signed, Pre-Release)

---

### v26.0-Alpha.3: Build System Research & Documentation

**Goal**: Research and document OpenJDK build system integration strategy.

**Deliverables**:
- `docs/07-build-system.md` (EN + ZH):
  - OpenJDK build system overview (Make + autoconf)
  - AprismJDK patch strategy (patch files vs. overlay directories)
  - Boot JDK requirements (JDK N-1 for building JDK N)
  - Build profiles (fastdebug, release, slowdebug)
  - Cross-compilation considerations
- `docs/08-release-process.md` (EN + ZH):
  - JDK/JRE artifact structure
  - Packaging formats (tar.gz, zip, installer)
  - Signature and verification workflow
  - Distribution channels

**Technical focus**: Understand OpenJDK build complexity before attempting fork.

**Exit criteria**:
- Build system documentation complete
- Release process documented
- Decision recorded in FACT.md: which OpenJDK version to fork (25 LTS confirmed)
- Tag: `v26.0-Alpha.3` (signed, Pre-Release)

---

### v26.0-Alpha.4: OpenJDK Source Integration Preparation

**Goal**: Prepare repository structure for OpenJDK source fork.

**Deliverables**:
- `jdk/` directory structure design:
  - `jdk/src/` - OpenJDK source (to be added in Alpha.5)
  - `jdk/patches/` - AprismJDK patch files
  - `jdk/make/` - Build script overlays
- Git submodule vs. subtree evaluation (decision documented)
- `.gitattributes` for line-ending handling (CRLF/LF)
- CI workflow update: prepare for multi-hour JDK builds (caching strategy)

**Technical focus**: Repository ergonomics for large OpenJDK source tree.

**Exit criteria**:
- Directory structure ready
- Git strategy decided and documented
- CI caching strategy validated
- Tag: `v26.0-Alpha.4` (signed, Pre-Release)

---

### v26.0-Alpha.5: OpenJDK 25 Source Fork & Basic Build

**Goal**: Fork OpenJDK 25 source and achieve first successful JDK build.

**Deliverables**:
- OpenJDK 25 source integrated into `jdk/src/`
- `configure` script execution (detect toolchain, boot JDK)
- First successful `make images` on Linux (primary build platform)
- Build artifacts:
  - `build/linux-x64/images/jdk/` - Full JDK image
  - `build/linux-x64/images/jre/` - JRE image
- Build time documented (baseline for optimization)

**Technical focus**: Unmodified OpenJDK build; validation of toolchain and dependencies.

**Requirements**:
- Boot JDK: OpenJDK 24 or 23 (for building JDK 25)
- Build tools: GCC/Clang, Make, autoconf, zip, unzip
- Build time: ~1-2 hours (full build)

**Exit criteria**:
- `java -version` from built JDK shows correct version
- JDK can compile and run "Hello World"
- Tag: `v26.0-Alpha.5` (signed, Pre-Release)

---

### v26.0-Alpha.6: AprismJDK Branding & Initial Patches

**Goal**: Apply AprismJDK branding and inject `jdk.aprismate` module into JDK build.

**Deliverables**:
- Patch 001: Version string modification
  - `java -version` output shows "AprismJDK" branding
  - Build number includes AprismJDK version
- Patch 002: Add `jdk.aprismate` module to JDK source
  - Source: `jdk/src/jdk.aprismate/share/classes/`
  - Module descriptor registered in build system
- Patch 003: Export `jdk.aprismate` from JDK image
- Build validation: `java --list-modules` includes `jdk.aprismate`

**Technical focus**: First JDK patches; validate patch application workflow.

**Exit criteria**:
- Branded JDK builds successfully
- `jdk.aprismate` module accessible at runtime
- Patch files documented in `jdk/patches/README.md`
- Tag: `v26.0-Alpha.6` (signed, Pre-Release)

---

### v26.0-Alpha.7: AprismateAgent Stub Integration

**Goal**: Integrate AprismateAgent stub into JDK build as a bundled agent.

**Deliverables**:
- Patch 004: Add AprismateAgent Java source to JDK
  - Location: `jdk/src/jdk.aprismate.agent/share/classes/`
  - Agent manifest: `META-INF/MANIFEST.MF` with `Premain-Class`, `Agent-Class`
- Patch 005: Build `aprismate.jar` as part of JDK build
  - Output: `jdk/lib/aprismate.jar` in JDK image
- Agent attachment validation:
  - Manual: `java -javaagent:lib/aprismate.jar ...`
  - Auto: `java -XX:+AprismateAgent ...` (JVM flag support)
- Agent logs "AprismateAgent loaded" on startup

**Technical focus**: JavaAgent packaging and JVM integration.

**Exit criteria**:
- Agent loads successfully (both manual and auto modes)
- Agent can access `jdk.aprismate` APIs
- Tag: `v26.0-Alpha.7` (signed, Pre-Release)

---

### v26.0-Alpha.8: Multi-Platform Build Support

**Goal**: Extend builds to Windows and macOS; validate cross-platform portability.

**Deliverables**:
- Windows build:
  - Visual Studio 2022 toolchain
  - MSYS2 or Cygwin for POSIX layer
  - Build output: `build/windows-x64/images/jdk/`
- macOS build:
  - Xcode toolchain
  - Build output: `build/macosx-x64/images/jdk/` (x64) and `macosx-aarch64/` (ARM64)
- CI workflows:
  - Linux: Ubuntu 22.04 (primary)
  - Windows: Server 2022
  - macOS: macOS 13 (x64) and macOS 14 (ARM64)
- Platform-specific patches (if needed)

**Technical focus**: Cross-platform build validation; CI matrix expansion.

**Exit criteria**:
- All three platforms build successfully
- Platform-specific `java -version` correct
- CI green on all platforms
- Tag: `v26.0-Alpha.8` (signed, Pre-Release)

---

### v26.0-Alpha.9: JDK/JRE Packaging & Distribution

**Goal**: Create distributable JDK/JRE packages; establish release artifact structure.

**Deliverables**:
- Packaging formats:
  - Linux: `tar.gz` (JDK and JRE)
  - Windows: `zip` (JDK and JRE)
  - macOS: `tar.gz` (JDK and JRE)
- Artifact naming: `AprismJDK-<version>-<platform>-<arch>-<type>.tar.gz`
  - Example: `AprismJDK-26.0-Alpha.9-linux-x64-jdk.tar.gz`
- Checksums: SHA-256 for all artifacts
- Signature: cosign keyless signing (GitHub OIDC)
- CI release workflow:
  - Trigger on tag push
  - Build all platforms
  - Generate artifacts
  - Create GitHub Release (Pre-Release)

**Technical focus**: Release automation; artifact verification.

**Exit criteria**:
- All artifacts build and package successfully
- Checksums verify
- Signatures verify with cosign
- GitHub Release published
- Tag: `v26.0-Alpha.9` (signed, Pre-Release)

---

### v26.0: General Availability (Foundation Release)

**Goal**: Stable, production-ready JDK foundation with basic agent framework.

**Deliverables**:
- Feature freeze: no new functionality from Alpha.9
- Full test pass:
  - JDK regression tests (tier1: hotspot, jdk, langtools)
  - AprismJDK-specific tests (agent loading, module access)
  - Cross-platform smoke tests
- Documentation review:
  - All docs updated (EN + ZH)
  - Known issues documented (`docs/09-known-issues.md`)
  - Release notes: `docs/release-notes/v26.0.md`
- Artifact finalization:
  - Remove "Alpha" from version strings
  - Final branding pass
- GitHub Release (non-prerelease)

**Technical focus**: Stability, testing, documentation completeness.

**Exit criteria**:
- Zero regression test failures
- All documentation complete and accurate
- Release notes published
- Tag: `v26.0` (signed, Release)

---

## v26.1 Line: Capabilities (10 releases)

### v26.1-Alpha.1: Agent Logging & Instrumentation Framework

**Goal**: Establish agent logging, error handling, and instrumentation API foundation.

**Deliverables**:
- `jdk.aprismate.agent.Logger` - Structured logging (INFO, WARN, ERROR)
  - Output: `jdk/logs/aprismate.log`
  - Log rotation support
- `jdk.aprismate.agent.InstrumentationContext` - Access to `java.lang.instrument.Instrumentation`
- Fail-safe framework:
  - Exception isolation (agent errors never crash JVM)
  - Error reporting to log
- Enhanced `VmInfo`:
  - `getCapabilities()` - Returns capability descriptor
  - `isCapabilityEnabled(String)` - Runtime capability query

**Technical focus**: Agent infrastructure; fail-safe contract implementation.

**Exit criteria**:
- Agent logs to file successfully
- Instrumentation context accessible
- Capability query API functional
- Tag: `v26.1-Alpha.1` (signed, Pre-Release)

---

### v26.1-Alpha.2: ClassRedefiner+ Design & Basic Implementation

**Goal**: Implement structural class redefinition (add/remove fields/methods).

**Deliverables**:
- `jdk.aprismate.agent.ClassRedefiner` API:
  - `redefineClass(Class<?>, byte[])` - Redefine with structural changes
  - `addField(Class<?>, FieldDescriptor)` - Add field to existing class
  - `removeField(Class<?>, String)` - Remove field
  - `addMethod(Class<?>, MethodDescriptor)` - Add method
- HotSpot VM patches:
  - Patch 006: VM support for structural redefinition
  - Instance migration for field changes
  - Method table updates
- Test suite:
  - Add field to class, verify instances updated
  - Remove field, verify no crashes
  - Add method, verify callable

**Technical focus**: VM-level class redefinition; instance migration.

**Exit criteria**:
- Basic structural redefinition works
- Test suite passes (10+ scenarios)
- Tag: `v26.1-Alpha.2` (signed, Pre-Release)

---

### v26.1-Alpha.3: ClassRedefiner+ Hardening & Edge Cases

**Goal**: Harden ClassRedefiner+ against edge cases; comprehensive testing.

**Deliverables**:
- Edge case handling:
  - Redefine class with active instances on stack
  - Redefine during GC
  - Redefine with inheritance hierarchies
  - Concurrent redefinition attempts
- Enhanced error reporting:
  - Pre-validation of class bytes
  - Detailed failure messages
- Test suite expansion (50+ scenarios)
- Performance benchmarking:
  - Redefinition latency
  - Impact on JIT compilation

**Technical focus**: Production readiness; reliability under stress.

**Exit criteria**:
- All edge cases handled gracefully
- Zero crashes in stress tests
- Performance acceptable (<100ms for typical redefinition)
- Tag: `v26.1-Alpha.3` (signed, Pre-Release)

---

### v26.1-Alpha.4: MethodHookRegistry+ Implementation

**Goal**: Implement method entry/exit hooks with JIT survival.

**Deliverables**:
- `jdk.aprismate.agent.MethodHookRegistry` API:
  - `registerEntryHook(Method, EntryHook)` - Hook before method execution
  - `registerExitHook(Method, ExitHook)` - Hook after method execution
  - `unregisterHook(Method)` - Remove hook
- HotSpot VM patches:
  - Patch 007: JIT-aware hook points
  - Deoptimization anchors for hooked methods
  - Hook invocation without safepoint overhead
- Test suite:
  - Hook method, verify calls
  - Hook hot method (JIT-compiled), verify survives inlining
  - Hook recursive method
  - Hook with exceptions

**Technical focus**: JIT interaction; low-overhead hooks.

**Exit criteria**:
- Hooks work on interpreted and JIT-compiled methods
- Performance impact <5% for hooked methods
- Test suite passes (20+ scenarios)
- Tag: `v26.1-Alpha.4` (signed, Pre-Release)

---

### v26.1-Alpha.5: BytecodeTransformer Integration

**Goal**: Implement load-time bytecode transformation pipeline.

**Deliverables**:
- `jdk.aprismate.agent.BytecodeTransformer` API:
  - `registerTransformer(ClassFileTransformer)` - Add transformer
  - `unregisterTransformer(ClassFileTransformer)` - Remove transformer
- ASM integration:
  - Bundle ASM 9.x in agent
  - Transformer receives `ClassReader`, returns `ClassWriter`
- Mixin-style weaving support:
  - Transform before verification
  - Support for method injection, field addition
- Test suite:
  - Transform class at load time
  - Inject method, verify callable
  - Weave multiple transformers

**Technical focus**: Load-time weaving; ASM pipeline.

**Exit criteria**:
- Transformers execute at load time
- ASM-based transformations work
- Test suite passes (15+ scenarios)
- Tag: `v26.1-Alpha.5` (signed, Pre-Release)

---

### v26.1-Alpha.6: ThreadInsight & HeapInsight APIs

**Goal**: Expose thread and heap introspection through stable APIs.

**Deliverables**:
- `jdk.aprismate.runtime.ThreadInsight`:
  - `getAllThreads()` - Thread enumeration
  - `getThreadStack(Thread)` - Stack trace without exception
  - `getThreadCpuTime(Thread)` - Per-thread CPU time
  - `getThreadAllocatedBytes(Thread)` - Per-thread allocation
- `jdk.aprismate.runtime.HeapInsight`:
  - `getHeapRegions()` - Heap region summary (young, old, humongous)
  - `getObjectSize(Object)` - Shallow size
  - `getRetainedSize(Object)` - Retained size (estimate)
- HotSpot VM patches:
  - Patch 008: Expose thread statistics
  - Patch 009: Expose heap region info
- Test suite:
  - Query thread info, verify accuracy
  - Query heap regions, verify structure

**Technical focus**: VM introspection APIs; low-overhead access.

**Exit criteria**:
- APIs return accurate data
- Performance overhead negligible
- Test suite passes (20+ scenarios)
- Tag: `v26.1-Alpha.6` (signed, Pre-Release)

---

### v26.1-Alpha.7: JitInsight API & Performance Tuning

**Goal**: Expose JIT compilation introspection; optimize agent performance.

**Deliverables**:
- `jdk.aprismate.runtime.JitInsight`:
  - `getCompilationQueue()` - Methods pending compilation
  - `getCompiledMethods()` - Currently compiled methods
  - `getMethodCompilationLevel(Method)` - C1/C2 tier
  - `deoptimizeMethod(Method)` - Force deoptimization (for testing)
- Performance tuning:
  - Agent startup time optimization
  - Hook invocation overhead reduction
  - Memory footprint optimization
- Benchmarking suite:
  - Agent overhead on microbenchmarks
  - Real-world workload simulation

**Technical focus**: JIT awareness; performance optimization.

**Exit criteria**:
- JitInsight APIs functional
- Agent overhead <2% on typical workloads
- Benchmarks documented
- Tag: `v26.1-Alpha.7` (signed, Pre-Release)

---

### v26.1-Alpha.8: Real-World Testing (Minecraft Integration)

**Goal**: Validate AprismJDK with real Minecraft workload; integration with Aprism loader.

**Deliverables**:
- Minecraft 1.21+ testing:
  - Launch with AprismJDK 26.1-Alpha.8
  - Load Aprism agent
  - Apply Mixin transformations
  - Exercise hooks, redefinition
- Integration tests:
  - Aprism detects AprismJDK capabilities
  - Aprism upgrades behavior on AprismJDK
  - Fallback to stock JDK behavior works
- Performance validation:
  - FPS impact measurement
  - Startup time comparison
  - Memory footprint comparison
- Bug fixes from real-world testing

**Technical focus**: Real-world reliability; Aprism integration.

**Exit criteria**:
- Minecraft runs stably on AprismJDK
- Aprism capability upgrade works
- No performance regressions vs. stock JDK
- Tag: `v26.1-Alpha.8` (signed, Pre-Release)

---

### v26.1-Alpha.9: Documentation & Final Hardening

**Goal**: Complete documentation; final bug fixes and hardening.

**Deliverables**:
- Documentation completion:
  - `docs/04-perf-hardware.md` (EN + ZH) - Performance/hardware APIs
  - `docs/05-cross-language.md` (EN + ZH) - Cpp2Java/Rust2Java design
  - `docs/09-known-issues.md` (EN + ZH) - Known limitations and workarounds
  - API reference javadoc generation
- Final hardening:
  - Security review (agent permissions, JVM access)
  - Error message quality review
  - Edge case coverage expansion
- Test suite finalization:
  - Target: 200+ tests
  - Coverage report generation

**Technical focus**: Documentation completeness; production readiness.

**Exit criteria**:
- All documentation complete (EN + ZH)
- Test suite comprehensive
- No known critical bugs
- Tag: `v26.1-Alpha.9` (signed, Pre-Release)

---

### v26.1: General Availability (Full-Featured Release)

**Goal**: Production-ready AprismJDK with full deep-capability tier.

**Deliverables**:
- Feature freeze: no new functionality from Alpha.9
- Full test pass:
  - JDK regression tests (tier1 + tier2)
  - AprismJDK capability tests (all 200+ tests)
  - Cross-platform validation
  - Minecraft smoke test (10+ mods)
- Documentation review:
  - All docs updated and accurate
  - Release notes: `docs/release-notes/v26.1.md`
- Artifact finalization:
  - Final version strings
  - Final branding
- GitHub Release (non-prerelease)
- Announcement:
  - AprismLab GitHub Discussions
  - Community channels

**Technical focus**: Stability, documentation, community readiness.

**Exit criteria**:
- Zero critical bugs
- All documentation complete
- Release notes published
- Community announcement posted
- Tag: `v26.1` (signed, Release)

---

## Development Principles

### Version Control
- Follow Aprism main-project version control conventions
- Every commit signed (SSH ED25519)
- Every tag signed and annotated
- Tag message format: `<version>: <one-line-summary>`

### Testing
- Test suite grows with each alpha
- No release without full test pass
- Regression tests mandatory for bug fixes
- Performance benchmarks tracked across releases

### Documentation
- Bilingual (EN canonical + ZH mirror)
- Updated in same commit as code changes
- No feature ships without documentation
- Known issues documented transparently

### Release Process
1. Code complete for alpha milestone
2. Full test pass (`./gradlew test --rerun-tasks`)
3. Documentation review (EN + ZH)
4. Version bump commit
5. Signed tag
6. CI builds artifacts
7. GitHub Release (Pre-Release for alphas, Release for GA)
8. Update FACT.md session log

### Fail-Safe Contract
- Agent errors never crash JVM
- Capabilities degrade gracefully
- Stock JDK fallback always works
- User-facing error messages clear and actionable

---

## Timeline Estimate

Assuming 1-2 weeks per alpha (research, implementation, testing, documentation):

- **v26.0 line**: ~10-20 weeks (2.5-5 months)
- **v26.1 line**: ~10-20 weeks (2.5-5 months)
- **Total**: ~20-40 weeks (5-10 months)

Timeline is guidance only; quality and stability take precedence over schedule.

---

## Success Criteria

By v26.1 GA, AprismJDK must:

1. **Build successfully** on Linux, Windows, macOS (x64 + ARM64 for macOS)
2. **Pass OpenJDK tier1 tests** (HotSpot, JDK, langtools)
3. **Pass all AprismJDK capability tests** (200+ tests)
4. **Run Minecraft 1.21+** with Aprism loader stably
5. **Demonstrate <5% performance overhead** vs. stock OpenJDK
6. **Provide complete documentation** (EN + ZH, 9+ documents)
7. **Support graceful degradation** (all features optional, stock JDK fallback)

---

## Non-Goals (Deferred Post-v26.1)

- **Hardware-fusion APIs** (CpuFeatures, CacheTopology, NumaTopology) - v26.2+
- **Cross-language bridges** (Cpp2Java, Rust2Java implementation) - v26.3+
- **Java 21/17 backports** - v26.4+
- **Installer packages** (MSI, DMG, DEB, RPM) - v26.5+
- **GraalVM integration** - Research phase, TBD

---

# ROADMAP v2: v26.2 - v26.3 (Active Plan)

> The original v26.0/v26.1 plan was executed out of order by a previous AI
> session: the OpenJDK fork/build milestones (Alpha.3-7) were skipped and
> replaced with Java-layer API stubs under mixed version tags. The version
> numbers v26.0 and v26.1 are spent; development continues on v26.2/v26.3.
> See FACT.md Sessions 9/10/11 for the full audit history.
>
> Author: BlockConnect@StarsailsClover | Status: Active
> Updated: 2026-08-18

## Positioning Correction

AprismJDK is **not** a Minecraft optimization tool. Minecraft support is
incidental. AprismJDK's core mission:

> **A Java Runtime that empowers developers: stronger capabilities,
> better development experience, extended JDK/Java boundaries, and
> AI + Coding integration.**

## v26.2 Line: Real JDK Foundation (10 releases)

Goal: turn the existing API surface into a runtime that actually ships
as a JDK variant. Strategy note: MSYS2 `fixpath` path-corruption bugs
(documented in .trae/sessions.md) block native Windows OpenJDK builds;
WSL2 or Cygwin is required for fork builds. Alpha.1 therefore validates
the API against a prebuilt OpenJDK 25 while fork infrastructure lands.

### v26.2-Alpha.1: Environment Validation & Baseline

**Deliverables**:
- Build environment audit recorded (MSYS2/MSVC/Boot JDK status)
- `aprismate-api` + `aprismate-agent` build & full test pass against
  prebuilt OpenJDK 25 (Temurin 25.0.3+9)
- Version string corrected to v26.2-Alpha.1
- Build environment documentation (`docs/13-build-environment.md`)
- WSL2/Cygwin evaluation for future fork builds

**Exit criteria**: All modules compile; test suite green on JDK 25;
version metadata consistent.

### v26.2-Alpha.2: Module System & JPMS Hardening

**Deliverables**:
- `module-info.java` for all three modules (`jdk.aprismate`,
  `aprismate.agent`, tests)
- jdeps validation of module graph
- Multi-Release JAR layout for Java 17/21/25 targets
- SBOM generation (CycloneDX)

**Exit criteria**: `jdeps --check` clean; modular jar runs on 17/21/25.

### v26.2-Alpha.3: Fork Infrastructure (WSL2)

**Deliverables**:
- WSL2 Ubuntu installed and verified as build host
- OpenJDK 25 configure + first successful `make images` in WSL2
- Build time baseline documented
- `jdk/patches/` + `jdk/overlay/` directory structure created
- `.gitattributes` for LF/CRLF handling across the fork tree

**Exit criteria**: Unmodified OpenJDK 25 image boots in WSL2; patch
directory scaffold committed.

### v26.2-Alpha.4: Branding Patch (Patch 001)

**Deliverables**: vendor name/URL/version injection; `java -version`
shows AprismJDK identity; reproducible patch application script.

### v26.2-Alpha.5: jdk.aprismate Module Injection (Patches 002-003)

**Deliverables**: `aprismate-api` sources compiled into the fork image
as module `jdk.aprismate`; `java --list-modules` includes it; stock-JDK
fallback behavior unchanged.

### v26.2-Alpha.6: Agent Embedding (Patches 004-005)

**Deliverables**: AprismateAgent built as `lib/aprismate.jar` inside the
image; `-javaagent:lib/aprismate.jar` loads via premain/agentmain;
manifest correct.

### v26.2-Alpha.7: Auto-Load Flag (Patch 006)

**Deliverables**: `-XX:+AprismateAgent` VM flag wires the agent before
main; flag ignored (with warning) when agent absent.

### v26.2-Alpha.8: Cross-Version Compatibility Sweep

**Deliverables**: full test matrix (17/21/25 x stock/fork); capability
descriptor accuracy per platform; compatibility matrix doc refresh.

### v26.2-Alpha.9: Packaging & Distribution

**Deliverables**: tar.gz/zip packaging; SHA-256 checksums; cosign
keyless signing; GitHub Release workflow; SBOM attached.

### v26.2 GA

Feature freeze; regression suite green; bilingual docs complete; signed
release published.

## v26.3 Line: Developer Power & AI-Coding Runtime (10 releases)

Goal: deliver the capabilities that make AprismJDK worth using — deeper
introspection, hot iteration, hardware awareness, and AI-friendly
runtime surfaces. Minecraft optimization rides on these primitives as a
consumer, not the goal itself.

### v26.3-Alpha.1: Runtime Introspection Hardening

ThreadInsight/HeapInsight/JitInsight move from JMX-backed stubs to
accurate low-overhead implementations; capability descriptor wired to
real availability detection.

### v26.3-Alpha.2: Hot Reload Foundations

BytecodeTransformer gains retransform orchestration + change-set diffing;
safe class evolution rules documented (method bodies first).

### v26.3-Alpha.3: Reflection Elimination Framework

`jdk.aprismate.reflect`: runtime bytecode generation for direct invoker/
field accessors replacing Method.invoke/Field.get hot paths.

### v26.3-Alpha.4: Memory & Concurrency Primitives GA

Arena/DirectBufferPool/OffHeapMap/LockFreeQueue/FiberScheduler promoted
from reference stubs to supported APIs with stress tests.

### v26.3-Alpha.5: Hardware Awareness APIs

CpuFeatures/CacheTopology/NumaTopology read via FFM/sysconf; advisory
only; graceful absence everywhere.

### v26.3-Alpha.6: AI-Coding Surface Part 1 — Structured Introspection

Machine-readable runtime state export (JSON): loaded classes, JIT state,
GC pressure, thread topology — designed for LLM consumption in coding
agents.

### v26.3-Alpha.7: AI-Coding Surface Part 2 — Safe Experimentation Hooks

Sandboxed evaluation of candidate optimizations (e.g., try-transform +
rollback), enabling agents to propose and verify runtime changes without
crashing the host.

### v26.3-Alpha.8: Performance Pass

Agent attach <50ms budget; hook invocation overhead benchmarks; zero-
allocation fast paths for introspection APIs.

### v26.3-Alpha.9: Minecraft Integration Test Line

Real-game validation using MDL (`--java-path` to AJR image); Aprism
loader capability upgrade path exercised; perf comparison vs stock JDK.

### v26.3 GA

Full-featured release: all capability tiers fail-safe, docs EN+ZH,
signed distribution, migration guide from plain JDK workflows.

## Deferred Beyond v26.3

- Cpp2Java/Rust2Java generator tooling (FFM bridges mature first)
- Structural class redefinition requiring HotSpot patches (ClassRedefiner+
  stays API-stable, impl-gated until VM patch line lands)
- Regional GC experiments; installer packages; GraalVM research

---

## References

- [Aprism Project Management & Version Control](../Aprism/docs/en/10-project-management-and-version-control.md)
- [AprismJDK Design Document](../Aprism/docs/research/aprismjdk/01-design.md)
- [OpenJDK Build Documentation](https://github.com/openjdk/jdk/blob/master/doc/building.md)
- [Java Agent Specification](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html)

---

**Document status**: Living document, updated as development progresses.
**Next review**: v26.0-Alpha.5 (after OpenJDK source integration)
