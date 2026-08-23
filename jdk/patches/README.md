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
| 003+ | reserved for agent embed / auto-load flag work | v26.2-Alpha.6-7 | planned |

Historical note: original plan had 002/003 as "add module sources" +
"register/export module". Empirically the build auto-discovers modules
via src/*/share/classes/module-info.java, so module addition is carried
entirely by jdk/overlay/ (see scripts/sync-overlay.sh); no make-file
patch was needed. The freed numbering was reassigned to the capability-
property wiring.

## Overlay directory

`jdk/overlay/` mirrors the upstream tree layout for whole-file additions
(new modules' sources). Files are copied in by `apply-patches.sh` before
`.patch` files are applied.
