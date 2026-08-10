# Configuration System

## Overview

The AprismJDK configuration system provides a flexible, format-agnostic interface for managing application and mod configurations. It supports multiple configuration formats through pluggable loaders and provides automatic loading, saving, and caching.

## Core Concepts

### Config

The central interface for accessing configuration values with type-safe getters.

```java
Config config = Configs.getManager().load(Path.of("config.json"));

// Get values with Optional
Optional<String> name = config.getString("app.name");
Optional<Integer> port = config.getInt("server.port");
Optional<Boolean> enabled = config.getBoolean("features.logging");

// Get values with defaults
String name = config.getString("app.name", "MyApp");
int port = config.getInt("server.port", 8080);
boolean enabled = config.getBoolean("features.logging", true);

// Get complex types
Optional<List<String>> items = config.getStringList("allowed.hosts");
Optional<Config> section = config.getSection("database");
```

### Supported Types

- **String**: `getString(key)`, `getString(key, default)`
- **Integer**: `getInt(key)`, `getInt(key, default)`
- **Long**: `getLong(key)`, `getLong(key, default)`
- **Double**: `getDouble(key)`, `getDouble(key, default)`
- **Boolean**: `getBoolean(key)`, `getBoolean(key, default)`
- **String List**: `getStringList(key)`, `getStringList(key, default)`
- **Nested Config**: `getSection(key)`

### Hierarchical Keys

Configuration keys use dot notation for nested structures:

```java
// Config structure:
// server:
//   host: localhost
//   port: 8080
//   ssl:
//     enabled: true
//     keystore: /path/to/keystore

String host = config.getString("server.host", "0.0.0.0");
int port = config.getInt("server.port", 80);
boolean sslEnabled = config.getBoolean("server.ssl.enabled", false);
String keystore = config.getString("server.ssl.keystore", "");

// Or navigate sections
Optional<Config> serverConfig = config.getSection("server");
serverConfig.ifPresent(server -> {
    String host = server.getString("host", "0.0.0.0");
    Optional<Config> ssl = server.getSection("ssl");
    // ...
});
```

### ConfigLoader

Handles format-specific parsing and serialization.

```java
public interface ConfigLoader {
    String getFormat();              // "json", "yaml", "toml", etc.
    String[] getExtensions();        // [".json"], [".yml", ".yaml"]
    Config load(InputStream input);
    Config load(Path path);
    void save(Config config, OutputStream output);
    void save(Config config, Path path);
    boolean canHandle(Path path);
    Config createEmpty();
}
```

### ConfigManager

Central management for loading, saving, and caching configurations.

```java
ConfigManager cm = Configs.getManager();

// Load/save with auto-detection
Config config = cm.load(Path.of("config.json"));
cm.save(config, Path.of("output.json"));

// Load/save with specific format
Config config = cm.load(Path.of("data.txt"), "json");
cm.save(config, Path.of("data.txt"), "yaml");

// Mod configurations
Config modConfig = cm.getModConfig("mymod", "config.json");

// System configuration
Config sysConfig = cm.getSystemConfig();
```

### Configs

Static access point for the global configuration manager.

```java
// Check if initialized
if (Configs.isInitialized()) {
    ConfigManager cm = Configs.getManager();
    // Use configuration manager
}

// Initialize (done by runtime)
Configs.setManager(myConfigManager);

// Cleanup
Configs.clear();
```

## Usage Examples

### Loading a Configuration File

```java
try {
    Config config = Configs.getManager().load(Path.of("config/app.json"));
    
    String appName = config.getString("name", "DefaultApp");
    int port = config.getInt("port", 8080);
    boolean debug = config.getBoolean("debug", false);
    
    System.out.println("Starting " + appName + " on port " + port);
    
} catch (IOException e) {
    System.err.println("Failed to load config: " + e.getMessage());
} catch (ConfigException e) {
    System.err.println("Invalid config format: " + e.getMessage());
}
```

### Creating and Saving a Configuration

```java
ConfigManager cm = Configs.getManager();
Config config = cm.getLoader("json").orElseThrow().createEmpty();

// Set values
config.set("app.name", "MyApp");
config.set("app.version", "1.0.0");
config.set("server.port", 8080);
config.set("server.host", "localhost");
config.set("features.logging", true);
config.set("features.metrics", false);

// Save to file
try {
    cm.save(config, Path.of("config/app.json"));
    System.out.println("Configuration saved");
} catch (IOException | ConfigException e) {
    System.err.println("Failed to save config: " + e.getMessage());
}
```

### Working with Nested Sections

```java
Config config = Configs.getManager().load(Path.of("config.json"));

// Access nested sections
Optional<Config> database = config.getSection("database");
database.ifPresent(db -> {
    String url = db.getString("url", "jdbc:h2:mem:test");
    String user = db.getString("user", "sa");
    String pass = db.getString("password", "");
    
    Optional<Config> pool = db.getSection("pool");
    pool.ifPresent(p -> {
        int minSize = p.getInt("min", 5);
        int maxSize = p.getInt("max", 20);
        // Configure connection pool
    });
});
```

### Mod Configuration

```java
// Each mod gets its own config directory
// Format: config/{modId}/{fileName}

try {
    Config config = Configs.getManager().getModConfig("mymod", "settings.json");
    
    // Read settings
    boolean enabled = config.getBoolean("enabled", true);
    int level = config.getInt("level", 1);
    
    // Modify settings
    config.set("lastUsed", System.currentTimeMillis());
    
    // Auto-saved on shutdown or manual save
    Configs.getManager().saveAll();
    
} catch (IOException | ConfigException e) {
    System.err.println("Failed to access mod config: " + e.getMessage());
}
```

### Checking Keys and Iteration

```java
Config config = /* ... */;

// Check if key exists
if (config.contains("server.port")) {
    int port = config.getInt("server.port").get();
}

// Get all top-level keys
Set<String> keys = config.getKeys();
for (String key : keys) {
    System.out.println("Key: " + key);
}

// Get all keys recursively
Set<String> allKeys = config.getAllKeys();
for (String key : allKeys) {
    System.out.println("Full path: " + key);
}

// Check if empty
if (config.isEmpty()) {
    System.out.println("Configuration is empty");
}

// Get size
int numKeys = config.size();
System.out.println("Configuration has " + numKeys + " top-level keys");
```

### Working with String Lists

```java
Config config = /* ... */;

// Read list
List<String> allowedHosts = config.getStringList("security.allowedHosts", 
    Arrays.asList("localhost", "127.0.0.1"));

// Modify list
List<String> newHosts = new ArrayList<>(allowedHosts);
newHosts.add("192.168.1.100");
config.set("security.allowedHosts", newHosts);

// Save changes
Configs.getManager().saveAll();
```

### Implementing a ConfigLoader

```java
public class JsonConfigLoader implements ConfigLoader {
    
    @Override
    public String getFormat() {
        return "json";
    }
    
    @Override
    public String[] getExtensions() {
        return new String[] { ".json" };
    }
    
    @Override
    public Config load(InputStream input) throws IOException, ConfigException {
        try {
            JsonObject json = JsonParser.parseStream(input);
            return new JsonConfig(json);
        } catch (JsonException e) {
            throw new ConfigException("Failed to parse JSON", e);
        }
    }
    
    @Override
    public Config load(Path path) throws IOException, ConfigException {
        try (InputStream is = Files.newInputStream(path)) {
            return load(is);
        }
    }
    
    @Override
    public void save(Config config, OutputStream output) throws IOException, ConfigException {
        if (!(config instanceof JsonConfig)) {
            throw new ConfigException("Config must be JsonConfig");
        }
        JsonConfig jsonConfig = (JsonConfig) config;
        output.write(jsonConfig.toJson().toString().getBytes());
    }
    
    @Override
    public void save(Config config, Path path) throws IOException, ConfigException {
        try (OutputStream os = Files.newOutputStream(path)) {
            save(config, os);
        }
    }
    
    @Override
    public Config createEmpty() {
        return new JsonConfig(new JsonObject());
    }
}
```

### Registering Custom Loaders

```java
// During initialization
ConfigManager cm = Configs.getManager();

// Register built-in loaders
cm.registerLoader(new JsonConfigLoader());
cm.registerLoader(new YamlConfigLoader());
cm.registerLoader(new TomlConfigLoader());
cm.registerLoader(new PropertiesConfigLoader());

// Register custom loader
cm.registerLoader(new MyCustomConfigLoader());

// Use registered loader
Config config = cm.load(Path.of("data.custom"), "custom");
```

### Reloading Configurations

```java
// Reload all cached configurations from disk
try {
    Configs.getManager().reloadAll();
    System.out.println("All configurations reloaded");
} catch (IOException | ConfigException e) {
    System.err.println("Failed to reload configs: " + e.getMessage());
}
```

### Error Handling

```java
try {
    Config config = Configs.getManager().load(Path.of("config.json"));
    
    // Use configuration
    
} catch (IOException e) {
    // File not found, permission denied, etc.
    System.err.println("I/O error: " + e.getMessage());
    
} catch (ConfigException e) {
    // Invalid format, parsing error, etc.
    System.err.println("Configuration error: " + e.getMessage());
    
    // Check for nested cause
    if (e.getCause() != null) {
        System.err.println("Caused by: " + e.getCause().getMessage());
    }
}
```

## Configuration File Formats

### JSON Example

```json
{
  "app": {
    "name": "MyApp",
    "version": "1.0.0"
  },
  "server": {
    "host": "localhost",
    "port": 8080,
    "ssl": {
      "enabled": true,
      "keystore": "/path/to/keystore"
    }
  },
  "features": {
    "logging": true,
    "metrics": false
  },
  "allowedHosts": [
    "localhost",
    "127.0.0.1",
    "192.168.1.0/24"
  ]
}
```

### YAML Example

```yaml
app:
  name: MyApp
  version: 1.0.0

server:
  host: localhost
  port: 8080
  ssl:
    enabled: true
    keystore: /path/to/keystore

features:
  logging: true
  metrics: false

allowedHosts:
  - localhost
  - 127.0.0.1
  - 192.168.1.0/24
```

### TOML Example

```toml
[app]
name = "MyApp"
version = "1.0.0"

[server]
host = "localhost"
port = 8080

[server.ssl]
enabled = true
keystore = "/path/to/keystore"

[features]
logging = true
metrics = false

allowedHosts = ["localhost", "127.0.0.1", "192.168.1.0/24"]
```

### Properties Example

```properties
app.name=MyApp
app.version=1.0.0
server.host=localhost
server.port=8080
server.ssl.enabled=true
server.ssl.keystore=/path/to/keystore
features.logging=true
features.metrics=false
allowedHosts=localhost,127.0.0.1,192.168.1.0/24
```

## Directory Structure

Default configuration directory layout:

```
config/
  aprism.json              # System configuration
  mymod/
    config.json            # Mod-specific configuration
    settings.json          # Additional mod settings
  anothermod/
    config.yml             # Different format
```

Access paths:
- System config: `Configs.getManager().getSystemConfig()`
- Mod config: `Configs.getManager().getModConfig("mymod", "config.json")`
- Custom path: `Configs.getManager().load(Path.of("config/custom.toml"))`

## Best Practices

### 1. Always Provide Defaults

```java
// Good - handles missing keys gracefully
int port = config.getInt("port", 8080);
String host = config.getString("host", "localhost");

// Less safe - requires null checks
Optional<Integer> port = config.getInt("port");
if (port.isPresent()) {
    // Use port
}
```

### 2. Validate Configuration Values

```java
int port = config.getInt("server.port", 8080);
if (port < 1 || port > 65535) {
    throw new ConfigException("Invalid port: " + port);
}

String logLevel = config.getString("logging.level", "INFO");
if (!Arrays.asList("DEBUG", "INFO", "WARN", "ERROR").contains(logLevel)) {
    throw new ConfigException("Invalid log level: " + logLevel);
}
```

### 3. Use Sections for Organization

```java
// Good - organized in sections
Optional<Config> server = config.getSection("server");
server.ifPresent(s -> {
    String host = s.getString("host", "localhost");
    int port = s.getInt("port", 8080);
});

// Less organized - flat keys
String host = config.getString("serverHost", "localhost");
int port = config.getInt("serverPort", 8080);
```

### 4. Handle Errors Gracefully

```java
Config config;
try {
    config = Configs.getManager().load(Path.of("config.json"));
} catch (IOException e) {
    System.err.println("Config not found, using defaults");
    config = Configs.getManager().getLoader("json").orElseThrow().createEmpty();
    // Set defaults
    config.set("port", 8080);
} catch (ConfigException e) {
    System.err.println("Invalid config, using defaults");
    config = Configs.getManager().getLoader("json").orElseThrow().createEmpty();
}
```

### 5. Save Configurations Safely

```java
try {
    // Save to temporary file first
    Path tempFile = Path.of("config.json.tmp");
    Configs.getManager().save(config, tempFile);
    
    // Move to final location
    Files.move(tempFile, Path.of("config.json"), 
               StandardCopyOption.REPLACE_EXISTING);
    
} catch (IOException | ConfigException e) {
    System.err.println("Failed to save config: " + e.getMessage());
}
```

## Implementation Status (v26.0-Alpha.6)

### Completed
- ✅ Config interface with type-safe getters
- ✅ ConfigLoader interface for format support
- ✅ ConfigManager interface for centralized management
- ✅ ConfigException for error handling
- ✅ Configs static API for global access
- ✅ Comprehensive test suite (141 tests)
- ✅ Documentation

### Not Yet Implemented (Alpha.6)
- ⏳ Concrete Config implementation
- ⏳ JSON ConfigLoader
- ⏳ YAML ConfigLoader
- ⏳ TOML ConfigLoader
- ⏳ Properties ConfigLoader
- ⏳ Concrete ConfigManager implementation
- ⏳ Configuration caching
- ⏳ Auto-save on modification
- ⏳ Configuration validation
- ⏳ Configuration migration

The API is complete and tested. Implementation will be added in aprismate-agent in future releases.

## Design Principles

1. **Format Agnostic** - Support any text-based configuration format
2. **Type Safety** - Strong typing with Optional for missing values
3. **Hierarchical** - Support nested structures with dot notation
4. **Flexible** - Pluggable loaders for custom formats
5. **Caching** - Automatic caching of frequently accessed configs
6. **Thread-Safe** - Safe for concurrent reads

## Testing

Configuration components should be tested for:
- Type conversion and default values
- Nested section navigation
- Key existence and iteration
- Setting and removing values
- Error handling for invalid types
- Null safety

See test classes in `jdk.aprismate.config` package for examples.

## Future Enhancements (Post-v26.0)

- Configuration schema validation
- Configuration migration between versions
- Environment variable interpolation
- Configuration file watching (hot-reload)
- Encrypted configuration values
- Configuration templating
- Configuration merging and overrides
- Configuration profiling (dev/prod profiles)
- Configuration documentation generation
- GUI configuration editor
