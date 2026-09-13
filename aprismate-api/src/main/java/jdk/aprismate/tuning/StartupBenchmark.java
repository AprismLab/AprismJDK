package jdk.aprismate.tuning;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standardized startup benchmark suite. Measures JVM startup phases
 * and outputs structured results suitable for CI regression tracking.
 *
 * <p>Run this class as the main class: it measures its own startup
 * (class loading, MXBean init, collection init) and prints results
 * as CSV lines for easy parsing.
 *
 * <p>Usage: {@code java -cp ... jdk.aprismate.tuning.StartupBenchmark}
 */
public final class StartupBenchmark {

    public static void main(String[] args) {
        var results = new LinkedHashMap<String, Long>();

        // Phase 1: class loading (implicit — measure up to first call)
        long phase1End = System.nanoTime();

        // Phase 2: MXBean initialization (forces management module init)
        var rt = ManagementFactory.getRuntimeMXBean();
        var mem = ManagementFactory.getMemoryMXBean();
        var th = ManagementFactory.getThreadMXBean();
        var cl = ManagementFactory.getClassLoadingMXBean();
        long phase2End = System.nanoTime();

        // Phase 3: collection initialization
        var list = new java.util.ArrayList<String>(500);
        for (int i = 0; i < 500; i++) list.add("item" + i);
        var map = new java.util.HashMap<String, Integer>(200);
        for (int i = 0; i < 200; i++) map.put("key" + i, i);
        long phase3End = System.nanoTime();

        long jvmStart = rt.getStartTime();

        results.put("jvm_to_phase1_ms", (phase1End - jvmStart) / 1_000_000);
        results.put("phase1_to_mxbean_ms", (phase2End - phase1End) / 1_000_000);
        results.put("mxbean_to_collections_ms", (phase3End - phase2End) / 1_000_000);
        results.put("total_uptime_ms", rt.getUptime());
        results.put("loaded_classes", (long) cl.getLoadedClassCount());

        // Output as CSV for CI parsing
        System.out.println("metric,value");
        for (var e : results.entrySet()) {
            System.out.println(e.getKey() + "," + e.getValue());
        }
        System.out.println("runtime," + System.getProperty("java.vendor"));
        System.out.println("version," + System.getProperty("java.version"));
    }

    private StartupBenchmark() {
    }
}
