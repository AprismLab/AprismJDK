package jdk.aprismate.memory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * DirectBufferPool - High-performance direct buffer pool with leak detection.
 * 
 * <p>This pool manages a collection of reusable direct {@link ByteBuffer}s to
 * avoid the overhead of repeated allocation and GC pressure. It includes
 * automatic leak detection to identify buffers that are acquired but never
 * released.
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Zero-allocation buffer reuse (>95% reuse rate typical)</li>
 *   <li>Automatic leak detection with stack traces</li>
 *   <li>Configurable size classes and pool limits</li>
 *   <li>Thread-safe with minimal contention</li>
 *   <li>JMX metrics integration</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * PoolConfig config = PoolConfig.builder()
 *     .minBufferSize(1024)
 *     .maxBufferSize(1024 * 1024)
 *     .maxPoolSize(1000)
 *     .enableLeakDetection(true)
 *     .build();
 * 
 * DirectBufferPool pool = DirectBufferPool.create(config);
 * 
 * ByteBuffer buffer = pool.acquire(8192);
 * try {
 *     // Use buffer...
 * } finally {
 *     pool.release(buffer);
 * }
 * 
 * // Check statistics
 * PoolStats stats = pool.stats();
 * System.out.println("Reuse rate: " + stats.reuseRate() + "%");
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 * @see ByteBuffer
 * @see PoolConfig
 */
public interface DirectBufferPool extends AutoCloseable {
    
    /**
     * Acquires a buffer from the pool.
     * 
     * <p>If a suitable buffer is available in the pool, it is returned
     * immediately. Otherwise, a new buffer is allocated. The returned
     * buffer's position is 0 and limit is set to capacity.
     * 
     * @param capacity the minimum required capacity in bytes
     * @return a direct byte buffer with at least the requested capacity
     * @throws IllegalArgumentException if capacity is negative or exceeds max
     * @throws OutOfMemoryError if allocation fails
     */
    ByteBuffer acquire(int capacity);
    
    /**
     * Acquires a buffer with timeout.
     * 
     * <p>Waits for up to the specified timeout for a buffer to become
     * available. Returns null if timeout expires.
     * 
     * @param capacity the minimum required capacity in bytes
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout
     * @return a direct byte buffer, or null if timeout expires
     * @throws IllegalArgumentException if capacity is negative or exceeds max
     * @throws InterruptedException if interrupted while waiting
     */
    ByteBuffer acquire(int capacity, long timeout, TimeUnit unit) throws InterruptedException;
    
    /**
     * Returns a buffer to the pool.
     * 
     * <p>The buffer is cleared (position=0, limit=capacity) and returned
     * to the pool for reuse. If the pool is full, the buffer may be
     * discarded and garbage collected.
     * 
     * <p>After calling this method, the caller must not access the buffer
     * again. Doing so may result in data corruption or crashes.
     * 
     * @param buffer the buffer to release, must be direct
     * @throws IllegalArgumentException if buffer is not direct
     * @throws NullPointerException if buffer is null
     */
    void release(ByteBuffer buffer);
    
    /**
     * Returns pool statistics.
     * 
     * @return current pool statistics
     */
    PoolStats stats();
    
    /**
     * Checks for leaked buffers.
     * 
     * <p>This scans for buffers that were acquired but never released
     * within the configured timeout. Leaked buffers are logged with
     * their allocation stack traces.
     * 
     * @return the number of leaked buffers detected
     */
    int checkLeaks();
    
    /**
     * Clears the pool, releasing all buffers.
     * 
     * <p>This forces all pooled buffers to be garbage collected. Buffers
     * currently in use are not affected.
     */
    void clear();
    
    /**
     * Closes this pool and releases all resources.
     * 
     * <p>After closing, acquire operations will fail. Release operations
     * on buffers from this pool will be ignored.
     */
    @Override
    void close();
    
    /**
     * Creates a buffer pool with the specified configuration.
     * 
     * @param config the pool configuration
     * @return a new buffer pool
     * @throws NullPointerException if config is null
     */
    static DirectBufferPool create(PoolConfig config) {
        return DirectBufferPoolFactory.create(config);
    }
    
    /**
     * Creates a buffer pool with default configuration.
     * 
     * @return a new buffer pool with defaults
     */
    static DirectBufferPool create() {
        return create(PoolConfig.builder().build());
    }
}
