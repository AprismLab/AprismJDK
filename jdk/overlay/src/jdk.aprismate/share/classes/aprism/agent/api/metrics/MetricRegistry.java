package aprism.agent.api.metrics;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Registry for managing metrics collected by the agent.
 * <p>
 * The registry provides methods to register, record, and query metrics.
 * Metrics are organized by name and can be filtered by tags.
 * 
 * @since v26.1-Alpha.3
 */
public interface MetricRegistry {
    /**
     * Records a counter metric.
     * <p>
     * Counters are monotonically increasing values.
     *
     * @param name the metric name
     * @param value the counter value to add
     * @throws NullPointerException if name is null
     */
    void counter(String name, double value);
    
    /**
     * Records a counter metric with tags.
     *
     * @param name the metric name
     * @param value the counter value to add
     * @param tags the tags as key-value pairs
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    void counter(String name, double value, String... tags);
    
    /**
     * Records a gauge metric.
     * <p>
     * Gauges represent instantaneous values that can go up or down.
     *
     * @param name the metric name
     * @param value the gauge value
     * @throws NullPointerException if name is null
     */
    void gauge(String name, double value);
    
    /**
     * Records a gauge metric with tags.
     *
     * @param name the metric name
     * @param value the gauge value
     * @param tags the tags as key-value pairs
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    void gauge(String name, double value, String... tags);
    
    /**
     * Registers a gauge with a supplier.
     * <p>
     * The supplier will be called each time the gauge value is queried.
     *
     * @param name the metric name
     * @param valueSupplier the supplier providing the gauge value
     * @throws NullPointerException if name or valueSupplier is null
     */
    void gauge(String name, Supplier<Double> valueSupplier);
    
    /**
     * Records a histogram value.
     * <p>
     * Histograms track the distribution of values.
     *
     * @param name the metric name
     * @param value the value to record
     * @throws NullPointerException if name is null
     */
    void histogram(String name, double value);
    
    /**
     * Records a histogram value with tags.
     *
     * @param name the metric name
     * @param value the value to record
     * @param tags the tags as key-value pairs
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    void histogram(String name, double value, String... tags);
    
    /**
     * Starts a timer for measuring operation duration.
     * <p>
     * Example usage:
     * <pre>{@code
     * Timer.Sample sample = registry.timer();
     * try {
     *     // timed operation
     * } finally {
     *     sample.stop("operation.duration");
     * }
     * }</pre>
     *
     * @return a timer sample
     */
    Timer.Sample timer();
    
    /**
     * Records a timer value in nanoseconds.
     *
     * @param name the metric name
     * @param nanos the duration in nanoseconds
     * @throws NullPointerException if name is null
     */
    void timer(String name, long nanos);
    
    /**
     * Records a timer value with tags.
     *
     * @param name the metric name
     * @param nanos the duration in nanoseconds
     * @param tags the tags as key-value pairs
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if tags length is odd
     */
    void timer(String name, long nanos, String... tags);
    
    /**
     * Records a custom metric.
     *
     * @param metric the metric to record
     * @throws NullPointerException if metric is null
     */
    void record(Metric metric);
    
    /**
     * Gets a metric by name.
     *
     * @param name the metric name
     * @return the metric, or empty if not found
     * @throws NullPointerException if name is null
     */
    Optional<Metric> getMetric(String name);
    
    /**
     * Gets all metrics.
     *
     * @return an unmodifiable collection of all metrics
     */
    Collection<Metric> getMetrics();
    
    /**
     * Gets metrics with the specified tag.
     *
     * @param tagKey the tag key
     * @param tagValue the tag value
     * @return an unmodifiable collection of matching metrics
     * @throws NullPointerException if tagKey or tagValue is null
     */
    Collection<Metric> getMetricsByTag(String tagKey, String tagValue);
    
    /**
     * Clears all metrics.
     */
    void clear();
    
    /**
     * Removes a metric by name.
     *
     * @param name the metric name
     * @return true if the metric was removed, false if it didn't exist
     * @throws NullPointerException if name is null
     */
    boolean remove(String name);
}
