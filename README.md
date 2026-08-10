# AprismJDK

**AJR — Aprism Java Runtime** is an open-source, cross-platform OpenJDK
variant designed bottom-up around the Aprism loader ecosystem: a bundled
**AprismateAgent** (JavaAgent-like, low-level), opened and stable JVM
interfaces, performance/hardware-fusion APIs, cross-language bridges
(Cpp2Java / Rust2Java) on the Foreign Function & Memory API, and extreme
cross-Java-version compatibility.

## Status

**Design phase (v26.4-Alpha.1).** This repository currently contains the
design documents and the subproject skeleton. The OpenJDK source fork,
the JDK build, and the AprismateAgent implementation are later
milestones tracked by the Aprism main project
(`AprismLab/Aprism`, FACT.md roadmap, v26.4 line).

## Documents

- [`docs/01-architecture.md`](docs/01-architecture.md) — positioning,
  architecture, relationship to Aprism, roadmap (canonical).
- `docs/02-aprismate-agent.md` — AprismateAgent entry points and
  capabilities (planned).
- `docs/03-opened-interfaces.md` — the `jdk.aprismate` stable surface
  (planned).
- `docs/04-perf-hardware.md` — performance & hardware-fusion APIs
  (planned).
- `docs/05-cross-language.md` — Cpp2Java / Rust2Java bridge design
  (planned).
- `docs/06-compatibility-matrix.md` — cross-Java-version compatibility
  contract (planned).

## Governance

AprismJDK follows the Aprism main-project management and version-control
convention (signed commits/tags, per-alpha Pre-Releases, GA as a bare
number, bilingual docs, FACT.md session log). See
`Aprism/docs/en/10-project-management-and-version-control.md`.

## License

GPL-2.0 (see `LICENSE`). OpenJDK upstream is GPL-2.0 with the Classpath
Exception; AprismJDK tracks upstream LTS lines (currently OpenJDK 25).
