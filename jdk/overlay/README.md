# jdk/overlay — whole-file additions to the fork

This directory mirrors the OpenJDK source layout. Everything here is
copied over `openjdk-25/` by `scripts/apply-patches.sh` BEFORE numbered
patches run.

Planned contents (v26.2-Alpha.5):

```
overlay/
└── src/
    └── jdk.aprismate/
        └── share/classes/
            ├── module-info.java
            └── jdk/aprismate/**   (migrated from aprismate-api)
```

Rules:
- Only NEW files go here; modifications to upstream files belong in
  `jdk/patches/*.patch`.
- Paths must be relative to the openjdk-25 root exactly as they should
  land in the build tree.
