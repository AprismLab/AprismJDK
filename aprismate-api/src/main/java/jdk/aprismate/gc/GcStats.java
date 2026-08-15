package jdk.aprismate.gc;

import java.time.Duration;

/**
 * GcStats - Global GC statistics.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface GcStats {
    
    /**
     * Returns the total number of GC pauses.
     * 
     * @return total pause count
     */
    long totalPauses();
    
    /**
     * Returns the number of young generation GCs.
     * 
     * @return young GC count
     */
    long youngGcCount();
    
    /**
     * Returns the number of old generation GCs.
     * 
     * @return old GC count
     */
    long oldGcCount();
    
    /**
     * Returns the number of full GCs.
     * 
     * @return full GC count
     */
    long fullGcCount();
    
    /**
     * Returns the total time spent in GC pauses.
     * 
     * @return total pause time
     */
    Duration totalPauseTime();
    
    /**
     * Returns the total time spent in young GC.
     * 
     * @return young GC time
     */
    Duration youngGcTime();
    
    /**
     * Returns the total time spent in old GC.
     * 
     * @return old GC time
     */
    Duration oldGcTime();
    
    /**
     * Returns the average pause time.
     * 
     * @return average pause time
     */
    Duration averagePauseTime();
    
    /**
     * Returns the maximum pause time.
     * 
     * @return max pause time
     */
    Duration maxPauseTime();
    
    /**
     * Returns the minimum pause time.
     * 
     * @return min pause time
     */
    Duration minPauseTime();
    
    /**
     * Returns the 50th percentile (median) pause time.
     * 
     * @return P50 pause time
     */
    Duration p50PauseTime();
    
    /**
     * Returns the 95th percentile pause time.
     * 
     * @return P95 pause time
     */
    Duration p95PauseTime();
    
    /**
     * Returns the 99th percentile pause time.
     * 
     * @return P99 pause time
     */
    Duration p99PauseTime();
    
    /**
     * Returns the 99.9th percentile pause time.
     * 
     * @return P999 pause time
     */
    Duration p999PauseTime();
    
    /**
     * Returns the total bytes allocated.
     * 
     * @return total allocated bytes
     */
    long totalAllocatedBytes();
    
    /**
     * Returns the total bytes freed by GC.
     * 
     * @return total freed bytes
     */
    long totalFreedBytes();
    
    /**
     * Returns the current allocation rate in bytes/second.
     * 
     * @return allocation rate
     */
    long allocationRate();
    
    /**
     * Returns the current promotion rate (young -> old) in bytes/second.
     * 
     * @return promotion rate
     */
    long promotionRate();
    
    /**
     * Returns the current heap occupancy.
     * 
     * @return heap used in bytes
     */
    long heapUsed();
    
    /**
     * Returns the current heap capacity.
     * 
     * @return heap capacity in bytes
     */
    long heapCapacity();
    
    /**
     * Returns the maximum heap size.
     * 
     * @return max heap in bytes
     */
    long heapMax();
    
    /**
     * Returns the young generation used size.
     * 
     * @return young gen used in bytes
     */
    long youngGenUsed();
    
    /**
     * Returns the young generation capacity.
     * 
     * @return young gen capacity in bytes
     */
    long youngGenCapacity();
    
    /**
     * Returns the old generation used size.
     * 
     * @return old gen used in bytes
     */
    long oldGenUsed();
    
    /**
     * Returns the old generation capacity.
     * 
     * @return old gen capacity in bytes
     */
    long oldGenCapacity();
    
    /**
     * Returns the metaspace used size.
     * 
     * @return metaspace used in bytes
     */
    long metaspaceUsed();
    
    /**
     * Returns the metaspace capacity.
     * 
     * @return metaspace capacity in bytes
     */
    long metaspaceCapacity();
    
    /**
     * Returns the GC overhead percentage (time in GC / total time).
     * 
     * @return overhead percentage (0-100)
     */
    double gcOverhead();
    
    /**
     * Returns the current number of GC threads.
     * 
     * @return GC thread count
     */
    int gcThreads();
    
    /**
     * Returns the current number of concurrent GC threads.
     * 
     * @return concurrent GC thread count
     */
    int concurrentGcThreads();
    
    /**
     * Resets all statistics.
     */
    void reset();
    
    /**
     * Calculates heap utilization percentage.
     * 
     * @return utilization percentage (0-100)
     */
    default double heapUtilization() {
        long capacity = heapCapacity();
        if (capacity == 0) {
            return 0.0;
        }
        return (heapUsed() * 100.0) / capacity;
    }
    
    /**
     * Calculates young generation utilization percentage.
     * 
     * @return utilization percentage (0-100)
     */
    default double youngGenUtilization() {
        long capacity = youngGenCapacity();
        if (capacity == 0) {
            return 0.0;
        }
        return (youngGenUsed() * 100.0) / capacity;
    }
    
    /**
     * Calculates old generation utilization percentage.
     * 
     * @return utilization percentage (0-100)
     */
    default double oldGenUtilization() {
        long capacity = oldGenCapacity();
        if (capacity == 0) {
            return 0.0;
        }
        return (oldGenUsed() * 100.0) / capacity;
    }
}
