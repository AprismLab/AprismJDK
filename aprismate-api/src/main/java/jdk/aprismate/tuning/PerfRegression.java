package jdk.aprismate.tuning;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple performance regression detector. Compares current benchmark
 * results against a baseline, flags operations that regressed beyond
 * a configurable threshold.
 *
 * <p>Methodology: warmup + median-of-rounds (same as MicroBench).
 * A regression is flagged when the current median is more than
 * {@code threshold} times the baseline median.
 *
 * <p>This is NOT JMH-grade; it catches gross regressions (>20%) which
 * is the real-world use case for CI gating.
 */
public final class PerfRegression {

    private double threshold = 1.20; // 20% tolerance

    /** Baseline entry: name -> median nanoseconds per operation. */
    private final Map<String, Long> baseline = new LinkedHashMap<>();

    public PerfRegression() {
    }

    public PerfRegression threshold(double ratio) {
        this.threshold = ratio;
        return this;
    }

    /**
     * Records a baseline measurement.
     */
    public PerfRegression baseline(String name, long medianNanos) {
        baseline.put(name, medianNanos);
        return this;
    }

    /**
     * Measures a scenario and compares against baseline.
     * Returns a RegressionResult for each baseline entry measured.
     */
    public Map<String, RegressionResult> measure(Object... nameAndRunnable) {
        var results = new LinkedHashMap<String, RegressionResult>();
        for (int i = 0; i < nameAndRunnable.length; i += 2) {
            String name = (String) nameAndRunnable[i];
            Runnable r = (Runnable) nameAndRunnable[i + 1];
            Long base = baseline.get(name);
            if (base == null) {
                results.put(name, new RegressionResult(name, 0, 0, false, "no baseline"));
                continue;
            }

            // Warmup
            for (int w = 0; w < 3; w++) {
                for (int j = 0; j < 10_000; j++) r.run();
            }

            // Measure median of 5 rounds
            long[] times = new long[5];
            for (int round = 0; round < 5; round++) {
                long start = System.nanoTime();
                for (int j = 0; j < 100_000; j++) r.run();
                times[round] = System.nanoTime() - start;
            }
            java.util.Arrays.sort(times);
            long median = times[2];

            double ratio = (double) median / Math.max(base, 1);
            boolean regressed = ratio > threshold;
            results.put(name, new RegressionResult(
                    name, base, median, regressed,
                    String.format("%.2fx %s", ratio, regressed ? "REGRESSED" : "ok")));
        }
        return results;
    }

    /**
     * Records a benchmark result.
     */
    public record RegressionResult(
            String name, long baselineNanos, long currentNanos,
            boolean regressed, String detail) {
    }
}
