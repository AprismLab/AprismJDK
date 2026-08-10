package aprism.agent.api.metrics;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a single metric collected by the agent.
 * <p>
 * Metrics are identified by a name and optional tags, and have a type and value.
 * 
 * @since v26.1-Alpha.3
 */
public interface Metric {
    /**
     * Gets the name of this metric.
     *
     * @return the metric name
     */
    String getName();
    
    /**
     * Gets the type of this metric.
     *
     * @return the metric type
     */
    MetricType getType();
    
    /**
     * Gets the current value of this metric.
     *
     * @return the metric value
     */
    double getValue();
    
    /**
     * Gets the tags associated with this metric.
     * <p>
     * Tags are key-value pairs used to categorize and filter metrics.
     *
     * @return an unmodifiable map of tags
     */
    Map<String, String> getTags();
    
    /**
     * Gets the timestamp when this metric was recorded, in milliseconds since epoch.
     *
     * @return the timestamp
     */
    long getTimestamp();
    
    /**
     * Gets the description of this metric.
     *
     * @return the description, or empty string if not set
     */
    String getDescription();
    
    /**
     * Builder for creating {@link Metric} instances.
     */
    interface Builder {
        /**
         * Sets the name of the metric.
         *
         * @param name the metric name
         * @return this builder
         * @throws NullPointerException if name is null
         * @throws IllegalArgumentException if name is empty
         */
        Builder name(String name);
        
        /**
         * Sets the type of the metric.
         *
         * @param type the metric type
         * @return this builder
         * @throws NullPointerException if type is null
         */
        Builder type(MetricType type);
        
        /**
         * Sets the value of the metric.
         *
         * @param value the metric value
         * @return this builder
         */
        Builder value(double value);
        
        /**
         * Adds a tag to the metric.
         *
         * @param key the tag key
         * @param value the tag value
         * @return this builder
         * @throws NullPointerException if key or value is null
         */
        Builder tag(String key, String value);
        
        /**
         * Sets the timestamp of the metric.
         * <p>
         * If not set, current time will be used.
         *
         * @param timestamp the timestamp in milliseconds since epoch
         * @return this builder
         */
        Builder timestamp(long timestamp);
        
        /**
         * Sets the description of the metric.
         *
         * @param description the description
         * @return this builder
         */
        Builder description(String description);
        
        /**
         * Builds the metric.
         *
         * @return the metric
         * @throws IllegalStateException if required fields are not set
         */
        Metric build();
    }
    
    /**
     * Creates a new metric builder.
     *
     * @return a new builder
     */
    static Builder builder() {
        return new MetricImpl.BuilderImpl();
    }
}
