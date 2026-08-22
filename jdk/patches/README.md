# AprismJDK Patch Series

OpenJDK fork customization lives here as an ordered patch series applied
onto the pristine upstream tree (`openjdk-25/`, tag jdk-25+10). The
upstream tree stays **untracked** (gitignored); only patches + overlay
are version-controlled.

## Conventions

- Files: `NNN-short-name.patch`, applied in lexical order by
  `scripts/apply-patches.sh`.
- Format: `git format-patch` style (a/b prefixes) so `git apply -p1`
  works from the openjdk-25 root.
- Every patch must be reversible: `scripts/revert-patches.sh` restores
  a clean upstream checkout (`git -C openjdk-25 checkout -- .`).
- One logical change per patch; branding, module injection, agent embed,
  and VM flags each get their own number.

## Planned series

| # | Name | Line |
|---|------|------|
| 001 | vendor-branding (vendor name/URL/version string) | v26.2-Alpha.4 |
| 002 | add jdk.aprismate module sources (overlay import) | v26.2-Alpha.5 |
| 003 | register + export jdk.aprismate in module build | v26.2-Alpha.5 |
| 004 | bundle aprismate-agent jar into images | v26.2-Alpha.6 |
| 005 | agent premain wiring / manifest integration | v26.2-Alpha.6 |
| 006 | `-XX:+AprismateAgent` auto-load flag | v26.2-Alpha.7 |

## Overlay directory

`jdk/overlay/` mirrors the upstream tree layout for whole-file additions
(new modules' sources). Files are copied in by `apply-patches.sh` before
`.patch` files are applied.
