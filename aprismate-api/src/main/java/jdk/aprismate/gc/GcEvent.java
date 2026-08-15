package jdk.aprismate.gc;

import java.time.Duration;
import java.time.Instant;

/**
 * GcEvent - Represents a garbage collection event.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface GcEvent {
    
    /**
     * Returns the GC type.
     * 
     * @return GC type
     */
    GcType type();
    
    /**
     * Returns the GC name.
     * 
     * <p>Examples: "G1 Young Generation", "G1 Old Generation", 
     * "ZGC", "Shenandoah Pauses"
     * 
     * @return GC name
     */
    String name();
    
    /**
     * Returns the event timestamp.
     * 
     * @return timestamp
     */
    Instant timestamp();
    
    /**
     * Returns the GC duration.
     * 
     * @return duration
     */
    Duration duration();
    
    /**
     * Returns the memory freed in bytes.
     * 
     * @return bytes freed
     */
    long freedBytes();
    
    /**
     * Returns the heap size before GC.
     * 
     * @return heap size in bytes
     */
    long heapBefore();
    
    /**
     * Returns the heap size after GC.
     * 
     * @return heap size in bytes
     */
    long heapAfter();
    
    /**
     * Returns the maximum heap size.
     * 
     * @return max heap size in bytes
     */
    long heapMax();
    
    /**
     * Returns the number of GC threads used.
     * 
     * @return thread count
     */
    int threads();
    
    /**
     * Checks if this was a full GC.
     * 
     * @return true if full GC
     */
    boolean isFull();
    
    /**
     * Checks if this was a concurrent GC.
     * 
     * @return true if concurrent
     */
    boolean isConcurrent();
    
    /**
     * Returns the cause of this GC.
     * 
     * <p>Examples: "Allocation Failure", "System.gc()", "Metadata GC Threshold"
     * 
     * @return GC cause
     */
    String cause();
    
    /**
     * GC type enumeration.
     */
    enum GcType {
        /** Young generation collection (minor GC). */
        YOUNG,
        
        /** Old generation collection (major GC). */
        OLD,
        
        /** Full heap collection. */
        FULL,
        
        /** Concurrent marking phase. */
        CONCURRENT_MARK,
        
        /** Concurrent compaction phase. */
        CONCURRENT_COMPACT,
        
        /** Mixed collection (young + some old regions). */
        MIXED,
        
        /** Other/unknown type. */
        OTHER
    }
}
