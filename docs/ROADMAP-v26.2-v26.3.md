# AprismJDK Development Roadmap (v26.2 - v26.3)

> Complete development plan for AprismJDK v26.2 and v26.3 lines.
> 20 releases total: v26.2-Alpha.1 through v26.2-Alpha.9 + v26.2 GA +
> v26.3-Alpha.1 through v26.3-Alpha.9 + v26.3 GA.
>
> Author: BlockConnect@StarsailsClover
> Status: Active development plan
> Created: 2026-08-16
>
> Context: v26.0/v26.1 lines produced API stubs and Java-level
> implementations but never executed the core OpenJDK fork and build.
> v26.2 returns to ROADMAP to produce a real JDK variant. v26.3 builds
> Minecraft-specific optimizations on top of the real JDK.

---

## Overview

- **v26.2 line (Real JDK Foundation)**: 9 Alphas + 1 GA = 10 releases
- **v26.3 line (Minecraft Optimization Suite)**: 9 Alphas + 1 GA = 10 releases

The v26.2 line transforms AprismJDK from a Java library into a real
OpenJDK 25 variant with branded builds, injected `jdk.aprismate`
module, and bundled AprismateAgent. The v26.3 line implements
Minecraft-specific VM-level optimizations (regionalized GC, JIT
pre-compilation, fiber scheduling, cross-region messaging).

---

## v26.2 Line: Real JDK Foundation (10 releases)

### v26.2-Alpha.1: Environment Setup & First Configure

**Goal**: Establish the build toolchain and run OpenJDK configure.

**Deliverables**:
- Install MSYS2 to `C:\msys64` (provides make, autoconf, m4, zip, etc.)
- Verify MSVC + MSYS2 interop (`vcvars64.bat` then `bash configure`)
- Run `bash configure --with-debug-level=release` in `openjdk-25/`
- Document: exact toolchain versions, configure parameters, boot JDK
- Create `docs/en/13-build-environment.md` + ZH mirror

**Exit criteria**:
- `configure` completes without errors
- `build/` directory created with generated Makefiles
- Toolchain documentation complete

> **STATUS (2026-08-18): COMPLETE with scope pivot.**
>
> MSYS2 native fork builds are BLOCKED (make reports cygwin build;
> fixpath backslash/atfile corruption — postmortem in FACT.md Session
> 11 and .trae/sessions.md). Native fork work moves to WSL2 starting
> Alpha.2. Alpha.1 was re-scoped to validate the full API surface on
> prebuilt Temurin JDK 25: Gradle wrapper 9.7.0, toolchain 25,
> Mockito 5.23 — **647 tests, 0 failures, 3 skipped** (FACT.md
> Session 11). docs/13-build-environment.md published; ZH mirror
> pending.

---

### v26.2-Alpha.2: First OpenJDK Build

**Goal**: Complete first unmodified OpenJDK 25 build on Windows.

**Deliverables**:
- Run `make images` in `openjdk-25/`
- Verify built JDK: `java -version` from image
- Run "Hello World" on built JDK
- Document build time baseline, memory requirements
- Create build verification script (`scripts/verify-build.ps1`)

**Exit criteria**:
- `openjdk-25/build/*/images/jdk/bin/java -version` works
- Build time documented
- Tag: `v26.2-Alpha.2`

> **PLAN UPDATE (2026-08-18)**: build host is **WSL2 Ubuntu**, not
> native Windows. Steps: install WSL2 + Ubuntu LTS; apt install
> build-essential autoconf zip unzip; boot JDK 24/25 inside WSL;
> reuse clean `openjdk-25/` checkout (tag jdk-25+10). Configure flags
> from the Session 10 attempt carry over minus Windows-specific ones.
> Disk guard: require >15 GB free before starting; MC caches eviction
> needs user sign-off.

---

### v26.2-Alpha.3: Patch Framework & Repo Structure

**Goal**: Establish patch application infrastructure.

**Deliverables**:
- Create `jdk/` directory structure:
  - `jdk/patches/` — patch files (series format)
  - `jdk/overlay/` — overlay source tree
  - `jdk/scripts/` — patch apply/revert scripts
- Decide git strategy: OpenJDK source stays gitignored, patches tracked
- Create `.gitattributes` for CRLF/LF handling
- Patch apply/revert automation (`scripts/apply-patches.sh`)
- Document patch workflow (`docs/en/14-patch-workflow.md` + ZH)

**Exit criteria**:
- Patch framework tested with a trivial patch
- Repo structure clean and documented
- Tag: `v26.2-Alpha.3`

---

### v26.2-Alpha.4: Branding Patch (Patch 001)

**Goal**: Apply AprismJDK branding to the JDK build.

**Deliverables**:
- Patch 001: Version string modification
  - `java -version` output shows "AprismJDK"
  - Vendor string: "AprismLab"
  - Build number includes AprismJDK version
- Version string injection from `gradle.properties`
- Rebuild with branding patch applied
- Verify: `java -version` shows AprismJDK branding
- Document patch 001 in `jdk/patches/README.md`

**Exit criteria**:
- Branded JDK builds successfully
- `java -version` correct
- Tag: `v26.2-Alpha.4`

---

### v26.2-Alpha.5: jdk.aprismate Module Injection (Patch 002-003)

**Goal**: Inject `jdk.aprismate` module into the JDK source tree.

**Deliverables**:
- Patch 002: Add `jdk.aprismate` module source to JDK
  - Source: `jdk/overlay/jdk.aprismate/share/classes/`
  - Migrate `aprismate-api` source into JDK module structure
  - Register module in JDK build system (`modules.xml` or equivalent)
- Patch 003: Export `jdk.aprismate` from JDK image
- Rebuild with module injection
- Verify: `java --list-modules` includes `jdk.aprismate`
- Verify: `java -e "jdk.aprismate.VmInfo.isAprismJdk()"` returns true
- Document module injection strategy

**Exit criteria**:
- `jdk.aprismate` module accessible at runtime
- All existing `aprismate-api` tests pass on built JDK
- Tag: `v26.2-Alpha.5`

---

### v26.2-Alpha.6: AprismateAgent Embed (Patch 004-005)

**Goal**: Embed AprismateAgent as a bundled agent in the JDK image.

**Deliverables**:
- Patch 004: Add AprismateAgent Java source to JDK
  - Location: `jdk/overlay/jdk.aprismate.agent/share/classes/`
  - Agent manifest: `META-INF/MANIFEST.MF` with `Premain-Class`, `Agent-Class`
- Patch 005: Build `aprismate.jar` as part of JDK build
  - Output: `jdk/lib/aprismate.jar` in JDK image
- Agent attachment validation:
  - Manual: `java -javaagent:lib/aprismate.jar ...`
  - Verify: agent logs "AprismateAgent loaded"
- Document agent packaging

**Exit criteria**:
- Agent loads successfully via `-javaagent`
- Agent can access `jdk.aprismate` APIs
- Tag: `v26.2-Alpha.6`

---

### v26.2-Alpha.7: Auto-Load Flag (Patch 006)

**Goal**: Implement `-XX:+AprismateAgent` VM flag for auto-loading.

**Deliverables**:
- Patch 006: HotSpot VM flag for agent auto-loading
  - Flag: `-XX:+AprismateAgent`
  - Wires agent before `main` without `-javaagent` flag
  - HotSpot argument processing changes
- Verify: `java -XX:+AprismateAgent -jar app.jar` loads agent
- Verify: agent initialized before application main
- Document VM flag behavior

**Exit criteria**:
- `-XX:+AprismateAgent` loads agent correctly
- No `-javaagent` flag needed
- Tag: `v26.2-Alpha.7`

---

### v26.2-Alpha.8: Multi-Platform Build

**Goal**: Extend builds to Linux and macOS.

**Deliverables**:
- Linux x64 build (CI or local WSL2)
- macOS x64/aarch64 build (CI)
- Platform-specific patches (if needed)
- CI workflows:
  - Linux: Ubuntu 22.04
  - Windows: Server 2022
  - macOS: macOS 13 (x64) and macOS 14 (ARM64)
- Cross-platform smoke tests

**Exit criteria**:
- All three platforms build successfully
- Platform-specific `java -version` correct
- CI green on all platforms
- Tag: `v26.2-Alpha.8`

---

### v26.2-Alpha.9: Packaging & Distribution

**Goal**: Create distributable JDK/JRE packages.

**Deliverables**:
- Packaging formats:
  - Linux: `tar.gz` (JDK and JRE)
  - Windows: `zip` (JDK and JRE)
  - macOS: `tar.gz` (JDK and JRE)
- Artifact naming: `AprismJDK-<version>-<platform>-<arch>-<type>.tar.gz`
- SHA-256 checksums for all artifacts
- cosign keyless signing (GitHub OIDC)
- CI release workflow (trigger on tag push)
- SBOM generation (CycloneDX)

**Exit criteria**:
- All artifacts package successfully
- Checksums verify
- Signatures verify
- Tag: `v26.2-Alpha.9`

---

### v26.2 GA: Stable JDK Variant Release

**Goal**: Production-ready AprismJDK foundation.

**Deliverables**:
- Feature freeze
- Full test pass:
  - JDK regression tests (tier1: hotspot, jdk, langtools)
  - AprismJDK-specific tests (agent loading, module access)
  - Cross-platform smoke tests
- Documentation review (EN + ZH)
- Release notes
- GitHub Release (non-prerelease)

**Exit criteria**:
- Zero regression test failures
- All documentation complete
- Tag: `v26.2`

---

## v26.3 Line: Minecraft Optimization Suite (10 releases)

### v26.3-Alpha.1: Minecraft Detection & Profiling API

**Goal**: Detect Minecraft runtime and profile its performance.

**Deliverables**:
- New module: `aprismate-minecraft/`
- `jdk.aprismate.minecraft.MinecraftDetector`:
  - `detect()` — detect MC via main class name, loaded class signatures
  - `getVersion()` — MC version, side (client/server), mod loader type
  - `shouldOptimize()` — exclude dev environments, incompatible mods
- `jdk.aprismate.minecraft.MinecraftProfiler`:
  - `captureSnapshot()` — tick duration, entity density, chunk load freq
  - `detectHotspots()` — identify TPS bottlenecks
- 20+ tests
- Stock JDK fallback (returns "not detected" on non-MC)

**Exit criteria**:
- Detection works for MC 1.8.9 - 1.21+
- Profiler captures accurate tick data
- Tag: `v26.3-Alpha.1`

---

### v26.3-Alpha.2: Bytecode Pre-Optimization Engine

**Goal**: Optimize bytecode at load time, before JIT.

**Deliverables**:
- `jdk.aprismate.transform.BytecodePreOptimizer`:
  - `optimize(className, bytecode)` — eliminate reflection, inline small methods
  - `optimizeForMinecraft(className, bytecode)` — MC-specific patterns
    - `MinecraftServer.tick()` loop unrolling
    - `Entity.tick()` vectorization hints
    - `ChunkProvider.getChunk()` de-virtualization
- Bytecode cache: `~/.aprismate/bytecode-cache/<version>/`
- ASM 9.7+ integration with `COMPUTE_FRAMES`
- 15+ tests

**Exit criteria**:
- Load-time optimization works
- Reflection call elimination: 80-90% performance improvement
- Tag: `v26.3-Alpha.2`

---

### v26.3-Alpha.3: Reflection Elimination Framework

**Goal**: Generate direct-call bytecode replacing reflective calls.

**Deliverables**:
- `jdk.aprismate.reflection.ReflectionAdapter`:
  - `generateDirectInvoker(Method)` — bytecode replacing `Method.invoke()`
  - `generateFieldAccessor(Field)` — bytecode replacing `Field.get()/set()`
- 34 MC version adapter patterns (from MCJEBooster research)
- Runtime code generation via ASM + `MethodHandles.Lookup.defineClass()`
- 20+ tests
- Performance: MethodHandle 1.3x overhead → direct call 1.0x

**Exit criteria**:
- Generated invokers work correctly
- Performance benchmark shows improvement
- Tag: `v26.3-Alpha.3`

---

### v26.3-Alpha.4: Regionalized Garbage Collection

**Goal**: Region-based GC for stable Minecraft tick performance.

**Deliverables**:
- `jdk.aprismate.gc.RegionalGC`:
  - `defineRegion(regionId, profile)` — map MC regions to GC regions
  - `incrementalCollect(regionId)` — tick-gap incremental GC (<5ms)
  - Player-near regions: high-frequency GC
  - Distant regions: low-frequency GC
- Based on ZGC/Shenandoah regionalized architecture
- Per-region TLAB (Thread-Local Allocation Buffer)
- Trigger after `MinecraftServer.tick()` boundary
- 15+ tests + performance benchmarks

**Exit criteria**:
- GC pause reduction: 60-70%
- TPS variance reduction: 90%
- Tag: `v26.3-Alpha.4`

---

### v26.3-Alpha.5: JIT Pre-Compilation

**Goal**: Pre-compile Minecraft hotspot methods at startup.

**Deliverables**:
- `jdk.aprismate.jit.JitPreCompiler`:
  - `precompileHotspots()` — pre-compile known MC hotspot methods
  - `loadProfile(mcVersion)` — load hotspot list per MC version
  - Target methods: `MinecraftServer.tick()`, `Entity.tick()`,
    `ChunkProvider.getChunk()`, `WorldRenderer.renderWorld()`
- JVMCI (JVM Compiler Interface) integration
- Parallel pre-compilation at JVM startup (non-blocking)
- JIT cache: `~/.aprismate/jit-cache/<version>/`
- 15+ tests

**Exit criteria**:
- Cold-start TPS fluctuation eliminated
- Stable optimal TPS from first tick
- Tag: `v26.3-Alpha.5`

---

### v26.3-Alpha.6: Fiber Scheduler

**Goal**: Lightweight fiber-based region scheduling.

**Deliverables**:
- `jdk.aprismate.thread.FiberScheduler`:
  - `createFiber(Runnable)` — create lightweight fiber per MC region
  - `yield()` — cooperative yield after region tick
  - Context switch: ~10μs → ~100ns
  - Support thousands of concurrent regions
- Based on Virtual Threads (Project Loom)
- Main thread coordinates fiber scheduling
- 15+ tests

**Exit criteria**:
- Context switch overhead: 70% reduction
- Thousands of concurrent regions supported
- Tag: `v26.3-Alpha.6`

---

### v26.3-Alpha.7: Cross-Region Async Communication

**Goal**: Efficient inter-region message passing.

**Deliverables**:
- `jdk.aprismate.region.RegionMessaging`:
  - `sendAsync(targetRegion, message)` — lock-free async messaging
  - `flushBatch()` — batch all cross-region ops at tick boundary
- LMAX Disruptor-style lock-free queue
- Single-direction latency: <50μs
- 15+ tests

**Exit criteria**:
- Latency target met
- No synchronization overhead on main thread
- Tag: `v26.3-Alpha.7`

---

### v26.3-Alpha.8: Predictive Load Balancing

**Goal**: ML-based prediction for dynamic region rebalancing.

**Deliverables**:
- `jdk.aprismate.balancer.PredictiveBalancer`:
  - `predict(region, ticksAhead)` — predict load based on history
  - `rebalance()` — split high-load regions, merge low-load ones
- Linear regression model (simplicity over complexity)
- Training data from `MinecraftProfiler` snapshots
- Evaluate every 100 ticks, adjust every 500 ticks
- 10+ tests

**Exit criteria**:
- Load variance reduction: significant
- No oscillation in region sizing
- Tag: `v26.3-Alpha.8`

---

### v26.3-Alpha.9: Client-Side Optimization

**Goal**: Client rendering and chunk mesh optimization.

**Deliverables**:
- `jdk.aprismate.client.RenderOptimizer`:
  - `batchChunkMeshes()` — merge adjacent chunk render calls
  - `loadAsync(chunkPos)` — async chunk mesh generation
  - Reduce OpenGL draw calls
- MCJEBooster migration tool:
  - `aprism-minecraft migrate-from-mcjebooster`
  - Auto-convert config files
  - Performance comparison report generation
- Launcher integration: HMCL, PCL2, Prism Launcher
- 15+ tests

**Exit criteria**:
- Client FPS improvement measured
- Migration tool functional
- Tag: `v26.3-Alpha.9`

---

### v26.3 GA: MCJEBooster Migration & Stable Release

**Goal**: Production-ready Minecraft optimization suite.

**Deliverables**:
- Feature freeze
- Full test pass (300+ tests)
- Minecraft integration test: MC 1.21+ with Aprism loader
  - TPS > 20 with 1000 entities, 10 players
  - No crashes in 1-hour stress test
- MCJEBooster compatibility layer
- Documentation: optimization guide, migration guide (EN + ZH)
- GitHub Release (non-prerelease)

**Success metrics**:
- TPS improvement: 3-5x vs vanilla
- GC pauses: <10ms (99th percentile)
- Cold-start TPS stability: >95%
- 1000+ active users within 6 months
- 3+ launcher integrations

**Exit criteria**:
- All success metrics met
- Tag: `v26.3`

---

## Development Principles

### Version Control
- `gradle.properties` bumped BEFORE tagging, never after
- Every commit signed (SSH ED25519)
- Every tag signed and annotated

### Testing
- Test suite grows with each alpha
- No release without full test pass
- Performance benchmarks tracked across releases
- Minecraft integration tests use `mdl` for automated launch

### Documentation
- Bilingual (EN canonical + ZH mirror)
- Updated in same commit as code changes
- No feature ships without documentation

### Fail-Safe Contract
- Agent errors never crash JVM
- Capabilities degrade gracefully on stock JDK
- Stock JDK fallback always works

---

## Dependency Chain

```
v26.2 (Real JDK Foundation)
  Alpha.1-3: Environment + first build + patch framework
  Alpha.4-6: Branding + module injection + agent embed
  Alpha.7-9: Auto-load + multi-platform + packaging
  GA: Stable JDK variant
        |
        v
v26.3 (Minecraft Optimization)
  Alpha.1-3: Detection + bytecode optimization + reflection elimination
  Alpha.4-6: Regionalized GC + JIT pre-compile + fiber scheduler
  Alpha.7-9: Cross-region messaging + load balancing + client opt
  GA: MCJEBooster migration + stable release
```

---

## References

- [OpenJDK Build Documentation](https://github.com/openjdk/jdk/blob/master/doc/building.md)
- [Original v26.0-v26.1 ROADMAP](ROADMAP.md) (historical, lines completed as API stubs)
- [Minecraft Optimization Plan](planning/minecraft-optimization-integration.md)
- [v26.0-Alpha.9 Advanced APIs](planning/v26.0-alpha.9-advanced-apis.md)

---

**Document status**: Living document, updated as development progresses.
