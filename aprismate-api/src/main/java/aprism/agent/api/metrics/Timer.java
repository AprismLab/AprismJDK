package aprism.agent.api.metrics;

import java.util.Objects;

/**
 * Utility for timing operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * Timer.Sample sample = Timer.start();
 * try {
 *     // timed operation
 * } finally {
 *     long nanos = sample.stop();
 *     registry.timer("operation.duration", nanos);
 * }
 * }</pre>
 * 
 * @since v26.1-Alpha.3
 */
public final class Timer {
    private Timer() {
        throw new AssertionError("Cannot instantiate Timer");
    }
    
    /**
     * Starts a new timer sample.
     *
     * @return a new timer sample
     */
    public static Sample start() {
        return new Sample(System.nanoTime());
    }
    
    /**
     * Starts a new timer sample using the provided registry.
     * <p>
     * This is a convenience method equivalent to calling {@code registry.timer()}.
     *
     * @param registry the metric registry
     * @return a new timer sample
     * @throws NullPointerException if registry is null
     */
    public static Sample start(MetricRegistry registry) {
        Objects.requireNonNull(registry, "registry cannot be null");
        return registry.timer();
    }
    
    /**
     * A timer sample that records the elapsed time.
     */
    public static final class Sample {
        private final long startNanos;
        
        /**
         * Creates a new timer sample with the given start time.
         *
         * @param startNanos the start time in nanoseconds
         */
        public Sample(long startNanos) {
            this.startNanos = startNanos;
        }
        
        /**
         * Stops the timer and returns the elapsed time in nanoseconds.
         *
         * @return the elapsed time in nanoseconds
         */
        public long stop() {
            return System.nanoTime() - startNanos;
        }
        
        /**
         * Stops the timer and records it to the registry.
         *
         * @param registry the metric registry
         * @param name the metric name
         * @throws NullPointerException if registry or name is null
         */
        public void stop(MetricRegistry registry, String name) {
            Objects.requireNonNull(registry, "registry cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            registry.timer(name, stop());
        }
        
        /**
         * Stops the timer and records it to the registry with tags.
         *
         * @param registry the metric registry
         * @param name the metric name
         * @param tags the tags as key-value pairs
         * @throws NullPointerException if registry or name is null
         * @throws IllegalArgumentException if tags length is odd
         */
        public void stop(MetricRegistry registry, String name, String... tags) {
            Objects.requireNonNull(registry, "registry cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            registry.timer(name, stop(), tags);
        }
    }
}
