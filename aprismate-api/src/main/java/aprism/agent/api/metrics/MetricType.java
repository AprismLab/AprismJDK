package aprism.agent.api.metrics;

/**
 * Types of metrics that can be collected by the agent.
 * 
 * @since v26.1-Alpha.3
 */
public enum MetricType {
    /**
     * Counter metric - monotonically increasing value.
     */
    COUNTER,
    
    /**
     * Gauge metric - instantaneous value that can go up or down.
     */
    GAUGE,
    
    /**
     * Histogram metric - distribution of values.
     */
    HISTOGRAM,
    
    /**
     * Timer metric - measures duration of operations.
     */
    TIMER,
    
    /**
     * Summary metric - similar to histogram but calculates quantiles.
     */
    SUMMARY
}
