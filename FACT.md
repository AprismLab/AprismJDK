# AprismJDK Session Log (FACT.md)

> This is the local working log for AprismJDK development sessions. It is
> intentionally **not** committed. It records what was done, decisions made,
> bugs fixed, and the roadmap for future sessions. Git history is the
> "what"; FACT.md is the "why".

---

## Session 1: Project Initialization & v26.0 Planning (2026-08-11)

### [DECISION] Version Line Planning

AprismJDK follows the main Aprism version scheme. Per Doc 10, each minor line
delivers up to 9 Alphas, then 1 GA. **Rule: never skip Alpha.9 before GA.**

Total delivery: v26.0 (10 releases) + v26.1 (10 releases) = **20 releases**
(18 Pre-Releases + 2 Releases)

**v26.0 Line (Foundation - 9 Alphas + 1 GA):**
- Alpha.1: Project structure, build system, FACT.md, module skeleton, initial docs
- Alpha.2: OpenJDK 25 source fork setup, basic build verification
- Alpha.3: JDK build pipeline complete (Windows/Linux/macOS)
- Alpha.4: JRE extraction and packaging pipeline
- Alpha.5: AprismateAgent skeleton (premain/agentmain entry points, manifest)
- Alpha.6: jdk.aprismate.VmInfo + jdk.aprismate.Agent stub APIs
- Alpha.7: Basic instrumentation integration (Java agent attachment working)
- Alpha.8: Documentation complete (docs 02/03/06 EN+ZH), testing infrastructure
- Alpha.9: Release candidate - v26.0 surface frozen, all platforms verified
- **v26.0 GA**: First stable JDK/JRE build with basic agent attachment

**v26.1 Line (Core Capabilities - 9 Alphas + 1 GA):**
- Alpha.1: ClassRedefiner+ foundation (VM patch prep, structural change design)
- Alpha.2: ClassRedefiner+ implementation (add/remove fields/methods with instance migration)
- Alpha.3: MethodHookRegistry+ foundation (deopt anchor design, JIT integration points)
- Alpha.4: MethodHookRegistry+ implementation (entry/exit hooks surviving inlining)
- Alpha.5: BytecodeTransformer pipeline (ASM integration, load-time weaving)
- Alpha.6: VmIntrospection APIs part 1 (ThreadInsight, HeapInsight)
- Alpha.7: VmIntrospection APIs part 2 (JitInsight, GC introspection)
- Alpha.8: Cross-version compatibility (Java 21/17 backports, stock JDK fallback)
- Alpha.9: Release candidate - v26.1 surface frozen, fail-safe contract verified
- **v26.1 GA**: Production-ready with full deep VM capabilities

### [DECISION] Target Java Versions

AprismJDK will support three Java versions with tiered priority:

1. **Java 25 (Primary)** - OpenJDK 25 LTS (GA 2025-09, support to ~2032)
   - Full AprismateAgent capabilities
   - All jdk.aprismate APIs
   - Primary development target

2. **Java 21 (Secondary)** - Previous LTS
   - Backport AprismJDK patches to 21 LTS baseline
   - Full compatibility with Java 25 APIs (additive only)
   - Support for mods targeting Java 21

3. **Java 17 (Maintenance)** - Extended LTS
   - Basic AprismateAgent support
   - Subset of jdk.aprismate APIs (FFM limited in 17)
   - Graceful degradation for advanced features

### [DECISION] Build & Distribution Strategy

**Build System:**
- Use OpenJDK's native build system (Make + autoconf)
- Gradle wrapper for AprismateAgent Java components
- Separate build profiles for JDK and JRE artifacts

**Artifacts per Release:**
1. `AprismJDK-<version>-jdk-<platform>.tar.gz` - Full JDK
2. `AprismJDK-<version>-jre-<platform>.tar.gz` - Runtime only (minimum)
3. `aprismate-agent-<version>.jar` - Standalone agent (for stock JDK)
4. `checksums.txt` - SHA-256 hashes
5. `.sig` + `.bundle` - cosign keyless signatures
6. `AprismJDK-sbom.cdx.json` - CycloneDX SBOM

**Platform Coverage (v26.1):**
- Windows x64 (primary)
- Linux x64 (primary)
- macOS x64/aarch64 (secondary)

### [DECISION] Documentation Structure

Following Aprism conventions, bilingual docs (EN canonical + ZH mirror):

**Planned Documents:**
- `docs/01-architecture.md` ✓ (exists, from v26.4-Alpha.1)
- `docs/02-aprismate-agent.md` (Alpha.1 - v26.0-Alpha.4)
- `docs/03-opened-interfaces.md` (Alpha.1 - v26.0-Alpha.5)
- `docs/04-perf-hardware.md` (deferred to v26.2+)
- `docs/05-cross-language.md` (deferred to v26.2+)
- `docs/06-compatibility-matrix.md` (Alpha.1 - v26.1-Alpha.5)
- `docs/zh/` - Chinese mirrors (parallel with EN)

### [DECISION] Testing Strategy

**Test Targets (incremental growth):**
- v26.0-Alpha.1: 0 tests (skeleton only)
- v26.0-Alpha.5: ~30 tests (agent attachment basics)
- v26.0-Alpha.9: ~80 tests (API contracts, build verification, RC)
- v26.0 GA: ~100 tests (platform coverage, first stable release)
- v26.1-Alpha.3: ~130 tests (hook registry foundation)
- v26.1-Alpha.6: ~180 tests (introspection APIs)
- v26.1-Alpha.9: ~230 tests (cross-JDK compatibility, RC)
- v26.1 GA: ~250+ tests (production hardening, full capabilities)

**Test Categories:**
1. Unit tests: API contract verification
2. Integration tests: Agent attachment + transformation
3. Cross-JDK tests: Stock OpenJDK 21/25 fallback behavior
4. Real-game tests: Aprism loader + AprismJDK integration

### [NOTE] Scope Boundaries

**In Scope for v26.0 GA:**
- Project build infrastructure (Gradle + OpenJDK build integration)
- OpenJDK 25 fork with AprismJDK branding
- JDK and JRE packaging pipeline (Windows/Linux/macOS)
- AprismateAgent jar with premain/agentmain entry points
- Basic jdk.aprismate API stubs (VmInfo, Agent)
- Documentation framework (docs 02/03/06 EN+ZH)
- GitHub Actions CI/CD pipeline
- Signed releases with cosign verification

**In Scope for v26.1 GA:**
- AprismateAgent core capabilities (ClassRedefiner+, MethodHookRegistry+, BytecodeTransformer)
- Full jdk.aprismate.runtime APIs (ThreadInsight, HeapInsight, JitInsight)
- VM patches for structural class redefinition
- JIT-aware hook anchors (deoptimization points)
- Fail-safe contract implementation (bad hooks never crash VM)
- Stock JDK fallback for all APIs
- Cross-Java-version support (25/21/17)
- Real-game integration testing with Aprism loader

**Deferred to v26.2+:**
- Performance & hardware fusion APIs (CpuFeatures, CacheTopology, NumaTopology, VectorHints)
- Cross-language bridges (Cpp2Java, Rust2Java on FFM)
- Advanced VM optimizations for hook overhead reduction
- Android/iOS platform support
- Auto-load via -XX:+AprismateAgent flag

**Explicit Non-Goals:**
- Changing Java language semantics
- Modifying JVM class-file format
- Replacing stock OpenJDK for non-Aprism users
- Server/enterprise optimizations (not the target use case)

### [DONE] Initial Research

- ✓ Read Aprism main project docs (Doc 01-10)
- ✓ Read AprismJDK design document (v26.4-Alpha.1)
- ✓ Reviewed Aprism version control conventions (Doc 10)
- ✓ Analyzed main project release history (v26.2, v26.3)
- ✓ Confirmed AprismJDK repo structure (skeleton + docs only)

### Roadmap: v26.0-Alpha.1 Implementation Plan

**Next Session Tasks:**

1. **Project Structure Setup**
   - Create build.gradle for agent components
   - Create Makefile wrapper for OpenJDK integration points
   - Setup gradle.properties with version tracking
   - Create settings.gradle for multi-module structure

2. **Module Skeleton**
   - aprismate-agent/ (core agent implementation)
   - aprismate-api/ (public API surface - jdk.aprismate)
   - aprismate-tests/ (test suite)
   - openjdk-patches/ (HotSpot patch sets, deferred to Alpha.2)

3. **Documentation Expansion**
   - Create docs/02-aprismate-agent.md (EN)
   - Create docs/03-opened-interfaces.md (EN)
   - Create docs/06-compatibility-matrix.md (EN)
   - Create Chinese mirrors in docs/zh/

4. **GitHub Actions Setup**
   - .github/workflows/build.yml (basic compilation)
   - .github/workflows/release.yml (triggered by tags)
   - .github/workflows/test.yml (test suite execution)

5. **Verification & Release**
   - Build skeleton compiles successfully
   - Docs pass markdown lint
   - README.md updated with v26.0-Alpha.1 status
   - Signed commit + signed tag
   - GitHub Pre-Release with artifacts

### Test Count Tracking

- Session start: N/A (no code yet)
- Session end: TBD (Alpha.1 target: 0 tests, skeleton only)

---

## Notes for Future Sessions

- OpenJDK 25 source must be cloned from upstream before Alpha.2
- Build verification should use JDK 21 as bootstrap JDK
- Agent jar must be self-contained (fat jar with relocated ASM)
- All APIs must document stock-JDK fallback behavior
- Performance baseline: agent attach overhead <50ms on stock hardware
