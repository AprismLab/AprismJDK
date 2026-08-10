# Resource Management System

## Overview

The AprismJDK resource management system provides a unified, namespace-aware interface for loading and managing resources from various sources including the file system, JAR files, mods, and resource packs.

## Core Concepts

### ResourceLocation

A namespaced identifier for resources, consisting of a namespace and a path.

```java
// Format: namespace:path
ResourceLocation loc1 = new ResourceLocation("minecraft", "textures/block/stone.png");
ResourceLocation loc2 = ResourceLocation.of("minecraft:textures/block/stone.png");

// Without namespace, uses default "aprism"
ResourceLocation loc3 = ResourceLocation.of("data/config.json"); // aprism:data/config.json
```

#### Namespace Rules
- Only lowercase letters, digits, hyphens, and underscores: `[a-z0-9_-]+`
- Examples: `minecraft`, `my-mod`, `core_utils`

#### Path Rules
- Lowercase letters, digits, underscores, hyphens, dots, and slashes: `[a-z0-9_\-./]+`
- Examples: `items/sword`, `textures/ui/button.png`, `data-v2.json`

### Resource

Represents a single loadable resource with metadata and content access.

```java
public interface Resource extends AutoCloseable {
    ResourceLocation getLocation();
    String getSource();              // mod ID or "system"
    InputStream open() throws IOException;
    Optional<Long> getSize();
    boolean exists();
    void close();
}
```

### ResourceManager

Central interface for querying and loading resources.

```java
ResourceManager rm = Resources.getManager();

// Get a single resource (highest priority)
Optional<Resource> resource = rm.getResource(ResourceLocation.of("config:settings.json"));

// Get all resources at a location (from all providers)
Collection<Resource> all = rm.getAllResources(ResourceLocation.of("textures:block/stone.png"));

// Find resources by predicate
Collection<Resource> configs = rm.findResources(loc -> 
    loc.getPath().endsWith(".json") && loc.getPath().startsWith("config/"));

// Find by namespace
Collection<Resource> modResources = rm.findResourcesInNamespace("my-mod");

// Find by path prefix
Collection<Resource> textures = rm.findResourcesByPath("textures/");
```

### ResourceProvider

Supplies resources from a specific source (mod, resource pack, system).

```java
public interface ResourceProvider {
    String getId();                  // Unique provider ID
    String getName();                // Display name
    Resource getResource(ResourceLocation location);
    Collection<ResourceLocation> listResources();
    Collection<String> getNamespaces();
    int getPriority();              // Higher = higher priority
}
```

### Resources

Static access point for the global resource manager.

```java
// Check if initialized
if (Resources.isInitialized()) {
    ResourceManager rm = Resources.getManager();
    // Use resource manager
}

// Initialize (done by runtime)
Resources.setManager(myResourceManager);

// Cleanup
Resources.clear();
```

## Resource Priority

When multiple providers supply the same resource, priority determines which is used:

1. **User resource packs**: -1000 to -1 (lowest priority, user overrides)
2. **Mods**: 0 to 999 (typical mod resources)
3. **System/Built-in**: 1000+ (highest priority, core resources)

Higher priority resources override lower priority ones.

## Usage Examples

### Loading a Configuration File

```java
ResourceLocation configLoc = ResourceLocation.of("mymod:config/settings.json");

Resources.getManager().getResource(configLoc).ifPresent(resource -> {
    try (InputStream is = resource.open()) {
        // Parse JSON from stream
        JsonObject config = JsonParser.parseStream(is);
        System.out.println("Loaded config from: " + resource.getSource());
    } catch (IOException e) {
        System.err.println("Failed to load config: " + e.getMessage());
    }
});
```

### Loading All Overrides

```java
// Get all versions of a resource (from all mods/packs)
ResourceLocation textureLoc = ResourceLocation.of("minecraft:textures/block/stone.png");
Collection<Resource> allVersions = Resources.getManager().getAllResources(textureLoc);

for (Resource resource : allVersions) {
    System.out.println("Found in: " + resource.getSource());
    System.out.println("Size: " + resource.getSize().orElse(-1L) + " bytes");
}
```

### Finding Resources by Pattern

```java
ResourceManager rm = Resources.getManager();

// Find all JSON files in config directory
Collection<Resource> configs = rm.findResources(loc ->
    loc.getPath().startsWith("config/") && loc.getPath().endsWith(".json"));

// Find all textures
Collection<Resource> textures = rm.findResourcesByPath("textures/");

// Find all resources from a specific mod
Collection<Resource> modResources = rm.findResourcesInNamespace("my-mod");

// Process found resources
for (Resource resource : configs) {
    System.out.println("Config: " + resource.getLocation());
}
```

### Checking Resource Existence

```java
ResourceLocation loc = ResourceLocation.of("mymod:optional-data.json");

if (Resources.getManager().hasResource(loc)) {
    // Resource exists, load it
    Resources.getManager().open(loc).ifPresent(is -> {
        try (is) {
            // Read data
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
} else {
    // Use default values
    System.out.println("Optional data not found, using defaults");
}
```

### Implementing a ResourceProvider

```java
public class MyModResourceProvider implements ResourceProvider {
    private final String modId;
    private final Path resourceRoot;
    
    public MyModResourceProvider(String modId, Path resourceRoot) {
        this.modId = modId;
        this.resourceRoot = resourceRoot;
    }
    
    @Override
    public String getId() {
        return modId;
    }
    
    @Override
    public String getName() {
        return "My Mod Resources";
    }
    
    @Override
    public Resource getResource(ResourceLocation location) {
        if (!location.getNamespace().equals(modId)) {
            return null; // Not our namespace
        }
        
        Path resourcePath = resourceRoot.resolve(location.getPath());
        if (Files.exists(resourcePath)) {
            return new FileResource(location, modId, resourcePath);
        }
        return null;
    }
    
    @Override
    public Collection<ResourceLocation> listResources() {
        // Walk directory tree and build ResourceLocation for each file
        // Implementation details omitted
        return Collections.emptyList();
    }
    
    @Override
    public Collection<String> getNamespaces() {
        return Collections.singleton(modId);
    }
    
    @Override
    public int getPriority() {
        return 0; // Default mod priority
    }
}
```

### Resource Reloading

```java
// Reload all resources (e.g., after loading new mods or resource packs)
try {
    Resources.getManager().reload();
    System.out.println("Resources reloaded successfully");
} catch (IOException e) {
    System.err.println("Failed to reload resources: " + e.getMessage());
}
```

## Resource Namespaces

Namespaces prevent naming conflicts between mods and organize resources logically.

### System Namespace
- `aprism:*` - Core AprismJDK resources
- Used when no namespace is specified

### Mod Namespaces
- Each mod should use its own namespace matching its mod ID
- `mymod:textures/logo.png`
- `coolplugin:config/defaults.json`

### Common Namespace Conventions
- `minecraft:*` - Minecraft resources (if applicable)
- `forge:*` - Forge resources (if applicable)
- `fabric:*` - Fabric resources (if applicable)

## Resource Organization

Recommended directory structure within a mod JAR:

```
assets/
  mymod/
    textures/
      items/
        sword.png
      blocks/
        stone.png
    sounds/
      click.ogg
    lang/
      en_us.json
      
data/
  mymod/
    config/
      defaults.json
    recipes/
      sword.json
```

Access as:
- `mymod:textures/items/sword.png`
- `mymod:config/defaults.json`

## Best Practices

### 1. Always Use Namespaces
```java
// Good
ResourceLocation loc = ResourceLocation.of("mymod:config.json");

// Avoid (uses default namespace)
ResourceLocation loc = ResourceLocation.of("config.json");
```

### 2. Check Resource Existence
```java
// Good
if (rm.hasResource(loc)) {
    rm.open(loc).ifPresent(/* ... */);
}

// Less safe
rm.open(loc).ifPresent(/* ... */); // May silently fail
```

### 3. Always Close Streams
```java
// Good - try-with-resources
try (InputStream is = resource.open()) {
    // Use stream
}

// Bad - may leak resources
InputStream is = resource.open();
// Use stream
// Forgot to close!
```

### 4. Use Specific Paths
```java
// Good - specific path
rm.findResourcesByPath("config/settings/");

// Less efficient - broad search
rm.findResources(loc -> loc.getPath().contains("config"));
```

### 5. Handle Multiple Resources
```java
// When overrides matter, get all resources
Collection<Resource> allVersions = rm.getAllResources(loc);

// Default behavior: highest priority only
Optional<Resource> primary = rm.getResource(loc);
```

## Implementation Status (v26.0-Alpha.5)

### Completed
- ✅ ResourceLocation with namespace/path validation
- ✅ Resource interface with content access
- ✅ ResourceManager interface with querying methods
- ✅ ResourceProvider interface with priority system
- ✅ Resources static API for global access
- ✅ Comprehensive test suite (100 tests)
- ✅ Documentation

### Not Yet Implemented (Alpha.5)
- ⏳ Concrete ResourceManager implementation
- ⏳ File-based ResourceProvider
- ⏳ JAR-based ResourceProvider
- ⏳ Resource pack support
- ⏳ Resource reloading mechanism
- ⏳ Resource caching
- ⏳ Async resource loading

The API is complete and tested. Implementation will be added in aprismate-agent in future releases.

## Design Principles

1. **Namespace Isolation** - Prevent naming conflicts between mods
2. **Priority System** - Allow overriding resources predictably
3. **Multiple Sources** - Support files, JARs, network, memory
4. **Lazy Loading** - Resources loaded on demand, not at startup
5. **Type Agnostic** - Works with any file type (images, JSON, binary)
6. **Stream-Based** - Efficient for large resources

## Testing

Resource components should be tested for:
- Valid and invalid namespace/path formats
- Resource location parsing and formatting
- Resource existence checking
- Stream opening and closing
- Provider priority ordering
- Null safety and error handling

See test classes in `jdk.aprismate.resource` package for examples.

## Future Enhancements (Post-v26.0)

- Resource pack UI and management
- Hot-reloading of resources
- Resource compression (ZIP, GZIP)
- Remote resource loading (HTTP/HTTPS)
- Resource metadata and annotations
- Resource dependency tracking
- Resource validation and verification
- Async/parallel resource loading
- Resource watching (file system events)
- Resource profiling and metrics
