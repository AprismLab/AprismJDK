# AprismJDK

**AJR — Aprism Java Runtime** is an open-source, cross-platform OpenJDK
variant designed bottom-up around the Aprism loader ecosystem: a bundled
**AprismateAgent** (JavaAgent-like, low-level), opened and stable JVM
interfaces, performance/hardware-fusion APIs, cross-language bridges
(Cpp2Java / Rust2Java) on the Foreign Function & Memory API, and extreme
cross-Java-version compatibility.

## Status

**v26.0-Alpha.1** — Initial skeleton release. This release delivers:

- **Core API surface** — `jdk.aprismate.VmInfo` and `jdk.aprismate.Agent` stub implementations
- **Documentation** — Architecture, AprismateAgent design, opened interfaces, compatibility matrix
- **Build infrastructure** — Gradle multi-module setup, GitHub Actions workflows
- **Test framework** — JUnit 5 + AssertJ test skeleton

**Not yet implemented:**
- OpenJDK source fork and JDK/JRE image builds (v26.0-Alpha.5+)
- AprismateAgent capabilities (v26.1 line)
- VmIntrospection APIs (v26.1-Alpha.6+)

See [ROADMAP.md](docs/ROADMAP.md) for the complete v26.0-v26.1 development plan.

## Documentation

All documentation follows bilingual approach (EN canonical + ZH mirror):

- [`docs/01-architecture.md`](docs/01-architecture.md) — Positioning, architecture, relationship to Aprism, roadmap
- [`docs/02-aprismate-agent.md`](docs/02-aprismate-agent.md) — AprismateAgent entry points, capabilities, fail-safe contract
- [`docs/03-opened-interfaces.md`](docs/03-opened-interfaces.md) — The `jdk.aprismate` stable API module
- [`docs/06-compatibility-matrix.md`](docs/06-compatibility-matrix.md) — Cross-Java-version compatibility and fallback behavior
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — Complete v26.0-v26.1 development roadmap (20 releases)

**Planned for v26.0-Alpha.3+:**
- `docs/04-perf-hardware.md` — Performance & hardware-fusion APIs
- `docs/05-cross-language.md` — Cpp2Java / Rust2Java bridge design

## Quick Start

```bash
# Clone repository
git clone https://github.com/AprismLab/AprismJDK.git
cd AprismJDK

# Build all modules
./gradlew build

# Run tests
./gradlew test
```

**Note:** This release builds API stubs only. Full JDK/JRE distributions will be available starting v26.0-Alpha.5.

## Project Structure

```
AprismJDK/
├── aprismate-api/          # jdk.aprismate module (VmInfo, Agent API)
├── aprismate-agent/        # AprismateAgent implementation (skeleton)
├── aprismate-tests/        # Integration tests
├── docs/                   # Documentation (EN + ZH)
│   ├── en/                 # English documentation
│   └── zh/                 # Chinese documentation (mirrors)
└── .github/workflows/      # CI/CD automation
```

## Supported Java Versions

AprismJDK v26.x targets three Java LTS lines:

| Java Version | Status | Capabilities |
|--------------|--------|--------------|
| **Java 25** | Primary target | Full feature set |
| **Java 21** | Secondary target | Full (backported) |
| **Java 17** | Maintenance | Subset (FFM limitations) |

See [docs/06-compatibility-matrix.md](docs/06-compatibility-matrix.md) for detailed compatibility information.

## Governance

AprismJDK follows the Aprism main-project management and version-control
convention:

- **Signed commits/tags** — All commits and tags must be signed (SSH ED25519)
- **Version scheme** — `v<MAJOR>.<MINOR>[-Alpha.<N>]` (e.g., v26.0-Alpha.1, v26.0)
- **Release cadence** — Each minor line has Alpha.1-9, then GA
- **Bilingual docs** — EN (canonical) + ZH (mirror)
- **Session log** — FACT.md tracks decisions and progress

See `Aprism/docs/en/10-project-management-and-version-control.md` for complete governance model.

## Development Roadmap

**v26.0 Line (Foundation):**
- Alpha.1: Project skeleton, API stubs, documentation
- Alpha.2-4: Gradle build system, dependency management
- Alpha.5: OpenJDK source fork, basic JDK build
- Alpha.6: JDK/JRE packaging and distribution
- Alpha.7: Agent attachment infrastructure
- Alpha.8-9: Testing infrastructure, CI/CD hardening
- GA: Stable foundation release

**v26.1 Line (Capabilities):**
- Alpha.1: Agent logging and instrumentation integration
- Alpha.2: ClassRedefiner+ implementation
- Alpha.3: ClassRedefiner+ testing and hardening
- Alpha.4: MethodHookRegistry+ implementation
- Alpha.5: BytecodeTransformer implementation
- Alpha.6: ThreadInsight + HeapInsight
- Alpha.7: JitInsight + performance tuning
- Alpha.8-9: Real-world testing (Minecraft integration)
- GA: Full-featured stable release

See [docs/ROADMAP.md](docs/ROADMAP.md) for detailed milestone planning.

## Contributing

AprismJDK is part of the Aprism ecosystem. Contributions follow the same
process as the main Aprism project:

1. **Fork and branch** — Create a feature branch from `main`
2. **Sign commits** — All commits must be signed
3. **Test coverage** — Include tests for new functionality
4. **Documentation** — Update both EN and ZH docs
5. **Pull request** — Submit PR against `main` branch

## Community

- **Issues** — Report bugs and request features via GitHub Issues
- **Discussions** — Technical discussions on GitHub Discussions
- **Main project** — [AprismLab/Aprism](https://github.com/AprismLab/Aprism)

## License

GPL-2.0 (see `LICENSE`). OpenJDK upstream is GPL-2.0 with the Classpath
Exception; AprismJDK tracks upstream LTS lines (currently OpenJDK 25).
