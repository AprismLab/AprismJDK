package jdk.aprismate.tuning;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Memory footprint analyzer and optimization advisor.
 *
 * <p>Reports current memory breakdown (heap, metaspace, code cache,
 * direct buffers) and recommends optimization flags based on observed
 * usage patterns.
 *
 * <p>Advisory only — actual optimization requires JVM restart with the
 * recommended flags.
 */
public final class MemoryAdvisor {

    private MemoryAdvisor() {
    }

    /**
     * Analyzes current memory usage and returns structured findings.
     */
    public static Map<String, Object> analyze() {
        var results = new LinkedHashMap<String, Object>();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();

        // Heap summary
        var heap = mem.getHeapMemoryUsage();
        results.put("heap_used_mb", heap.getUsed() / 1048576.0);
        results.put("heap_committed_mb", heap.getCommitted() / 1048576.0);
        results.put("heap_max_mb", heap.getMax() < 0 ? "unbounded" : heap.getMax() / 1048576.0);
        results.put("heap_usage_pct", heap.getMax() > 0
                ? Math.round(heap.getUsed() * 1000.0 / heap.getMax()) / 10.0 : -1);

        // Non-heap summary
        var nonHeap = mem.getNonHeapMemoryUsage();
        results.put("non_heap_used_mb", nonHeap.getUsed() / 1048576.0);
        results.put("non_heap_committed_mb", nonHeap.getCommitted() / 1048576.0);

        // Memory pool breakdown
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        var poolDetail = new LinkedHashMap<String, Object>();
        for (var pool : pools) {
            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                var usage = pool.getUsage();
                if (usage != null && usage.getMax() > 0) {
                    var detail = new LinkedHashMap<String, Object>();
                    detail.put("used_mb", usage.getUsed() / 1048576.0);
                    detail.put("max_mb", usage.getMax() / 1048576.0);
                    detail.put("pct", Math.round(usage.getUsed() * 1000.0 / usage.getMax()) / 10.0);
                    poolDetail.put(pool.getName(), detail);
                }
            }
        }
        results.put("pools", poolDetail);

        return results;
    }

    /**
     * Recommends optimization flags based on current memory patterns.
     * Returns empty list if no optimization is recommended.
     */
    public static List<String> recommend() {
        var recs = new java.util.ArrayList<String>();
        var analysis = analyze();

        // High heap usage -> recommend larger heap or G1 tuning
        double heapPct = (double) analysis.getOrDefault("heap_usage_pct", -1.0);
        if (heapPct > 80) {
            recs.add("-Xmx" + Math.max(512, (int) ((double) analysis.get("heap_committed_mb") * 1.5)) + "m");
            recs.add("-XX:+UseG1GC");
        }

        // High non-heap -> metaspace or code cache tuning
        double nonHeapMb = (double) analysis.getOrDefault("non_heap_used_mb", 0.0);
        if (nonHeapMb > 128) {
            recs.add("-XX:MaxMetaspaceSize=" + (int) (nonHeapMb * 1.5) + "m");
        }

        // Class data sharing
        recs.add("-XX:+AutoCreateSharedArchive");

        // String dedup for memory-constrained environments
        recs.add("-XX:+UseStringDeduplication");

        return recs;
    }

    /**
     * Formats a human-readable memory report.
     */
    public static String report() {
        var a = analyze();
        var sb = new StringBuilder();
        sb.append("=== Memory Analysis ===\n");
        sb.append(String.format("Heap: %.1f MB used", a.get("heap_used_mb")));
        if (a.get("heap_max_mb") instanceof Double max && (Double) max > 0) {
            sb.append(String.format(" / %.0f MB max (%.1f%%)", max, a.get("heap_usage_pct")));
        }
        sb.append('\n');
        sb.append(String.format("Non-heap: %.1f MB\n", a.get("non_heap_used_mb")));
        if (a.get("pools") instanceof Map<?, ?> pools && !pools.isEmpty()) {
            sb.append("Pools:\n");
            for (var e : pools.entrySet()) {
                if (e.getValue() instanceof Map<?, ?> detail) {
                    sb.append(String.format("  %s: %.1f MB (%.1f%%)\n",
                            e.getKey(), detail.get("used_mb"), detail.get("pct")));
                }
            }
        }
        var recs = recommend();
        if (!recs.isEmpty()) {
            sb.append("Recommendations:\n");
            for (var r : recs) {
                sb.append("  ").append(r).append('\n');
            }
        }
        return sb.toString();
    }
}
