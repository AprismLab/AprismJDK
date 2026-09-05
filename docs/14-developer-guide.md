# AprismJDK Developer Guide

> v26.4 line. English canonical; Chinese mirror pending.

## Quick Start

### Using the full fork image

```bash
# Unzip and run
unzip AprismJDK-26.3-windows-x64-jdk.zip
export JAVA_HOME=AprismJDK-26.3-windows-x64-jdk
$JAVA_HOME/bin/java -version

# Auto-load agent
$JAVA_HOME/bin/java -XX:+AprismateAgent -jar myapp.jar

# Or with diagnostic server
$JAVA_HOME/bin/java -XX:+AprismateAgent -Daprismate.diag.port=25590 -jar myapp.jar
curl http://127.0.0.1:25590/aprism/state
```

### Using the agent on stock JDKs

```bash
java -javaagent:aprismate-26.3.jar -jar myapp.jar

# With bytecode pre-optimization
java -javaagent:aprismate-26.3.jar \
     -Daprismate.optimizer.rules=rules.properties \
     -jar myapp.jar
```

### Building from source

```bash
# Prerequisites: JDK 25 (boot JDK), MSYS2 (Windows) or build-essential (Linux)
git clone https://github.com/AprismLab/AprismJDK.git
cd AprismJDK

# JVM module build + tests
./gradlew build

# OpenJDK fork build (Windows: scripts/configure-fork.sh)
cd openjdk-25
bash ../scripts/configure-fork.sh
make images JOBS=6 LOG=info
```

## API Guide

### Reflection Elimination (`jdk.aprismate.invoke`)

Replace `Method.invoke()` hot paths with cached MethodHandle invokers:

```java
import jdk.aprismate.invoke.FastReflection;

// Acquire once (cached, thread-safe)
Method greet = MyService.class.getMethod("greet", String.class);
DirectInvoker inv = FastReflection.invoker(greet);

// Invoke in hot path (~1.3x direct call, vs 10-30x for raw reflection)
Object result = inv.invoke(serviceInstance, new Object[]{"world"});

// Exceptions propagate UNWRAPPED (no InvocationTargetException)
```

### Runtime State Export (`jdk.aprismate.export`)

Machine-readable JVM state for AI agents and monitoring:

```java
import jdk.aprismate.export.RuntimeExporter;

// Full JSON snapshot (identity, memory, threads, GC, classes, properties)
String state = RuntimeExporter.full();

// Compact summary (<2KB)
String brief = RuntimeExporter.summary();

// Custom selection
String custom = RuntimeExporter.builder()
    .includeMemory()
    .includeGc()
    .maxThreads(5)
    .prettyPrint(true)
    .build()
    .export();

// V2: history + diff + alerts
import jdk.aprismate.export.RuntimeExporterV2;
var diff = RuntimeExporterV2.takeSnapshot();
// diff.toString() -> {"heap_delta_pct":2.5,"thread_delta":-1,...}
RuntimeExporterV2.setAlertListener(alert ->
    System.err.println("ALERT: " + alert.type() + " " + alert.detail()));
```

### Safe Experimentation (`aprism.agent.experiment`)

Propose bytecode changes and roll back — no restarts:

```java
import aprism.agent.experiment.SafeExperiment;

var result = SafeExperiment.tryReplace(
    com.example.Service.class, optimizedBytes);

if (result.isSuccess()) {
    // Test the change; roll back if wrong
    SafeExperiment.rollback("com.example.Service");
}
// On failure: JVM rejected the change; class untouched
```

### Hot Reload (`aprism.agent.reload`)

Atomic multi-class replacement with safe-evolution validation:

```java
import aprism.agent.reload.ChangeSet;
import aprism.agent.reload.HotReloader;

var cs = new ChangeSet("fix-1", Map.of(
    "com.example.Service", newServiceBytes,
    "com.example.Helper", newHelperBytes
));
var result = HotReloader.apply(cs);
// Atomic: all classes swap or none. Validation: superclass/interfaces
// unchanged, no public/protected method removal, no field removal.
```

### Bytecode Pre-Optimization (`aprism.agent.optimize`)

Elide debug logging at class-load time:

```properties
# rules.properties
elide = com.example.Debug log trace
probe-enter = com.example.Hot compute
```

```bash
java -javaagent:aprismate.jar \
     -Daprismate.optimizer.rules=rules.properties \
     -jar myapp.jar
```

### GC Tuning Presets (`jdk.aprismate.tuning`)

```java
import jdk.aprismate.tuning.GcPresets;
String flags = GcPresets.forName("server").orElseThrow().asLaunchArgs();
// -> "-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled"
```

Profiles: `server` (throughput), `desktop` (ZGC latency),
`container` (cgroup-aware), `compact` (serverless/embedded).

### Startup Profiler (`aprism.agent.startup`)

```bash
java -javaagent:aprismate.jar \
     -Daprismate.startup.profile \
     -jar myapp.jar
# After startup, call StartupProfiler.stop() to get the report
```

### Diagnostic HTTP Server (`aprism.agent.diag`)

```bash
java -javaagent:aprismate.jar \
     -Daprismate.diag.port=25590 \
     -jar myapp.jar

curl http://127.0.0.1:25590/aprism/state      # full JSON
curl http://127.0.0.1:25590/aprism/summary    # <2KB summary
curl http://127.0.0.1:25590/aprism/experiments # active experiments
curl http://127.0.0.1:25590/aprism/health     # liveness
```

### SBOM Generation (`jdk.aprismate.secure`)

```java
import jdk.aprismate.secure.SbomGenerator;
var artifacts = Map.of(
    "aprismate.jar", Path.of("aprismate-agent/build/libs/aprismate.jar")
);
String sbom = SbomGenerator.generate(artifacts, "v26.4");
// CycloneDX 1.5 JSON with SHA-256 hashes
```

## Agent Configuration Reference

| Property | Effect | Default |
|---|---|---|
| `-Daprismate.diag.port=<n>` | Start HTTP diagnostic server on port n | disabled |
| `-Daprismate.optimizer.rules=<file>` | Enable bytecode pre-optimizer with rules file | disabled |
| `-Daprismate.startup.profile` | Enable startup class-load profiler | disabled |
| `-Daprismate.gc.profile=<name>` | Log GC tuning advice for named profile | disabled |

## Migrating from stock OpenJDK

1. Replace `JAVA_HOME` with the AprismJDK image — all standard APIs work unchanged
2. Add `-XX:+AprismateAgent` to enable deep capabilities
3. Query `VmInfo.isAprismJdk()` to detect the runtime and upgrade behavior
4. All AprismJDK-specific APIs degrade gracefully on stock JDKs (safe defaults, no exceptions)

## Migrating from v26.2 to v26.3+

- `jdk.aprismate.invoke` replaces ad-hoc MethodHandle wiring
- `RuntimeExporter` replaces manual MXBean queries
- `SafeExperiment` replaces ad-hoc retransformClasses calls
- GC presets replace manual flag tuning
- Agent jar is now self-contained (bundles api + ASM classes)
