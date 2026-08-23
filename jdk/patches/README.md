# AprismJDK Patch Series

OpenJDK fork customization lives here as an ordered patch series applied
onto the pristine upstream tree (`openjdk-25/`, tag jdk-25+10). The
upstream tree stays **untracked** in this repo (gitignored); only
patches + overlay are version-controlled.

## Git strategy decision (v26.2-Alpha.3)

**Chosen: independent upstream clone + tracked patch series.**

- `openjdk-25/` keeps its own upstream `.git` — updates are a plain
  `git fetch && git checkout <new-tag>`, then `apply-patches.sh` replays
  the series. No subtree history bloat, no submodule pinning friction.
- Reproducibility contract: a fresh clone of THIS repo + `git clone`
  upstream at the recorded tag + `scripts/configure-fork.sh` +
  `apply-patches.sh` + `make images` must yield the identical branded
  image. The recorded upstream tag lives in `gradle.properties`
  (`openjdkVersion`/`openjdkUpdate`/`openjdkBuild`) and in each patch's
  context lines themselves.
- `scripts/apply-patches.sh` refuses to run on a dirty upstream tree;
  `scripts/revert-patches.sh` restores pristine state via
  `git checkout -- .` inside the upstream clone.

## Conventions

- Files: `NNN-short-name.patch`, applied in lexical order by
  `scripts/apply-patches.sh`.
- Format: `git format-patch` style (a/b prefixes) so `git apply -p1`
  works from the openjdk-25 root. Generated via `git diff > patchfile`
  inside the upstream clone after making the change on a clean tree.
- Every patch must be reversible: `scripts/revert-patches.sh` restores
  a clean upstream checkout.
- One logical change per patch; branding, module injection, agent embed,
  and VM flags each get their own number.

## Planned series

| # | Name | Line | Status |
|---|------|------|--------|
| 001 | vendor-branding (AprismJDK identity in java -version) | v26.2-Alpha.4 | done |
| 002 | add jdk.aprismate module sources (overlay import) | v26.2-Alpha.5 | planned |
| 003 | register + export jdk.aprismate in module build | v26.2-Alpha.5 | planned |
| 004 | bundle aprismate-agent jar into images | v26.2-Alpha.6 | planned |
| 005 | agent premain wiring / manifest integration | v26.2-Alpha.6 | planned |
| 006 | `-XX:+AprismateAgent` auto-load flag | v26.2-Alpha.7 | planned |

## Overlay directory

`jdk/overlay/` mirrors the upstream tree layout for whole-file additions
(new modules' sources). Files are copied in by `apply-patches.sh` before
`.patch` files are applied.
