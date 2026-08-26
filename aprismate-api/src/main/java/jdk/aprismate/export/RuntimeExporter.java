package jdk.aprismate.export;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * Structured runtime state export for AI coding agents.
 *
 * <p>Produces machine-readable JSON describing the JVM's current state:
 * identity, memory, threads, classes, GC, buffer pools, system
 * properties, and (on AprismJDK) the capability descriptor.
 *
 * <p>Every section is independently fail-safe: a failure in one section
 * produces an {@code "error": "..."} entry without affecting others.
 *
 * <p>Usage:
 * <pre>
 *   String full    = RuntimeExporter.full();     // everything
 *   String compact = RuntimeExporter.summary();  // &lt;10 KB, key metrics only
 *   String custom  = RuntimeExporter.builder()
 *                        .includeMemory()
 *                        .includeThreads(5)      // top 5 by CPU
 *                        .build()
 *                        .export();
 * </pre>
 */
public final class RuntimeExporter {

    private final boolean memory;
    private final boolean threads;
    private final int maxThreadDetail;
    private final boolean classes;
    private final boolean gc;
    private final boolean buffers;
    private final boolean properties;
    private final boolean capabilities;
    private final boolean pretty;

    private RuntimeExporter(Builder b) {
        this.memory = b.memory;
        this.threads = b.threads;
        this.maxThreadDetail = b.maxThreadDetail;
        this.classes = b.classes;
        this.gc = b.gc;
        this.buffers = b.buffers;
        this.properties = b.properties;
        this.capabilities = b.capabilities;
        this.pretty = b.pretty;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Full export: all sections, top 10 threads, no pretty-print. */
    public static String full() {
        return builder()
                .allSections()
                .maxThreads(10)
                .build()
                .export();
    }

    /** Compact summary: identity + memory + GC only. Target < 2KB. */
    public static String summary() {
        return builder()
                .includeIdentity()
                .includeMemory()
                .threads(false)
                .includeGc()
                .build()
                .export();
    }

    public String export() {
        var json = new Json(pretty);
        json.startObject();

        writeIdentity(json);
        if (memory) {
            writeMemory(json);
        }
        if (threads) {
            writeThreads(json);
        }
        if (classes) {
            writeClasses(json);
        }
        if (gc) {
            writeGc(json);
        }
        if (buffers) {
            writeBuffers(json);
        }
        if (properties) {
            writeProperties(json);
        }
        if (capabilities) {
            writeCapabilities(json);
        }

        json.endObject();
        return json.toString();
    }

    // ---------- section writers ----------

    private void writeIdentity(Json json) {
        try {
            RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
            json.key("jvm").startObject();
            json.key("version").value(System.getProperty("java.version", "unknown"));
            json.key("vendor").value(System.getProperty("java.vendor", "unknown"));
            json.key("vm_name").value(System.getProperty("java.vm.name", "unknown"));
            json.key("pid").value(rt.getPid());
            json.key("uptime_ms").value(rt.getUptime());
            json.key("spec_version").value(System.getProperty("java.specification.version", ""));
            var aprismVer = getAprismVersion();
            if (aprismVer != null) {
                json.key("aprismjdk_version").value(aprismVer);
            }
            json.endObject();
        } catch (Throwable t) {
            sectionError(json, "jvm", t);
        }
    }

    @SuppressWarnings("deprecation")
    private void writeMemory(Json json) {
        try {
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            var heap = mem.getHeapMemoryUsage();
            var nonHeap = mem.getNonHeapMemoryUsage();
            json.key("memory").startObject();
            json.key("heap").startObject();
            json.key("used_mb").value(heap.getUsed() / 1048576.0);
            json.key("committed_mb").value(heap.getCommitted() / 1048576.0);
            json.key("max_mb").value(heap.getMax() < 0 ? -1 : heap.getMax() / 1048576.0);
            json.key("usage_pct").value(
                    heap.getMax() > 0 ? heap.getUsed() * 100.0 / heap.getMax() : -1);
            json.endObject();
            json.key("non_heap").startObject();
            json.key("used_mb").value(nonHeap.getUsed() / 1048576.0);
            json.key("committed_mb").value(nonHeap.getCommitted() / 1048576.0);
            json.endObject();
            json.key("pending_finalization").value(mem.getObjectPendingFinalizationCount()); // deprecated but still functional
            json.endObject();
        } catch (Throwable t) {
            sectionError(json, "memory", t);
        }
    }

    private void writeThreads(Json json) {
        try {
            ThreadMXBean th = ManagementFactory.getThreadMXBean();
            json.key("threads").startObject();
            json.key("live").value(th.getThreadCount());
            json.key("daemon").value(th.getDaemonThreadCount());
            json.key("peak").value(th.getPeakThreadCount());
            json.key("total_started").value(th.getTotalStartedThreadCount());
            json.key("deadlocked").value(th.findDeadlockedThreads() != null);

            if (maxThreadDetail > 0) {
                long[] ids = th.getAllThreadIds();
                record ThreadCpu(long id, String name, long cpuNanos) {}
                var sorted = java.util.Arrays.stream(ids)
                        .mapToObj(id -> {
                            try {
                                var info = th.getThreadInfo(id);
                                String name = info != null ? info.getThreadName() : "dead";
                                return new ThreadCpu(id, name,
                                        th.getThreadCpuTime(id));
                            } catch (Throwable e) {
                                return new ThreadCpu(id, "?", -1);
                            }
                        })
                        .sorted((a, b) -> Long.compare(b.cpuNanos(), a.cpuNanos()))
                        .limit(maxThreadDetail)
                        .toList();

                json.key("top_by_cpu").startArray();
                for (var tc : sorted) {
                    json.startObject();
                    json.key("id").value(tc.id());
                    json.key("name").value(tc.name());
                    json.key("cpu_ms").value(tc.cpuNanos() / 1_000_000.0);
                    json.endObject();
                }
                json.endArray();
            }
            json.endObject();
        } catch (Throwable t) {
            sectionError(json, "threads", t);
        }
    }

    private void writeClasses(Json json) {
        try {
            var cl = ManagementFactory.getClassLoadingMXBean();
            json.key("classes").startObject();
            json.key("loaded").value(cl.getLoadedClassCount());
            json.key("total_loaded").value(cl.getTotalLoadedClassCount());
            json.key("unloaded").value(cl.getUnloadedClassCount());
            json.endObject();
        } catch (Throwable t) {
            sectionError(json, "classes", t);
        }
    }

    private void writeGc(Json json) {
        try {
            List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
            json.key("gc").startArray();
            for (var gcBean : gcs) {
                json.startObject();
                json.key("name").value(gcBean.getName());
                json.key("collections").value(gcBean.getCollectionCount());
                json.key("time_ms").value(gcBean.getCollectionTime());
                json.endObject();
            }
            json.endArray();
        } catch (Throwable t) {
            sectionError(json, "gc", t);
        }
    }

    private void writeBuffers(Json json) {
        try {
            List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
            json.key("buffer_pools").startArray();
            for (var pool : pools) {
                json.startObject();
                json.key("name").value(pool.getName());
                json.key("count").value(pool.getCount());
                json.key("used_mb").value(pool.getMemoryUsed() / 1048576.0);
                json.key("capacity_mb").value(pool.getTotalCapacity() / 1048576.0);
                json.endObject();
            }
            json.endArray();
        } catch (Throwable t) {
            sectionError(json, "buffer_pools", t);
        }
    }

    private static final java.util.Set<String> SENSITIVE_PROPS = java.util.Set.of(
            "java.class.path", "java.library.path", "sun.boot.library.path",
            "java.ext.dirs", "java.endorsed.dirs"
    );

    private void writeProperties(Json json) {
        try {
            var props = System.getProperties();
            json.key("properties").startObject();
            for (var name : props.stringPropertyNames()) {
                if (SENSITIVE_PROPS.contains(name)) {
                    continue;
                }
                String val = props.getProperty(name, "");
                // Truncate long values to keep output bounded
                if (val.length() > 256) {
                    val = val.substring(0, 253) + "...";
                }
                json.key(name).value(val);
            }
            json.endObject();
        } catch (Throwable t) {
            sectionError(json, "properties", t);
        }
    }

    private void writeCapabilities(Json json) {
        try {
            Class<?> vmInfo = Class.forName("jdk.aprismate.VmInfo");
            boolean isAjr = (Boolean) vmInfo.getMethod("isAprismJdk").invoke(null);
            json.key("aprismate").startObject();
            json.key("is_aprism_jdk").value(isAjr);
            if (isAjr) {
                json.key("version").value((String) vmInfo.getMethod("getAprismJdkVersion").invoke(null));
            }
            json.endObject();
        } catch (ClassNotFoundException e) {
            // Not on AprismJDK — omit entirely
        } catch (Throwable t) {
            sectionError(json, "aprismate", t);
        }
    }

    // ---------- helpers ----------

    private static void sectionError(Json json, String section, Throwable t) {
        json.key(section).startObject();
        json.key("error").value(t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
        json.endObject();
    }

    private static String getAprismVersion() {
        try {
            return System.getProperty("aprismjdk.version");
        } catch (Throwable t) {
            return null;
        }
    }

    // ---------- Builder ----------

    public static final class Builder {
        boolean identity = true;
        boolean memory = false;
        boolean threads = false;
        int maxThreadDetail = 0;
        boolean classes = false;
        boolean gc = false;
        boolean buffers = false;
        boolean properties = false;
        boolean capabilities = false;
        boolean pretty = false;

        public Builder includeIdentity() { this.identity = true; return this; }
        public Builder includeMemory() { this.memory = true; return this; }
        public Builder includeClasses() { this.classes = true; return this; }
        public Builder classes(boolean on) { this.classes = on; return this; }
        public Builder includeGc() { this.gc = true; return this; }
        public Builder includeBuffers() { this.buffers = true; return this; }
        public Builder includeProperties() { this.properties = true; return this; }
        public Builder includeCapabilities() { this.capabilities = true; return this; }
        public Builder threads(boolean on) { this.threads = on; return this; }
        public Builder maxThreads(int n) { this.threads = true; this.maxThreadDetail = n; return this; }
        public Builder prettyPrint(boolean on) { this.pretty = on; return this; }

        public Builder allSections() {
            identity = true; memory = true; threads = true; maxThreadDetail = 10;
            classes = true; gc = true; buffers = true; properties = true; capabilities = true;
            return this;
        }

        public RuntimeExporter build() {
            return new RuntimeExporter(this);
        }
    }
}
