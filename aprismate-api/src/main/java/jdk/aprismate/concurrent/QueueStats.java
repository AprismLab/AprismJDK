package jdk.aprismate.concurrent;

/**
 * QueueStats - Statistics for LockFreeQueue.
 * 
 * <p>Provides metrics about queue performance and health. All statistics
 * are collected with minimal overhead (typically <1ns per operation).
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface QueueStats {
    
    /**
     * Returns the total number of enqueue operations.
     * 
     * @return total enqueues
     */
    long totalEnqueues();
    
    /**
     * Returns the total number of dequeue operations.
     * 
     * @return total dequeues
     */
    long totalDequeues();
    
    /**
     * Returns the number of failed enqueue operations (queue full).
     * 
     * @return failed enqueues
     */
    long failedEnqueues();
    
    /**
     * Returns the number of failed dequeue operations (queue empty).
     * 
     * @return failed dequeues (empty polls)
     */
    long failedDequeues();
    
    /**
     * Returns the current queue size.
     * 
     * @return current size (approximate)
     */
    int currentSize();
    
    /**
     * Returns the peak queue size.
     * 
     * @return peak size
     */
    int peakSize();
    
    /**
     * Returns the average queue size over time.
     * 
     * @return average size
     */
    double averageSize();
    
    /**
     * Calculates the current throughput in operations per second.
     * 
     * <p>This is calculated over a sliding time window (typically 1 second).
     * 
     * @return throughput in ops/s
     */
    double throughput();
    
    /**
     * Returns the average enqueue latency in nanoseconds.
     * 
     * @return average enqueue time in ns
     */
    long averageEnqueueLatency();
    
    /**
     * Returns the average dequeue latency in nanoseconds.
     * 
     * @return average dequeue time in ns
     */
    long averageDequeueLatency();
    
    /**
     * Returns the number of CAS (Compare-And-Swap) retries.
     * 
     * <p>Higher retry counts indicate contention. Typical values are <2
     * retries per operation.
     * 
     * @return total CAS retries
     */
    long casRetries();
    
    /**
     * Calculates the average number of CAS retries per operation.
     * 
     * @return average CAS retries
     */
    default double averageCasRetries() {
        long total = totalEnqueues() + totalDequeues();
        if (total == 0) {
            return 0.0;
        }
        return (double) casRetries() / total;
    }
    
    /**
     * Calculates the success rate for enqueue operations.
     * 
     * @return success rate percentage (0-100)
     */
    default double enqueueSuccessRate() {
        long total = totalEnqueues();
        if (total == 0) {
            return 100.0;
        }
        long successful = total - failedEnqueues();
        return (successful * 100.0) / total;
    }
    
    /**
     * Calculates the utilization for bounded queues.
     * 
     * <p>For unbounded queues, this always returns 0.
     * 
     * @return utilization percentage (0-100)
     */
    double utilization();
    
    /**
     * Resets all statistics to zero.
     */
    void reset();
    
    /**
     * Returns the time when statistics collection started.
     * 
     * @return start time in milliseconds since epoch
     */
    long startTime();
    
    /**
     * Returns the elapsed time since statistics collection started.
     * 
     * @return elapsed time in milliseconds
     */
    default long elapsedTime() {
        return System.currentTimeMillis() - startTime();
    }
}
