package jdk.aprismate.profiler;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Profiler - Low-overhead performance profiling API.
 * 
 * <p>This provides CPU, memory, and allocation profiling capabilities
 * with minimal overhead using async-profiler integration:
 * <ul>
 *   <li>CPU profiling (sampling-based)</li>
 *   <li>Allocation profiling (TLAB-based)</li>
 *   <li>Wall-clock profiling</li>
 *   <li>Lock contention profiling</li>
 *   <li>Native stack traces</li>
 * </ul>
 * 
 * <h2>Usage Example - CPU Profiling</h2>
 * <pre>{@code
 * // Start CPU profiling
 * Profiler.cpu()
 *     .interval(Duration.ofMillis(10))  // 10ms sampling
 *     .start();
 * 
 * // Run workload
 * runApplication();
 * 
 * // Stop and save flamegraph
 * ProfileResult result = Profiler.stop();
 * result.saveFlamegraph(Path.of("cpu-profile.html"));
 * 
 * // Print top hotspots
 * result.topMethods(10).forEach(method -> {
 *     System.out.printf("%s: %.2f%% (%d samples)%n",
 *         method.name(),
 *         method.percentage(),
 *         method.samples()
 *     );
 * });
 * }</pre>
 * 
 * <h2>Usage Example - Allocation Profiling</h2>
 * <pre>{@code
 * // Profile allocations
 * Profiler.allocation()
 *     .threshold(1024)  // Only track >1KB allocations
 *     .start();
 * 
 * runApplication();
 * 
 * ProfileResult result = Profiler.stop();
 * 
 * // Find allocation hotspots
 * result.topAllocations(10).forEach(alloc -> {
 *     System.out.printf("%s: %d MB allocated%n",
 *         alloc.type(),
 *         alloc.totalBytes() / 1024 / 1024
 *     );
 * });
 * }</pre>
 * 
 * <h2>Usage Example - Lock Profiling</h2>
 * <pre>{@code
 * // Profile lock contention
 * Profiler.lock()
 *     .threshold(Duration.ofMicros(100))  // >100μs waits
 *     .start();
 * 
 * runApplication();
 * 
 * ProfileResult result = Profiler.stop();
 * 
 * // Find contended locks
 * result.topLocks(10).forEach(lock -> {
 *     System.out.printf("%s: %d ms total wait%n",
 *         lock.monitor(),
 *         lock.totalWaitTime().toMillis()
 *     );
 * });
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public final class Profiler {
    
    private Profiler() {
        // No instantiation
    }
    
    /**
     * Starts CPU profiling.
     * 
     * @return CPU profiler builder
     */
    public static CpuProfiler cpu() {
        return ProfilerFactory.cpu();
    }
    
    /**
     * Starts allocation profiling.
     * 
     * @return allocation profiler builder
     */
    public static AllocationProfiler allocation() {
        return ProfilerFactory.allocation();
    }
    
    /**
     * Starts wall-clock profiling.
     * 
     * <p>This profiles all threads including those waiting/sleeping.
     * 
     * @return wall-clock profiler builder
     */
    public static WallClockProfiler wallClock() {
        return ProfilerFactory.wallClock();
    }
    
    /**
     * Starts lock contention profiling.
     * 
     * @return lock profiler builder
     */
    public static LockProfiler lock() {
        return ProfilerFactory.lock();
    }
    
    /**
     * Stops the current profiling session.
     * 
     * @return profiling result
     * @throws IllegalStateException if no profiling is active
     */
    public static ProfileResult stop() {
        return ProfilerFactory.stop();
    }
    
    /**
     * Checks if profiling is currently active.
     * 
     * @return true if profiling
     */
    public static boolean isActive() {
        return ProfilerFactory.isActive();
    }
    
    /**
     * Returns the current profiling mode.
     * 
     * @return profiling mode, or null if not active
     */
    public static ProfilingMode mode() {
        return ProfilerFactory.mode();
    }
    
    /**
     * CPU profiler builder.
     */
    public interface CpuProfiler {
        
        /**
         * Sets the sampling interval.
         * 
         * <p>Lower values give more accuracy but higher overhead.
         * Default is 10ms.
         * 
         * @param interval sampling interval
         * @return this builder
         */
        CpuProfiler interval(Duration interval);
        
        /**
         * Includes native stack frames (JNI, C/C++).
         * 
         * @return this builder
         */
        CpuProfiler includeNative();
        
        /**
         * Includes kernel stack frames.
         * 
         * <p>Requires root or perf_event_paranoid <= 1 on Linux.
         * 
         * @return this builder
         */
        CpuProfiler includeKernel();
        
        /**
         * Filters to specific threads.
         * 
         * @param threadFilter thread predicate
         * @return this builder
         */
        CpuProfiler threads(Predicate<Thread> threadFilter);
        
        /**
         * Starts profiling.
         */
        void start();
    }
    
    /**
     * Allocation profiler builder.
     */
    public interface AllocationProfiler {
        
        /**
         * Sets the minimum allocation size to track.
         * 
         * <p>Default is 0 (track all allocations).
         * 
         * @param bytes minimum size in bytes
         * @return this builder
         */
        AllocationProfiler threshold(long bytes);
        
        /**
         * Filters to specific classes.
         * 
         * @param classFilter class predicate
         * @return this builder
         */
        AllocationProfiler classes(Predicate<Class<?>> classFilter);
        
        /**
         * Includes allocation size in profile.
         * 
         * @return this builder
         */
        AllocationProfiler includeSize();
        
        /**
         * Starts profiling.
         */
        void start();
    }
    
    /**
     * Wall-clock profiler builder.
     */
    public interface WallClockProfiler {
        
        /**
         * Sets the sampling interval.
         * 
         * @param interval sampling interval
         * @return this builder
         */
        WallClockProfiler interval(Duration interval);
        
        /**
         * Filters to specific threads.
         * 
         * @param threadFilter thread predicate
         * @return this builder
         */
        WallClockProfiler threads(Predicate<Thread> threadFilter);
        
        /**
         * Starts profiling.
         */
        void start();
    }
    
    /**
     * Lock contention profiler builder.
     */
    public interface LockProfiler {
        
        /**
         * Sets the minimum wait time to track.
         * 
         * <p>Default is 1ms.
         * 
         * @param threshold minimum wait duration
         * @return this builder
         */
        LockProfiler threshold(Duration threshold);
        
        /**
         * Filters to specific monitors.
         * 
         * @param monitorFilter monitor predicate
         * @return this builder
         */
        LockProfiler monitors(Predicate<Object> monitorFilter);
        
        /**
         * Starts profiling.
         */
        void start();
    }
    
    /**
     * Profiling mode enumeration.
     */
    public enum ProfilingMode {
        /** CPU profiling (sampling). */
        CPU,
        
        /** Allocation profiling (TLAB). */
        ALLOCATION,
        
        /** Wall-clock profiling. */
        WALL_CLOCK,
        
        /** Lock contention profiling. */
        LOCK
    }
}
