package jdk.aprismate.memory;

/**
 * PoolStats - Statistics for DirectBufferPool.
 * 
 * <p>This class provides metrics about buffer pool usage, performance,
 * and health. All statistics are thread-safe and reflect the state at
 * the time of the query.
 * 
 * <h2>Key Metrics</h2>
 * <ul>
 *   <li><b>Reuse Rate</b>: Percentage of acquires served from pool (higher is better)</li>
 *   <li><b>Hit Rate</b>: Alias for reuse rate</li>
 *   <li><b>Miss Rate</b>: Percentage of acquires requiring new allocation (lower is better)</li>
 *   <li><b>Leak Count</b>: Number of buffers that were never released (should be 0)</li>
 * </ul>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface PoolStats {
    
    /**
     * Returns the total number of acquire operations.
     * 
     * @return total acquires
     */
    long totalAcquires();
    
    /**
     * Returns the total number of release operations.
     * 
     * @return total releases
     */
    long totalReleases();
    
    /**
     * Returns the number of acquires served from the pool.
     * 
     * @return pool hits
     */
    long poolHits();
    
    /**
     * Returns the number of acquires requiring new allocation.
     * 
     * @return pool misses
     */
    long poolMisses();
    
    /**
     * Returns the current number of buffers in the pool.
     * 
     * @return pooled buffer count
     */
    int pooledBuffers();
    
    /**
     * Returns the current number of buffers in use (acquired but not released).
     * 
     * @return active buffer count
     */
    int activeBuffers();
    
    /**
     * Returns the total memory held by pooled buffers.
     * 
     * @return pooled memory in bytes
     */
    long pooledMemory();
    
    /**
     * Returns the total memory held by active buffers.
     * 
     * @return active memory in bytes
     */
    long activeMemory();
    
    /**
     * Returns the peak number of active buffers.
     * 
     * @return peak active count
     */
    int peakActiveBuffers();
    
    /**
     * Returns the number of detected leaks.
     * 
     * @return leak count
     */
    long leakCount();
    
    /**
     * Calculates the reuse rate as a percentage.
     * 
     * <p>Reuse rate = (poolHits / totalAcquires) * 100
     * 
     * @return reuse rate percentage (0-100)
     */
    default double reuseRate() {
        long total = totalAcquires();
        if (total == 0) {
            return 0.0;
        }
        return (poolHits() * 100.0) / total;
    }
    
    /**
     * Calculates the hit rate (alias for reuse rate).
     * 
     * @return hit rate percentage (0-100)
     */
    default double hitRate() {
        return reuseRate();
    }
    
    /**
     * Calculates the miss rate as a percentage.
     * 
     * <p>Miss rate = (poolMisses / totalAcquires) * 100
     * 
     * @return miss rate percentage (0-100)
     */
    default double missRate() {
        long total = totalAcquires();
        if (total == 0) {
            return 0.0;
        }
        return (poolMisses() * 100.0) / total;
    }
    
    /**
     * Calculates the pool utilization as a percentage.
     * 
     * <p>This indicates how full the pool is relative to its maximum size.
     * 
     * @return utilization percentage (0-100)
     */
    double poolUtilization();
    
    /**
     * Returns the average buffer size in bytes.
     * 
     * @return average size of pooled buffers
     */
    long averageBufferSize();
    
    /**
     * Resets all statistics to zero.
     * 
     * <p>This does not affect actual pool state, only the counters.
     */
    void reset();
}
