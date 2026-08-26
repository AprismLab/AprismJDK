package jdk.aprismate.invoke;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Lightweight benchmark harness (no JMH dependency). Proper warmup,
 * nanosecond timing, median-of-runs to reduce jitter. Results are
 * indicative, not TCK-grade -- but honest.
 */
public final class MicroBench {

    private MicroBench() {
    }

    /**
     * Runs one named scenario: warmup then measured rounds.
     */
    public static BenchResult run(String name, int warmupRounds,
                                  int measureRounds, int opsPerRound,
                                  Runnable scenario) {
        // Warmup: let JIT compile, populate caches, stabilize
        for (int i = 0; i < warmupRounds * opsPerRound; i++) {
            scenario.run();
        }

        long[] times = new long[measureRounds];
        for (int r = 0; r < measureRounds; r++) {
            long start = System.nanoTime();
            for (int i = 0; i < opsPerRound; i++) {
                scenario.run();
            }
            times[r] = System.nanoTime() - start;
        }
        java.util.Arrays.sort(times);
        long median = times[measureRounds / 2];
        long p99 = times[(int) (measureRounds * 0.99)];
        return new BenchResult(name, opsPerRound, median, p99);
    }

    /**
     * Compares multiple scenarios and produces relative-throughput table.
     */
    public static Map<String, BenchResult> compare(
            int warmupRounds, int measureRounds, int opsPerRound,
            Object... nameAndRunnable) {
        if (nameAndRunnable.length % 2 != 0) {
            throw new IllegalArgumentException("pairs expected");
        }
        var results = new LinkedHashMap<String, BenchResult>();
        for (int i = 0; i < nameAndRunnable.length; i += 2) {
            String name = (String) nameAndRunnable[i];
            Runnable r = (Runnable) nameAndRunnable[i + 1];
            results.put(name, run(name, warmupRounds, measureRounds, opsPerRound, r));
        }
        return results;
    }

    public record BenchResult(String name, int ops, long medianNanos, long p99Nanos) {
        /** Nanoseconds per operation (median round). */
        public double nsPerOp() {
            return (double) medianNanos / ops;
        }

        /** Relative throughput vs slowest entry (1.0x = fastest). */
        public double relativeTo(BenchResult slowest) {
            return slowest.nsPerOp() / nsPerOp();
        }

        @Override
        public String toString() {
            return String.format("%-28s %10.1f ns/op  (p99: %10.1f)", name, nsPerOp(), p99Nanos / (double) ops);
        }
    }
}
