package aprism.agent.api.metrics;

import java.util.Collection;
import java.util.Optional;
import java.util.function.DoubleSupplier;

/**
 * Entry point for accessing the agent's metric registry.
 * <p>
 * This class provides static access to the metric registry for recording
 * performance metrics and monitoring data.
 * <p>
 * The registry must be initialized by the agent before use.
 * 
 * @since v26.1-Alpha.3
 */
public final class AgentMetrics {
    private static volatile MetricRegistry registry;
    private static volatile boolean initialized = false;
    
    private AgentMetrics() {
        throw new AssertionError("Cannot instantiate AgentMetrics");
    }
    
    /**
     * Initializes the metrics system with the provided registry.
     * <p>
     * This method can be called multiple times to replace the registry.
     *
     * @param registry the metric registry
     * @throws NullPointerException if registry is null
     */
    public static synchronized void setRegistry(MetricRegistry registry) {
        if (registry == null) {
            throw new NullPointerException("registry cannot be null");
        }
        AgentMetrics.registry = registry;
        initialized = true;
    }
    
    /**
     * Gets the metric registry.
     *
     * @return the metric registry
     * @throws IllegalStateException if not initialized
     */
    public static MetricRegistry getRegistry() {
        if (!initialized) {
            throw new IllegalStateException("AgentMetrics not initialized");
        }
        return registry;
    }
    
    /**
     * Checks if the metrics system is initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Clears the initialization state (for testing purposes).
     * <p>
     * This method is intended for testing only and should not be called
     * in production code.
     */
    static synchronized void reset() {
        if (registry != null) {
            registry.clear();
        }
        registry = null;
        initialized = false;
    }
    
    /**
     * Clears all metrics from the registry.
     *
     * @throws IllegalStateException if not initialized
     */
    public static void clear() {
        getRegistry().clear();
    }
    
    // Convenience methods that delegate to the registry
    
    /**
     * Records a counter metric.
     *
     * @param name the metric name
     * @param value the counter value to add
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     */
    public static void counter(String name, double value) {
        getRegistry().counter(name, value);
    }
    
    /**
     * Records a counter metric with tags.
     *
     * @param name the metric name
     * @param value the counter value to add
     * @param tags the tags as key-value pairs
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    public static void counter(String name, double value, String... tags) {
        getRegistry().counter(name, value, tags);
    }
    
    /**
     * Records a gauge metric.
     *
     * @param name the metric name
     * @param value the gauge value
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     */
    public static void gauge(String name, double value) {
        getRegistry().gauge(name, value);
    }
    
    /**
     * Records a gauge metric with a supplier.
     *
     * @param name the metric name
     * @param supplier the value supplier
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name or supplier is null
     */
    public static void gauge(String name, DoubleSupplier supplier) {
        getRegistry().gauge(name, () -> supplier.getAsDouble());
    }
    
    /**
     * Records a gauge metric with tags.
     *
     * @param name the metric name
     * @param value the gauge value
     * @param tags the tags as key-value pairs
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    public static void gauge(String name, double value, String... tags) {
        getRegistry().gauge(name, value, tags);
    }
    
    /**
     * Records a histogram value.
     *
     * @param name the metric name
     * @param value the value to record
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     */
    public static void histogram(String name, double value) {
        getRegistry().histogram(name, value);
    }
    
    /**
     * Records a histogram value with tags.
     *
     * @param name the metric name
     * @param value the value to record
     * @param tags the tags as key-value pairs
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    public static void histogram(String name, double value, String... tags) {
        getRegistry().histogram(name, value, tags);
    }
    
    /**
     * Starts a timer for measuring operation duration.
     *
     * @return a timer sample
     * @throws IllegalStateException if not initialized
     */
    public static Timer.Sample timer() {
        return getRegistry().timer();
    }
    
    /**
     * Records a timer value in nanoseconds.
     *
     * @param name the metric name
     * @param nanos the duration in nanoseconds
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     */
    public static void timer(String name, long nanos) {
        getRegistry().timer(name, nanos);
    }
    
    /**
     * Records a timer value with tags.
     *
     * @param name the metric name
     * @param nanos the duration in nanoseconds
     * @param tags the tags as key-value pairs
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    public static void timer(String name, long nanos, String... tags) {
        getRegistry().timer(name, nanos, tags);
    }
    
    /**
     * Records a custom metric.
     *
     * @param metric the metric to record
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if metric is null
     */
    public static void record(Metric metric) {
        getRegistry().record(metric);
    }
    
    /**
     * Gets a metric by name.
     *
     * @param name the metric name
     * @return the metric if found, empty otherwise
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     */
    public static Optional<Metric> getMetric(String name) {
        return getRegistry().getMetric(name);
    }
    
    /**
     * Gets all metrics.
     *
     * @return all metrics
     * @throws IllegalStateException if not initialized
     */
    public static Collection<Metric> getMetrics() {
        return getRegistry().getMetrics();
    }
    
    /**
     * Gets metrics by tag.
     *
     * @param key the tag key
     * @param value the tag value
     * @return metrics with the specified tag
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if key or value is null
     */
    public static Collection<Metric> getMetricsByTag(String key, String value) {
        return getRegistry().getMetricsByTag(key, value);
    }
    
    /**
     * Removes a metric by name.
     *
     * @param name the metric name
     * @return true if removed, false if not found
     * @throws IllegalStateException if not initialized
     * @throws NullPointerException if name is null
     */
    public static boolean remove(String name) {
        return getRegistry().remove(name);
    }
}
