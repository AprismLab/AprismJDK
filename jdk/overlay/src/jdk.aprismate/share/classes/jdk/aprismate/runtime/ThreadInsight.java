package jdk.aprismate.runtime;

import java.util.List;

/**
 * Provides introspection into thread states and statistics.
 * <p>
 * This API exposes low-level thread information typically available only through
 * JVM internal APIs or management beans, optimized for minimal overhead.
 * </p>
 *
 * @since v26.1-Alpha.6
 */
public interface ThreadInsight {
    
    /**
     * Returns all live threads in the JVM.
     * <p>
     * This includes daemon threads, system threads, and application threads.
     * The returned list is a snapshot and may not reflect threads created
     * after this call.
     * </p>
     *
     * @return an immutable list of all live threads
     */
    List<Thread> getAllThreads();
    
    /**
     * Returns the stack trace for a specific thread without throwing an exception.
     * <p>
     * Unlike {@link Thread#getStackTrace()}, this method provides a more efficient
     * way to retrieve stack traces and works even for threads in non-standard states.
     * </p>
     *
     * @param thread the thread to inspect
     * @return the stack trace elements, or empty array if unavailable
     * @throws NullPointerException if thread is null
     */
    StackTraceElement[] getThreadStack(Thread thread);
    
    /**
     * Returns the total CPU time consumed by a thread in nanoseconds.
     * <p>
     * This includes both user and system CPU time. Returns -1 if CPU time
     * measurement is not supported or the thread is not alive.
     * </p>
     *
     * @param thread the thread to measure
     * @return CPU time in nanoseconds, or -1 if unavailable
     * @throws NullPointerException if thread is null
     */
    long getThreadCpuTime(Thread thread);
    
    /**
     * Returns the total number of bytes allocated by a thread.
     * <p>
     * This tracks heap allocations performed by the thread. Returns -1 if
     * allocation tracking is not supported or the thread is not alive.
     * </p>
     *
     * @param thread the thread to measure
     * @return allocated bytes, or -1 if unavailable
     * @throws NullPointerException if thread is null
     */
    long getThreadAllocatedBytes(Thread thread);
}
