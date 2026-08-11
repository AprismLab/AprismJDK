package jdk.aprismate.runtime;

import java.util.List;

/**
 * Provides introspection into JVM heap structure and object memory usage.
 * <p>
 * This API exposes low-level heap information for memory profiling and
 * analysis, optimized for minimal performance overhead.
 * </p>
 *
 * @since v26.1-Alpha.6
 */
public interface HeapInsight {
    
    /**
     * Represents a heap memory region.
     */
    interface HeapRegion {
        /**
         * Returns the type of this heap region.
         *
         * @return the region type
         */
        RegionType getType();
        
        /**
         * Returns the size of this region in bytes.
         *
         * @return region size in bytes
         */
        long getSize();
        
        /**
         * Returns the number of bytes currently used in this region.
         *
         * @return used bytes
         */
        long getUsed();
        
        /**
         * Returns the number of bytes available in this region.
         *
         * @return free bytes
         */
        long getFree();
    }
    
    /**
     * Heap region types in modern garbage collectors.
     */
    enum RegionType {
        /** Young generation / Eden space */
        YOUNG,
        
        /** Old generation / Tenured space */
        OLD,
        
        /** Humongous objects (G1/ZGC/Shenandoah) */
        HUMONGOUS,
        
        /** Survivor space */
        SURVIVOR,
        
        /** Metaspace / Non-heap */
        METASPACE,
        
        /** Code cache */
        CODE_CACHE,
        
        /** Unknown or other */
        OTHER
    }
    
    /**
     * Returns a snapshot of all heap regions.
     * <p>
     * The structure depends on the active garbage collector:
     * - Serial/Parallel GC: Young, Old, Survivor
     * - G1 GC: Young, Old, Humongous, Survivor
     * - ZGC/Shenandoah: Region-based layout
     * </p>
     *
     * @return an immutable list of heap regions
     */
    List<HeapRegion> getHeapRegions();
    
    /**
     * Returns the shallow size of an object in bytes.
     * <p>
     * This includes the object header and instance fields, but does not
     * include the size of referenced objects.
     * </p>
     *
     * @param obj the object to measure
     * @return shallow size in bytes, or -1 if unavailable
     * @throws NullPointerException if obj is null
     */
    long getObjectSize(Object obj);
    
    /**
     * Estimates the retained size of an object in bytes.
     * <p>
     * The retained size is the total size of objects that would be freed
     * if this object were garbage collected. This is an estimate and may
     * not be 100% accurate due to shared references.
     * </p>
     * <p>
     * <strong>Warning:</strong> This operation can be expensive for large
     * object graphs. Use with caution in production.
     * </p>
     *
     * @param obj the object to measure
     * @return estimated retained size in bytes, or -1 if unavailable
     * @throws NullPointerException if obj is null
     */
    long getRetainedSize(Object obj);
}
