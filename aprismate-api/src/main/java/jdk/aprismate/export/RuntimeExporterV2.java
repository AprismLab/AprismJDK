package jdk.aprismate.export;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Runtime export with history tracking and threshold alerts.
 *
 * <p>Maintains a bounded ring buffer of past {@link Snapshot}s, computes
 * diffs between consecutive snapshots, and fires alert callbacks when
 * thresholds are crossed (heap usage %, deadlocked threads, etc.).
 *
 * <p>Designed for AI agents: instead of calling RuntimeExporter.full()
 * in a loop, the agent can ask {@code RuntimeExporterV2.diff()} to see
 * WHAT CHANGED since last snapshot.
 */
public final class RuntimeExporterV2 {

    private static final int DEFAULT_HISTORY_SIZE = 30;
    private static final Deque<Snapshot> HISTORY = new ConcurrentLinkedDeque<>();
    private static final AtomicLong SNAPSHOT_COUNTER = new AtomicLong();
    private static volatile AlertListener listener;
    private static volatile int historySize = DEFAULT_HISTORY_SIZE;

    // Thresholds (defaults can be overridden via system properties)
    private static volatile double heapAlertPct = 90.0;
    private static volatile boolean alertOnDeadlock = true;

    private RuntimeExporterV2() {
    }

    /**
     * Callback fired when a threshold is crossed.
     */
    @FunctionalInterface
    public interface AlertListener {
        void onAlert(Alert alert);
    }

    public record Alert(String type, String detail, long timestampNanos) { }

    /**
     * One point-in-time capture of key metrics.
     */
    public static final class Snapshot {
        final long seq;
        final long timestampNanos;
        final String json;
        final double heapUsagePct;
        final int liveThreads;
        final boolean deadlocked;

        Snapshot(long seq, long timestampNanos, String json,
                 double heapUsagePct, int liveThreads, boolean deadlocked) {
            this.seq = seq;
            this.timestampNanos = timestampNanos;
            this.json = json;
            this.heapUsagePct = heapUsagePct;
            this.liveThreads = liveThreads;
            this.deadlocked = deadlocked;
        }

        public long seq() { return seq; }
        public long timestampNanos() { return timestampNanos; }
        public String json() { return json; }
        public double heapUsagePct() { return heapUsagePct; }
        public int liveThreads() { return liveThreads; }
        public boolean deadlocked() { return deadlocked; }
    }

    /**
     * Difference between two consecutive snapshots.
     */
    public static final class Diff {
        final Snapshot previous;
        final Snapshot current;
        final double heapDeltaPct;
        final int threadDelta;

        Diff(Snapshot prev, Snapshot cur) {
            this.previous = prev;
            this.current = cur;
            this.heapDeltaPct = cur.heapUsagePct - prev.heapUsagePct;
            this.threadDelta = cur.liveThreads - prev.liveThreads;
        }

        public double heapDeltaPct() { return heapDeltaPct; }
        public int threadDelta() { return threadDelta; }
        public Snapshot current() { return current; }

        @Override
        public String toString() {
            var sb = new StringBuilder("{");
            sb.append("\"heap_delta_pct\":").append(String.format("%.2f", heapDeltaPct));
            sb.append(",\"thread_delta\":").append(threadDelta);
            sb.append(",\"heap_pct\":").append(String.format("%.2f", current.heapUsagePct));
            sb.append(",\"live_threads\":").append(current.liveThreads);
            sb.append(",\"deadlocked\":").append(current.deadlocked);
            sb.append("}");
            return sb.toString();
        }
    }

    // ---------- configuration ----------

    public static void setHistorySize(int size) {
        historySize = Math.max(2, size);
    }

    public static void setHeapAlertThreshold(double pct) {
        heapAlertPct = pct;
    }

    public static void setAlertListener(AlertListener l) {
        listener = l;
    }

    // ---------- capture ----------

    /**
     * Takes a snapshot, stores it in history, checks thresholds.
     * Returns the Diff with the previous snapshot (or null if first).
     */
    public static Diff takeSnapshot() {
        var exporter = RuntimeExporter.builder()
                .includeIdentity()
                .includeMemory()
                .threads(false)
                .includeGc()
                .includeClasses()
                .build();

        String json = exporter.export();
        long seq = SNAPSHOT_COUNTER.incrementAndGet();
        long now = System.nanoTime();

        // Extract key metrics from ManagementFactory (not from JSON parse)
        var mem = java.lang.management.ManagementFactory.getMemoryMXBean();
        var heap = mem.getHeapMemoryUsage();
        double heapPct = heap.getMax() > 0 ? heap.getUsed() * 100.0 / heap.getMax() : -1;
        var th = java.lang.management.ManagementFactory.getThreadMXBean();
        int liveThreads = th.getThreadCount();
        boolean deadlocked = th.findDeadlockedThreads() != null;

        var snapshot = new Snapshot(seq, now, json, heapPct, liveThreads, deadlocked);

        // Add to history (bounded)
        HISTORY.addLast(snapshot);
        while (HISTORY.size() > historySize) {
            HISTORY.pollFirst();
        }

        // Check thresholds
        checkAlerts(snapshot);

        // Compute diff
        Snapshot prev = null;
        if (HISTORY.size() >= 2) {
            var iter = HISTORY.descendingIterator();
            iter.next(); // skip current
            prev = iter.next();
        }
        return prev != null ? new Diff(prev, snapshot) : new Diff(emptySnapshot(seq, now), snapshot);
    }

    /**
     * Returns the last N diffs between consecutive snapshots.
     */
    public static String historyAsJson(int lastN) {
        var json = new Json();
        json.startArray();
        var snapshots = HISTORY.stream().toList();
        int start = Math.max(1, snapshots.size() - lastN);
        for (int i = start; i < snapshots.size(); i++) {
            var prev = snapshots.get(i - 1);
            var cur = snapshots.get(i);
            var diff = new Diff(prev, cur);
            json.startObject();
            json.key("seq").value(cur.seq);
            json.key("heap_delta_pct").value(diff.heapDeltaPct);
            json.key("thread_delta").value(diff.threadDelta);
            json.key("heap_pct").value(cur.heapUsagePct);
            json.key("threads").value(cur.liveThreads);
            json.key("deadlocked").value(cur.deadlocked);
            json.endObject();
        }
        json.endArray();
        return json.toString();
    }

    public static int historySize() {
        return HISTORY.size();
    }

    // ---------- alerts ----------

    private static void checkAlerts(Snapshot s) {
        var l = listener;
        if (l == null) {
            return;
        }
        if (s.heapUsagePct >= heapAlertPct && heapAlertPct > 0) {
            l.onAlert(new Alert("heap_pressure",
                    String.format("heap usage %.1f%% >= threshold %.1f%%", s.heapUsagePct, heapAlertPct),
                    s.timestampNanos));
        }
        if (alertOnDeadlock && s.deadlocked) {
            l.onAlert(new Alert("deadlock",
                    "deadlocked threads detected", s.timestampNanos));
        }
    }

    private static Snapshot emptySnapshot(long seq, long now) {
        return new Snapshot(seq, now, "{}", -1, 0, false);
    }
}
