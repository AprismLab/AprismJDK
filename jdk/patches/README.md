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
| 001 | aprismjdk-branding (identity in java -version) | v26.2-Alpha.4 | done |
| 002 | aprismjdk-version-property (VmInfo capability wiring) | v26.2-Alpha.5 | done |
| 003 | agent-image-embed (lib/aprismate.jar via Images.gmk) | v26.2-Alpha.6 | done |
| 004 | aprismagent-autoload (-XX:+AprismateAgent at launcher tier) | v26.2-Alpha.7 | done |
| 005+ | reserved | — | — |

Historical notes:
- Original 002/003 "add module sources / register module" collapsed into
  the overlay: the build auto-discovers src/*/share/classes/
  module-info.java, no make registration patch needed.
- Original 006 "-XX:+AprismateAgent VM flag" was implemented at the
  LAUNCHER tier (java.c arg translation -> -javaagent) instead of
  HotSpot globals plumbing: same observable behavior, far smaller diff,
  and fail-safe jar-existence check before injecting.

## Overlay directory

`jdk/overlay/` mirrors the upstream tree layout for whole-file additions
(new modules' sources). Files are copied in by `apply-patches.sh` before
`.patch` files are applied.
