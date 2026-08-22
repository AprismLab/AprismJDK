# AprismJDK Build Environment Reference

> Authoritative record of the toolchain used to build AprismJDK.
> Updated per FACT.md Session 11 (2026-08-18). English canonical;
> Chinese mirror pending (docs/zh/13-build-environment.md).

---

## 1. Host Baseline (v26.2-Alpha.1)

| Component | Value | Notes |
|---|---|---|
| OS | Windows 11 x64 (build 10.0.28020) | Primary dev host |
| CPU / RAM | 20 cores / 16 GB | `make JOBS=15` ceiling for fork builds |
| Disk free after cleanup | ~23.6 GB | MC toolchain caches (~8.5 GB) deliberately retained |
| System Java | Temurin 21.0.11+10-LTS | Default `java` on PATH |
| Target Java | Temurin **25.0.3+9-LTS** at `C:\Users\Sails\Java\jdk-25.0.3+9` | Gradle daemon + toolchain for all modules |

## 2. JVM Build (Gradle side)

| Item | Version | Rationale |
|---|---|---|
| Gradle wrapper | **9.7.0-bin** (Tencent mirror) | 8.5 cannot run on JDK 25; mirror matches sibling projects and local dist cache |
| Toolchain | Java **25** (root build.gradle) | FFM (`java.lang.foreign`) final since 22; no preview flags needed |
| junit-jupiter | 5.10.2 (+ platform launcher 1.10.2 explicit) | Gradle 9 no longer injects the launcher |
| Mockito | **5.23.0** | <=5.11 ByteBuddy cannot instrument JDK 25 classes ("Could not modify all classes") |
| ASM | 9.7.1 | unchanged |
| Javadoc | `-Xdoclint:none -quiet` | JDK 25 javadoc enables doclint by default; legacy stubs fail HTML checks |

### Reproduce the JVM build

```powershell
$env:JAVA_HOME = "C:\Users\Sails\Java\jdk-25.0.3+9"
.\gradlew.bat build            # compiles + runs full suite
# Verified 2026-08-18: 647 tests, 0 failures, 3 skipped
```

## 3. Native Fork Build Status (v26.2-Alpha.3 target)

Native OpenJDK builds are **blocked on MSYS2** and will move to WSL2:

1. MSYS2 make reports `Built for x86_64-pc-cygwin`; OpenJDK configure
   (msys2 path) demands `msys` — rejected.
2. Forcing cygwin detection corrupts TEMP extraction (`-t` receives
   cmd.exe banner), breaking fixpath generation.
3. fixpath.sh backslash corruption through bash `-c` strings
   (`C:\msys64\usr\bin\bash` → `C:msys64usrbinbash`).
4. fixpath atfile EXIT-trap race deletes jar argfiles before use.

Full postmortem: `.trae/sessions.md` (local, untracked); summary in
FACT.md Session 11. All source-tree patches from that session were
reverted (`git checkout` of basic_windows.m4, toolchain_microsoft.m4,
fixpath.sh — clean tree verified 2026-08-18).

### WSL2 plan (Alpha.3)

- Install WSL2 + Ubuntu LTS
- `sudo apt install build-essential autoconf zip unzip` (boot JDK 24/25)
- Reuse `openjdk-25/` source tree (clean checkout, tag jdk-25+10)
- Configure flags preserved from Session 10 attempt:
  `--with-vendor-name=AprismLab --with-version-string=26.2.1 ...`
  (drop `--with-toolchain-version`, Windows-specific)

## 4. Minecraft Test Harness (v26.3-Alpha.9 target)

MDL (MCDebugLauncher) v26.1.0 present on PATH. Key capabilities for
AJR validation launches:

```bash
mdl launch <name> --java-path C:\path\to\AprismJDK-image   # boot AJR
mdl launch <name> --aprism                                  # attach Aprism loader agent
mdl logs <name> --level error                               # triage
```

Existing instances cover MC 26.x vanilla/fabric/neoforge matrices
(62 instances). Despotes control plane available on fabric/quilt lines
for in-game assertions during perf comparisons.

## 5. Known Constraints

- **Disk**: keep >15 GB free before any native fork build; evicting
  `ng_execute`/`forge_gradle` caches (~8.5 GB combined) requires user
  sign-off (slow re-downloads for active MC projects).
- **Network**: gradle.org direct downloads time out; Tencent mirror is
  the reliable path. Maven Central reachable directly.
- **Signing**: releases require SSH-signed commits/tags (see §Governance
  in README). Verify `git config gpg.format ssh` before tagging GA.
