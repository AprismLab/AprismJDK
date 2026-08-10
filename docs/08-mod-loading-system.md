# Mod Loading System

## Overview

The AprismJDK mod loading system provides a comprehensive framework for discovering, loading, and managing mods. It handles dependency resolution, version constraints, environment compatibility, and mod lifecycle management.

## Core Components

### ModMetadata

Contains descriptive information about a mod, typically loaded from a `mod.json` file in the mod's JAR.

```java
public interface ModMetadata {
    String getId();              // Unique mod identifier
    String getVersion();         // Semantic version
    String getName();            // Human-readable name
    Optional<String> getDescription();
    List<String> getAuthors();
    List<ModDependency> getDependencies();
    ModEnvironment getEnvironment();
    // ... and more
}
```

#### Mod ID Requirements

- Must be unique across all mods
- Contains only lowercase letters, digits, hyphens, and underscores
- Must start with a letter
- Between 3 and 64 characters long

Examples: `my-mod`, `example_plugin`, `coolmod123`

### ModContainer

Wraps a loaded mod instance along with its metadata and state.

```java
public interface ModContainer {
    ModMetadata getMetadata();
    Optional<Object> getModInstance();
    ModState getState();
    Path getSource();
    ClassLoader getClassLoader();
    boolean isLoaded();
}
```

#### Mod States

1. **DISCOVERED** - Mod found but not yet loaded
2. **LOADING** - Currently being loaded
3. **LOADED** - Successfully loaded and active
4. **ERRORED** - Failed to load due to an error
5. **DISABLED** - Disabled by user or system

### ModDependency

Specifies a dependency relationship between mods.

```java
public interface ModDependency {
    String getModId();
    String getVersionRange();
    boolean isRequired();
    DependencyType getType();
}
```

#### Dependency Types

- **REQUIRED** - Must be present and loaded first
- **OPTIONAL** - Used if present, loaded first
- **CONFLICTS** - Cannot coexist with this mod
- **BEFORE** - This mod loads before the dependency
- **AFTER** - This mod loads after the dependency

#### Version Ranges

Uses Maven-style version constraints:

```
"1.0"         → Any version >= 1.0
"[1.0]"       → Exactly version 1.0
"[1.0,2.0)"   → Version >= 1.0 and < 2.0
"[1.0,)"      → Version >= 1.0
"(,2.0)"      → Version < 2.0
"*"           → Any version
```

### ModEnvironment

Specifies where a mod can run:

- **CLIENT** - Client-side only (UI, rendering, input)
- **SERVER** - Server-side only (gameplay logic, world gen)
- **UNIVERSAL** - Both client and server

```java
ModEnvironment.CLIENT.isCompatibleWith(ModEnvironment.UNIVERSAL); // true
ModEnvironment.CLIENT.isCompatibleWith(ModEnvironment.SERVER);    // false
```

### ModRegistry

Central registry for querying loaded mods.

```java
// Check if a mod is loaded
if (ModRegistry.isModLoaded("example-mod")) {
    // Use the mod's features
}

// Get mod container
ModRegistry.getMod("example-mod").ifPresent(container -> {
    System.out.println("Found: " + container.getMetadata().getName());
});

// Get all mods
Collection<ModContainer> allMods = ModRegistry.getAllMods();

// Find which mod owns a class
ModRegistry.getModFromClass(MyClass.class).ifPresent(container -> {
    System.out.println("Class from: " + container.getMetadata().getId());
});
```

## mod.json Format

Every mod should include a `mod.json` file at the root of its JAR:

```json
{
  "id": "example-mod",
  "version": "1.0.0",
  "name": "Example Mod",
  "description": "An example mod for AprismJDK",
  "authors": ["YourName"],
  "homepage": "https://example.com/mymod",
  "source": "https://github.com/yourname/example-mod",
  "license": "MIT",
  "icon": "assets/icon.png",
  "environment": "universal",
  "dependencies": [
    {
      "modId": "aprism-api",
      "versionRange": "[26.0,)",
      "required": true,
      "type": "required"
    },
    {
      "modId": "optional-feature",
      "versionRange": "*",
      "required": false,
      "type": "optional"
    }
  ],
  "entrypoint": "com.example.mymod.ExampleMod",
  "custom": {
    "myCustomField": "custom value"
  }
}
```

### Required Fields

- `id` - Unique mod identifier
- `version` - Semantic version string
- `name` - Human-readable name

### Optional Fields

- `description` - Brief description
- `authors` - List of author names
- `homepage` - Mod website URL
- `source` - Source code repository URL
- `issues` - Issue tracker URL
- `license` - SPDX license identifier
- `icon` - Path to icon file in JAR
- `environment` - "client", "server", or "universal" (default: "universal")
- `dependencies` - Array of mod dependencies
- `entrypoint` - Main class for the mod
- `custom` - Object with custom metadata

## Mod Lifecycle

1. **Discovery** - Scan mods directory for JAR files
2. **Metadata Loading** - Parse `mod.json` from each JAR
3. **Dependency Resolution** - Sort mods by dependencies
4. **Validation** - Check for conflicts and missing dependencies
5. **Loading** - Instantiate mod classes in dependency order
6. **Initialization** - Fire `StartupEvent` to all mods
7. **Running** - Mods respond to events and provide features
8. **Shutdown** - Fire `ShutdownEvent` before exit

## Implementation Status (v26.0-Alpha.4)

### Completed
- ✅ ModMetadata interface with comprehensive fields
- ✅ ModContainer interface with state management
- ✅ ModDependency interface with multiple dependency types
- ✅ ModEnvironment enum with compatibility checking
- ✅ ModRegistry API for mod queries
- ✅ Comprehensive test suite
- ✅ Documentation

### Not Yet Implemented (Alpha.4)
- ⏳ mod.json parsing
- ⏳ Mod discovery and scanning
- ⏳ Dependency resolution algorithm
- ⏳ Mod class loading
- ⏳ Mod instantiation

The API is complete and tested. Implementation will be added in aprismate-agent in future releases.

## Design Principles

1. **Declarative** - Mods declare their requirements in metadata
2. **Safe** - Validation catches errors before loading
3. **Isolated** - Each mod has its own classloader
4. **Ordered** - Dependencies control load order
5. **Queryable** - Runtime mod discovery and introspection

## Usage Examples

### Simple Mod

```java
// mod.json
{
  "id": "simple-mod",
  "version": "1.0.0",
  "name": "Simple Mod",
  "entrypoint": "com.example.SimpleMod"
}

// SimpleMod.java
package com.example;

import jdk.aprismate.event.Subscribe;
import jdk.aprismate.event.lifecycle.StartupEvent;

public class SimpleMod {
    @Subscribe
    public void onStartup(StartupEvent event) {
        System.out.println("Simple Mod loaded!");
    }
}
```

### Mod with Dependencies

```java
// mod.json
{
  "id": "advanced-mod",
  "version": "2.0.0",
  "name": "Advanced Mod",
  "dependencies": [
    {
      "modId": "library-mod",
      "versionRange": "[1.0,2.0)",
      "required": true,
      "type": "required"
    },
    {
      "modId": "compatibility-mod",
      "versionRange": "*",
      "required": false,
      "type": "optional"
    },
    {
      "modId": "old-mod",
      "versionRange": "*",
      "required": false,
      "type": "conflicts"
    }
  ]
}
```

### Querying Loaded Mods

```java
public class ModIntegration {
    public void checkIntegrations() {
        // Check if optional mod is loaded
        if (ModRegistry.isModLoaded("optional-feature")) {
            enableOptionalFeature();
        }
        
        // Get mod information
        ModRegistry.getMod("library-mod").ifPresent(container -> {
            String version = container.getMetadata().getVersion();
            System.out.println("Using library version: " + version);
        });
        
        // List all mods
        int modCount = ModRegistry.getModCount();
        System.out.println("Loaded " + modCount + " mods");
        
        for (ModContainer mod : ModRegistry.getAllMods()) {
            System.out.println("- " + mod.getMetadata().getName() + 
                             " v" + mod.getMetadata().getVersion());
        }
    }
}
```

### Environment-Specific Mods

```java
// Client-only mod
{
  "id": "ui-enhancements",
  "environment": "client",
  "name": "UI Enhancements"
}

// Server-only mod
{
  "id": "admin-tools",
  "environment": "server",
  "name": "Admin Tools"
}

// Check environment compatibility at runtime
ModEnvironment current = getCurrentEnvironment();
ModEnvironment modEnv = metadata.getEnvironment();
if (modEnv.isCompatibleWith(current)) {
    loadMod();
}
```

## Future Enhancements (Post-v26.0)

- Mod hot-reloading
- Multiple entrypoints per mod
- Mod configuration UI
- Mod update checking
- Dependency download/installation
- Mod signing and verification
- Mod sandboxing and permissions
- Mod profiling and performance metrics

## Testing

Mod components should be tested for:
- Metadata parsing and validation
- Dependency resolution correctness
- Version range matching
- Environment compatibility
- State transitions
- Error handling

See test classes in `jdk.aprismate.mod` package for examples.
