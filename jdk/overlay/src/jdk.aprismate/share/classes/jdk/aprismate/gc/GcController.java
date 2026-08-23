package jdk.aprismate.gc;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * GcController - Fine-grained garbage collector control.
 * 
 * <p>This API provides control over GC behavior beyond what's available
 * through standard {@link java.lang.management.MemoryMXBean}:
 * <ul>
 *   <li>Trigger specific GC phases</li>
 *   <li>Control GC concurrency</li>
 *   <li>Adjust GC parameters at runtime</li>
 *   <li>Monitor GC activity in real-time</li>
 * </ul>
 * 
 * <h2>Usage Example - Basic Control</h2>
 * <pre>{@code
 * // Trigger young generation GC
 * GcController.triggerYoungGc();
 * 
 * // Trigger full GC
 * GcController.triggerFullGc();
 * 
 * // Trigger concurrent marking (G1/ZGC/Shenandoah)
 * GcController.triggerConcurrentMark();
 * }</pre>
 * 
 * <h2>Usage Example - Runtime Tuning</h2>
 * <pre>{@code
 * // Adjust GC thread count
 * GcController.setConcurrentThreads(4);
 * 
 * // Adjust young generation size
 * GcController.setYoungGenSize(512 * 1024 * 1024);  // 512 MB
 * 
 * // Adjust GC pause target
 * GcController.setPauseTarget(Duration.ofMillis(10));
 * }</pre>
 * 
 * <h2>Usage Example - Monitoring</h2>
 * <pre>{@code
 * // Add GC listener
 * GcController.addListener(event -> {
 *     System.out.printf("GC %s: %d ms, %d KB freed%n",
 *         event.type(),
 *         event.duration().toMillis(),
 *         event.freedBytes() / 1024
 *     );
 * });
 * 
 * // Get current GC statistics
 * GcStats stats = GcController.getStats();
 * System.out.println("Total pauses: " + stats.totalPauses());
 * System.out.println("Average pause: " + stats.averagePauseTime());
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public final class GcController {
    
    private GcController() {
        // No instantiation
    }
    
    /**
     * Triggers a young generation GC.
     * 
     * <p>This collects only the young generation (Eden + Survivor spaces).
     * Typically completes in 1-10ms.
     */
    public static void triggerYoungGc() {
        GcControllerFactory.triggerYoungGc();
    }
    
    /**
     * Triggers a full GC.
     * 
     * <p>This collects all generations and may cause significant pauses
     * (10-1000ms depending on heap size).
     */
    public static void triggerFullGc() {
        GcControllerFactory.triggerFullGc();
    }
    
    /**
     * Triggers concurrent marking phase.
     * 
     * <p>Only applicable for concurrent collectors (G1, ZGC, Shenandoah).
     * This starts the marking phase without stopping the application.
     * 
     * @throws UnsupportedOperationException if current GC doesn't support concurrent marking
     */
    public static void triggerConcurrentMark() {
        GcControllerFactory.triggerConcurrentMark();
    }
    
    /**
     * Triggers concurrent compaction phase.
     * 
     * <p>Only applicable for ZGC and Shenandoah.
     * 
     * @throws UnsupportedOperationException if current GC doesn't support concurrent compaction
     */
    public static void triggerConcurrentCompaction() {
        GcControllerFactory.triggerConcurrentCompaction();
    }
    
    /**
     * Sets the number of concurrent GC threads.
     * 
     * <p>For G1GC, this controls ConcGCThreads.
     * For ZGC/Shenandoah, this controls the parallel worker count.
     * 
     * @param threads the number of threads (must be >= 1)
     * @throws IllegalArgumentException if threads < 1
     * @throws UnsupportedOperationException if current GC doesn't support this
     */
    public static void setConcurrentThreads(int threads) {
        GcControllerFactory.setConcurrentThreads(threads);
    }
    
    /**
     * Sets the number of parallel GC threads.
     * 
     * <p>This controls ParallelGCThreads, used during stop-the-world phases.
     * 
     * @param threads the number of threads (must be >= 1)
     * @throws IllegalArgumentException if threads < 1
     */
    public static void setParallelThreads(int threads) {
        GcControllerFactory.setParallelThreads(threads);
    }
    
    /**
     * Sets the young generation size.
     * 
     * @param bytes the size in bytes
     * @throws IllegalArgumentException if size is invalid
     */
    public static void setYoungGenSize(long bytes) {
        GcControllerFactory.setYoungGenSize(bytes);
    }
    
    /**
     * Sets the old generation size.
     * 
     * @param bytes the size in bytes
     * @throws IllegalArgumentException if size is invalid
     */
    public static void setOldGenSize(long bytes) {
        GcControllerFactory.setOldGenSize(bytes);
    }
    
    /**
     * Sets the GC pause time target.
     * 
     * <p>This is a soft goal; the GC will try to meet it but may exceed it.
     * Only applicable for G1GC and Shenandoah.
     * 
     * @param target the target pause duration
     * @throws UnsupportedOperationException if current GC doesn't support this
     */
    public static void setPauseTarget(Duration target) {
        GcControllerFactory.setPauseTarget(target);
    }
    
    /**
     * Sets the G1 region size.
     * 
     * <p>Only applicable for G1GC. Must be power of 2 between 1MB and 32MB.
     * Cannot be changed after JVM startup in standard JVM.
     * 
     * @param bytes the region size in bytes
     * @throws IllegalArgumentException if size is invalid
     * @throws UnsupportedOperationException if not using G1GC or cannot be changed
     */
    public static void setG1RegionSize(int bytes) {
        GcControllerFactory.setG1RegionSize(bytes);
    }
    
    /**
     * Adds a GC event listener.
     * 
     * <p>The listener will be called after each GC event.
     * 
     * @param listener the listener
     */
    public static void addListener(Consumer<GcEvent> listener) {
        GcControllerFactory.addListener(listener);
    }
    
    /**
     * Removes a GC event listener.
     * 
     * @param listener the listener
     */
    public static void removeListener(Consumer<GcEvent> listener) {
        GcControllerFactory.removeListener(listener);
    }
    
    /**
     * Returns the current GC statistics.
     * 
     * @return GC statistics
     */
    public static GcStats getStats() {
        return GcControllerFactory.getStats();
    }
    
    /**
     * Returns the current GC type.
     * 
     * @return GC type (e.g., "G1", "ZGC", "Shenandoah", "Parallel", "Serial")
     */
    public static String gcType() {
        return GcControllerFactory.gcType();
    }
    
    /**
     * Checks if the current GC supports concurrent marking.
     * 
     * @return true if concurrent marking is supported
     */
    public static boolean supportsConcurrentMarking() {
        return GcControllerFactory.supportsConcurrentMarking();
    }
    
    /**
     * Checks if the current GC supports concurrent compaction.
     * 
     * @return true if concurrent compaction is supported
     */
    public static boolean supportsConcurrentCompaction() {
        return GcControllerFactory.supportsConcurrentCompaction();
    }
    
    /**
     * Disables explicit GC (System.gc()).
     * 
     * <p>After calling this, System.gc() becomes a no-op.
     */
    public static void disableExplicitGc() {
        GcControllerFactory.disableExplicitGc();
    }
    
    /**
     * Enables explicit GC (System.gc()).
     */
    public static void enableExplicitGc() {
        GcControllerFactory.enableExplicitGc();
    }
    
    /**
     * Checks if explicit GC is enabled.
     * 
     * @return true if System.gc() will trigger GC
     */
    public static boolean isExplicitGcEnabled() {
        return GcControllerFactory.isExplicitGcEnabled();
    }
}
