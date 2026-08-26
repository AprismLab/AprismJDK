package jdk.aprismate.tuning;

import java.util.List;

/**
 * Named GC tuning presets. Each profile maps to a list of JVM options
 * suitable for a specific workload type.
 *
 * <p>These are ADVISORY: use them when launching the JVM, e.g.
 * {@code java -XX:+UseG1GC -Xms4g ...}. They cannot be changed after
 * JVM startup.
 */
public enum GcProfile {

    /**
     * Throughput-oriented: G1 GC, large heap, parallel threads.
     * For batch processing, build servers, CI workers.
     */
    SERVER("Throughput-first: G1 GC, large heap, parallel",
            List.of("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200",
                    "-XX:+ParallelRefProcEnabled")),

    /**
     * Latency-oriented: ZGC, sub-millisecond pauses.
     * For REST APIs, game servers, trading systems.
     */
    DESKTOP("Latency-first: ZGC sub-ms pauses",
            List.of("-XX:+UseZGC", "-XX:+ZGenerational")),

    /**
     * Container-aware: respects cgroup memory limits.
     * For Docker/K8s deployments.
     */
    CONTAINER("Container-aware: cgroup-respecting defaults",
            List.of("-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0",
                    "-XX:+ExitOnOutOfMemoryError")),

    /**
     * Low-footprint: minimal heap, serial GC where possible.
     * For serverless / FaaS / embedded.
     */
    COMPACT("Low-footprint: minimal heap, fast startup",
            List.of("-Xmx256m", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1"));

    private final String description;
    private final List<String> options;

    GcProfile(String description, List<String> options) {
        this.description = description;
        this.options = List.copyOf(options);
    }

    public String description() {
        return description;
    }

    /**
     * Returns the recommended JVM options for this profile.
     */
    public List<String> options() {
        return options;
    }

    /**
     * Returns the full launch command fragment including these options.
     */
    public String asLaunchArgs() {
        return String.join(" ", options);
    }
}
