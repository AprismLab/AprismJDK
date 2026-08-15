package jdk.aprismate.memory;

import java.time.Duration;
import java.util.Objects;

/**
 * PoolConfig - Configuration for DirectBufferPool.
 * 
 * <p>This class defines the behavior and limits of a buffer pool.
 * Use the builder pattern to create instances with custom settings.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * PoolConfig config = PoolConfig.builder()
 *     .minBufferSize(1024)
 *     .maxBufferSize(1024 * 1024)
 *     .maxPoolSize(1000)
 *     .enableLeakDetection(true)
 *     .leakDetectionTimeout(Duration.ofMinutes(5))
 *     .build();
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public final class PoolConfig {
    
    private final int minBufferSize;
    private final int maxBufferSize;
    private final int maxPoolSize;
    private final boolean enableLeakDetection;
    private final Duration leakDetectionTimeout;
    private final boolean enableMetrics;
    
    private PoolConfig(Builder builder) {
        this.minBufferSize = builder.minBufferSize;
        this.maxBufferSize = builder.maxBufferSize;
        this.maxPoolSize = builder.maxPoolSize;
        this.enableLeakDetection = builder.enableLeakDetection;
        this.leakDetectionTimeout = builder.leakDetectionTimeout;
        this.enableMetrics = builder.enableMetrics;
    }
    
    /**
     * Returns the minimum buffer size in bytes.
     * 
     * @return minimum buffer size
     */
    public int minBufferSize() {
        return minBufferSize;
    }
    
    /**
     * Returns the maximum buffer size in bytes.
     * 
     * @return maximum buffer size
     */
    public int maxBufferSize() {
        return maxBufferSize;
    }
    
    /**
     * Returns the maximum pool size.
     * 
     * @return maximum number of buffers to pool
     */
    public int maxPoolSize() {
        return maxPoolSize;
    }
    
    /**
     * Checks if leak detection is enabled.
     * 
     * @return true if leak detection is enabled
     */
    public boolean enableLeakDetection() {
        return enableLeakDetection;
    }
    
    /**
     * Returns the leak detection timeout.
     * 
     * @return timeout duration
     */
    public Duration leakDetectionTimeout() {
        return leakDetectionTimeout;
    }
    
    /**
     * Checks if metrics collection is enabled.
     * 
     * @return true if metrics are enabled
     */
    public boolean enableMetrics() {
        return enableMetrics;
    }
    
    /**
     * Creates a new builder.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for PoolConfig.
     */
    public static final class Builder {
        private int minBufferSize = 1024;
        private int maxBufferSize = 1024 * 1024;
        private int maxPoolSize = 1000;
        private boolean enableLeakDetection = true;
        private Duration leakDetectionTimeout = Duration.ofMinutes(5);
        private boolean enableMetrics = true;
        
        private Builder() {}
        
        /**
         * Sets the minimum buffer size.
         * 
         * @param size minimum size in bytes, must be positive
         * @return this builder
         * @throws IllegalArgumentException if size is not positive
         */
        public Builder minBufferSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("minBufferSize must be positive: " + size);
            }
            this.minBufferSize = size;
            return this;
        }
        
        /**
         * Sets the maximum buffer size.
         * 
         * @param size maximum size in bytes, must be positive
         * @return this builder
         * @throws IllegalArgumentException if size is not positive
         */
        public Builder maxBufferSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("maxBufferSize must be positive: " + size);
            }
            this.maxBufferSize = size;
            return this;
        }
        
        /**
         * Sets the maximum pool size.
         * 
         * @param size maximum number of buffers to pool
         * @return this builder
         * @throws IllegalArgumentException if size is not positive
         */
        public Builder maxPoolSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("maxPoolSize must be positive: " + size);
            }
            this.maxPoolSize = size;
            return this;
        }
        
        /**
         * Enables or disables leak detection.
         * 
         * @param enable true to enable, false to disable
         * @return this builder
         */
        public Builder enableLeakDetection(boolean enable) {
            this.enableLeakDetection = enable;
            return this;
        }
        
        /**
         * Sets the leak detection timeout.
         * 
         * @param timeout timeout duration, must not be null
         * @return this builder
         * @throws NullPointerException if timeout is null
         */
        public Builder leakDetectionTimeout(Duration timeout) {
            this.leakDetectionTimeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }
        
        /**
         * Enables or disables metrics collection.
         * 
         * @param enable true to enable, false to disable
         * @return this builder
         */
        public Builder enableMetrics(boolean enable) {
            this.enableMetrics = enable;
            return this;
        }
        
        /**
         * Builds the configuration.
         * 
         * @return a new PoolConfig instance
         * @throws IllegalStateException if configuration is invalid
         */
        public PoolConfig build() {
            if (minBufferSize > maxBufferSize) {
                throw new IllegalStateException(
                    "minBufferSize (" + minBufferSize + ") > maxBufferSize (" + maxBufferSize + ")");
            }
            return new PoolConfig(this);
        }
    }
    
    @Override
    public String toString() {
        return "PoolConfig{" +
               "minBufferSize=" + minBufferSize +
               ", maxBufferSize=" + maxBufferSize +
               ", maxPoolSize=" + maxPoolSize +
               ", enableLeakDetection=" + enableLeakDetection +
               ", leakDetectionTimeout=" + leakDetectionTimeout +
               ", enableMetrics=" + enableMetrics +
               '}';
    }
}
